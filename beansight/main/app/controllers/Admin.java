package controllers;

import helpers.TimeSeriePoint;
import helpers.TimeSeriePointHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jobs.AnalyticsJob;
import jobs.CheckFacebookFriendsAndFollowSyncJob;
import jobs.InsightTrendsCalculateJob;
import jobs.scoring.InsightValidationJob;
import jobs.scoring.ScoresComputationInitJob;
import jobs.scoring.ScoresComputationJob;
import jobs.weeklymailing.WeeklyMailingJob;
import jobs.weeklymailing.WeeklyMailingSenderJob;
import models.Category;
import models.CategoryEnum;
import models.Comment;
import models.FacebookFriend;
import models.Insight;
import models.InsightActivity;
import models.Language;
import models.PeriodEnum;
import models.Tag;
import models.TagActivity;
import models.User;
import models.UserActivity;
import models.analytics.DailyTotalComment;
import models.analytics.DailyTotalInsight;
import models.analytics.DailyTotalVote;
import models.analytics.UserInsightVisit;
import models.analytics.UserListInsightsVisit;
import models.job.ComputeScoreForUsersTask;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import exceptions.InsightAlreadySharedException;
import exceptions.NotFollowingUserException;

import play.Logger;
import play.Play;
import play.data.binding.As;
import play.modules.search.Search;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.results.RenderText;


@With(Secure.class)
@Check("admin")
public class Admin extends Controller {

	/**
	 * Add invitations to this user
	 */
	public static void addInvitationsToUser(long userId, long number) {
		User user = User.findById(userId);
		user.addInvitations(number);
		render(user);
	}
	
	/**
	 * flag this user as a sponsor.
	 */
	public static void makeSponsorUser(long userId) {
		User user = User.findById(userId);
		user.sponsor = true;
		user.save();
		Application.showUser(user.userName);
	}
	
	/**
	 * flag this user as a dangerous
	 */
	public static void makeDangerousUser(long userId, boolean dangerous) {
		User user = User.findById(userId);
		user.isDangerous = dangerous;
		user.save();
		Application.showUser(user.userName);
	}
	
	public static void index() {
		renderArgs.put("userTotal", User.count());
		
		render();
	}
	
	public static void top() {
		renderArgs.put("bestUserVotes", User.findBestVoters(20));
		renderArgs.put("bestUserInsights", User.findBestCreators(20));
		
		// top 20 most read insights by registered users since last 7 days
		List<Object[]> top20Insights = UserInsightVisit.find("select v.insight.uniqueId, v.insight.content, count(v) " +
				"from UserInsightVisit v " +
				"where v.creationDate > :crDate " +
				"group by v.insight.id order by count(v) desc").
				bind("crDate", new DateMidnight().minusDays(7).toDate()).fetch(20);
		renderArgs.put("top20Insights", top20Insights);
		
		// last ten comments
		List<Comment> comments = Comment.find("order by creationDate desc").fetch(15);
		renderArgs.put("comments", comments);
		
		render();
	}
	
