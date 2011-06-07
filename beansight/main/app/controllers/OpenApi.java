package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import com.google.gson.Gson;

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
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.libs.WS;
import play.mvc.*;
import play.mvc.results.RenderHtml;
import play.mvc.results.RenderText;

public class OpenApi extends Controller {

	public static final String API_URL_CALLBACK = "api_url_callback";
	public static final String API_JSON_CALLBACK = "callback";
	public static final String API_ACCESS_TOKEN = "access_token";
	
	/**
	 * Check before every API call that the accessToken is valid
	 */
	@Before(unless={"authenticate", "authenticateSuccess"})
	public static void checkAccessToken() {
		String accessToken = params.get(API_ACCESS_TOKEN);
		if(accessToken == null) {
			// TODO generate error
			renderText(API_ACCESS_TOKEN + " parameter is needed.");
		}
		
		String email = (String)Cache.get(accessToken);
		if (email == null) {
			// TODO generate error
			renderText("The provided access_token %s is not valid.", accessToken);
		}
	}
	
	/**
	 * @return the accessToken associated with this user
	 */
	protected static User getUserFromAccessToken() {
		String accessToken = params.get(API_ACCESS_TOKEN);
		String email = (String)Cache.get(accessToken);
		User user = User.findByEmail(email);
		return user;
	}
	
	/**
	 * prepend the "callback" parameter to the JSON serialization of the object
	 * @param o : object to serialize
	 * @param callback : callback to prepend
	 */
	protected static void renderJSONP(Object o, String callback) {
		renderText( callback + "(" + new Gson().toJson(o) + ")" );
	}
	
	/**
	 * render the object either in JSON or JSONP, depending on the presence of the "callback" parameter
	 * @param o : JSON Object to render
	 */
	protected static void renderAPI(Object o) {
		String callback = params.get(API_JSON_CALLBACK);
		Logger.info(callback);
		if(callback != null) {
			renderJSONP(o, callback);
		} else {
			renderJSON(o);
		}
	}
	
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
		
		if(validation.hasErrors()) {
			error();
		}
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

		renderAPI(jsonResult);
	}

	/**
	 * Get detailed information about a given insight
	 * 
	 * @param insightUniqueId
	 * @return {content, endDate, startDate, category, agreeCount,
	 *         disagreeCount, comments[], tags[] }
	 */
	public static void getInsight(@Required String insightUniqueId) {
		if(validation.hasErrors()) {
			error();
		}
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

		renderJSON(jsonResult);
	}

	
	public static void authenticate(String urlCallback) {
		if (urlCallback == null) {
			urlCallback = String.format(Router.getFullUrl(request.controller + ".authenticateSuccess"));
		}
		session.put(API_URL_CALLBACK, urlCallback);
		renderTemplate("Secure/login.html");
	}
	
	/**
	 * generic callback url if no specific url provided in authenticate(String url) : the access token will be available in the url.
	 * For example : www.beansight.com/openapi/authenticateSuccess#access_token=a52795fc-8374-4c2b-8f46-7c8684687536
	 */
	public static void authenticateSuccess() {
		render();
	}
	
}
