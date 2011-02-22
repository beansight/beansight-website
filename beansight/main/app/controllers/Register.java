package controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.Promocode;
import models.User;
import models.Vote.State;
import notifiers.Mails;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.Match;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.i18n.Messages;
import play.mvc.Controller;

public class Register extends Controller {

	public static void register(String promocode, String email, String username) {
		render(email, username, promocode);
	}
	
	// TODO : I18N
	public static void registerNew(@Required @Email String email, @Required @Match(value="[a-zA-Z0-9_]{3,16}", message="username has to be 3-16 chars, no space, no accent and no punctuation") String username, @Required @MinSize(5) String password, @Required @Equals("password") String passwordconfirm, @Required String promocode) throws Throwable {
		// Check if username or email not already in use, because username and email must be unique !
		if(!User.isEmailAvailable(email)) {
			validation.addError("email", Messages.get("registeremailexist")); 
		}
		if(!User.isUsernameAvailable(username)) {
			validation.addError("username", Messages.get("registerusernameexist")); 
		}
		
		// See if promocode is ok:
		Promocode code = Promocode.findbyCode(promocode);
		if(code != null && code.nbUsageLeft > 0 && code.endDate.after(new Date())) {
			// remove one to the promocode only if no validation error remaining
			if (!validation.hasErrors()) {
				code.nbUsageLeft--;
				code.save();
			}
		} else {
			validation.addError("promocode", Messages.get("registernotvalidpromocode"));
		}
		
		if (validation.hasErrors()) {
	        validation.keep();
	        register(promocode, email, username);
	    }
		
		Logger.info("Register: " + email + "/" + username);
		User user = new User(email, username, password);
		user.save();
			
		// send a password confirmation mail
		Mails.confirmation(user);
		
		// connect immediately the user
		Secure.authenticate(email, password, false);
	}
	
	/**
	 * Is the given userName available on beansight.com?
	 * @return : true if the userName is available, false otherwise
	 */
	public static void isUserNameAvailable(String username) {
		boolean available = User.isUsernameAvailable(username);
		if(available) {
			renderJSON(available);
		} else {
			renderJSON("\""+ Messages.get("registerusernameexist") + "\"");
		}
	}
	
	/**
	 * Is the given email available on beansight.com?
	 * @return : true if the email is available, false otherwise
	 */
	public static void isEmailAvailable(String email) {
		boolean available = User.isEmailAvailable(email);
		if(available) {
			renderJSON(available);
		} else {
			renderJSON("\""+ Messages.get("registeremailexist") + "\"");
		}
	}
	
	public static void extAuthFirstTimeConnectPage() {
		if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			renderArgs.put("email", currentUser.email);
			renderArgs.put("username", currentUser.userName);
			render();
		} else {
			Application.index();
		}
	}
	
	
	public static void facebookFirstTimeConnect(
		@Required @Email String email, 
		@Required @Match(value="[a-zA-Z0-9_]{3,16}", message="username has to be 3-16 chars, no space, no accent and no punctuation") String username, 
		@Required String promocode) {
		
		if(Security.isConnected()) {
			Promocode code = Promocode.findbyCode(promocode);
			if(code != null && code.nbUsageLeft > 0 && code.endDate.after(new Date())) {
				// remove one to the promocode only if no validation error remaining
				if (!validation.hasErrors()) {
					code.nbUsageLeft--;
					code.save();
				}
			} else {
				validation.addError("promocode", Messages.get("registernotvalidpromocode"));
			}
			
			User currentUser = CurrentUser.getCurrentUser();
			
			// if the user have change its username check that it's available
			if (!username.equalsIgnoreCase(currentUser.userName)) {
				if (!User.isUsernameAvailable(username)) {
					validation.addError("username", Messages.get("registerusernameexist")); 
				}
			}
			
			// if the user have change its email check that it's available
			if (!email.equalsIgnoreCase(currentUser.email)) {
				if (!User.isEmailAvailable(email)) {
					validation.addError("email", Messages.get("registeremailexist")); 
				}
			}
			
			if (validation.hasErrors()) {
				renderArgs.put("email", email);
				renderArgs.put("username", username);
				renderArgs.put("promocode", promocode);
		        renderTemplate("Register/extAuthFirstTimeConnectPage.html");
		    }
			
			currentUser.email = email;
			currentUser.userName = username;
			currentUser.isPromocodeValidated = true;
			currentUser.save();
		} 
		
		Application.index();
	}
	
	/** Confirm that the email address of the user is a real one */
	public static void confirm(String uuid) {
		if(uuid == null || uuid.isEmpty()) {
			notFound();
		}
		User user = User.find("byUuid", uuid).first();
		notFoundIfNull(user);
		user.emailConfirmed = true;
		user.save();
		
		Logger.info("Email confirmation for user : " + user.email);
		
		render(user);
	}
	
}
