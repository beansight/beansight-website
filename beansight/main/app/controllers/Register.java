package controllers;

import java.util.Date;

import notifiers.Mails;
import models.Promocode;
import models.User;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.Required;
import play.i18n.Messages;
import play.libs.Crypto;
import play.mvc.Controller;

public class Register extends Controller {

	public static void register(String promocode, String email, String username) {
		render(email, username, promocode);
	}
	
	public static void registerNew(@Required @Email String email, @Required String username, @Required String password, @Required @Equals("password") String passwordconfirm, String promocode) {
		// See if promocode is ok:
		Promocode code = Promocode.find("byCode", promocode).first();
		if(code != null && code.nbUsageLeft > 0 && code.endDate.after(new Date())) {
			code.nbUsageLeft--;
			code.save();
		} else {
			validation.addError("promocode", Messages.get("registernotvalidpromocode"));
		}
		
		// Check if username or email not already in use, because username and email must be unique !
		if(!User.isEmailAvailable(email)) {
			validation.addError("email", Messages.get("registeremailexist")); 
		}
		if(!User.isUsernameAvailable(username)) {
			validation.addError("username", Messages.get("registerusernameexist")); 
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
		
		Application.index();
	}
	
	/** Confirm that the email adress of the user is a real one */
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
