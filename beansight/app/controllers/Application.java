package controllers;

import java.util.List;

import models.Insight;
import models.User;
import models.Vote.State;
import play.mvc.Controller;
import exceptions.UserIsAlreadyFollowingInsightException;

public class Application extends Controller {

	public static void index() {
		List<Insight> insights = Insight.findAll();
		User currentUser = User.findByUserName(Security.connected());
		
		render(insights, currentUser);
	}

	public static void createInsight(String insightContent) {
		User currentUser = User.findByUserName(Security.connected());
		currentUser.createInsight(insightContent);

		index();
	}
	
	public static void addTag(Long insightId, String label) {
		User currentUser = User.findByUserName(Security.connected());
		Insight insight = Insight.findById(insightId);
		
		currentUser.tag(insight, label);
		
		showInsight(insightId);
	}

	/**
	 * Agree a given insight
	 * 
	 * @param insightId
	 */
	public static void agree(Long insightId) {
		User currentUser = User.findByUserName(Security.connected());
		
		currentUser.voteToInsight(insightId, State.AGREE);
		
		// TODO : only return JSON to use with AJAX
		index();
	}

	/**
	 * Disagree a given insight
	 * 
	 * TODO : should return JSON to user AJAX
	 * 
	 * @param insightId
	 */
	public static void disagree(Long insightId) {
		User currentUser = User.findByUserName(Security.connected());
		currentUser.voteToInsight(insightId, State.DISAGREE);
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
    
    public static void startFollowingInsight(Long insightId) {
    	User currentUser = User.findByUserName(Security.connected());
    	try {
			currentUser.startFollowingThisInsight(insightId);
		} catch (UserIsAlreadyFollowingInsightException e) {
			flash.error(e.getMessage());
		}
		
    	index();
    }

    public static void stopFollowingInsight(Long insightId) {
    	User currentUser = User.findByUserName(Security.connected());
		currentUser.stopFollowingThisInsight(insightId);
    	index();
    }
    
    public static void addComment(Long insightId, String content) {
    	User currentUser = User.findByUserName(Security.connected());
    	Insight insight = Insight.findById(insightId);
    	insight.addComment(content, currentUser);
    	showInsight(insightId);
    }
    
}