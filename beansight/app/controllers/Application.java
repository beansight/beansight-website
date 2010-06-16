package controllers;

import java.util.List;

import models.Insight;

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
    
}