	public static void analytics() {
		renderArgs.put("dailyTotalVote", DailyTotalVote.findAll());
		renderArgs.put("dailyTotalInsight", DailyTotalInsight.findAll());
		renderArgs.put("dailyTotalComment", DailyTotalComment.findAll());

		// total users and new users evolution
		Map<Date, TimeSeriePoint> dailyTotalUsersMap = new HashMap<Date, TimeSeriePoint>();
		Map<Date, TimeSeriePoint> dailyNewUsersMap = new HashMap<Date, TimeSeriePoint>();
		List<User> users = User.all().fetch(); // users are supposed to be sorted by creationDate
		Double totalUser = 0d;
		Double previousTotalUser = 0d; //used to calculate the size of daily new users
		for (User user : users) {
			totalUser++;
			DateMidnight date = new DateMidnight(user.getCrdate());
			TimeSeriePoint point = dailyTotalUsersMap.get(date.toDate());
			if (point == null) {
				point = new TimeSeriePoint(date.toDate(), totalUser);
				dailyTotalUsersMap.put(date.toDate(), point);
				dailyNewUsersMap.put(date.toDate(), new TimeSeriePoint(date.toDate(), totalUser - previousTotalUser));
				previousTotalUser = totalUser;
			}
			point.value = totalUser;
		}
		renderArgs.put("dailyNewUsers", dailyNewUsersMap.values()); // be careful, these values are not sorted by date.
		renderArgs.put("dailyTotalUsers", dailyTotalUsersMap.values());
		
		
		Map<DateMidnight, Set<Long>> uniqueUserVisitsDayMap = new HashMap<DateMidnight, Set<Long>>(); // a map to store the IDs of logged user each day
		Map<DateMidnight, Integer> activeUserbyDayMap = new HashMap<DateMidnight, Integer>();
		
		List<UserListInsightsVisit> list = UserListInsightsVisit.all().fetch(); 
		DateMidnight firstDate = new DateMidnight( list.get(0).creationDate );
		for (UserListInsightsVisit v : list) {
			DateMidnight date = new DateMidnight(v.creationDate);
			if (!uniqueUserVisitsDayMap.containsKey(date)) {
				uniqueUserVisitsDayMap.put(date, new HashSet<Long>());
				activeUserbyDayMap.put(date, 0);
			}
			uniqueUserVisitsDayMap.get(date).add(v.user.id);
			activeUserbyDayMap.put(date, uniqueUserVisitsDayMap.get(date).size());
		}
		
		List<TimeSeriePoint> activeUsersByDay = new ArrayList<TimeSeriePoint>();
		List<TimeSeriePoint> activeUsersByDayMinusNewUsersByDay = new ArrayList<TimeSeriePoint>();
		List<TimeSeriePoint> prctActiveUsersByDay = new ArrayList<TimeSeriePoint>();
		List<TimeSeriePoint> prctActiveUsersMinusNewUsersByDay = new ArrayList<TimeSeriePoint>();
		
		Integer totalActiveUserToday;
		while (!activeUserbyDayMap.isEmpty()) {
			totalActiveUserToday = activeUserbyDayMap.get(firstDate);
			activeUserbyDayMap.remove(firstDate);
			
			Date datePlusOne = firstDate.plusDays(1).toDate();
			if(totalActiveUserToday != null && dailyNewUsersMap.get(datePlusOne) != null) {
				// We don't know why but there is a decay of one day between these data and the dailyNewUsers data. So add a day.
				activeUsersByDay.add(					new TimeSeriePoint(firstDate.toDate(), totalActiveUserToday.doubleValue() ) );
				activeUsersByDayMinusNewUsersByDay.add(	new TimeSeriePoint(firstDate.toDate(), totalActiveUserToday - dailyNewUsersMap.get(datePlusOne).value ) );
				
				prctActiveUsersByDay.add(				new TimeSeriePoint(firstDate.toDate(), totalActiveUserToday.doubleValue() / dailyTotalUsersMap.get(firstDate.toDate()).value * 100 ) );
				prctActiveUsersMinusNewUsersByDay.add(	new TimeSeriePoint(firstDate.toDate(), totalActiveUserToday.doubleValue() /(dailyTotalUsersMap.get(firstDate.toDate()).value - dailyNewUsersMap.get(firstDate.toDate()).value) * 100 ) );
			}
			firstDate = firstDate.plusDays(1);
		}
		
		renderArgs.put("activeUsers", 				activeUsersByDay									);
		renderArgs.put("activeUsersWeek", 			TimeSeriePointHelper.smooth( activeUsersByDay, 7) 	);
		
		renderArgs.put("activeUsersMinusNewUsers", 		activeUsersByDayMinusNewUsersByDay);
		renderArgs.put("activeUsersMinusNewUsersWeek", 	TimeSeriePointHelper.smooth( activeUsersByDayMinusNewUsersByDay, 7) );
		
		render();
	}
	
