package services;

import java.util.List;

import play.cache.Cache;

import models.Insight;
import models.User;

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
	   
	   public static void incrementAgree(Long insightId, Long whoId) {
		   Insight insight = Insight.findById(insightId);
		   User who = User.findById(whoId);
		   insight.addSomeoneWhoAgreed(who);
		   insight.save();
	   }
	
	   public static void incrementDisagree(Long insightId) {
		   Insight insight = Insight.findById(insightId);
		   insight.disagreeCount++;
		   insight.save();
	   }
}
