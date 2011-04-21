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
import jobs.scoring.BuildInsightValidationAndUserScoreJob;
import jobs.scoring.InsightValidationJob;
import jobs.weeklymailing.WeeklyMailingJob;
import jobs.weeklymailing.WeeklyMailingSenderJob;
import models.CategoryEnum;
import models.Comment;
import models.Insight;
import models.Language;
import models.PeriodEnum;
import models.Tag;
import models.Topic;
import models.User;
import models.analytics.DailyTotalComment;
import models.analytics.DailyTotalInsight;
import models.analytics.DailyTotalVote;
import models.analytics.UserInsightVisit;
import models.analytics.UserListInsightsVisit;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import play.Logger;
import play.Play;
import play.data.binding.As;
import play.modules.search.Search;
import play.mvc.Controller;
import play.mvc.With;


@With(Secure.class)
@Check("admin")
public class Admin extends Controller {

    /**
     * Make sure the language is the one the user has chosen.
     */
	/*
	@Before(only={"addInvitationsToUser"})
    static void loadMenuData() {
		Application.loadMenuData();
    }
    */
	
	/**
	 * Add invitations to this user
	 */
	public static void addInvitationsToUser(long userId, long number) {
		User user = User.findById(userId);
		user.addInvitations(number);
		render(user);
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
		List<User> users = User.all().fetch();
		Double totalUser = 0d;
		for (User user : users) {
			totalUser++;
			DateMidnight date = new DateMidnight(user.getCrdate());
			TimeSeriePoint point = dailyTotalUsersMap.get(date.toDate());
			if (point == null) {
				point = new TimeSeriePoint(date.toDate(), totalUser);
				dailyTotalUsersMap.put(date.toDate(), point);
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
		
		List<TimeSeriePoint> results = new ArrayList<TimeSeriePoint>();
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
					results.add(new TimeSeriePoint(firstDate.toDate(), (total/DAYS)/dailyTotalUsersMap.get(firstDate.toDate()).value * 100 ) );
				}
			}
			firstDate = firstDate.plusDays(1);
		}
		renderArgs.put("activeUsers", results);
		
		// last ten comments
		List<Comment> comments = Comment.find("order by creationDate desc").fetch(15);
		renderArgs.put("comments", comments);
		
		render();
	}
	
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
	 * TODO : temporary method to create the user analytics without waiting for the job to start the first time we'll release it !
	 */
	public static void doUserAnalyticsJob() {
		try {
			new AnalyticsJob().doJob();
		} catch (Throwable e) {
			renderText("doUserAnalyticsJob finished with error : " + e.getMessage());
			throw new RuntimeException(e) ;
		}
		renderText("doUserAnalyticsJob finished: ok");
	}
	
	public static void doInsightValidationAndUserScoreJob() {
		try {
			new InsightValidationJob().doJob();
		} catch (Exception e) {
			renderText("doInsightValidationAndUserScoreJob finished with error : " + e.getMessage());
			throw new RuntimeException(e) ;
		}
		renderText("doInsightValidationAndUserScoreJob finished: ok");
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
	
	public static void buildScores (@As("yyyy-MM-dd") Date fromDate, @As("yyyy-MM-dd") Date toDate) {
		try {
			new BuildInsightValidationAndUserScoreJob(fromDate, toDate).now();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void insightValidation() {
		new InsightValidationJob().now();
	}
	
	public static void computeUserScore() {
		Date toDate = new DateMidnight().minusDays(1).toDate();
		Date from = new Date(toDate.getTime() - PeriodEnum.THREE_MONTHS.getTimePeriod());
		List<User> usersToUpdate = User.findAll();

		int i = 1;
		for(User u : usersToUpdate) {
			Logger.info("updating user " + i + "/" + usersToUpdate.size());
			u.computeUserScore(toDate, PeriodEnum.THREE_MONTHS);
			i++;
		}
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
	
	
	public static void updateInsightTrend(String uniqueId) {
		Insight i = Insight.findByUniqueId(uniqueId);
		System.out.println("updateInsightTrends : " + i.id);
		i.buildInsightTrends();
	}
	
	public static void updateInsightTrends() throws Exception {
		new InsightTrendsCalculateJob(1, true).doJob();
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
}