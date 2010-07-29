package services;

import java.util.List;

import exceptions.CannotVoteForAnInsightYouOwnException;

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
	   
	   public static void incrementAgree(Long insightId, User who) throws CannotVoteForAnInsightYouOwnException {
		   Insight insight = Insight.findById(insightId);
		   
		   // On ne peut voter que pour l'insight de quelqu'un d'autre
		   if (who.ownThisInsight(insightId))
			   throw new CannotVoteForAnInsightYouOwnException();
		   
		   // On ne peut pas voter plus d'une fois pour le même insight
		   if (who.hasAlreadyVotedForThisInsight(insightId))
			   System.out.println("multi vote détecté");
		   
		   insight.addSomeoneWhoAgreed(who);
		   insight.save();
	   }
	
	   public static void incrementDisagree(Long insightId, User who) throws CannotVoteForAnInsightYouOwnException {
		   Insight insight = Insight.findById(insightId);
		   
		   // On ne peut voter que pour l'insight de quelqu'un d'autre
		   if (who.ownThisInsight(insightId))
			   	throw new CannotVoteForAnInsightYouOwnException();
		   
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
