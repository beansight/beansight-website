package controllers;

import notifiers.Mails;
import models.ForgotPassword;
import models.User;
import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.Mailer;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
        return User.authenticate(username, password);
    }

	static void onDisconnected() {
		Application.index();
    }
	
    static boolean check(String profile) {
        if("admin".equals(profile)) {
            return CurrentUser.isAdmin();
        }
        return false;
    }
    
    public static boolean isConnected() {
    	return Secure.Security.isConnected();
    }
	
    /**
     * Just render the page to edit user email used to connect to Beansight
     */
    public static void forgotPasswordAskEmail() {
    	render();
    }
    
    /**
     * Send an email to the user who has lost its password
     */
    public static void provideEmailToChangePassword(@Required @Email String email) {
    	// email should exist ...
    	User user = User.findByEmail(email);
    	if (user == null) {
    		validation.addError("email", "Sorry but this email is not linked to a Beansight user");
    	}
    	if (validation.hasErrors()) {
    		renderTemplate("Security/forgotPasswordAskEmail.html", email);
    	}
    	
    	ForgotPassword forgotPassword = new ForgotPassword(email);
    	forgotPassword.save();
    	Mails.forgotPassword(forgotPassword.code, email, "Mails/forgotPassword.html", "en");
    	
    	render(email);
    }
    
    /**
     * redirect to the change password page
     */
    public static void changePassword(String forgotPasswordCode) {
    	ForgotPassword forgotPassword = ForgotPassword.find("code = ?", forgotPasswordCode).first();
    	
    	if (forgotPassword == null) {
    		renderTemplate("Security/changePasswordCodeError.html");
    	}
    	render(forgotPasswordCode);
    }
    
    public static void saveChangedPassword(@Required String forgotPasswordCode, @Required @MinSize(5) String password, @Required @Equals("password") String passwordconfirm) {
    	if (validation.hasErrors()) {
    		validation.keep();
    		renderTemplate("Security/changePassword.html", forgotPasswordCode);
    	}
    	ForgotPassword forgotPassword = ForgotPassword.find("code = ?", forgotPasswordCode).first();
    	User user = User.findByEmail(forgotPassword.email);
    	user.changePassword(password);
    	render();
    }
}