	/**
	 * Analytics about the returning users.
	 * @param dateFrom : the date from which to start the experiment. 
	 * @param daysRange : account creations done between dateFrom and dateFrom + daysRange will be taken into account.
	 * @param comebackInXXDays : we will measure the number of times a new user came back in the comebackInXXDays days after account creation.
	 * @param reLogStepHours : after how much hour a new visit is considered as a re-log.
	 */
	public static void countVisits(@As("yyyy-MM-dd") Date dateFrom, Integer daysRange, Integer comebackInXXDays, Integer reLogStepHours) {
		if(reLogStepHours == null) {
			reLogStepHours = 12;
		}
		if(daysRange == null) {
			daysRange = 15;
		}
		if(dateFrom == null) {
			dateFrom = new DateTime().minusDays(daysRange).toDate();
		}
		if (comebackInXXDays == null) {
			comebackInXXDays = 15;			
		}
		Date dateTo = new DateTime(dateFrom).plusDays(daysRange).toDate();

		// select all users who created an accoutn between the two dates
		List<User> createdAccounts = User.find("crdate > ? and crdate < ?", dateFrom, dateTo).fetch();
		renderArgs.put("createdAccountsNumber", createdAccounts.size());

		// get all the visits of these users
		List<UserListInsightsVisit> visits = UserListInsightsVisit.find("creationDate < ? and user.id in (select id from User where crdate > ? and crdate < ?)", new DateTime(dateFrom).plusDays(daysRange + comebackInXXDays ).toDate(), dateFrom, dateTo ).fetch();

		Map<Integer, Integer> count = new HashMap<Integer, Integer>(); // map <number of log, number of users>
		int countVar = 0; // for a user, count how much time he came
		Date lastLog = null; // this is the last date a given user came
		for(User u : createdAccounts) {
			for(UserListInsightsVisit visit : visits) {
				if(visit.user == u
						&& visit.creationDate.after(new DateTime(u.getCrdate()).plusHours(reLogStepHours).toDate()) 
						&& visit.creationDate.before(new DateTime(u.getCrdate()).plusDays(comebackInXXDays).toDate()) 
						&& (lastLog == null || visit.creationDate.after(new DateTime(lastLog).plusHours(reLogStepHours).toDate()))) {
					countVar++;
					lastLog = visit.creationDate;
					Integer tmp = count.get(countVar);
					if(tmp == null) {
						tmp = 0;
					}
					count.put(countVar, tmp + 1 );
					
				}
			}
			countVar = 0;
			lastLog = null;
		}
		
		
		renderArgs.put("onceInTheWeek", count.get(1));
		renderArgs.put("twiceInTheWeek", count.get(2));
		
		
		
		render(dateFrom, dateTo, comebackInXXDays, count, reLogStepHours);
		
	}

	/**
	 * Run this action to rebuild all search indexes
	 */
	public static void rebuildAllIndexes() {
		try {
			Search.rebuildAllIndexes();
			int page = 1;
			List<Insight> insights = null;
			insights = Insight.find("hidden is true").fetch(page, 50);
			while(!insights.isEmpty())  {
				for (Insight insight : insights) {
					Search.unIndex(insight);
				}
				page++;
				insights = Insight.find("hidden is true").fetch(page, 50);
			} 
			
		} catch (Exception e) {
			renderText(e.getMessage());
		}
		renderText("rebuilt all indexes : ok");
	}
	
	/**
	 * @return the play id
	 */
	public static void playId() {
		renderText(Play.id);
	}

	/**
	 * @return the play id
	 */
	public static void applicationPath() {
		renderText(System.getProperty("application.path"));
	}	
	
	/**
	 * 
	 * @param insightId
	 */
	public static void hideInsight(Long insightId) {
		Insight insight = Insight.findById(insightId);
		insight.hidden = true;
		insight.save();
		Search.unIndex(insight);
		renderText("Insight deleted");
	}
	
	/**
	 * AJAX
	 * @param insightId
	 */
	public static void insightHideComment(final Long commentId) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			Comment comment = Comment.findById(commentId);
			comment.hidden = true;
			comment.save();
			
