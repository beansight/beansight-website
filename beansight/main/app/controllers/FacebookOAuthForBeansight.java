package controllers;

import models.User;
import gson.FacebookModelObject;
import play.Play;
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
    
                        
    public static void onFacebookAuthentication(FacebookModelObject facebookModelObject) {
        Long facebookUserId = facebookModelObject.getId();
        String facebookScreenName = facebookModelObject.getName();

        User facebookUser = User.findByFacebookUserId(facebookUserId);
        
        // If this is the first time this user uses his facebook account to
        // connect to beansight
        // then create a beansight account linked to his facebook account
        if (null == facebookUser) {
        	// if the username is already in use on beansight we add @facebook to the initial userName
			if (!User.isUsernameAvailable(facebookScreenName)) {
				facebookScreenName = facebookScreenName + "@facebook";
			}
            facebookUser = new User(facebookModelObject.getEmail(), facebookScreenName, "");
            facebookUser.facebookUserId = facebookUserId;
            facebookUser.save();

        } 
        
        session.put("isFacebookUser", Boolean.TRUE);
        session.put("facebookUserId", facebookUserId);
        session.put("username", facebookScreenName);
        
        Application.index();
    }
    
    public static String getExtendedPermissions() {
    	return "email";
    }
    
}
