package controllers;

import java.util.List;

import models.Insight;

import play.mvc.*;

public class Application extends Controller {

    public static void index() {
    	List<Insight> allInsights = Insight.getAll();
    	
    	render(allInsights);
    }

    public static void createInsight(String insightStr) {
    	Insight insight = new Insight();
    	insight.content = insightStr;
    	insight.insert();
    	
    	List<Insight> allInsights = Insight.getAll();
    	
    	render(allInsights);
    }
    
}