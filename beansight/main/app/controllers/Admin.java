package controllers;

import jobs.UsersAnalyticsJob;
import models.Insight;
import models.User;
import play.Logger;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Required;
import play.i18n.Lang;
import play.modules.search.Search;
import play.mvc.Before;
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
	}
	
	/**
	 * TODO : temporary method to create the user analytics without waiting for the job to start the first time we'll release it !
	 */
	public static void usersAnalyticsJob() {
		try {
			new UsersAnalyticsJob().doJob();
		} catch (Throwable e) {
			renderText("UsersAnalyticsJob finished with error : " + e.getMessage());
			Logger.error(e, "UsersAnalyticsJob finished with error");
		}
		renderText("UsersAnalyticsJob finished : ok");
	}
}