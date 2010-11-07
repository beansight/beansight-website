package controllers;

import java.util.List;

import exceptions.CannotVoteForAnInsightYouOwnException;

import models.Insight;
import models.User;
import play.mvc.Controller;

public class CurrentUser extends Controller {

    public static User getCurrentUser() {
        if (Boolean.parseBoolean(session.get("isTwitterUser"))) {
            return User.findByUserName(Security.connected());
        }
        return User.find("byEmail", Security.connected()).first();    
    }
    
    public static String getCurrentUserName() {
    	return getCurrentUser().userName;
    }
    
}