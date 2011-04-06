package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import models.Category;
import models.Filter;
import models.Insight;
import models.Language;
import models.User;
import models.Vote;
import models.Filter.FilterType;
import models.Insight.InsightResult;
import models.Vote.State;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.mvc.*;

public class API extends Controller {

	// TODO : what if the content evolves between two calls ?
	// Maybe a better solution would be to give the uniqueId of the latest
	// downloaded insight
	/**
	 * Get a list of insights
	 * 
	 * @param from
	 *            index of the first insight to return, default = 0
	 * @param number
	 *            number of insights to return, default = 20
	 * 
	 * @param sort
	 *            possible values : ["updated", "trending", "incoming"]
	 *            (String), default = "updated"
	 * @param category
	 *            id of the category to restrict to, default = null
	 * @param vote
	 *            filter by vote state, possible values : ["all", "voted",
	 *            "non-voted"] (String), default = "all"
	 * @param topic
	 *            String of the topic, default = null
	 * @param closed
	 *            true to return closed insights, default = false
	 * @param created
	 *            true to return only insights created by the user, default =
	 *            false
	 * 
	 * @return [{content, startDate, endDate, category, agreeCount,
	 *         disagreeCount, currentUserVote}, ...]
	 */
	public static void getInsights(@Min(0) Integer from,
			@Min(1) @Max(100) Integer number, String sort, Integer category,
			String vote, String topic, Boolean closed, Boolean created) {

		if (from == null) {
			from = 0;
		}
		if (number == null) {
			number = 20;
		}
		if (sort == null) {
			sort = "updated";
		}
		if (vote == null) {
			vote = "all";
		}
		if (closed == null) {
			closed = false;
		}
		if (created == null) {
			created = false;
		}

		InsightResult result = null;
		Filter filter = new Filter();
		filter.filterType = FilterType.UPDATED;
		filter.languages.add(Language.findByLabelOrCreate("en"));
		filter.languages.add(Language.findByLabelOrCreate("fr"));
		filter.filterVote = "voted";

		if (sort.equals("trending")) {
			result = Insight.findTrending(from, number, filter);
		} else if (sort.equals("incoming")) {
			result = Insight.findIncoming(from, number, filter);
		} else {
			result = Insight.findLatest(from, number, filter);
		}

		List<Object> jsonResult = new ArrayList<Object>();
		for (Insight insight : result.results) {
			Map<String, Object> insightResult = new HashMap<String, Object>();
			insightResult.put("content", insight.content);
			insightResult.put("uniqueId", insight.uniqueId);
			// TODO : do the date formatting client side
			insightResult.put("endDate", new DateTime(insight.endDate)
					.toString(DateTimeFormat.forPattern("d MMMM yyyy")));
			jsonResult.add(insightResult);
		}

		renderJSON(jsonResult);
	}

	/**
	 * Get detailed information about a given insight
	 * 
	 * @param insightUniqueId
	 * @return {content, endDate, startDate, category, agreeCount,
	 *         disagreeCount, comments[], tags[] }
	 */
	public static void getInsight(@Required String insightUniqueId) {
		renderJSON(getInsightResult(insightUniqueId));
	}
	
	public static Map<String, Object> getInsightResult(String insightUniqueId) {
		Insight insight = Insight.findByUniqueId(insightUniqueId);
		Map<String, Object> jsonResult = new HashMap<String, Object>();
		jsonResult.put("uniqueId", insight.uniqueId);
		jsonResult.put("content", insight.content);
		// TODO : do the date formatting client side
		jsonResult.put("creationDate", new DateTime(insight.creationDate)
				.toString(DateTimeFormat.forPattern("d MMMM yyyy")));
		// TODO : do the date formatting client side
		jsonResult.put("endDate", new DateTime(insight.endDate)
				.toString(DateTimeFormat.forPattern("d MMMM yyyy")));
		jsonResult.put("creator", insight.creator.userName);

		jsonResult.put("agreeCount", insight.agreeCount);
		jsonResult.put("disagreeCount", insight.disagreeCount);

		User currentUser = CurrentUser.getCurrentUser();
		if (currentUser != null) {
			jsonResult.put("currentUser", currentUser.userName);
			Vote lastUserVote = Vote.findLastVoteByUserAndInsight(
					currentUser.id, insight.uniqueId);
			if (lastUserVote != null) {
				if (lastUserVote.state.equals(State.AGREE)) {
					jsonResult.put("lastUserVote", "agree");
				} else {
					jsonResult.put("lastUserVote", "disagree");
				}

			}
		}
		
		return jsonResult;
	}

	/**
	 * The current user agree a given insight
	 * 
	 * @param insightUniqueId
	 * @return {uniqueId, updatedAgreeCount, updatedDisagreeCount, voteState}
	 */
	public static void agree(@Required String insightUniqueId) {
		vote(insightUniqueId, State.AGREE);
	}

	/**
	 * The current user disagree a given insight
	 * 
	 * @param insightId
	 * @return {uniqueId, updatedAgreeCount, updatedDisagreeCount, voteState}
	 */
	public static void disagree(@Required String insightUniqueId) {
		vote(insightUniqueId, State.DISAGREE);
	}

	/**
	 * Get a list of all the categories
	 * 
	 * @return JSON [{label, id}, ...]
	 */
	public static void getCategories() {
		List<Category> categories = Category.findAll();
		renderJSON(categories);
	}

	private static void vote(String insightUniqueId, State voteState) {
		User currentUser = CurrentUser.getCurrentUser();

		try {
			currentUser.voteToInsight(insightUniqueId, voteState);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			// its ok, do not show anything
		}

		Insight insight = Insight.findByUniqueId(insightUniqueId);

		Map<String, Object> jsonResult = new HashMap<String, Object>();
		jsonResult.put("uniqueId", insight.uniqueId);
		jsonResult.put("updatedAgreeCount", insight.agreeCount);
		jsonResult.put("updatedDisagreeCount", insight.disagreeCount);
		if (voteState.equals(State.AGREE)) {
			jsonResult.put("voteState", "agree");
		} else {
			jsonResult.put("voteState", "disagree");
		}

		renderJSON(jsonResult);
	}

}
