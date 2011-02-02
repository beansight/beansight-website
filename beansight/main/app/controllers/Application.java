package controllers;

import helpers.ImageHelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import play.Logger;
import play.Play;
import play.data.validation.Email;
import play.data.validation.InFuture;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Blob;
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.Images;
import play.modules.search.Query;
import play.modules.search.Search;
import play.mvc.Before;
import play.mvc.Controller;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.InsightAlreadySharedException;
import exceptions.NotFollowingUserException;
import exceptions.UserIsAlreadyFollowingInsightException;

public class Application extends Controller {

	public static final int NUMBER_INSIGHTS_INSIGHTPAGE = 12;
	public static final int NUMBER_INSIGHTACTIVITY_INDEXPAGE = 8;
	public static final int NUMBER_INSIGHTS_USERPAGE = 10;
	public static final int NUMBER_EXPERTS_EXPERTPAGE = 2;

	public static final int NUMBER_INSIGHTS_SEARCHPAGE = 12;
	public static final int NUMBER_EXPERTS_SEARCHPAGE = 2;

	public static final int NUMBER_SUGGESTED_USERS = 10;
	public static final int NUMBER_SUGGESTED_TAGS = 10;
	
	public static final double INSIGHT_VALIDATED_TRUE_MINVAL = 0.7;
	public static final double INSIGHT_VALIDATED_FALSE_MAXVAL = 0.3;
	
