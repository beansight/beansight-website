package controllers;

import java.util.List;

import exceptions.CannotVoteForAnInsightYouOwnException;

import models.Insight;
import models.User;
import models.Vote;
import models.Vote.State;
import play.mvc.Controller;

public class Application extends Controller {

	public static void index() {
		List<Insight> insights = Insight.findAll();
		render(insights);
	}

	public static void createInsight(String insightContent) {
		User currentUser = User.findByUserName(Security.connected());
		currentUser.createInsight(insightContent);

		index();
	}

	/**
	 * Agree a given insight
	 * 
	 * @param insightId
	 */
	public static void agree(Long insightId) {
		User currentUser = User.findByUserName(Security.connected());
		Insight insight = Insight.findById(insightId);
		
		currentUser.voteToInsight(insight, State.AGREE);
		
		// TODO : only return JSON to use with AJAX
		index();
	}

	/**
	 * Disagree a given insight
	 * 
	 * @param insightId
	 */
	public static void disagree(Long insightId) {
		User currentUser = User.findByUserName(Security.connected());
		Insight insight = Insight.findById(insightId);
		
		currentUser.voteToInsight(insight, State.DISAGREE);
		
		
		// TODO : only return JSON to use with AJAX
		index();
	}
	
	/**
	 * Show info about a given insight
	 * @param id
	 */
    public static void showInsight(Long id) {
        Insight insight = Insight.findById(id);
        notFoundIfNull(insight);
        render(insight);
    }
    
    /**
     * Show info about a given user
     * @param id
     */
    public static void showUser(Long id) {
    	User user = User.findById(id);
        notFoundIfNull(user);
    	render(user);
    }

}