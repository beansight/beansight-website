package controllers;

import java.net.URL;
import java.util.List;

import org.hibernate.Session;

import exceptions.CannotVoteForAnInsightYouOwnException;

import models.Insight;
import models.User;
import play.mvc.Controller;

public class CurrentUser extends Controller {

    public static User getCurrentUser() {
    	if ( session.get("userId")!=null) {
            User u = User.findById(new Long(session.get("userId")));
            // email should be the same as the one in the cookie
            if (u.email.equalsIgnoreCase(Security.connected()) ) {
            	return u;
            }
        }
        if ( Boolean.parseBoolean(session.get("isTwitterUser")) ) {
            User user = User.findByTwitterUserId(session.get("twitterUserId"));
            session.put("userId", user.getId());
            return user;
        }
        if ( Boolean.parseBoolean(session.get("isFacebookUser")) ) {
        	User user = User.findByFacebookUserId(new Long(session.get("facebookUserId")));
            session.put("userId", user.getId());
            return user;
        }        
//        return User.find("byEmail", Security.connected()).first();
        User user = User.findByEmail(Security.connected());
        if (user != null && !session.contains("userId")) {
        	session.put("userId", user.getId());
        }
        
        return user;
    }
    
    public static String getCurrentUserName() {
    	return getCurrentUser().userName;
    }
    
    public static String getCurrentUserEmail() {
    	return getCurrentUser().email;
    }
    
    public static String getCurrentUserHashCode() {
    	return getCurrentUser().avatarHashCode();
    }
    
    public static boolean isAdmin() {
    	return getCurrentUser().isAdmin;
    }
    
}