package controllers;

import java.util.List;

import exceptions.CannotVoteForAnInsightYouOwnException;

import models.Insight;
import models.User;
import play.mvc.Controller;

public class CurrentUser extends Controller {

    public static User getCurrentUser() {
        if ( Boolean.parseBoolean(session.get("isTwitterUser")) ) {
            return User.findByTwitterUserId(session.get("twitterUserId"));
        }
        if ( Boolean.parseBoolean(session.get("isFacebookUser")) ) {
            return User.findByFacebookUserId(new Long(session.get("facebookUserId")));
        }        
        return User.find("byEmail", Security.connected()).first();    
    }
    
    public static String getCurrentUserName() {
    	return getCurrentUser().userName;
    }
    
    public static boolean isAdmin() {
    	return getCurrentUser().isAdmin;
    }
    
}