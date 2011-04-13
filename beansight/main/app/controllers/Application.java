package controllers;

import helpers.ImageHelper;
import helpers.InSitemap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import jregex.MatchIterator;
import jregex.MatchResult;
import jregex.Pattern;

import models.Category;
import models.Comment;
import models.Filter;
import models.Filter.FilterType;
import models.FollowNotificationTask;
import models.Insight;
import models.Insight.InsightResult;
import models.InsightTrend;
import models.Language;
import models.PeriodEnum;
import models.Tag;
import models.Topic;
import models.Trend;
import models.User;
import models.User.UserResult;
import models.UserCategoryScore;
import models.UserInsightsFilter;
import models.Vote;
import models.Vote.State;
import models.WaitingEmail;
import models.analytics.UserClientInfo;

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
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.InsightAlreadySharedException;
import exceptions.InvitationException;
import exceptions.NotFollowingUserException;
import exceptions.UserIsAlreadyFollowingInsightException;

public class Application extends Controller {

	public static final int NUMBER_INSIGHTS_INSIGHTPAGE = 12;
	public static final int NUMBER_INSIGHTS_INSIGHTPAGE_NOTLOGGED = 5;
	public static final int NUMBER_INSIGHTACTIVITY_INDEXPAGE = 4;
	public static final int NUMBER_INSIGHTS_USERPAGE = 10;
	public static final int NUMBER_EXPERTS_EXPERTPAGE = 5;

	public static final int NUMBER_INSIGHTS_SEARCHPAGE = 12;
	public static final int NUMBER_EXPERTS_SEARCHPAGE = 5;

	public static final int NUMBER_SUGGESTED_USERS = 10;
	public static final int NUMBER_SUGGESTED_TAGS = 10;
	
	public static final double INSIGHT_VALIDATED_TRUE_MINVAL = 0.6;
	public static final double INSIGHT_VALIDATED_FALSE_MAXVAL = 0.4;
	
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
    @Before(unless={"insightsFilter", "moreInsights", "leaveYourEmail", "shareInsight", "agree", "disagree", "loadFollowedUsers", "toggleFollowingUser", "toggleFollowingInsight", "searchExperts", "showAvatarSmall", "showAvatarMedium", "showAvatarLarge"})
    public static void loadMenuData() {
        if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			
			// if this is twitter user and he hasn't validated its promocode the redirect  
			if ( (currentUser.twitterUserId != null && currentUser.twitterUserId.trim().equals("") == false) && !currentUser.isPromocodeValidated) {
				session.put("url", request.url);
				Register.extAuthFirstTimeConnectPage(currentUser.email, currentUser.userName);
			}
			
			renderArgs.put("insightActivities", currentUser.getInsightActivity(NUMBER_INSIGHTACTIVITY_INDEXPAGE));
			// TODO limit the number and order by update
			renderArgs.put("followedInsights", currentUser.getNotHiddenFollowedInsights());
			renderArgs.put("followedUsers", currentUser.followedUsers);
			
			renderArgs.put("emailConfirmed", currentUser.emailConfirmed);
			renderArgs.put("invitationsLeft", currentUser.invitationsLeft);
        }    	
    }
    
    public static void welcome() {
    	if(!Security.isConnected()) {
    		render();
    	} else {
    		index();
    	}
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
		
		// log for analytics
		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			currentUser.visitInsightsList(new UserClientInfo(request, APPLICATION_ID));
		}
		render(sortBy, topic, closed);
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
		renderArgs.put("insights", result.results);
		render();
	}
	
	
	/**
	 * AJAX get a list of insights starting at index 0 : [0, from + NUMBER_INSIGHTS]
	 * @param from : the index of the first insight to return
	 */
	public static void reloadInsights(int from, String sortBy, long cat, String filterVote, String topic, Boolean closed) {
		if (filterVote == null || filterVote.trim().equals("")) {
			filterVote = "all";
		}
		
		InsightResult result = getFilteredInsightsList(0, (from + getNumberInsightsInsightPage()), sortBy, cat, filterVote, topic, closed, null);
		renderArgs.put("insights", result.results);
		renderTemplate("Application/getInsights.html");
	}
	
	private static InsightResult getFilteredInsightsList(int from, int numberInsights, String sortBy, long cat, String filterVote, String topicStr, Boolean closed, String userName) {
		Filter filter = new Filter();
		filter.filterVote = filterVote;

		Topic topic = null;
		if(topicStr != null && !topicStr.trim().equalsIgnoreCase("undefined")) {
			topic = Topic.findByLabel(topicStr);
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

		// tags
		if(topic != null) {
			for(Tag tag : topic.tags) {
				filter.tags.add(tag);
			}
		}
		
		InsightResult result;
		
		
		if (closed != null && closed == true) {
			result = Insight.findClosedInsights(from, numberInsights, filter);
		} else {
			// depending on the sortBy
			if(sortBy != null && sortBy.equals("updated")) {
				filter.filterType = FilterType.UPDATED;
				result = Insight.findLatest(from, numberInsights, filter);
			} else if (sortBy != null && sortBy.equals("trending")) {
				filter.filterType = FilterType.TRENDY;
				result = Insight.findTrending(from, numberInsights, filter);
			} else if (sortBy != null && sortBy.equals("incoming")) {
				filter.filterType = FilterType.INCOMING;
				result = Insight.findIncoming(from, numberInsights, filter);
			} else { 
				// default is incoming
				filter.filterType = FilterType.INCOMING;
				result = Insight.findIncoming(from, numberInsights, filter);
			}
		}
		return result;
	}
	
	

	
	public static void experts() {
		List<User> experts = User.findBest(0, NUMBER_EXPERTS_EXPERTPAGE );
		
		// If connected, log analytic
		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			// log for analytics
			currentUser.visitExpertsList(new UserClientInfo(request, APPLICATION_ID));
		}
		
		render(experts);
	}

	public static void searchExperts(String query, int from) {
		if (query == null || query.isEmpty()) {
			List<User> experts = User.findBest(from, NUMBER_EXPERTS_EXPERTPAGE );
			renderTemplate("Application/expertsSearchResult.html", experts);
		}
		
		UserResult userSearchResult = User.search(query, from, NUMBER_EXPERTS_SEARCHPAGE);
		List<User> experts = userSearchResult.results;
		renderArgs.put("query", query);
		renderTemplate("Application/expertsSearchResult.html", experts);
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
			
			// unless the insight date is passed, remove the last trends which is set at the insight's endDate 
			if (agreeInsightTrends != null && !agreeInsightTrends.isEmpty() && insight.endDate.after(new Date())) {
				agreeInsightTrends.remove(agreeInsightTrends.size() - 1);
			}
			agreeInsightTrendsCache.put(insight.id, agreeInsightTrends);
		}
		
        renderArgs.put("lastVotes", lastVotes);
        renderArgs.put("agreeInsightTrends", agreeInsightTrends);
        renderArgs.put("comments", insight.getNotHiddenComments());
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

		List<UserCategoryScore> categoryScores = UserCategoryScore.find("select cs from UserCategoryScore cs " +
				"where cs.historic.user = :user and cs.historic.scoreDate = :scoreDate and cs.period = :period " +
				"order by normalizedScore DESC")
				.bind("user", user)
				.bind("scoreDate", new DateMidnight(new Date()).toDate())
				.bind("period", PeriodEnum.THREE_MONTHS)
				.fetch();
