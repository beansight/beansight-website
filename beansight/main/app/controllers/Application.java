package controllers;

import helpers.ImageHelper;
import helpers.InSitemap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import models.Category;
import models.Comment;
import models.FacebookFriend;
import models.FeaturedInsight;
import models.FeaturedSponsor;
import models.FeaturedTag;
import models.Filter;
import models.Filter.FilterVote;
import models.Filter.SortBy;
import models.Insight;
import models.Insight.InsightResult;
import models.InsightSuggest;
import models.InsightTrend;
import models.Language;
import models.Message;
import models.Tag;
import models.User;
import models.User.UserResult;
import models.UserCategoryScore;
import models.UserInsightsFilter;
import models.Vote;
import models.Vote.State;
import models.WaitingEmail;
import models.analytics.UserClientInfo;
import notifiers.Mails;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.binding.As;
import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.InFuture;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.Crypto;
import play.libs.Images;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Router.Route;
import play.templates.JavaExtensions;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.InsightAlreadySharedException;
import exceptions.InvitationException;
import exceptions.NotFollowingUserException;
import exceptions.UserIsAlreadyFollowingInsightException;
import ext.StringExtensions;

public class Application extends Controller {

	public static final int NUMBER_INSIGHTS_INSIGHTPAGE = 12;
	public static final int NUMBER_INSIGHTS_INSIGHTPAGE_NOTLOGGED = 5;
	public static final int NUMBER_INSIGHTACTIVITY_INDEXPAGE = 8;
	public static final int NUMBER_USERACTIVITY_INDEXPAGE = 6;
	public static final int NUMBER_TOPICACTIVITY_INDEXPAGE = 4;	
	
	public static final int NUMBER_INSIGHTS_USERPAGE = 10;
	
	public static final int NUMBER_EXPERTS_EXPERTPAGE = 6;
	public static final int NUMBER_CATEGORYEXPERTS_EXPERTPAGE = 5;
	public static final int NUMBER_FOLLOWED_EXPERTPAGE = 5;
	
	public static final int NUMBER_INSIGHTS_SEARCHPAGE = 12;

	public static final int NUMBER_SUGGESTED_USERS = 10;
	public static final int NUMBER_SUGGESTED_TAGS = 10;

