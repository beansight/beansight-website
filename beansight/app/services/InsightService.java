package services;

import java.util.List;

import play.cache.Cache;

import models.Insight;
import models.User;

public class InsightService {

	
	   public static void createInsight(String insight, User user) {
	    	user.createAnInsight(insight);
	    }
	
	   public static List<Insight> loadInsights() {
	    	List<Insight> allInsights = Insight.findAll();
	    	
	    	return allInsights;
	   }
	   
	   public static void incrementAgree(Long insightId, User who) {
		   Insight insight = Insight.findById(insightId);
		   
		   // On ne peut voter qu'une seule fois pour un insight
		   if (who.ownThisInsight(insightId))
			   	return;
		   
		   insight.addSomeoneWhoAgreed(who);
		   insight.save();
	   }
	
	   public static void incrementDisagree(Long insightId, User who) {
		   Insight insight = Insight.findById(insightId);
		   
		   // On ne peut voter qu'une seule fois pour un insight
		   if (who.ownThisInsight(insightId))
			   	return;
		   
		   insight.addSomeoneWhoDisagreed(who);
		   insight.save();
	   }
	   
//	   public static boolean isOwningThisInsight(Long insightId, Long someoneId) {
//		   Insight insight = Insight.findById(insightId);
//		   if (insight.owner.id.equals(insightId))
//			   return true;
//		   else
//			   return false;
//	   }
}
