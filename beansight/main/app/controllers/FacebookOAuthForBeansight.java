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
            // TODO : we should check that the username is not already in use, and if so add something like _twitterat the end of the username.            
            facebookUser = new User("", facebookScreenName, "");
            facebookUser.facebookScreenName = facebookScreenName;
            facebookUser.facebookUserId = facebookUserId;
            facebookUser.save();

        } else {
            // update the facebook screen name
            facebookUser.facebookScreenName = facebookScreenName;
            facebookUser.save();
        }
        
        session.put("isFacebookUser", Boolean.TRUE);
        session.put("facebookUserId", facebookUserId);
        session.put("username", facebookScreenName);
        
        Application.index();
    }
    
}
