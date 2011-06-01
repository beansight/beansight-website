package controllers;

import helpers.TimeSeriePoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jobs.AnalyticsJob;
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
import models.Topic;
import models.TopicActivity;
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
	
	public static void index() {
		render();
	}
	
	public static void analytics() {
		renderArgs.put("dailyTotalVote", DailyTotalVote.findAll());
		renderArgs.put("dailyTotalInsight", DailyTotalInsight.findAll());
		renderArgs.put("dailyTotalComment", DailyTotalComment.findAll());
		renderArgs.put("bestUserVotes", User.findBestVoters(20));
		renderArgs.put("bestUserInsights", User.findBestCreators(20));

		// total users evolution
		Map<Date, TimeSeriePoint> dailyTotalUsersMap = new HashMap<Date, TimeSeriePoint>();
		Map<Date, TimeSeriePoint> dailyNewUsersMap = new HashMap<Date, TimeSeriePoint>();
		List<TimeSeriePoint> dailyNewUsersList = new ArrayList<TimeSeriePoint>();
		List<User> users = User.all().fetch();
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
				dailyNewUsersList.add(new TimeSeriePoint(date.toDate(), totalUser - previousTotalUser));
				previousTotalUser = totalUser;
			}
			point.value = totalUser;
		}
		renderArgs.put("dailyTotalUsers", dailyTotalUsersMap.values());
		
		// top 20 most read insights by registered users since last 7 days
		List<Object[]> top20Insights = UserInsightVisit.find("select v.insight.uniqueId, v.insight.content, count(v) " +
				"from UserInsightVisit v " +
				"where v.creationDate > :crDate " +
				"group by v.insight.id order by count(v) desc").
				bind("crDate", new DateMidnight().minusDays(7).toDate()).fetch(20);
		renderArgs.put("top20Insights", top20Insights);
		
		// % active users / day
		int DAYS = 1;
		Map<DateMidnight, Set<Long>> visitsDayMap = new HashMap<DateMidnight, Set<Long>>();
		List<UserListInsightsVisit> list = UserListInsightsVisit.all().fetch(); 
		DateMidnight firstDate = new DateMidnight( list.get(0).creationDate );
		for (UserListInsightsVisit v : list) {
			DateMidnight date = new DateMidnight(v.creationDate);
			if (!visitsDayMap.containsKey(date)) {
				visitsDayMap.put(date, new HashSet<Long>());
			}
			visitsDayMap.get(date).add(v.user.id);
		}
		
		List<TimeSeriePoint> prctActiveUsersByDay = new ArrayList<TimeSeriePoint>();
		List<TimeSeriePoint> prctActiveUsersMinusNewUsersByDay = new ArrayList<TimeSeriePoint>();
		CircularFifoBuffer fifo = new CircularFifoBuffer(DAYS);
		while (!visitsDayMap.isEmpty()) {
			fifo.add(visitsDayMap.get(firstDate).size());
			visitsDayMap.remove(firstDate);
			if (fifo.size()==DAYS) {
				Iterator it = fifo.iterator();
				Double total = 0d;
				while (it.hasNext()) {
					total = total + (Integer)it.next();
				}
				if (dailyTotalUsersMap.get(firstDate.toDate()) != null) {
					prctActiveUsersByDay.add(new TimeSeriePoint(firstDate.toDate(), (total/DAYS)/dailyTotalUsersMap.get(firstDate.toDate()).value * 100 ) );
					prctActiveUsersMinusNewUsersByDay.add(new TimeSeriePoint(firstDate.toDate(), (total/DAYS)/(dailyTotalUsersMap.get(firstDate.toDate()).value - dailyNewUsersMap.get(firstDate.toDate()).value) * 100 ) );
				}
			}
			firstDate = firstDate.plusDays(1);
		}
		renderArgs.put("activeUsers", prctActiveUsersByDay);
		renderArgs.put("activeUsersMinusNewUsers", prctActiveUsersMinusNewUsersByDay);
		renderArgs.put("dailyNewUsers", dailyNewUsersList);
		
		// last ten comments
		List<Comment> comments = Comment.find("order by creationDate desc").fetch(15);
		renderArgs.put("comments", comments);
		
		render();
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
	
	
	public static void createTopic(Long topicId, String topicName, String tagLabelList) {
		if (tagLabelList != null) {
			List<Tag> tags = new ArrayList<Tag>();
			for (String tagLabel : tagLabelList.split(",")) {
				Tag tag = Tag.findByLabel(tagLabel.trim());
				if (tag == null) {
					tag = new Tag(tagLabel, null, CurrentUser.getCurrentUser());
					tag.save();
				}
				tags.add( tag );
			}
		
			Topic topic = null;
			if ( topicId !=null) {
				topic = Topic.findById(topicId);
				topic.label = topicName;
				topic.tags = tags; 
			} else {
				topic = new Topic(topicName, tags, CurrentUser.getCurrentUser());
			}
			topic.save();
		}
		
		List<Topic> topics = Topic.all().fetch();
		render(topics);
	}
	
	public static void getTopic(Long topicId) {
		Topic topic = Topic.findById(topicId);
		
		Map<String, Object> topicData = new HashMap<String, Object>();
		topicData.put("id", topic.id);
		topicData.put("name", topic.label);
		
		List<String> tags = new ArrayList<String>();
		for (Tag tag : topic.tags) {
			tags.add(tag.label);
		}
		
//		String tags = "";
//		for (Tag tag : topic.tags) {
//			if (tags.length() ==  0) {
//				tags += tag.label;
//			} else {
//				tags = tags + ", " + tag.label;
//			}
//			
//		}
		topicData.put("tags", tags);
		
		renderJSON(topicData);
	}
	
	public static void deleteTopic(Long topicId) {
		Topic topic = Topic.findById(topicId);
		topic.tags = null;
		topic.save();
		topic.delete();
	}

	/** feature the given topic */
	public static void featureTopic(Long topicId, String lang) {
		Topic topic = Topic.findById(topicId);
		Language language = Language.findByLabelOrCreate(lang);
		topic.feature(language);
		renderText("Topic "+ topic.label +" was featured in language " + language.label);
	}
	
	/** stop featuring the given topic */
	public static void stopFeatureTopic(Long topicId, String lang) {
		Topic topic = Topic.findById(topicId);
		Language language = Language.findByLabelOrCreate(lang);
		topic.stopFeature(language);
		renderText("Topic "+ topic.label +" is not featured anymore.");
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
	 * Admin only: Call this method to replace all activities with new activities based on the "following" information for Users, Topics and Insights
	 */
	public static void CopyFavoriteToActivity() {
		TopicActivity.deleteAll();
		UserActivity.deleteAll();
		InsightActivity.deleteAll();
		
		List<User> users = User.findAll(); 
		for(User user : users) {
			for(Insight insight : user.followedInsights) {
				new InsightActivity(user, insight).save();
			}
			for(Topic topic : user.followedTopics) {
				new TopicActivity(user, topic).save();
			}
			for(User followedUser : user.followedUsers) {
				new UserActivity(user, followedUser).save();
			}
		}
		
		renderText("All activities for all users generated");
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
}