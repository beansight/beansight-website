package controllers;

import java.util.List;

import exceptions.CannotVoteForAnInsightYouOwnException;

import models.Insight;
import models.User;
import play.mvc.Controller;

public class MyInsightsTag extends Controller {


    public static List<Insight> myInsights() {
    	User currentUser = User.findByUserName(Security.connected());
    	return currentUser.createdInsights;
    }
    
}