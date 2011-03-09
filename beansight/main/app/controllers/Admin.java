package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jobs.AnalyticsJob;
import jobs.InsightGraphTrendsJob;
import jobs.InsightValidationAndUserScoreJob;
import models.Comment;
import models.Insight;
import models.Trend;
import models.User;

import org.joda.time.DateTime;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.modules.search.Search;
import play.mvc.Controller;
import play.mvc.With;


@With(Secure.class)
@Check("admin")
public class Admin extends Controller {

	/**
	 * Add invitations to this user
	 */
	public static void addInvitationsToUser(long userId, long number) {
		User user = User.findById(userId);
		user.addInvitations(number);
		Application.loadMenuData();
		render(user);
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
			new InsightValidationAndUserScoreJob().doJob();
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
//		renderText("User.removeCreatedAccountWithNoInvitationBefore finished : ok, all user accounts with no invitation created before " +
//				datetime + " have been deleted");
	}
	
	
	public static void rebuildAllTrends(int period) {
		List<Insight> list = Insight.all().fetch();
		for (Insight i : list) {
			i.buildTrends(new DateTime(i.creationDate), null, 4);
		}
	}
	
	public static void updateAllTrends(int period) {
		List<Insight> list = Insight.all().fetch();
		for (Insight i : list) {
			i.buildTrends(null, null, 4);
		}
	}
	
	public static void rebuildTrendsForInsight(Long insightId, int period) {
		Insight i = Insight.findById(insightId);
		i.buildTrends(new DateTime(i.creationDate), null, period);
	}
	
	public static void rebuildTrendsForInsightUniqueId(String uniqueId, int period) {
		Insight i = Insight.findByUniqueId(uniqueId);
		i.buildTrends(new DateTime(i.creationDate), null, period);
	}
	
	public static void updateTrendsForInsightUniqueId(String uniqueId, int period) {
		Insight i = Insight.findByUniqueId(uniqueId);
		i.buildTrends(null, null, period);
	}
}