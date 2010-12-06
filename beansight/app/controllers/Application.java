package controllers;

import helpers.ImageHelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import models.Category;
import models.Comment;
import models.FollowNotificationTask;
import models.Insight;
import models.MailConfirmTask;
import models.Insight.InsightResult;
import models.Trend;
import models.User;
import models.Vote;
import models.Vote.State;
import play.Logger;
import play.Play;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.FileAttachment;
import play.i18n.Lang;
import play.libs.Files;
import play.libs.Images;
import play.modules.search.Search;
import play.modules.search.Search.Query;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.results.NotFound;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.UserIsAlreadyFollowingInsightException;

public class Application extends Controller {

	public static final int NUMBER_INSIGHTS_INDEXPAGE = 16;
	public static final int NUMBER_INSIGHTS_USERPAGE = 6;
	public static final int NUMBER_INSIGHTS_INSIGHTPAGE = 20;
	public static final int NUMBER_EXPERTS_EXPERTPAGE = 16;
	public static final int NUMBER_INSIGHTS_SEARCHPAGE = 20;
	public static final int NUMBER_EXPERTS_SEARCHPAGE = 20;

    @Before
    /**
     * Make sure the language is the one the user has chosen.
     */
    static void setLanguage() {
        if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			Lang.change(currentUser.uiLanguage);
        }
    }

	
	public static void index() {
		// TODO order by upDate
		List<Insight> insights = Insight.find("order by creationDate DESC")
				.fetch(NUMBER_INSIGHTS_INDEXPAGE);

		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			
			// TODO display activity, and not insights (such as "x persons agreed on your insight y")
			List<Insight> insightActivity = currentUser.createdInsights;

			// TODO limit the number and order by update
			List<Insight> followedInsights = currentUser.followedInsights;
			List<User> followedUsers = currentUser.followedUsers;
			
			boolean emailConfirmed = currentUser.emailConfirmed;

			render("Application/indexConnected.html", insights, followedInsights, followedUsers, insightActivity, emailConfirmed);
		}

		render("Application/indexNotConnected.html", insights);
	}

	public static void create(String insightContent, Date endDate, String tagLabelList, long categoryId, String insightLang) {
		if(insightLang == null ) {
			User currentUser = CurrentUser.getCurrentUser();
			insightLang = currentUser.writtingLanguage;
		}
		render(insightContent, endDate, tagLabelList, categoryId, insightLang);
	}

	public static void myInsights() {
		User currentUser = CurrentUser.getCurrentUser();
		List<Insight> myLastInsights = currentUser
				.getLastInsights(NUMBER_INSIGHTS_USERPAGE);

		render(myLastInsights);
	}

	public static void insights(long categoryId, String language) {
		Category category = Category.findById(categoryId);
		
		if(language == null || language.equals("")) {
			language = "en";
		}

		InsightResult result = Insight.getLatest(0, NUMBER_INSIGHTS_INSIGHTPAGE, category, language);
		renderArgs.put("insights", result.results);
		renderArgs.put("count", result.count);

		User currentUser = CurrentUser.getCurrentUser();
		List<Insight> followedInsights = currentUser.followedInsights;

		render(followedInsights, category, language);
	}

	/**
	 * AJAX get more insights from the explore page
	 * 
	 * @param from : the index of the first insight to return
	 * @param categoryId
	 */
	public static void moreInsights(int from, long categoryId, String language) {
		Category category = Category.findById(categoryId);

		InsightResult result = Insight.getLatest(from, NUMBER_INSIGHTS_INSIGHTPAGE, category, language);
		renderArgs.put("insights", result.results);
		render();
	}

	public static void experts() {
		// TODO order by score
		List<User> experts = User.find("order by crdate DESC").fetch(
				NUMBER_EXPERTS_EXPERTPAGE);

		User currentUser = CurrentUser.getCurrentUser();
		List<User> followedUsers = currentUser.followedUsers;

		render(experts, followedUsers);
	}

	/**
	 * create an insight for the current user
	 * 
	 * @param insightContent
	 *            : the content of this insight (min 6, max 140 characters)
	 * @param endDate
	 *            : the end date chosen by the user
	 * @param tagLabelList
	 *            : a comma separated list of tags
	 * @param categoryId
	 *            : the ID of the category of the insight
	 */
	public static void createInsight(
			@Required @MinSize(6) @MaxSize(140) String insightContent,
			@Required Date endDate, @MaxSize(100) String tagLabelList,
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

		showInsight(insight.id);
	}

	public static void displayAllInsights() {
		List<Insight> allInsights = Insight.all().fetch();
		render(allInsights);
	}

	/**
	 * AJAX Agree a given insight
	 * 
	 * @param insightId
	 */
	public static void agree(Long insightId) {
		vote(insightId, State.AGREE);
	}

	/**
	 * AJAX Disagree a given insight
	 * 
	 * @param insightId
	 */
	public static void disagree(Long insightId) {
		vote(insightId, State.DISAGREE);
	}

	private static void vote(Long insightId, State voteState) {
		User currentUser = CurrentUser.getCurrentUser();

		try {
			currentUser.voteToInsight(insightId, voteState);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			// its ok, do not show anything
		}

		Insight insight = Insight.findById(insightId);
		renderArgs.put("agree", voteState == State.AGREE);
		render("Application/vote.json", insight);
	}

	/**
	 * Show info about a given insight
	 * 
	 * @param id
	 */
	public static void showInsight(Long id) {
		Insight insight = Insight.findById(id);
		notFoundIfNull(insight);

		if (Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();
			Vote lastUserVote = Vote.findLastVoteByUserAndInsight(
					currentUser.id, id);
			
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
	public static void showUser(Long id) {
		User user = User.findById(id);
		notFoundIfNull(user);
		render(user);
	}

	/**
	 * AJAX: Change the follow state for the connected user toward this insight
	 */
	public static void toggleFollowingInsight(Long insightId) {
		User currentUser = CurrentUser.getCurrentUser();
		Insight insight = Insight.findById(insightId);

		if (currentUser.isFollowingInsight(insight) == true) {
			currentUser.stopFollowingThisInsight(insightId);
			renderArgs.put("follow", false);
		} else {
			try {
				currentUser.startFollowingThisInsight(insightId);
				renderArgs.put("follow", true);
			} catch (UserIsAlreadyFollowingInsightException e) {
				// it's ok to re-follow something
			}
		}

		render("Application/followInsight.json", insightId);
	}

	public static void startFollowingInsight(Long insightId) {
		User currentUser = CurrentUser.getCurrentUser();
		try {
			currentUser.startFollowingThisInsight(insightId);
		} catch (UserIsAlreadyFollowingInsightException e) {
			// it's ok to re-follow something
		}
		renderArgs.put("follow", true);
		render("Application/followInsight.json", insightId);
	}

	public static void stopFollowingInsight(Long insightId) {
		User currentUser = CurrentUser.getCurrentUser();
		currentUser.stopFollowingThisInsight(insightId);
		renderArgs.put("follow", false);
		render("Application/followInsight.json", insightId);
	}

	/**
	 * AJAX: Change the follow state for the connected user toward this user
	 */
	public static void toggleFollowingUser(Long userId) {
		User currentUser = CurrentUser.getCurrentUser();
		User user = User.findById(userId);
		if (currentUser.isFollowingUser(user) == true) {
			currentUser.stopFollowingThisUser(user);
			renderArgs.put("follow", false);
		} else {
			currentUser.startFollowingThisUser(user);
			
			FollowNotificationTask mail = new FollowNotificationTask(user, currentUser);
			mail.save();
			
			renderArgs.put("follow", true);
		}
		render("Application/followUser.json", userId);
	}

	/**
	 * Add a comment to a specific insight for the current user
	 * 
	 * @param insightId
	 *            : id of the insight
	 * @param content
	 *            : text content of the insight
	 */
	public static void addComment(Long insightId, String content) {
		User currentUser = CurrentUser.getCurrentUser();
		Insight insight = Insight.findById(insightId);
		Comment comment = insight.addComment(content, currentUser);

		render("Application/comment.json", comment);
	}

	/**
	 * add tags to an insight
	 * 
	 * @param insightId
	 *            : the id of the tagged insight
	 * @param tagLabelList
	 *            : a comma separated list of tag labels
	 */
	public static void addTags(Long insightId, String tagLabelList) {
		User currentUser = CurrentUser.getCurrentUser();
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
		User currentUser = CurrentUser.getCurrentUser();
		currentUser.computeScores();
		currentUser.save();
		showUser(currentUser.id);
	}

	public static void saveSettings(Long id, String userName, String firstName,
			String lastName, String uiLanguage, File originalImage) {
		User user = CurrentUser.getCurrentUser();
		// User should be the same as the one connected
		if (user.id.equals(id) == false) {
			forbidden("It seems you are trying to hack someone else settings");
		}
		// check if a new image has been uploaded
		if (originalImage != null) {
			File originalImageCopy = new File(FileAttachment.getStore(),
					"originalImage_" + user.id);
			originalImage.renameTo(originalImageCopy);
			// Default is we resize the originalImage without any modification.
			// Can be cropped later if necessary since we keep the original
			File resizedOriginalImage = new File(Play.getFile("tmp") + "/resizedOriginalImageTmp_" + user.id);
			ImageHelper.resizeRespectingRatio(originalImageCopy, resizedOriginalImage, 60, 60);
//			Images.resize(originalImageCopy, resizedOriginalImage, 60, 60);
			user.avatar.set(resizedOriginalImage);
			user.saveAttachment();
			resizedOriginalImage.deleteOnExit();
		}

		user.userName = userName;
		user.firstName = firstName;
		user.lastName = lastName;
		user.uiLanguage = uiLanguage;

		user.save();

		settings();
	}

	public static void settings() {
		User user = CurrentUser.getCurrentUser();
		render(user);
	}

	/**
	 * Render the user avatar
	 * 
	 * @param userId
	 */
	public static void showAvatar(Long userId) {
		User user = User.findById(userId);
		if (user != null && user.avatar.isSet()) {
			renderBinary(user.avatar.get());
		}
		renderBinary(new File(Play.getFile("public/images") + "/unknown.jpg"));
		notFound();
	}

	/**
	 * Render the uploaded image so that the user crop his avatar from it
	 */
	public static void displayOriginalUncropedImage() {
		User user = CurrentUser.getCurrentUser();
		File tmpFile = new File(FileAttachment.getStore(), "originalImage_"
				+ user.id);
		if (!tmpFile.exists()) {
			renderBinary(new File(Play.getFile("public/images")
					+ "/unknown.jpg"));
		}
		renderBinary(tmpFile);
	}

	/**
	 * Render to the page which give the opportunity to crop the avatar image
	 */
	public static void cropAvatar() {
		User user = CurrentUser.getCurrentUser();

		render(user);
	}

	public static void cropImage(Integer x1, Integer y1, Integer x2,
			Integer y2, Integer imageW, Integer imageH) {
		User user = CurrentUser.getCurrentUser();

		File imageToCrop = new File(FileAttachment.getStore(), "originalImage_"
				+ user.id);
		try {
			BufferedImage source = ImageIO.read(imageToCrop);
			int originalImageWidth = source.getWidth();
			int originalImageHeight = source.getHeight();
			float ratioX = new Float(originalImageWidth) / imageW;
			float ratioY = new Float(originalImageHeight) / imageH;

			Images.crop(imageToCrop, user.avatar.get(),
					Math.round(x1 * ratioX), Math.round(y1 * ratioY), Math
							.round(x2 * ratioX), Math.round((y2 * ratioY)));

			Images.resize(user.avatar.get(), user.avatar.get(), 60, 60);

			user.saveAttachment();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void search(String query, int offset, long categoryId) {
		if (query == null || query.isEmpty()) {
			insights(0, "en");
		}
		Category category = Category.findById(categoryId);

		InsightResult result = Insight.search(query, offset,
				NUMBER_INSIGHTS_SEARCHPAGE, category);

		renderArgs.put("count", result.count);
		renderArgs.put("insights", result.results);
		render(query, category, offset);
	}

	/**
	 * AJAX : get more formatted results for a search
	 * 
	 * @return: the HTML containing the lines to add to the search results
	 */
	public static void moreSearch(String query, int offset, long categoryId) {
		Category category = Category.findById(categoryId);

		InsightResult result = Insight.search(query, offset,
				NUMBER_INSIGHTS_SEARCHPAGE, category);

		renderArgs.put("insights", result.results);
		render("Application/moreInsights.html");
	}

	public static void userSearch(String query) {
		Query q = Search.search(query, User.class);
		List<User> users = q.fetch();
		render(users);
	}
	
	/** Confirm that the email adress of the user is a real one */
	public static void confirm(String uuid) {
		if(uuid == null || uuid.isEmpty()) {
			notFound();
		}
		User user = User.find("byUuid", uuid).first();
		notFoundIfNull(user);
		user.emailConfirmed = true;
		user.save();
		
		Logger.info("Email confirmation for user : " + user.email);
		
		render(user);
	}

}