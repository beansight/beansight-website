package controllers;

import java.util.List;

import exceptions.CannotVoteForAnInsightYouOwnException;

import models.Insight;
import models.User;
import play.mvc.Controller;

public class MyFollowedInsightsTag extends Controller {


    public static List<Insight> myFollowedInsights() {
    	User currentUser = User.findByUserName(Security.connected());
    	return currentUser.followedInsights;
    }
    
}