    @Before(unless={"welcome", "leaveYourEmail", "applicationPath"})
    /**
     * Make sure the language is the one the user has chosen.
     */
    static void setLanguage() {
        if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			Lang.change(currentUser.uiLanguage.label);			
        } 
    }

    // TODO : add all the ajax method here so that we don't load  data not useful during ajax call
    @Before(unless={"welcome", "leaveYourEmail", "applicationPath"})
    /**
     * If the user is connected, load the needed info into the menu
     */
    public static void loadMenuData() {
        if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			renderArgs.put("insightActivities", currentUser.getInsightActivity(NUMBER_INSIGHTACTIVITY_INDEXPAGE));
			// TODO limit the number and order by update
			renderArgs.put("followedInsights", currentUser.followedInsights);
			renderArgs.put("followedUsers", currentUser.followedUsers);
			
			renderArgs.put("emailConfirmed", currentUser.emailConfirmed);
			renderArgs.put("invitationsLeft", currentUser.invitationsLeft);
        }    	
    }
    
    @Before(unless={"welcome", "leaveYourEmail", "applicationPath"})
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
    	String msg = "";
    	boolean hasError = false;
    	if(validation.hasErrors()) {
    		hasError = true;
    		for (play.data.validation.Error error : validation.errors()) {
    			msg += error.message();
    		}
    		render("Application/leaveYourEmail.json", msg, hasError);
	   	}
    	WaitingEmail waitingEmail = new WaitingEmail(email);
    	waitingEmail.save();
    	msg = Messages.get("welcome.leaveYourEmailSuccess");
    	render("Application/leaveYourEmail.json", msg, hasError);
    }
    
    
    public static void index() {
    	insights(0,null);
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

	public static void insights(long cat, Set<String> lang) {
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
		
		// If connected, get suggested insights
		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			result = currentUser.getSuggestedInsights(0, NUMBER_INSIGHTS_INSIGHTPAGE, filter);
		} else {
			result = Insight.findLatest(0, NUMBER_INSIGHTS_INSIGHTPAGE, filter);
		}

		renderArgs.put("insights", result.results);
		renderArgs.put("count", result.count);
		
		// We convert to a List because Set don't work properly in views.
		// TODO  : re-use Sets when the Play! bug is corrected.
		List<String> langs = null;
		if(lang != null) {
			langs = new ArrayList<String>(lang);
		}
		
		render(category, langs);
	}

	/**
	 * AJAX get more insights from the explore page
	 * 
	 * @param from : the index of the first insight to return
	 * @param cat
	 */
	public static void moreInsights(int from, long cat, Set<String> lang) {
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
		
		InsightResult result = Insight.findLatest(from, NUMBER_INSIGHTS_INSIGHTPAGE, filter);
		renderArgs.put("insights", result.results);
		render();
	}

	public static void experts() {
		List<User> experts = User.findBest(0, NUMBER_EXPERTS_EXPERTPAGE );
		render(experts);
	}

	public static void searchExperts(String query, int from) {
		if (query == null || query.isEmpty()) {
			experts();
		}
		
		UserResult userSearchResult = User.search(query, from, NUMBER_EXPERTS_SEARCHPAGE);
		List<User> experts = userSearchResult.results;
		renderArgs.put("query", query);
		renderTemplate("Application/expertsSerachResult.html", experts);
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
			validation.addError("categoryId", "Not a valid Category");
			// FIXME: This error doesn't display
		}
		if (validation.hasErrors()) {
			flash.error("Error creating the insight");
			create(insightContent, endDate, tagLabelList, categoryId, lang);
		}

		User currentUser = CurrentUser.getCurrentUser();
		Insight insight = currentUser.createInsight(insightContent, endDate, tagLabelList, categoryId, lang);

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
		renderArgs.put("agree", voteState == State.AGREE);
		render("Application/vote.json", insight);
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
			Vote lastUserVote = Vote.findLastVoteByUserAndInsight(
					currentUser.id, insight.uniqueId);
			
			renderArgs.put("currentUser", currentUser);
			renderArgs.put("lastUserVote", lastUserVote);
		}
		
		List<Vote> lastVotes = insight.getLastVotes(5);
		
        renderArgs.put("lastVotes", lastVotes);
        renderArgs.put("agreeTrends", insight.getAgreeRatioTrends(100));
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

	public static void saveSettings(Long id, @Required @Match(value="[a-zA-Z0-9_]{3,16}", message="user name has to be 3-16 chars and no space") String userName, String realName,
			String description, String uiLanguage, File originalImage) {
		if (validation.hasErrors()) {
			params.flash();
	        validation.keep();
	        settings();
	    }
		
		// check if it's a valid image
		if (originalImage != null) {
			try {
				if (ImageIO.read(originalImage) == null) {
					flash.error(Messages.get("Error while reading image : is it a valid image ?")); // TODO: internationalize text
					settings();
				}
			} catch (IOException e1) {
				flash.error(Messages.get("Error while reading image : is it a valid image ?")); // TODO: internationalize text
				settings();
			}
		}
		
		User user = CurrentUser.getCurrentUser();
		// User should be the same as the one connected
		if (user.id.equals(id) == false) {
			forbidden("It seems you are trying to hack someone else settings"); // TODO: internationalize text
			settings();
		}
		// check if a new image has been uploaded
		if (originalImage != null) {
			try {
				// and save it if so
				user.updateAvatar(originalImage, true);
			} catch (FileNotFoundException e) {
				flash.error(Messages.get("saveSettingImageNotFoundException"));
				settings();
			}
		}

		user.userName = userName;
		user.realName = realName;
		user.description = description;
		user.uiLanguage = Language.findByLabelOrCreate(uiLanguage);

		user.save();

		settings();
	}

	public static void settings() {
		User user = CurrentUser.getCurrentUser();
		render(user);
	}
	
	/**
	 * Render the small avatar
	 * 
	 * @param userName
	 */
	public static void showAvatarSmall(String userName) {
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
	public static void showAvatarMedium(String userName) {
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
	public static void showAvatarLarge(String userName) {
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
	 * Render the uploaded image so that the user crop his avatar from it
	 */
	public static void displayOriginalUncropedImage() {
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
			insights(0, null);
		}
		
		Category category = Category.findById(cat);
		Filter filter = new Filter();
		if(category != null) {
			filter.categories.add(category);
		}
		InsightResult result = Insight.search(query, from, NUMBER_INSIGHTS_SEARCHPAGE, filter);

		renderArgs.put("count", result.count);
		renderArgs.put("insights", result.results);
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
		if(currentUser.invite(email, message)) {
			renderText("true");
		} else  {
			renderText("false");
		}
	}

	/**
	 * @return the play id
	 */
	public static void playId() {
		renderText(Play.id);
	}

	/**
	 * @return the play id
	 */
	public static void applicationPath() {
		renderText(System.getProperty("application.path"));
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
	
	public static void rebuildAllIndexes() {
		if (CurrentUser.isAdmin() == true) {
			try {
				Search.rebuildAllIndexes();
			} catch (Exception e) {
				renderText(e.getMessage());
			}
			renderText("rebuilt all indexes : ok");
		} else {
			renderText("you cannot do that");
		}
		
	}

}
