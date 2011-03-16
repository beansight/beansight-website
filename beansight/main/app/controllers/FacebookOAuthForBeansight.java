package controllers;

import org.apache.commons.lang.RandomStringUtils;

import models.Promocode;
import models.User;
import models.analytics.UserClientInfo;
import gson.FacebookModelObject;
import play.Play;
import play.libs.Crypto;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.results.Redirect;

import com.google.gson.Gson;

import controllers.FacebookOAuth.FacebookOAuthDelegate;

/**
 * 
 * @author jb
 *
 */
public class FacebookOAuthForBeansight extends FacebookOAuth.FacebookOAuthDelegate {
    
	public static final String FACEBOOK_REGISTER_VIA_LOGIN = "FACEBOOK_REGISTER_VIA_LOGIN";
	public static final String FACEBOOK_REGISTER_VIA_SIGNUP = "FACEBOOK_REGISTER_VIA_SIGNUP";
                        
    public static void onFacebookAuthentication(FacebookModelObject facebookModelObject) {
        Long facebookUserId = facebookModelObject.getId();

        User facebookUser = null;
        
        // facebook email could be null/void
        if (facebookModelObject.getEmail() != null && !facebookModelObject.getEmail().trim().equals("")) {
        	if (!User.isEmailAvailable(facebookModelObject.getEmail())) {
        		facebookUser = User.findByEmail(facebookModelObject.getEmail());
        	}
        }
        
        // facebookUser is still null after an email lookup we try to find him with his facebook id
        if (facebookUser == null) {
        	facebookUser = User.findByFacebookUserId(facebookModelObject.getId());
        }
        
        // Finally no user found this is the first time this user connects to beansight
        // then create a beansight account linked to his facebook account
        if (facebookUser == null) {
        	String facebookScreenName = User.createNewAvailableUserName(facebookModelObject.getFirst_name());
        	
        	// note : we have to generate a random password because if we use "" as a password facebook account could be easily hacked
            facebookUser = new User(facebookModelObject.getEmail(), facebookScreenName, RandomStringUtils.randomAlphabetic(15));
            facebookUser.facebookUserId = facebookUserId;
            facebookUser.emailConfirmed = true;
            facebookUser.isPromocodeValidated = true;
            facebookUser.save();
        } 
        // if the user already has a beansight account and he is trying 
        // to connect with his facebook account then "merge" the facebook 
        // account with the beansight account
        else if (facebookUser != null && facebookUser.facebookUserId == null) {
        	facebookUser.facebookUserId = facebookUserId;
        	facebookUser.save();
        }
        
        session.put("isFacebookUser", Boolean.TRUE);
        session.put("facebookUserId", facebookUserId);
        session.put("username", facebookUser.email);
        // Remember
        response.setCookie("rememberme", Crypto.sign(facebookUser.email) + "-" + facebookUser.email, "30d");
        
        Application.index();
    }
    
    
    public static String getExtendedPermissions() {
    	return "email";
    }
    
}
