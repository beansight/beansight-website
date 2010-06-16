package services;

import java.util.List;

import models.Insight;

public class InsightService {

	   public static void createInsight(String insight) {
	    	Insight i = new Insight();
	    	i.content = insight;
	    	i.save();
	    }
	
	   public static List<Insight> loadInsights() {
	    	List<Insight> allInsights = Insight.findAll();
	    	
	    	return allInsights;
	    }
	   
}
