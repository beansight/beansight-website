package controllers;

import models.User;
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
	
}