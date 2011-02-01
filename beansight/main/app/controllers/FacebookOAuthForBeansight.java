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

        User facebookUser = null;
        
        // facebook email could be null/void
        if (facebookModelObject.getEmail() != null && !facebookModelObject.getEmail().trim().equals("")) {
        	facebookUser = User.findByEmail(facebookModelObject.getEmail());
        }
        
        // facebookUser is still null after an email lookup we try to find him with his facebook id
        if (facebookUser == null) {
        	facebookUser = User.findByFacebookUserId(facebookModelObject.getId());
        }
        
        // Finally no user found this is the first time this user connects to beansight
        // then create a beansight account linked to his facebook account
        if (facebookUser == null) {
        	// if the username is already in use on beansight we add @facebook to the initial userName
			if (!User.isUsernameAvailable(facebookScreenName)) {
				facebookScreenName = facebookScreenName + "_facebook";
			}
            facebookUser = new User(facebookModelObject.getEmail(), facebookScreenName, "");
            facebookUser.facebookUserId = facebookUserId;
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
        session.put("username", facebookScreenName);
        
        Application.index();
    }
    
    public static String getExtendedPermissions() {
    	return "email";
    }
    
}
