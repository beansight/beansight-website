package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Category;
import models.Filter;
import models.Filter.FilterVote;
import models.Insight;
import models.Insight.InsightResult;
import models.Language;
import models.User;
import models.Vote;
import models.Vote.State;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import exceptions.CannotVoteTwiceForTheSameInsightException;

public class APIInsights extends APIController {

	// TODO : what if the content evolves between two calls ?
	// Maybe a better solution would be to give the uniqueId of the latest
	// downloaded insight
	/**
	 * Get a list of insights<br/>
	 * <b>response:</b> <code>[{content, endDate, uniqueId, category, 
	 *         agreeCount, disagreeCount, commentCount}, ...]</code>
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
	 */
	public static void list(@Min(0) Integer from,
			@Min(1) @Max(100) Integer number, String sort, Integer category,
			String vote, String topic, Boolean closed) {

		if (validation.hasErrors()) {
			badRequest();
		}
		if (from == null) {
			from = 0;
		}
		if (number == null) {
			number = 20;
		}
		if (sort == null) {
			sort = "updated";
		} else {
			if(!sort.equals("updated") && !sort.equals("trending") && !sort.equals("incoming")) {
				badRequest();
			}
		}
		if (vote == null) {
			vote = "all";
		} else {
			if( !vote.equals("all") && !vote.equals("voted") && !vote.equals("non-voted")) {
				badRequest();
			}
		}
		if (closed == null) {
			closed = false;
		}

		InsightResult result = null;
		Filter filter = new Filter();
		filter.languages.add(Language.findByLabelOrCreate("en"));
		filter.languages.add(Language.findByLabelOrCreate("fr"));

		filter.user = getUserFromAccessToken();
		if( vote.equals("voted") ) {
			filter.vote = FilterVote.VOTED;
		} else if ( vote.equals("non-voted") ) {
			filter.vote = FilterVote.NONVOTED;
		} else {
			filter.vote = FilterVote.ALL;
		}
		
		filter.closed = closed;
		
		if (category != null) {
			Category cat = Category.findById(category);
			if(cat != null) {
				filter.categories.add(cat);
			}
		}

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

		renderAPI(jsonResult);
	}

	/**
	 * Get detailed information about a given insight<br/>
	 * <b>response:</b> <code>{content, endDate, startDate, category, agreeCount,
	 *         disagreeCount, comments[], tags[] }</code>
	 * 
	 * @param id : unique ID of this insight
	 */
	public static void show(@Required String id) {
		if (validation.hasErrors()) {
			badRequest();
		}
		renderAPI(getInsightResult(id));
	}

	private static Map<String, Object> getInsightResult(String insightUniqueId) {
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
	 * The current user agree a given insight<br/>
	 * <b>response:</b> <code>{uniqueId, updatedAgreeCount, updatedDisagreeCount, voteState}</code>
	 * 
	 * @param id : unique ID of this insight
	 */
	public static void agree(@Required String id) {
		vote(id, State.AGREE);
	}

	/**
	 * The current user disagree a given insight<br/>
	 * <b>response:</b> <code>{uniqueId, updatedAgreeCount, updatedDisagreeCount, voteState}</code>
	 * 
	 * @param id : unique ID of this insight
	 */
	public static void disagree(@Required String id) {
		vote(id, State.DISAGREE);
	}

	private static void vote(String insightUniqueId, State voteState) {
		User currentUser = getUserFromAccessToken();

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

		renderAPI(jsonResult);
	}
	
	/**
	 * Get a list of all the categories <br/>
	 * <b>response:</b> <code>[{label, id}, ...]</code>
	 */
	public static void categories() {
		List<Category> categories = Category.findAll();
		renderAPI(categories);
	}

	
	// -------------------
	
	public static class InsightItemResult {
		public long count;
		public List<InsightItem> insightItems = new ArrayList();
		
		public InsightItemResult(List<Insight> insights) {
			count = insights.size();
			for (Insight insight : insights) {
				InsightItem insightItem = new InsightItem(insight);
				insightItem.uniqueId = insight.uniqueId;
				insightItem.content = insight.content;
				insightItems.add(insightItem);
			}
		}
	}
	
	public static class InsightItem {
		public String uniqueId;
		public String content;
		
		public InsightItem(Insight insight) {
			uniqueId = insight.uniqueId;
			content = insight.content;
		}
	}
}