	public static final String APPLICATION_ID = "web-desktop";
    /**
     * Make sure the language is the one the user has chosen.
     */
	@Before
    static void setLanguage() {
        if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			Lang.change(currentUser.uiLanguage.label);			
        } 
    }
	
    // TODO : add all the ajax method here so that we don't load  data not useful during ajax call
    /**
     * If the user is connected, load the needed info into the menu
     */
    @Before(unless={"insightsFilter", "getInsights", "leaveYourEmail", "shareInsight", "agree", "disagree", "loadFollowedUsers", "toggleFollowingUser", "toggleFollowingInsight", "searchExperts", "moreSearchExperts", "showAvatarSmall", "showAvatarMedium", "showAvatarLarge", "editComment", "addComment", "favoriteUserSuggest", "displayOriginalUncropedImage", "getUserInsights"})
    public static void loadMenuData() {
        if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			
			// if this is twitter user and he hasn't validated its promocode the redirect  
			if ( (currentUser.twitterUserId != null && currentUser.twitterUserId.trim().equals("") == false) && !currentUser.isPromocodeValidated) {
				session.put("url", request.url);
				Register.extAuthFirstTimeConnectPage(currentUser.email, currentUser.userName);
			}
			
			renderArgs.put("followedInsightActivities", currentUser.getFavoriteInsightActivity(NUMBER_INSIGHTACTIVITY_INDEXPAGE));
			renderArgs.put("followedUserActivities", currentUser.getFavoriteUserActivity(NUMBER_USERACTIVITY_INDEXPAGE));
			renderArgs.put("followedTopicActivities", currentUser.getFavoriteTopicActivity(NUMBER_TOPICACTIVITY_INDEXPAGE));
			renderArgs.put("advisedUsers", currentUser.findSuggestedFacebookFriends());
			
			renderArgs.put("emailConfirmed", currentUser.emailConfirmed);
			renderArgs.put("invitationsLeft", currentUser.invitationsLeft);

			// if connected, display featured sponsors
			List<FeaturedSponsor> featuredSponsors = FeaturedSponsor.findActive(currentUser.getWrittingLanguages());
			if(!featuredSponsors.isEmpty()) {
				FeaturedSponsor sponsor =  featuredSponsors.get(0);
				// do not check if already voted, always show.
				renderArgs.put("featuredSponsor", sponsor);
			}
        }
        
    }
    
    public static void welcome() {
    	if(!Security.isConnected()) {
    		render();
    	} else {
    		index();
    	}
    }
    
    /**
     * Serve the Mozilla Webapp manifest with the correct content Type
     */
    public static void manifest() {
    	request.contentType = "application/x-web-app-manifest+json";
    	request.format = "webapp";
    	render();
    }
    
    public static void leaveYourEmail(@Required @Email String email) {
    	Map<String, Object> jsonResult = new HashMap<String, Object>();
    	jsonResult.put("msg", "");
    	jsonResult.put("hasError", Boolean.FALSE);
    	
    	if(validation.hasErrors()) {
    		jsonResult.put("hasError", Boolean.TRUE);
    		for (play.data.validation.Error error : validation.errors()) {
    			jsonResult.put("msg",  jsonResult.get("msg") + error.message());
    		}
    		renderJSON(jsonResult);
	   	}
    	WaitingEmail waitingEmail = new WaitingEmail(email);
    	waitingEmail.save();
    	
    	jsonResult.put("msg", Messages.get("welcome.leaveYourEmailSuccess"));
    	renderJSON(jsonResult);
    }
    
    
    public static void index() {
    	insights("trending", 0, "all", null, null);
    }
    
    /**
     * Display the insight create form
     * 
     * @param insightContent
     * @param endDate
     * @param tagLabelList
     * @param categoryId
     * @param insightLang
     */
	@InSitemap(changefreq="monthly", priority=0.7)
	public static void create(String insightContent, Date endDate, String tagLabelList, long categoryId, String insightLang, String vote) {
		if(insightLang == null ) {
			User currentUser = CurrentUser.getCurrentUser();
			insightLang = currentUser.writtingLanguage.label;
		}
		render(insightContent, endDate, tagLabelList, categoryId, insightLang, vote);
	}

	public static void profile() {
		User currentUser = CurrentUser.getCurrentUser();
		showUser(currentUser.userName);
	}

	/**
	 * @return the number of insights to be displayed initially on the list Insights page
	 */
	public static int getNumberInsightsInsightPage() {
		if (Security.isConnected()) {
			return NUMBER_INSIGHTS_INSIGHTPAGE;
		}
		return NUMBER_INSIGHTS_INSIGHTPAGE_NOTLOGGED;
	}
	
	@InSitemap(changefreq="always", priority=1)
	public static void insights(String sortBy, long cat, String filterVote, String topic, Boolean closed) {
		if (filterVote == null || filterVote.trim().equals("")) {
			filterVote = "all";
		}
		
		// Featured Topics
		List<Language> writtenLanguages = new ArrayList<Language>();
		if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			writtenLanguages.add(currentUser.writtingLanguage);
			if(currentUser.secondWrittingLanguage != null) {
				writtenLanguages.add(currentUser.secondWrittingLanguage);
			}
		} else {
			writtenLanguages.add( Language.findByLabelOrCreate(Lang.get()));
		}
		List<FeaturedTag> featuredTags = FeaturedTag.findActive(writtenLanguages);
		renderArgs.put("featuredTopics", featuredTags);
		
		// return the real topic object
		Tag top = Tag.findByLabel(topic);
		renderArgs.put("topic", top);
		
		// log for analytics
		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			UserClientInfo userClientInfo = new UserClientInfo(request, APPLICATION_ID);
			currentUser.visitInsightsList(userClientInfo);
			if ( top != null ) {
				currentUser.visitTopic(top, userClientInfo);
			}
		}
		
		// if not a topic page, display a home screen
		if (topic == null && !Security.isConnected()) {
			// get the featured insights
			renderArgs.put("featuredInsights", Insight.findFeaturedHome());
			// render a special template
			render("Application/home.html");
		} else {
			render(sortBy, cat, filterVote, closed);
		}
	}

	/**
	 * AJAX get a list of insights : [from, from + NUMBER_INSIGHTS]
	 * @param from : the index of the first insight to return
	 */
	public static void getInsights(int from, String sortBy, long cat, String filterVote, String topic, Boolean closed) {
		if (filterVote == null || filterVote.trim().equals("")) {
			filterVote = "all";
		}
		
		InsightResult result = getFilteredInsightsList(from, getNumberInsightsInsightPage(), sortBy, cat, filterVote, topic, closed, null);
		renderArgs.put("result", result);
		render();
	}
	
	
	/**
	 * AJAX get a list of insights starting at index 0 : [0, from + NUMBER_INSIGHTS]
	 * @param from : the index of the first insight to return
	 * @param sortBy : updated, trending, incoming
	 */
	public static void reloadInsights(int from, String sortBy, long cat, String filterVote, String topic, Boolean closed) {
		if (filterVote == null || filterVote.trim().equals("")) {
			filterVote = "all";
		}
		
		InsightResult result = getFilteredInsightsList(0, (from + getNumberInsightsInsightPage()), sortBy, cat, filterVote, topic, closed, null);
		renderArgs.put("result", result);
		
		renderTemplate("Application/getInsights.html");
	}
	

	/**
	 * 
	 * @param from
	 * @param numberInsights
	 * @param sortBy : suggested will return the suggested insights and add the findTrending if not enough predictions
	 * @param cat
	 * @param filterVote
	 * @param topicStr
	 * @param closed
	 * @param userName
	 * @return
	 */
	private static InsightResult getFilteredInsightsList(int from, int numberInsights, String sortBy, long cat, String filterVote, String topicStr, Boolean closed, String userName) {
		Filter filter = new Filter();
		if(filterVote.equals("notVoted")) {
			filter.vote = FilterVote.NONVOTED;
		} else if(filterVote.equals("voted")) {
			filter.vote = FilterVote.VOTED;
		} else {
			filter.vote = FilterVote.ALL;
		}

		// tags
		if(topicStr != null && !topicStr.trim().equalsIgnoreCase("undefined")) {
			Tag topic = Tag.findByLabel(topicStr);
			if(topic != null) {
				for(Tag tag : topic.getContainedTags()) {
					filter.tags.add(tag);
				}
			}
		}
		
		Category category = Category.findById(cat);
		if(category != null) {
			filter.categories.add(category);
		}
		
		// languages
		if (Security.isConnected()) { // if user is connected, then get the insights in the languages he speaks
			User currentUser = CurrentUser.getCurrentUser();
			filter.user = currentUser;
			filter.languages.add(currentUser.writtingLanguage);
			if(currentUser.secondWrittingLanguage != null) {
				filter.languages.add(currentUser.secondWrittingLanguage);
			}
		} else { // else, get the insights in the language of the browser
			String lang = Lang.get();
			// if no language, then english
			if( lang == null || lang.equals("") ) { lang = "en"; }
			filter.languages.add( Language.findByLabelOrCreate(lang) );
		}

		
		InsightResult result;
		
		if (closed != null && closed == true) {
			result = Insight.findClosedInsights(from, numberInsights, filter);
		} else {
			// depending on the sortBy
			if(sortBy != null && sortBy.equals("updated")) {
				result = Insight.findLatest(from, numberInsights, filter);
			} else if (sortBy != null && sortBy.equals("trending")) {
				result = Insight.findTrending(from, numberInsights, filter);
			} else if (sortBy != null && sortBy.equals("incoming")) {
				result = Insight.findIncoming(from, numberInsights, filter);
			} else if (sortBy != null && sortBy.equals("suggested") && Security.isConnected()) {
				User currentUser = CurrentUser.getCurrentUser();
				result = InsightSuggest.toInsightResultList(InsightSuggest.findByUser(from, numberInsights, filter, currentUser));
				// if not enough insights to display, fill with trendings
				if( result.results.size() < numberInsights ) {
					InsightResult resultLatest = Insight.findTrending(result.results.size(), numberInsights - result.results.size(), filter);
					result.results.addAll(resultLatest.results);
				}
			} else {
				result = Insight.findIncoming(from, numberInsights, filter);
			}
			// featured insight
			if (Security.isConnected()) { // if user is connected, then get the insights in the languages he speaks
				if(from == 0) {
					User currentUser = CurrentUser.getCurrentUser();
					// if any, add featured insights to the result
					List<FeaturedInsight> featuredInsights = FeaturedInsight.findActive(currentUser.getWrittingLanguages());
					for(FeaturedInsight featured : featuredInsights) {
						// if the insight is not already in the result and if the user hasn't voted, display it at the top.
						if(!result.results.contains(featured.insight) && Vote.findLastVoteByUserAndInsight(currentUser.id, featured.insight.uniqueId) == null) {
							result.results.add(0, featured.insight);
						}
					}
				}
			}
		}

		
		return result;
	}
	
	

	/**
	 * Empty Expert page
	 */
	@InSitemap(changefreq="always", priority=0.4)
	public static void experts() {
		
		// If connected, log analytic
		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			// log for analytics
			currentUser.visitExpertsList(new UserClientInfo(request, APPLICATION_ID));
		}
		
		render();
	}

	/**
	 * A page with the best experts 
	 * This page is not currently used on beansight.com
	 */
	public static void bestExperts() {
		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			List<User> sortedFollowedUsers = currentUser.getFollowedUsersSortedByScore(null);
			renderArgs.put("currentUserRank", sortedFollowedUsers.indexOf(currentUser));
			renderArgs.put("sortedFollowedUsers", sortedFollowedUsers);
		}
		
		List<User> experts = User.findBest(0, NUMBER_EXPERTS_EXPERTPAGE );

		Map<Category, List<User>> bestUsersByCategory = new HashMap<Category, List<User>>();
		for(Category cat : getCategories()) {
			bestUsersByCategory.put(cat, User.findBestInCategory(0, NUMBER_CATEGORYEXPERTS_EXPERTPAGE, cat ));
		}
		
		render(experts, bestUsersByCategory);
	}
	
	/**
	 * AJAX : if no query, return the best experts globally or in a given category, 
	 * If query, return the result of this Search.
	 * @param query
	 * @param from
	 * @param cat : id of the category to filter by, 0 for no category. default to 0
	 * @param filter : "all" to display all, "favorites" to only show favorites. default to "all" Do not work when performing a search.
	 */
	public static void searchExperts(String query, long cat, String filter, int from) {
		displaySearchExpertsResult(query, cat, filter, from, from + NUMBER_EXPERTS_EXPERTPAGE);
	}

	public static void reloadSearchExperts(String query, long cat, String filter, int from) {
		displaySearchExpertsResult(query, cat, filter, 0, from + NUMBER_EXPERTS_EXPERTPAGE);
	}
	
	private static void displaySearchExpertsResult(String query, long cat, String filter, int from, int to) {
		List<User> experts = null;
		if (query == null || query.isEmpty()) {

			Category category = null;
			if( cat != 0 ) {
				category = Category.findById(cat);
			}

			// display only favorites if asked too and if connected
			if( filter != null && filter.equals("favorites") && Security.isConnected() ) {
				User currentUser = CurrentUser.getCurrentUser();
				List<User> allExperts = currentUser.getFollowedUsersSortedByScore(category);
				
				int indexFrom = allExperts.size();
				int indexTo = allExperts.size();
				if(from < allExperts.size()) {
					indexFrom = from;
					indexTo = from;
				}
				if (to < allExperts.size()) {
					indexTo = to;
				} else {
					indexTo = allExperts.size();
				}
				
				experts = allExperts.subList(indexFrom, indexTo);
				
			} else {
				if (category != null) {
					experts = User.findBestInCategory(from, to - from, category );
				} else {
					experts = User.findBest(from, to - from );
				}
			}
		
		} else {
			UserResult userSearchResult = User.search(query, from, to - from);
			experts = userSearchResult.results;
		}


		// Then format the result
		renderArgs.put("experts", experts);
		if (experts.size() <= 1) {
			renderTemplate("Application/expertsSearchResultNoFriend.html");
		} else {
			renderTemplate("Application/expertsSearchResult.html");
		}
	}

	/**
	 * create an insight for the current user
	 * 
	 * @param insightContent
	 *            : the content of this insight (min 6, max 120 characters)
	 * @param endDate
	 *            : the end date chosen by the user
	 * @param tagLabelList
	 *            : a comma separated list of tags
	 * @param categoryId
	 *            : the ID of the category of the insight
	 * @param vote
	 *            : "agree", "disagree" or "novote"
	 */
	public static void createInsight(
			@Required @MinSize(6) @MaxSize(120) String insightContent,
			@Required @InFuture @As("yyyy-MM-dd") Date endDate, @MaxSize(100) String tagLabelList,
			@Required long categoryId, String lang, String vote) {
		
		State voteState = State.AGREE;
		if(vote != null && vote.equals("disagree")) {
			voteState = State.DISAGREE;
		} else if(vote != null && vote.equals("novote")) {
			voteState = null;
		}
		
		// Check if the given category Id corresponds to a category
		Category category = Category.findById(categoryId);
		if (category == null) {
			validation.addError("categoryId", "Not a valid Category");
		}
		if (validation.hasErrors()) {
			validation.keep();
			flash.error(Messages.get("createInsight.errorcreating"));
			create(insightContent, endDate, tagLabelList, categoryId, lang, vote);
		}

		// force the end date time at 23h5ç and 59 seconds of the selected day
		Date midnightDate = new DateMidnight(endDate).plusDays(1).toDateTime().minusSeconds(1).toDate();
		
		User currentUser = CurrentUser.getCurrentUser();
		Insight insight = null;
		try {
			insight = currentUser.createInsight(insightContent, midnightDate, tagLabelList, categoryId, lang, voteState);
		} catch (Throwable t) {
			flash.error("Error creating the prediction: " + t.getMessage());
			create(insightContent, endDate, tagLabelList, categoryId, lang, vote);
		}
		
		// send a quick mail to admin users so they can check if insight is OK
        Mails.newInsightNotification(insight);

		showInsight(insight.uniqueId);
	}

	/**
	 * Show info about a given insight
	 * 
	 * @param insightUniqueId
	 */
	public static void showInsight(String insightUniqueId) {
		Insight insight = Insight.findByUniqueId(insightUniqueId);
		notFoundIfNull(insight);
		
		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			Vote lastUserVote = Vote.findLastVoteByUserAndInsight( currentUser.id, insight.uniqueId );
			
			// the user has read this insight (if it has been shared, removed from shared insights
			currentUser.readInsight(insight);
			
			// log for analytics 
			UserClientInfo userClientInfo = new UserClientInfo(request, APPLICATION_ID);
			currentUser.visitInsight(insight, userClientInfo);
			
			renderArgs.put("currentUser", currentUser);
			renderArgs.put("lastUserVote", lastUserVote);
		}
		
		List<Vote> lastVotes = insight.getLastVotes(5);
		
		List<InsightTrend> agreeInsightTrends = null;
		
		// get the trends list from the cache and put it if wasn't already in cache
		Map<Long, List<InsightTrend>> agreeInsightTrendsCache = (Map<Long, List<InsightTrend>>)Cache.get("agreeInsightTrendsCache");
		if (agreeInsightTrendsCache == null) {
			agreeInsightTrendsCache = new HashMap<Long, List<InsightTrend>>();
			Cache.add("agreeInsightTrendsCache", agreeInsightTrendsCache);
		}
		if (agreeInsightTrendsCache.containsKey(insight.id)) {
			agreeInsightTrends = agreeInsightTrendsCache.get(insight.id);
		} else {
			agreeInsightTrends = InsightTrend.find("select t from InsightTrend t where t.insight = :insight order by t.trendDate").bind("insight", insight).fetch();
			
			agreeInsightTrendsCache.put(insight.id, agreeInsightTrends);
		}
		
        renderArgs.put("lastVotes", lastVotes);
        renderArgs.put("agreeInsightTrends", agreeInsightTrends);
        renderArgs.put("comments", insight.getNotHiddenComments());
        
        List<Insight> relateds = insight.relatedInsights(5);
        
        renderArgs.put("relatedInsights", relateds );
        
        render(insight);
	}

	/**
	 * Show info about a given user
	 * 
	 * @param id
	 */
	public static void showUser(String userName) {
		User user = User.findByUserName(userName);
		notFoundIfNull(user);

		boolean currentUserProfilePage = false;
		
		if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			if(currentUser.id == user.id ) {
				currentUserProfilePage = true;
			} 
			// log for analytics 
			UserClientInfo userClientInfo = new UserClientInfo(request, APPLICATION_ID);
			currentUser.visitExpert(user, userClientInfo);
		}

		List<UserCategoryScore> categoryScores = user.getLatestCategoryScores();
		
		List<Message> directMessages = Message.find("byFromUser", user).fetch();
		
		render(user, categoryScores, currentUserProfilePage, directMessages);
	}

	/**
	 * AJAX get a list of insights for a user : [from, from + NUMBER_INSIGHTS]
	 * @param from : the index of the first insight to return
	 */
	public static void getUserInsights(String userName, int from, long cat, String filterVote) {
		User user = User.findByUserName(userName);
		notFoundIfNull(user);
		
		InsightResult result = getFilteredUserInsightsList(from, getNumberInsightsInsightPage(), cat, user, filterVote);
		renderArgs.put("result", result);
		renderArgs.put("targetUser", user);
		
		renderTemplate("Application/getInsights.html");
	}
	
	/**
	 * AJAX get a list of insights for a user starting at index 0 : [0, from + NUMBER_INSIGHTS]
	 * @param from : the index of the first insight to return
	 */
	public static void reloadUserInsights(String userName, int from, long cat, String filterVote) {
		User user = User.findByUserName(userName);
		notFoundIfNull(user);
		
		InsightResult result = getFilteredUserInsightsList(0, (from + getNumberInsightsInsightPage()), cat, user, filterVote);
		renderArgs.put("result", result);
		renderArgs.put("targetUser", user);
		
		renderTemplate("Application/getInsights.html");
	}
	
	public static InsightResult getFilteredUserInsightsList(int from, int numberInsights, long cat, User user, String filterVote) {
		UserInsightsFilter filter = new UserInsightsFilter();

		filter.user = user;
		filter.filterVote = filterVote;
		
		Category category = Category.findById(cat);
		if(category != null) {
			filter.categories.add(category);
		}
		
		InsightResult result = user.getLastInsights(from, numberInsights, filter);

		return result;
	}
	
	/**
	 * AJAX: Change the follow state for the connected user toward this insight
	 */
	public static void toggleFollowingInsight(String insightUniqueId) {
		User currentUser = CurrentUser.getCurrentUser();
		if(currentUser == null) {
			error();
		}
		
		Insight insight = Insight.findByUniqueId(insightUniqueId);

		if (currentUser.isFollowingInsight(insight) == true) {
			currentUser.stopFollowingThisInsight(insight.id);
			renderArgs.put("follow", false);
		} else {
			try {
				currentUser.startFollowingThisInsight(insight.id);
				renderArgs.put("follow", true);
			} catch (UserIsAlreadyFollowingInsightException e) {
				// it's ok to re-follow something
			}
		}

		render("Application/followInsight.json", insight);
	}
	
	/**
	 * AJAX: Change the follow state for the connected user toward this topic
	 */
	public static void toggleFollowingTopic(Long topicId) {
		User currentUser = CurrentUser.getCurrentUser();
		Tag topic = Tag.findById(topicId);

    	Map<String, Object> jsonResult = new HashMap<String, Object>();
    	jsonResult.put("topic", topic.id);
    	
		if(currentUser.isFollowingTopic(topic)) {
			currentUser.stopFollowingThisTopic(topic);
			jsonResult.put("follow", false);
		} else {
			currentUser.startFollowingThisTopic(topic);
			jsonResult.put("follow", true);
		}
		renderJSON(jsonResult);
	}

	public static void getFavoriteInsight(String insightUniqueId) {
		Insight insight = Insight.findByUniqueId(insightUniqueId);
		render(insight);
	}

	/**
	 * AJAX: Change the follow state for the connected user toward this user
	 */
	public static void toggleFollowingUser(Long userId) {
		User currentUser = CurrentUser.getCurrentUser();
		// user can't follow itself
		if (userId.equals(currentUser.id)) {
			renderArgs.put("follow", false);
			render("Application/followUser.json", userId);
		} 
		
		User user = User.findById(userId);
		if (currentUser.isFollowingUser(user) == true) {
			currentUser.stopFollowingThisUser(user);
			renderArgs.put("follow", false);
		} else {
			currentUser.startFollowingThisUser(user, true);
			renderArgs.put("follow", true);
		}	
		render("Application/followUser.json", userId);
	}

	/**
	 * action called from followNotification.html mail : to provide a "follow back" link for the user receiving the email.
	 * If the user is not authenticated he'll be redirected to the authentication page and then automatically redirected back 
	 * again on this action to follow the user.
	 * @param userName
	 */
	public static void followUser(String userName) {
		User currentUser = CurrentUser.getCurrentUser();
		if (currentUser == null) {
			session.put("url", request.url);
			flash.put("url", request.url);
			try {
				Secure.login();
			} catch (Throwable e) {
				Logger.error(e, "cannot redirect to login page");
			}
			return;
		}
		User user = User.findByUserName(userName);
		currentUser.startFollowingThisUser(user, true);
		Application.showUser(user.userName);
	}
	
	/**
	 * AJAX: remove a followed user and returns the 
	 */
	public static void removeFollowedUser(Long userId) {
		User currentUser = CurrentUser.getCurrentUser();
		
		User user = User.findById(userId);
		currentUser.stopFollowingThisUser(user);
		renderArgs.put("follow", false);
		render("Application/followUser.json", userId);
	}
	
	/**
	 * return the block to be inserted in the followed users section
	 * @param userId
	 */
	public static void loadFollowedUsers() {
		User currentUser = CurrentUser.getCurrentUser();

		renderArgs.put("_followedUserActivities", currentUser.getFavoriteUserActivity(NUMBER_USERACTIVITY_INDEXPAGE));
		renderTemplate("tags/followedUsers.tag");
	}
	
	/**
	 * return the block to be inserted in the followed topics section
	 */
	public static void loadFollowedTopics() {
		User currentUser = CurrentUser.getCurrentUser();
		if(currentUser == null ) {
			error();
		}

		renderArgs.put("_followedTopicActivities", currentUser.getFavoriteTopicActivity(NUMBER_USERACTIVITY_INDEXPAGE));
		renderTemplate("tags/followedTopics.tag");
	}
	
	public static void followAllFacebookFriends() {
		User currentUser = CurrentUser.getCurrentUser();
		
		List<FacebookFriend> facebookFriends = currentUser.findFriendsOnFacebookWhoAreOnBeansight();
		for (FacebookFriend facebookFriend : facebookFriends) {
			if (facebookFriend.isAdded != true) {
				currentUser.startFollowingThisUser(facebookFriend.beansightUserFriend, true);
			}
		}
		renderArgs.put("_friends", currentUser.findFriendsOnFacebookWhoAreOnBeansight());
		renderArgs.put("_currentUser", currentUser);
		renderTemplate("tags/facebookFriendList.tag");
	}
	
	/**
	 * AJAX set the given facebook friend as "added to favorites" (but do not add to favorite). 
	 * @param userId
	 */
	public static void addToFavoritesFromSuggestedFacebookFriends(Long userIdOfTheFriendToAdd) {
		User currentUser = CurrentUser.getCurrentUser();
		// user can't follow itself
		if (userIdOfTheFriendToAdd.equals(currentUser.id)) {
			return;
		} 
		User userToAdd = User.findById(userIdOfTheFriendToAdd);
		currentUser.startFollowingThisUser(userToAdd, true);
	}
	
	/**
	 * AJAX hide the provided userId from the suggested list of friends from facebook that also are on beansight
	 * @param userId
	 */
	public static void hideSuggestedFacebookFriend(Long userIdOfTheFriendToHide) {
		User currentUser = CurrentUser.getCurrentUser();
		// user can't hide itself
		if (userIdOfTheFriendToHide.equals(currentUser.id)) {
			return;
		} 
		
		User userToAdd = User.findById(userIdOfTheFriendToHide);
		currentUser.stopFollowingThisUser(userToAdd);
	}
	
	/**
	 * AJAX
	 * Add a comment to a specific insight for the current user
	 * 
	 * @param insightUniqueId
	 *            : unique id of the insight
	 * @param content
	 *            : text content of the insight
	 */
	public static void addComment(@Required String uniqueId, Long commentId, @MinSize(5) String content) {
    	if(validation.hasErrors()) {
    		return;
	   	}
    	
    	Comment comment = null;
    	
    	// if commentId != null then this mean that the user is updating its comment
		if (commentId != null) {
			comment = Comment.findById(commentId);
			if (comment.savedLessThanMinutesAgo(15)) {
				comment.content = content;
				comment.creationDate = new Date();
				comment.save();
			} else {
				// if the time limit for editing has exceeded add a message in the comment (without saving it of course...)
				comment.content = Messages.get("insights.commentEditTimeoutFailure") + comment.content;
			}
		} else {
			User commentWriter = CurrentUser.getCurrentUser();
			Insight insight = Insight.findByUniqueId(uniqueId);
			comment = insight.addComment(content, commentWriter);
			insight.save();
		}
		
		render(comment);
	}

	/**
	 * AJAX
	 * this is called to edit a comment in the insight view.
	 * only the owner of the comment can update it and only if the comment is less than 15 minutes
	 * @param uniqueId
	 * @param commentId
	 */
	public static void editComment(@Required String uniqueId, @Required Long commentId) {
    	if(validation.hasErrors()) {
    		return;
	   	}
		Comment comment = Comment.findById(commentId);
		User commentWriter = CurrentUser.getCurrentUser();
		// check that current user is the original writer of the comment
		if (comment.user.equals(commentWriter)) {
			if (comment.savedLessThanMinutesAgo(15)) {
				Map<String, Object> jsonResult = new HashMap<String, Object>();
				jsonResult.put("commentId", comment.id);
				jsonResult.put("content", comment.content);
				renderJSON(jsonResult);
			} else {
				// the comment is more than 15 minutes so return an error
				Map<String, Object> jsonResult = new HashMap<String, Object>();
				jsonResult.put("error", Messages.get("insight.commentEditableTimeout"));
				renderJSON(jsonResult);
			}
		}
	}
	
	
	/**
	 * add tags to an insight
	 * 
	 * @param insightId
	 *            : the id of the tagged insight
	 * @param tagLabelList
	 *            : a comma separated list of tag labels
	 */
	public static void addTags(String uniqueId, String tagLabelList) {
		User currentUser = CurrentUser.getCurrentUser();
		Insight insight = Insight.findByUniqueId(uniqueId);
		insight.addTags(tagLabelList, currentUser);
		showInsight(uniqueId);
	}

	/**
	 * Get a list of all the categories of the website
	 */
	public static List<Category> getCategories() {
		List<Category> categories = Category.findAll();
		return categories;
	}

	@InSitemap(changefreq="monthly", priority=0.3)
	public static void settings() {
		User user = CurrentUser.getCurrentUser();
		render(user);
	}
	
	/**
	 * Save the user's settings
	 * @param uiLanguage : language of the UI
	 * @param firstWrittingLanguage : main language of the user
	 * @param secondWrittingLanguage :  "none" if the user doesn't want to use another language
	 * @param username
	 */
	public static void saveSettings(
			@Required String uiLanguage,
			@Required String firstWrittingLanguage,
			@Required String secondWrittingLanguage,
			@Required @Match(value = "[a-zA-Z0-9_]{3,16}", message = "username has to be 3-16 chars, no space, no accent and no punctuation") String username,
			boolean followMail,
			boolean messageMail,
			boolean insightShareMail,			
			boolean commentCreatedMail,
			boolean commentFavoriteMail,
			boolean commentCommentMail,
			boolean commentMentionMail,
			boolean upcomingNewsletter,
			boolean statusNewsletter) {
		User user = CurrentUser.getCurrentUser();
		if(!username.equals(user.userName) && !User.isUsernameAvailable(username)) {
			validation.addError("userName", Messages.get("registerusernameexist")); 
		}
		if (validation.hasErrors()) {
	        validation.keep();
			flash.error(Messages.get("saveSettings.validation"));
			Application.settings();
	    }
		
		user.uiLanguage 			= Language.findByLabelOrCreate(uiLanguage);
		user.writtingLanguage 		= Language.findByLabelOrCreate(firstWrittingLanguage);
		if(!secondWrittingLanguage.equals("none") && !secondWrittingLanguage.equals(firstWrittingLanguage)) {
			user.secondWrittingLanguage = Language.findByLabelOrCreate(secondWrittingLanguage);
		} else {
			user.secondWrittingLanguage = null;
		}

		user.userName = username;
		
		user.followMail = followMail;
		user.messageMail = messageMail;
		user.insightShareMail = insightShareMail;
		user.commentCreatedMail = commentCreatedMail;
		user.commentFavoriteMail = commentFavoriteMail;
		user.commentCommentMail = commentCommentMail;
		user.commentMentionMail = commentMentionMail;
		user.upcomingNewsletter = upcomingNewsletter;
		user.statusNewsletter = statusNewsletter;
		
		user.save();
		
		Application.settings();
	}
	
	public static void saveNewPassword(@Required String oldPassword, @Required @MinSize(5) String newPassword, @Required @MinSize(5) @Equals("newPassword") String newPasswordConfirm) {
		User user = CurrentUser.getCurrentUser();
		if( !user.password.equals( Crypto.passwordHash(oldPassword) )) {
			validation.addError("oldPassword", "Old password not valid");
		}

		if (validation.hasErrors()) {
			params.flash();
	        validation.keep();
	        changePassword();
	    }
		
		user.changePassword(newPassword);
		
		settings();
	}
	
	public static void changePassword() {
		User user = CurrentUser.getCurrentUser();
		render(user);
	}
	
	/**
	 * Render the small avatar
	 * 
	 * @param userName
	 */
	public static void showAvatarSmall(String userName, String code) {
		User user = User.findByUserName(userName);
		notFoundIfNull(user);
		
		if (user != null) {
			if (user.avatarSmall.exists()) {
				renderBinary(user.avatarSmall.get());
			} else {
				renderBinary(new File(Play.getFile("public/images/avatar") + "/unknown-small.jpg"));
			}
		}
	}
	
	/**
	 * Render the medium avatar
	 * 
	 * @param userName
	 */
	public static void showAvatarMedium(String userName, String code) {
		User user = User.findByUserName(userName);
		notFoundIfNull(user);
		
		if (user != null) {
			if (user.avatarMedium.exists()) {
				renderBinary(user.avatarMedium.get());
			} else {
				renderBinary(new File(Play.getFile("public/images/avatar") + "/unknown-medium.jpg"));
			}
		}
	}
	
	/**
	 * Render the large avatar
	 * 
	 * @param userName
	 */
	public static void showAvatarLarge(String userName, String code) {
		User user = User.findByUserName(userName);
		notFoundIfNull(user);
		
		if (user != null) {
			if (user.avatarLarge.exists()) {
				renderBinary(user.avatarLarge.get());
			} else {
				renderBinary(new File(Play.getFile("public/images/avatar").getPath() + "/unknown-large.jpg"));
			}
		}
	}
	
	/**
	 * show the small avatar of the user from his email address.
	 */
	public static void showAvatarSmallFromEmail(String email) {
		User user = User.findByEmail(email);
		
		if (user != null && user.avatarSmall.exists()) {
			renderBinary(user.avatarSmall.get());
		} else {
			renderBinary(new File(Play.getFile("public/images/avatar") + "/unknown-small.jpg"));
		}
	}

	/**
	 * Render the uploaded image so that the user crop his avatar from it
	 */
	public static void displayOriginalUncropedImage(String code) {
		User user = CurrentUser.getCurrentUser();
		if (!user.avatarUnchanged.exists()) {
			renderBinary(new File(Play.getFile("public/images/avatar")
					+ "/unknown-large.jpg"));
		}
		renderBinary(user.avatarUnchanged.get());
	}

	public static void cropImage(Integer x1, Integer y1, Integer x2,
			Integer y2, Integer imageW, Integer imageH) {
		User user = CurrentUser.getCurrentUser();

		try {
			BufferedImage source = ImageIO.read(user.avatarUnchanged.get());
			int originalImageWidth = source.getWidth();
			int originalImageHeight = source.getHeight();
			float ratioX = new Float(originalImageWidth) / imageW;
			float ratioY = new Float(originalImageHeight) / imageH;

			File tmpCroppedFile = ImageHelper.getTmpImageFile();
			Images.crop(user.avatarUnchanged.getFile(), tmpCroppedFile,
					Math.round(x1 * ratioX), Math.round(y1 * ratioY), Math
							.round(x2 * ratioX), Math.round((y2 * ratioY)));

			user.updateAvatar(tmpCroppedFile, false);

			user.save();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void search(String query, int from, long cat) {
		if (query == null || query.isEmpty()) {
			insights("trending", 0, "all", null, false);
		}
		
		Category category = Category.findById(cat);
		Filter filter = new Filter();
		if(category != null) {
			filter.categories.add(category);
		}
		InsightResult result = Insight.search(query, from, NUMBER_INSIGHTS_SEARCHPAGE, filter);

		renderArgs.put("count", result.count);
		renderArgs.put("insights", result.results);
		
		if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			currentUser.visitInsightsSearch(query, new UserClientInfo(request, APPLICATION_ID));
		}
		
		render(query, category, from);
	}

	/**
	 * AJAX : get more formatted results for a search
	 * 
	 * @return: the HTML containing the lines to add to the search results
	 */
	public static void moreSearch(String query, int from, long cat) {
		Category category = Category.findById(cat);
		Filter filter = new Filter();
		if(category != null) {
			filter.categories.add(category);
		}
		InsightResult result = Insight.search(query, from, NUMBER_INSIGHTS_SEARCHPAGE, filter);

		renderArgs.put("insights", result.results);
		render("Application/getInsights.html");
	}
	
	/**
	 * AJAX reset the activity feed for the connected user
	 */
	public static void resetInsightActivity() {
		User currentUser = CurrentUser.getCurrentUser();
		if(currentUser == null) {
			renderText("false");
		}
		currentUser.resetInsightActivity();
		renderText("true");
	}
	
	/**
	 * AJAX The current user invites another user
	 * @param email : email to invite
	 * @param message : message displayed in the invitation email
	 */
	public static void invite(@Email @Required String email, String message) {
		if (validation.hasErrors()) {
			flash.error(Messages.get("validation.email"));
			renderText("false");
		}
		
		if (User.findByEmail(email) != null) {
			flash.error(Messages.get("invitation.user.already.registered"));
			renderText("false");
		}
		
		User currentUser = CurrentUser.getCurrentUser();
		try {
			currentUser.invite(email, message);
		} catch (InvitationException e) {
			renderText("false");
			Logger.error(e, e.getMessage());
		}
		renderText("true");
			
	}

	/**
	 * AJAX Send a message to a given user
	 * @param id : id of the user to send the invite
	 * @param content : message to send
	 */
	public static void sendMessage(@Required Long id, @Required String content) {
		if (validation.hasErrors()) {
			renderText("false");
		}
		
		User currentUser = CurrentUser.getCurrentUser();
		
		// block send message to itself ... 
		if (currentUser.id.equals(id)) {
			renderText("false");
		}
		
		User user = User.findById(id);
		if(currentUser.sendMessage(user, content)) {
			renderText("true");
		} else  {
			renderText("false");
		}
	}
	
	/**
	 * AJAX share an insight with a user you are following
	 * @param insightId
	 * @param userId
	 */
	public static void shareInsight(@Required String insightUniqueId, @Required String userName) {
		if (validation.hasErrors()) {
			renderText("false");
		}
		User currentUser = CurrentUser.getCurrentUser();
		User toUser = User.findByUserName(userName);
		if(toUser == null) {
			renderText("{\"error\":\"CannotFindUser\"}");
		}
		Insight insight = Insight.findByUniqueId(insightUniqueId);
		
		try {
			currentUser.shareInsight(toUser, insight);
			renderText("true");
		} catch (NotFollowingUserException e) {
			e.printStackTrace();
			renderText("{\"error\":\"NotFollowingUserException\"}");
		} catch (InsightAlreadySharedException e) {
			e.printStackTrace();
			renderText("{\"error\":\"InsightAlreadySharedException\"}");
		}
		
	}
	
	/**
	 * AJAX Suggests users from an input text
	 * @param term : input text entered by the user
	 */
	public static void favoriteUserSuggest(String term) {
		User currentUser = CurrentUser.getCurrentUser();
		List<User> users = User.find( 
				"select u from User u " 
				+ "join u.followers f "
				+ "where f.id = :id and LOWER(u.userName) like :userName")
				.bind("id", currentUser.id)
				.bind("userName", "%" + term.toLowerCase() + "%")
				.fetch(NUMBER_SUGGESTED_USERS);
		render(users);
	}
	
	/**
	 * AJAX Suggests tags from an input text
	 * @param term : input text entered by the user
	 */
	public static void tagSuggest(String term) {
		if (term == null) {
			error("term is null");
		}
		List <Tag> tags = Tag.find( "byLabelLike", "%" + term.toLowerCase() + "%").fetch(NUMBER_SUGGESTED_TAGS);
		render(tags);
	}
	
	/**
	 * this method should be used instead of calling manageFacebookFriends
	 * because it updates the user's social graph from facebook
	 * and if the user is'nt using facebook it will call manageFacebookFriends method
	 */
	public static void manageFacebookFriendsWithSynchronization() {
		User currentUser = CurrentUser.getCurrentUser();
		
		// if current user has its facebookUserId known then we synchronize his facebook social graph in beansight
		if (currentUser.facebookUserId != null) {
			// add an info in session so that we know it's for synchonizing with facebook account
			session.remove(FacebookOAuthForBeansight.LINK_FACEBOOK_TO_BEANSIGHT_COOKIE);
			session.put(FacebookOAuthForBeansight.FACEBOOK_SYNC_COOKIE, "true");
			
			// add url in session to redirect back the user to the page for managing facebook friends 
			session.put("url", Router.getFullUrl("Application.manageFacebookFriends"));
			
			// redirect to Facebook authentication
			FacebookOAuth.authenticate();
		} else {
			manageFacebookFriends();
		}
	}
	
	/**
	 * render the page to manage facebook friends.
	 * you should user manageFacebookFriendsWithSynchronization method first
	 * and let manageFacebookFriendsWithSynchronization redirect to manageFacebookFriends if necessary
	 */
	public static void manageFacebookFriends() {
		User currentUser = CurrentUser.getCurrentUser();
		
		renderArgs.put("friendsOnFacebookWhoAreOnBeansight", currentUser.findFriendsOnFacebookWhoAreOnBeansight());
		
		render(currentUser);
	}
	
	/**
	 * render the page to manage facebook friends.
	 * you should user manageFacebookFriendsWithSynchronization method first
	 * and let manageFacebookFriendsWithSynchronization redirect to manageFacebookFriends if necessary
	 */
	public static void manageFacebookFriendsFromSideBar() {
		User currentUser = CurrentUser.getCurrentUser();
		
		List<FacebookFriend> fbFriends = currentUser.findFriendsOnFacebookWhoAreOnBeansight();
		for (FacebookFriend friend : fbFriends) {
			if (!friend.isAdded) {
				friend.isHidden = true;
				friend.save();
			}
		}
		
		manageFacebookFriends();
	}
	
	/**
	 * called after having detected that the current user wants to link his account with Facebook
	 * but there is already one account which is linked to the same Facebook account
	 * So this action redirect the user to a page to decide to continue the facebook linking or not
	 * @param otherAccountId
	 */
    public static void facebookIdAlreadyInUseWarning(Long otherAccountId) {
    	User currentUser = CurrentUser.getCurrentUser();
    	
    	renderArgs.put("currentUser", currentUser);
    	renderArgs.put("otherAccount", User.findById(otherAccountId));
    	
    	session.remove("url");
    	
    	render();
    }
    
    /**
     * when the current decides to link his Facebook account on a another beansight account :
     *  other beansight account will have it's attribut facebookUserId set to null
     *  
     */
    public static void overrideFacebookLinkingOnAnotherAccount() {
    	session.put(FacebookOAuthForBeansight.OVERRIDE_FACEBOOK_LINKING, "true");
    	
    	Register.linkBeansightAccountWithFacebook();
    }
    
    /**
     * call this action to display the Facebook widget allowing user to send invitation to its Facebook Friends
     * (ift will add an application invitation in the invited facebook user page)
     */
    public static void inviteYourFacebookFriendsOnBeansightWithFacebookSynchro() {
    	
		// add an info in session so that we know it's for synchonizing with facebook account
		session.remove(FacebookOAuthForBeansight.LINK_FACEBOOK_TO_BEANSIGHT_COOKIE);
		session.put(FacebookOAuthForBeansight.FACEBOOK_SYNC_COOKIE, "true");
		
		// add url in session to redirect back the user to the page for managing facebook friends 
		session.put("url", Router.getFullUrl("Application.inviteYourFacebookFriendsOnBeansight"));
		
		// redirect to Facebook authentication
		FacebookOAuth.authenticate();
    	
    	
    }
    
    /**
     * call this action to display the Facebook widget allowing user to send invitation to its Facebook Friends
     * (ift will add an application invitation in the invited facebook user page)
     */
    public static void inviteYourFacebookFriendsOnBeansight() {
    	// get 
    	List<String> friendIdsToExclude = FacebookFriend.find("select fbf.beansightUserFriend.facebookUserId " +
    			"from FacebookFriend fbf " +
    			"where fbf.user = :currentUser " +
    			"and fbf.isBeansightUser = true")
    			.bind("currentUser", CurrentUser.getCurrentUser())
    			.fetch();
    	
    	renderArgs.put("friendIdsToExclude", friendIdsToExclude);
    	renderArgs.put("facebookAppId", Play.configuration.getProperty("facebook.client_id"));
    	
    	render();
    }
	
    /**
     * when you finished inviting your Facebook friends, Facebook invitation widget redirect to an url
     * and this action is the url Facebook will redirect to.
     * We receive an ids list of the facebook account that have been invited and we save an information
     * to remember that the connected uset have invite some facebook friends
     * @param ids
     */
    public static void facebookInvitationSent(String[] ids) {
    	User currentUser = CurrentUser.getCurrentUser();
    	if (currentUser == null) {
    		error("no valid connected user");
    	}
    	if (ids != null && ids.length > 0) {
	    	for (String id : ids) {
	    		FacebookFriend fbf = FacebookFriend.findRelationshipBetweenUserIdAndFacebookId(currentUser.id, Long.decode(id));
	    		fbf.hasInvited = true;
	    		fbf.save();
	    	}   
    	}
    	
    	
    	index();
    }
    
	@InSitemap(changefreq="yearly", priority=0.1)
	public static void privacyPolicy() {
		render();
	}
	
	@InSitemap(changefreq="yearly", priority=0.1)	
	public static void termsOfUse() {
		render();
	}
	@InSitemap(changefreq="monthly", priority=0.8)	
	public static void FAQ() {
		render();
	}
	
	/**
	 * Page to quickly agree on an insight
	 */
	public static void agree(@Required String id) {
		vote(id, State.AGREE);
	}
	
	/**
	 * Page to quickly disagree on an insight
	 */
	public static void disagree(@Required String id) {
		vote(id, State.DISAGREE);
	}
	
	private static void vote(String insightUniqueId, State voteState) {
		User currentUser = CurrentUser.getCurrentUser();

		if(currentUser != null){
			try {
				currentUser.voteToInsight(insightUniqueId, voteState);
			} catch (CannotVoteTwiceForTheSameInsightException e) {
				// do nothing
			}
			showInsight(insightUniqueId);
		}else{
			Register.register("","");
		}

	}
	
}