			result.put("error", "");
			result.put("id", commentId);
		} catch (Throwable e) {
			result.put("error", e.getMessage());
		}
		renderJSON(result);
	}
	
	
	/**
	 * Remove users who doesn't have validated their account with a promocode.
	 * 
	 * 
	 * @param minutes
	 */
	public static void flushNotInvitedUser(int minutes, boolean delete) {
		Logger.info("flushNotInvitedUser : minutes = " + minutes);
		DateTime datetime = new DateTime();
		datetime = datetime.minusMinutes(minutes);
		
		Logger.info("flushNotInvitedUser : minutes = " + minutes + " , datetime = " + datetime);
		
		List<User> users = null;
		try {
			users = User.removeCreatedAccountWithNoInvitationBefore(datetime.toDate(), delete);
		} catch (Throwable e) {
			renderText("User.removeCreatedAccountWithNoInvitationBefore finished with error : " + e.getMessage());
			throw new RuntimeException(e) ;
		}
		
		render(users, delete);
	}
	
	/**
	 * Call this method to remove all ComputeScoreForUsersTask
	 * (it's a mean to force beansight to stop computing user's scores)
	 */
	public static void flushScoreTasks() {
		ComputeScoreForUsersTask.flushAll();
	}
	
	/**
	 * This action enable an admin user to manually start a computation of the user's scores.
	 * Using this action, score computation will always be done on a THREE_MONTHS period basis.
	 * 
	 * If no parameter passed to the action then scores will be compute for the last available day
	 * which is yesterday by definition because the idea is to compute scores only for ended day.
	 * 
	 * If dates are passed as parameter then the action will compute the scores for each day
	 * 
	 * @param fromDate
	 * @param toDate
	 */
	public static void buildScores (@As("yyyy-MM-dd") Date fromDate, @As("yyyy-MM-dd") Date toDate) {
		try {
			// if there is already tasks in db then we do not run another one
			// because otherwise we could be have too many threads working
			long count = ComputeScoreForUsersTask.count();
			if (count > 0) {
				renderText("still %s ComputeScoreForUsersTask to execute, please resend request when there won't be anymore task waiting in DB or you can flush all the current ComputeScoreForUsersTask using flushScoreTasks action", count);
			}
			
			// if no date provided then we run the Job as of today 
			// (which actually means calculating scores for yesterday since 
			//  we don't calculate scores if the day is not over) 
			if (fromDate==null || toDate==null) {
				ScoresComputationInitJob job = new ScoresComputationInitJob();
				job.runNow = true;
				job.now();
			} else {
				// compute scores for many dates
				ComputeScoreForUsersTask.createTasksBetweenTwoDates(fromDate, toDate, PeriodEnum.THREE_MONTHS);
				ScoresComputationJob job = new ScoresComputationJob();
				job.now();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * to be used only if you are 100 certain that no other ScoresComputationJob is currently running !
	 */
	public static void restartScoresComputationJob() {
		ScoresComputationJob job = new ScoresComputationJob();
		job.now();
	}
	
	/**
	 * This will manualsy start the insaignts validation job
	 */
	public static void insightValidation() {
		new InsightValidationJob().now();
	}
	
	public static void showExpertTrend(String username) {
		User user = User.findByUserName(username);
		List<Object[]> categoryScoresCelebrities = user.getScoreTimelineByCategory(CategoryEnum.CELEBRITIES, PeriodEnum.THREE_MONTHS);
		List<Object[]> categoryScoresEconomics = user.getScoreTimelineByCategory(CategoryEnum.ECONOMICS, PeriodEnum.THREE_MONTHS);
		List<Object[]> categoryScoresEntertainement = user.getScoreTimelineByCategory(CategoryEnum.ENTERTAINEMENT, PeriodEnum.THREE_MONTHS);
		List<Object[]> categoryScoresFun = user.getScoreTimelineByCategory(CategoryEnum.FUN, PeriodEnum.THREE_MONTHS);
		List<Object[]> categoryScoresPolitics = user.getScoreTimelineByCategory(CategoryEnum.POLITICS, PeriodEnum.THREE_MONTHS);
		List<Object[]> categoryScoresSociety = user.getScoreTimelineByCategory(CategoryEnum.SOCIETY, PeriodEnum.THREE_MONTHS);
		List<Object[]> categoryScoresSport = user.getScoreTimelineByCategory(CategoryEnum.SPORT, PeriodEnum.THREE_MONTHS);
		List<Object[]> categoryScoresTechnology = user.getScoreTimelineByCategory(CategoryEnum.TECHNOLOGY, PeriodEnum.THREE_MONTHS);
		
		
		
		render(categoryScoresCelebrities, categoryScoresEconomics, 
				categoryScoresEntertainement, categoryScoresFun, 
				categoryScoresPolitics, categoryScoresSociety, 
				categoryScoresSport, categoryScoresTechnology);
	}
	
	/**
	 * call this action to update the InsightTrend list of only one insight
	 * @param uniqueId
	 */
	public static void updateInsightTrend(String uniqueId) {
		Insight i = Insight.findByUniqueId(uniqueId);
		i.buildInsightTrends();
	}
	
	/**
	 * call this action to manually update InsightTrend for all insights
	 * whose date isn't passed
	 * @throws Exception
	 */
	public static void updateInsightTrends() throws Exception {
		new InsightTrendsCalculateJob().doJob();
	}
	
	/**
	 * Set a fake validationScore for this insight, this score will be used for user score computation
	 * @param insightUniqueId
	 * @param fakeValidationScore
	 */
	public static void setFakeValidationScore(String insightUniqueId, Double fakeValidationScore) {
		Insight insight = Insight.findByUniqueId(insightUniqueId);
		insight.fakeValidationScore = fakeValidationScore;
		insight.save();
		Application.showInsight(insightUniqueId);
	}
	
	public static void setInsightLanguage(String insightUniqueId, String lang) {
		Insight insight = Insight.findByUniqueId(insightUniqueId);
		insight.lang = Language.findByLabelOrCreate(lang);
		insight.save();
		Application.showInsight(insightUniqueId);
	}

	public static void weeklyMail() {
		new WeeklyMailingJob().now();
	}
	
	public static void weeklyMailSend() {
		new WeeklyMailingSenderJob().now();
	}
	/**
	 * when you move the facebook from a beansight account to another the FacebookFriend.beansightUserFriend should reference
	 * the new account
	 * This method is only here to clean old FacebookFriend that haven't been correctly updated
	 */
	public static void cleanFacebookFriend() {
		List<FacebookFriend> fbfs = FacebookFriend.find("select fbf from FacebookFriend fbf where fbf.beansightUserFriend.facebookUserIdDisabled is not null").fetch();
		// for each FacebookFriend we get the User with the facebookUserId and we set this user on the FacebookFriend 
		for (FacebookFriend fbf : fbfs) {
			User actualUserToUse = User.findByFacebookUserId(fbf.beansightUserFriend.facebookUserIdDisabled);
			Logger.info("%s currently has %s as facebookfriend. The clean is going to replace it with %s", fbf.user.userName, fbf.beansightUserFriend.userName, actualUserToUse.userName);
			fbf.beansightUserFriend = actualUserToUse;
			if (fbf.user.isFollowingUser(actualUserToUse)) {
				fbf.isAdded = true;
				fbf.isHidden = false;
			}
			fbf.save();
		}
	}
	
	/**
	 * run the job that allow to check if your hare following/hiding a facebook friend
	 * then the beansight user should be followed/not followed
	 * 
	 */
	public static void runCheckFacebookFriendsAndFollowSyncJob() {
		new CheckFacebookFriendsAndFollowSyncJob().now();
	}
	
	
//	public static void changePassword(String userName, String newPassword) {
//		User.findByUserName(userName).changePassword(newPassword);
//	}
	
}