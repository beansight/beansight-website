package controllers;

import java.util.List;

import models.Insight;
import models.User;

import play.mvc.*;
import services.InsightService;

public class Application extends Controller {


    public static void index() {
    	display();
    }
    
    
    public static void display() {
    	List<Insight> insights = InsightService.loadInsights();
    	
    	render(insights);
    }
    
    
    public static void createInsight(String insight) {
    	InsightService.createInsight(insight);
    	
    	display();
    }
    
    public static void agree(Long id) {
    	User currentUser = User.findByUserName(Security.connected());
    	
    	InsightService.incrementAgree(id, currentUser.id);
    	
    	display();
    }
    
    public static void disagree(Long id) {
    	InsightService.incrementDisagree(id);
    	
    	display();
    }
}