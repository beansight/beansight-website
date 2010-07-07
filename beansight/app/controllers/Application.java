package controllers;

import java.util.List;

import models.Insight;
import models.User;
import play.mvc.Controller;
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
    	User currentUser = User.findByUserName(Security.connected());
    	InsightService.createInsight(insight, currentUser);
    	
    	display();
    }
    
    public static void agree(Long id) {
    	User currentUser = User.findByUserName(Security.connected());
    	InsightService.incrementAgree(id, currentUser);
    	display();
    }
    
    public static void disagree(Long id) {
    	User currentUser = User.findByUserName(Security.connected());
    	InsightService.incrementDisagree(id, currentUser);    	
    	display();
    }
    
}