//		List<Insight> lastInsights = user.getLastInsights(NUMBER_INSIGHTS_USERPAGE);
		
//		render(user, lastInsights, currentUserProfilePage);
		render(user, categoryScores, currentUserProfilePage);
	}

	/**
	 * AJAX get a list of insights for a user : [from, from + NUMBER_INSIGHTS]
	 * @param from : the index of the first insight to return
	 */
	public static void getUserInsights(String userName, int from, long cat, String filterVote) {
		User user = User.findByUserName(userName);
		notFoundIfNull(user);
		
		InsightResult result = getFilteredUserInsightsList(from, getNumberInsightsInsightPage(), cat, user, filterVote);
		renderArgs.put("insights", result.results);
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
		renderArgs.put("insights", result.results);
		renderArgs.put("targetUser", user);
		
		renderTemplate("Application/getInsights.html");
	}
	
	private static InsightResult getFilteredUserInsightsList(int from, int numberInsights, long cat, User user, String filterVote) {
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
			currentUser.startFollowingThisUser(user);
			renderArgs.put("follow", true);
		}
		render("Application/followUser.json", userId);
	}

	public static void loadFollowedUsers(Long userId) {
		User currentUser = CurrentUser.getCurrentUser();

		renderArgs.put("_followedUsers", currentUser.followedUsers);
		renderTemplate("tags/followedUsers.tag");
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
	public static void addComment(@Required String uniqueId, @MinSize(5) String content) {
    	if(validation.hasErrors()) {
    		return;
	   	}
		
		User commentWriter = CurrentUser.getCurrentUser();
		Insight insight = Insight.findByUniqueId(uniqueId);
		Comment comment = insight.addComment(content, commentWriter);
		insight.save();
		
		render(comment);
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
		List <Tag> tags = Tag.find( "byLabelLike", "%" + term.toLowerCase() + "%").fetch(NUMBER_SUGGESTED_TAGS);
		render(tags);
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
	
}
