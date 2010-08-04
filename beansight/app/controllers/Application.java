package controllers;

import java.util.List;

import exceptions.CannotVoteForAnInsightYouOwnException;

import models.Insight;
import models.User;
import models.Vote;
import play.mvc.Controller;

public class Application extends Controller {

	public static void index() {
		display();
	}

	public static void display() {
		List<Insight> insights = Insight.findAll();
		
		render(insights);
	}

	public static void createInsight(String insightContent) {
		User currentUser = User.findByUserName(Security.connected());
		currentUser.createInsight(insightContent);

		display();
	}

	/**
	 * Agree a given insight
	 * 
	 * @param insightId
	 */
	public static void agree(Long insightId) {
		User currentUser = User.findByUserName(Security.connected());
		Insight insight = Insight.findById(insightId);
		Vote vote = new Vote(currentUser, insight, Vote.State.AGREE);
		vote.save();
		currentUser.votes.add(vote);
		// TODO : only return JSON to use with AJAX
		display();
	}

	/**
	 * Disagree a given insight
	 * 
	 * @param insightId
	 */
	public static void disagree(Long insightId) {
		User currentUser = User.findByUserName(Security.connected());
		Insight insight = Insight.findById(insightId);
		Vote vote = new Vote(currentUser, insight, Vote.State.DISAGREE);
		vote.save();
		currentUser.votes.add(vote);
		// TODO : only return JSON to use with AJAX
		display();
	}

}