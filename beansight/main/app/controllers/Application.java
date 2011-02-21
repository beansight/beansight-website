package controllers;

import helpers.FormatHelper;
import helpers.ImageHelper;

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

import models.Category;
import models.Comment;
import models.Filter;
import models.FollowNotificationTask;
import models.Insight;
import models.Insight.InsightResult;
import models.Language;
import models.Tag;
import models.User;
import models.User.UserResult;
import models.Vote;
import models.Vote.State;
import models.WaitingEmail;
import models.analytics.UserClientInfo;
import play.Logger;
import play.Play;
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
	public static final int NUMBER_INSIGHTACTIVITY_INDEXPAGE = 8;
	public static final int NUMBER_INSIGHTS_USERPAGE = 10;
	public static final int NUMBER_EXPERTS_EXPERTPAGE = 5;

	public static final int NUMBER_INSIGHTS_SEARCHPAGE = 12;
	public static final int NUMBER_EXPERTS_SEARCHPAGE = 5;

	public static final int NUMBER_SUGGESTED_USERS = 10;
	public static final int NUMBER_SUGGESTED_TAGS = 10;
	
	public static final double INSIGHT_VALIDATED_TRUE_MINVAL = 0.7;
	public static final double INSIGHT_VALIDATED_FALSE_MAXVAL = 0.3;
	
	public static final String APPLICATION_ID = "web-desktop";
	
    /**
     * Make sure the language is the one the user has chosen.
     */
	@Before(unless={"welcome", "leaveYourEmail", "showInsight"})
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
    @Before
    public static void loadMenuData() {
        if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			
			// if this is facebook user and he hasn't validated its promocode the redirect  
			if (currentUser.facebookUserId != null && !currentUser.isPromocodeValidated) {
				Register.facebookFirstTimeConnectPage();
			}
			// if this is twitter user and he hasn't validated its promocode the redirect  
			if (currentUser.twitterUserId != null && !currentUser.isPromocodeValidated) {
				Register.facebookFirstTimeConnectPage();
			}
			
			renderArgs.put("insightActivities", currentUser.getInsightActivity(NUMBER_INSIGHTACTIVITY_INDEXPAGE));
			// TODO limit the number and order by update
			renderArgs.put("followedInsights", currentUser.getNotHiddenFollowedInsights());
			renderArgs.put("followedUsers", currentUser.followedUsers);
			
			renderArgs.put("emailConfirmed", currentUser.emailConfirmed);
			renderArgs.put("invitationsLeft", currentUser.invitationsLeft);
        }    	
    }
    
    @Before(unless={"welcome", "leaveYourEmail", "showInsight", "showAvatarSmallFromEmail", "showAvatarSmall"})
    static void checkAuthentication() {
    	if(!Security.isConnected()) {
    		welcome();
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
    	insights("trending", 0, null);
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
	public static void create(String insightContent, Date endDate, String tagLabelList, long categoryId, String insightLang) {
		if(insightLang == null ) {
			User currentUser = CurrentUser.getCurrentUser();
			insightLang = currentUser.writtingLanguage.label;
		}
		render(insightContent, endDate, tagLabelList, categoryId, insightLang);
	}

	public static void profile() {
		User currentUser = CurrentUser.getCurrentUser();
		showUser(currentUser.userName);
	}

	public static void insights(String sortBy, long cat, Set<String> lang) {
		Filter filter = new Filter();

		Category category = Category.findById(cat);
		if(category != null) {
			filter.categories.add(category);
		}
		
		// TODO For now, add French and English. Later get from the user's spoken languages:
		lang = new HashSet<String>();
		lang.add("en");
		lang.add("fr");
		
		//if(lang == null) {
		//	lang = new HashSet<String>();
		//	lang.add("en");
		//}
		
		filter.languages = Language.toLanguageSet(lang);

		InsightResult result;
		
		// depending on the sortBy
		if(sortBy != null && sortBy.equals("updated")) {
			// If connected, get suggested insights
			if (Security.isConnected()) {
				User currentUser = CurrentUser.getCurrentUser();
				result = currentUser.getSuggestedInsights(0, NUMBER_INSIGHTS_INSIGHTPAGE, filter);
			} else {
				result = Insight.findLatest(0, NUMBER_INSIGHTS_INSIGHTPAGE, filter);
			}
		} else {
			result = Insight.findTrending(0, NUMBER_INSIGHTS_INSIGHTPAGE, filter);
		}
		
		// log for analytics
		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			currentUser.visitInsightsList(new UserClientInfo(request, APPLICATION_ID));
		}

		renderArgs.put("insights", result.results);
		renderArgs.put("count", result.count);
		
		// We convert to a List because Set don't work properly in views.
		// TODO  : re-use Sets when the Play! bug is corrected.
		List<String> langs = null;
		if(lang != null) {
			langs = new ArrayList<String>(lang);
		}
		
		render(sortBy, category, langs);
	}

	/**
	 * AJAX get more insights from the explore page
	 * 
	 * @param from : the index of the first insight to return
	 * @param cat
	 */
	public static void moreInsights(String sortBy, int from, long cat, Set<String> lang) {
		Category category = Category.findById(cat);
		Filter filter = new Filter();
		if(category != null) {
			filter.categories.add(category);
		}
		if(lang == null) {
			lang = new HashSet<String>();
			lang.add("en");
		}
		filter.languages = Language.toLanguageSet(lang);
		
		InsightResult result = null;
		
		if(sortBy != null && sortBy.equals("updated")) {
			result = Insight.findLatest(from, NUMBER_INSIGHTS_INSIGHTPAGE, filter);
		} else {
			result = Insight.findTrending(from, NUMBER_INSIGHTS_INSIGHTPAGE, filter);
		}
		
		renderArgs.put("insights", result.results);
		render();
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
	 */
	public static void createInsight(
			@Required @MinSize(6) @MaxSize(120) String insightContent,
			@Required @InFuture Date endDate, @MaxSize(100) String tagLabelList,
			@Required long categoryId, String lang) {
		
		// Check if the given category Id corresponds to a category
		Category category = Category.findById(categoryId);
		if (category == null) {
			validation.addError("categoryId", "Not a valid Category"); // TODO : I18N
			// FIXME: This error doesn't display
		}
		if (validation.hasErrors()) {
			flash.error("Error creating the insight"); // TODO : I18N
			create(insightContent, endDate, tagLabelList, categoryId, lang);
		}

		User currentUser = CurrentUser.getCurrentUser();
		Insight insight = null;
		try {
			insight = currentUser.createInsight(insightContent, endDate, tagLabelList, categoryId, lang);
		} catch (Throwable t) {
			flash.error("Error creating the insight : " + t.getMessage()); // TODO : I18N
			create(insightContent, endDate, tagLabelList, categoryId, lang);
		}

		showInsight(insight.uniqueId);
	}

	/**
	 * AJAX Agree a given insight
	 * 
	 * @param insightId
	 */
	public static void agree(String insightUniqueId) {
		vote(insightUniqueId, State.AGREE);
		
	}

	/**
	 * AJAX Disagree a given insight
	 * 
	 * @param insightId
	 */
	public static void disagree(String insightUniqueId) {
		vote(insightUniqueId, State.DISAGREE);
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
		
        renderArgs.put("lastVotes", lastVotes);
        renderArgs.put("agreeTrends", insight.getAgreeRatioTrends(100));
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

		List<Insight> lastInsights = user.getLastInsights(NUMBER_INSIGHTS_USERPAGE);
		
		render(user, lastInsights, currentUserProfilePage);
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
			
			FollowNotificationTask mail = new FollowNotificationTask(currentUser, user);
			mail.save();
			
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
		
		User currentUser = CurrentUser.getCurrentUser();
		Insight insight = Insight.findByUniqueId(uniqueId);
		Comment comment = insight.addComment(content, currentUser);
		comment.content = FormatHelper.htmlLinkifyAll(comment.content);
		
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

	// TODO remove me, score computation should be called in a job.
	public static void temp_recomputeScore() {
		User currentUser = CurrentUser.getCurrentUser();
		currentUser.computeScores();
		currentUser.save();
		showUser(currentUser.userName);
	}

	
	public static void settings() {
		User user = CurrentUser.getCurrentUser();
		render(user);
	}
	
	public static void saveSettings(String uiLanguage, @Required @Match(value="[a-zA-Z0-9_]{3,16}", message="username has to be 3-16 chars, no space, no accent and no puncuation") String userName) {
		User user = CurrentUser.getCurrentUser();
		if(!userName.equals(user.userName) && !User.isUsernameAvailable(userName)) {
			validation.addError("username", Messages.get("registerusernameexist")); 
		}
		if (validation.hasErrors()) {
	        validation.keep();
			flash.error(Messages.get("saveSettings.validation"));
			Application.settings();
	    }
		
		user.uiLanguage = Language.findByLabelOrCreate(uiLanguage);
		user.userName = userName;
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
		
		if (user.avatarSmall.exists()) {
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
			insights("trending", 0, null);
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
		render("Application/moreInsights.html");
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
	
	public static void privacyPolicy() {
		renderTemplate("Legal/privacyPolicy.html");
	}
	
	public static void termsOfUse() {
		renderTemplate("Legal/termsOfUse.html");
	}

}
