package controllers;

import java.util.List;

import exceptions.CannotVoteForAnInsightYouOwnException;

import models.Insight;
import models.User;
import play.mvc.Controller;

public class Application extends Controller {


    public static void index() {
    	display();
    }
    
    
    public static void display() {
    	List<Insight> insights = Insight.findAll();
    	
    	render(insights);
    }
    
    
    public static void createInsight(String insight) {
    	User currentUser = User.findByUserName(Security.connected());
    	currentUser.createAnInsight(insight);
    	
    	display();
    }
    
    public static void agree(Long id) {
    	User currentUser = User.findByUserName(Security.connected());
    	display();
    }
    
    
    public static void disagree(Long id) {
    	User currentUser = User.findByUserName(Security.connected());
    	display();
    }
    
}