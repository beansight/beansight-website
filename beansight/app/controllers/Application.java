package controllers;

import java.util.Date;
import java.util.List;

import models.Category;
import models.Insight;
import models.User;
import models.Vote.State;
import play.mvc.Controller;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.UserIsAlreadyFollowingInsightException;

public class Application extends Controller {

	public static void index() {
		List<Insight> insights = Insight.findAll();
		
		if(Security.isConnected()) {
			User currentUser = User.findByUserName(Security.connected());
			render("Application/indexConnected.html", insights, currentUser);
		}
		
		render("Application/indexNotConnected.html", insights);
	}

	/**
	 * create an insight for the current user
	 * @param insightContent: the content of this insight
	 * @param endDate: the end date chosen by the user
	 * @param tagLabelList: a comma separated list of tags
	 * @param categoryId: the ID of the category of the insight
	 */
	public static void createInsight(String insightContent, Date endDate, String tagLabelList, long categoryId) {
		User currentUser = User.findByUserName(Security.connected());
		currentUser.createInsight(insightContent, endDate, tagLabelList, categoryId);

		// TODO : return JSON, this action should be AJAX, no page reload when submitting an insight
		index();
	}
	
	/**
	 * Agree a given insight
	 * 
	 * @param insightId
	 */
	public static void agree(Long insightId) {
		// TODO : if not connected: go to log / signin page
		User currentUser = User.findByUserName(Security.connected());
		try {
			currentUser.voteToInsight(insightId, State.AGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			// TODO : add a message on the UI ?
		}
		// TODO : only return JSON to use with AJAX
		index();
	}

	/**
	 * Disagree a given insight
	 * 
	 * @param insightId
	 */
	public static void disagree(Long insightId) {
		// TODO : if not connected: go to log / signin page
		User currentUser = User.findByUserName(Security.connected());
		try {
			currentUser.voteToInsight(insightId, State.DISAGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			// TODO : add a message on the UI ?
		}
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
    
    /**
     * Add a comment to a specific insight for the current user
     * @param insightId: id of the insight
     * @param content: text content of the insight
     */
    public static void addComment(Long insightId, String content) {
    	User currentUser = User.findByUserName(Security.connected());
    	Insight insight = Insight.findById(insightId);
    	insight.addComment(content, currentUser);
    	showInsight(insightId);
    }
    
    /**
     * add tags to an insight
     * @param insightId : the id of the tagged insight
     * @param tagLabelList: a comma separated list of tag labels
     */
	public static void addTags(Long insightId, String tagLabelList) {
		User currentUser = User.findByUserName(Security.connected());
		Insight insight = Insight.findById(insightId);
    	insight.addTags(tagLabelList, currentUser);
		showInsight(insightId);
	}
	
	/**
	 * Get a list of all the categories of the website	
	 */
	public static List<Category> getCategories() {
		List<Category> categories = Category.findAll();
		return categories;
	}
	
	// TODO remove me, score computation should be called in a job.
	public static void temp_recomputeScore() {
		User currentUser = User.findByUserName(Security.connected());
    	currentUser.computeScores();
    	currentUser.save();
    	showUser(currentUser.id);
	}


	
}