package models;

import helpers.ImageHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.Insight.InsightResult;
import models.Vote.State;
import models.Vote.Status;

import org.apache.commons.lang.text.StrSubstitutor;
import org.hibernate.annotations.Index;

import play.Logger;
import play.Play;
import play.db.jpa.Blob;
import play.db.jpa.Model;
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.Codec;
import play.libs.Crypto;
import play.modules.search.Field;
import play.modules.search.Indexed;
import play.modules.search.Query;
import play.modules.search.Search;
import play.mvc.Scope.Params;
import play.utils.Utils.Maps;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.InsightAlreadySharedException;
import exceptions.NotFollowingUserException;
import exceptions.UserIsAlreadyFollowingInsightException;

@Entity
@Indexed
public class User extends Model {

	public static final int NUMBER_SHAREDINSIGHTS_SUGGEDTEDINSIGHTS = 2;
	
	@Field
	@Index (name = "USER_USERNAME_IDX")
	public String userName;

	public String password;
	
	@Field
	public String realName;
	
	@Index (name = "USER_EMAIL_IDX")
	public String email;
	/** Has the user confirmed his email */
	public boolean emailConfirmed;
	
	/** Is this user an administrator ? */
	public boolean isAdmin;

	/** a unique identifier that designate this user */
	public String uuid;
	
	/** twitter specific informations */
	public String twitterUserId;
	public String twitterScreenName;

	/** facebook specific informations */
    public Long facebookUserId;
    public String facebookScreenName;
    
	@Lob
    public String description;
	
	/** Language the user wants his UI to be displayed in */
    @ManyToOne
	public Language uiLanguage;
	/** Language the user is writing insights in */
    @ManyToOne  
	public Language writtingLanguage;
	
	/** How many invitations this user can send, -1 for infinity*/
	public long invitationsLeft;
	
	public Blob avatarUnchanged;
	public Blob avatarSmall;
	public Blob avatarMedium;
	public Blob avatarLarge;

	/** Date the user created his account */
	private Date crdate; // private because must be read-only.

	/** the global score for this user */
	@Field
	public double score;

	/** list of scores of this users in all the categories */
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	public List<UserCategoryScore> categoryScores;

	/** list of insights created by this user */
	@OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
	public List<Insight> createdInsights;

	/** every votes of the current user */
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	public List<Vote> votes;

	/** the insights followed by this user */
	@ManyToMany(cascade = CascadeType.ALL)
	public List<Insight> followedInsights;

	/** the users followed by this user */
	@ManyToMany(cascade = CascadeType.ALL)
	public List<User> followedUsers;

	/** the users who follow this user */
	@ManyToMany(mappedBy = "followedUsers", cascade = CascadeType.ALL)
	public List<User> followers;

	/** the comments */
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	public List<Comment> comments;

	
	/** insights that has been shared with this user */
	@OneToMany(mappedBy = "toUser",  cascade = CascadeType.ALL)
	public List<InsightShare> shared;
	
	public User(String email, String userName, String password) {
		if (!User.isUsernameAvailable(userName)) {
			throw new RuntimeException(Messages.get("registerusernameexist"));
		}
		if (!User.isEmailAvailable(email)) {
			throw new RuntimeException(Messages.get("registeremailexist"));
		}
		Logger.info("New User: " + userName);
		this.email = email;
		this.password = Crypto.passwordHash(password);
		this.userName = userName;
		
		this.emailConfirmed = false;
		this.uuid = Codec.UUID();
		
		String lang = Lang.get();
		// if no language, then english
		if( lang == null || lang.equals("") ) { lang = "en"; }

		Language language = Language.findByLabelOrCreate(lang);

		this.uiLanguage = language;
		this.writtingLanguage = language;
		
		this.votes = new ArrayList<Vote>();
		this.createdInsights = new ArrayList<Insight>();
		this.followedInsights = new ArrayList<Insight>();
		this.followedUsers = new ArrayList<User>();
		this.crdate = new Date();
		
		this.shared = new ArrayList<InsightShare>();
		
		this.score = 0;
		this.categoryScores = new ArrayList<UserCategoryScore>();
		
	}

	public String toString() {
		return userName;
	}
	
	/**
	 * Return the Best users (comparing their scores)
	 */
	public static List<User> findBest(int from, int number) {
		return User.find("order by crdate DESC").from( from ).fetch( number );
	}

	public void setUiLanguage(Language language) {
		if(language == null) {
			language = Language.findByLabelOrCreate("en");
		}
		this.uiLanguage = language;
	}

	public void setWrittingLanguage(Language language) {
		if(language == null) {
			language = Language.findByLabelOrCreate("en");
		}
		this.writtingLanguage = language;
	}
	
	public static boolean isUsernameAvailable(String userName) {
		if (User.count("byUserName", userName) == 0) {
			return true;
		}
		return false;
	}
	
	public static boolean isEmailAvailable(String email) {
		if (User.count("byEmail", email) == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Return true if the given email / password is valid
	 * 
	 * @param email
	 * @param password
	 * @return true if authenticated, false otherwise
	 */
	public static boolean authenticate(String email, String password) {
		User user = find("email=? and password=?", email,
				Crypto.passwordHash(password)).first();
		if (user != null) {
			return true;
		}

		return false;
	}

	
	public Date getCrdate() {
		return crdate;
	}

	
	public void updateAvatar(File originalImage, boolean replaceAvatarUnchanged) throws FileNotFoundException {
		if (replaceAvatarUnchanged == true) {
			this.avatarUnchanged.set(new FileInputStream(originalImage), "Image");
		}
		// Default is we resize the originalImage without any modification.
		// Can be cropped later if necessary since we keep the original
		File smallImageTmp = ImageHelper.resizeRespectingRatio(originalImage, 26, 26);
		this.avatarSmall.set(new FileInputStream(smallImageTmp),
					"Image");
		File mediumImageTmp = ImageHelper.resizeRespectingRatio(originalImage, 46, 46);
		this.avatarMedium.set(new FileInputStream(mediumImageTmp),
					"Image");
		File largeImageTmp = ImageHelper.resizeRespectingRatio(originalImage, 94, 94);
		this.avatarLarge.set(new FileInputStream(largeImageTmp),
					"Image");
		this.save();
		originalImage.deleteOnExit();
		smallImageTmp.deleteOnExit();
		mediumImageTmp.deleteOnExit();
		largeImageTmp.deleteOnExit();
	}
	
	/**
	 * Static method to get a User instance given his username
	 * 
	 * @param userName
	 * @return
	 */
	public static User findByUserName(String userName) {
		return find("userName = ?", userName).first();
	}

	/**
	 * Static method to get a User instance given his username
	 * 
	 * @param userName
	 * @return
	 */
	public static User findByEmail(String email) {
		return find("email = ?", email).first();
	}
	   
	/**
     * Static method to get a User instance given his twitterUserId
     * 
     * @param userName
     * @return
     */
    public static User findByTwitterUserId(String twitterUserId) {
        return find("twitterUserId = ?", twitterUserId).first();
    }
	
    /**
     * Static method to get a User instance given his facebookUserId
     * 
     * @param userName
     * @return
     */
    public static User findByFacebookUserId(Long facebookUserId) {
        return find("facebookUserId = ?", facebookUserId).first();
    }
    

	/**
	 * Call this method to create a new insight that will be automatically owned
	 * by the current user.
	 * 
	 * @param insightContent : the content text of this insight
	 * @param endDate : date this insight should end
	 * @param tagLabelList : a comma separated list of tags
	 */
	public Insight createInsight(String insightContent, Date endDate, String tagLabelList, long cateopryId, String lang) {
		if(lang == null) { // if lang is not specified, use the language from the user's preferred insight language
			lang = this.writtingLanguage.label;
		}
		
		Category category = Category.findById(cateopryId);
		// TODO exception if null
		
		Language language = Language.findByLabelOrCreate(lang);

		Insight i = new Insight(this, insightContent, endDate, category, language);
		i.save();

		i.addTags(tagLabelList, this);

		this.createdInsights.add(i);
		// agree with this insight
		try {
			this.voteToInsight(i.uniqueId, State.AGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			
		}

		this.save();

		return i;
	}

	/**
	 * Call this method to set a vote for one insight for the current user.
	 * 
	 * @param insightId
	 *            : id of the insight user is voting for.
	 * @param voteState
	 *            State.AGREE or State.DISAGREE
	 */
	public void voteToInsight(String insightUniqueId, State voteState) throws CannotVoteTwiceForTheSameInsightException {
		Insight insight = Insight.findByUniqueId(insightUniqueId);

		boolean change = false;
		Vote vote = Vote.findLastVoteByUserAndInsight(this.id, insight.uniqueId);
		if (vote != null) {
			// there are only idiots who do not change their minds
			if (vote.state.equals(voteState)) {
				// voting twice for the same side ...
				throw new CannotVoteTwiceForTheSameInsightException();
			} else {
				change = true;
				// historized the current vote
				vote.status = Status.HISTORIZED;
				vote.save();
				// and create a new one
				Vote newVote = new Vote(this, insight, voteState);
				newVote.save();
				// if we change the side of the vote we increment the new
				// vote side and decrement the previous side
				if (voteState.equals(State.AGREE)) {
					insight.agreeCount++;
					insight.disagreeCount--;
				} else {
					insight.agreeCount--;
					insight.disagreeCount++;
				}
				insight.lastUpdated = new Date();
				insight.save();
			}
		} else {
			// First time voting for this insight
			vote = new Vote(this, insight, voteState);
			vote.save();
			if (voteState.equals(State.AGREE)) {
				insight.agreeCount++;
			} else {
				insight.disagreeCount++;
			}
			insight.lastUpdated = new Date();
			insight.save();
			
			// create an activity around this user / insight relation
			InsightActivity activity = new InsightActivity(this, insight);
			activity.save();
		}
		
		// the user has seen this insight (remove it from shared ones)
		this.readInsight(insight);
		
		// update the activities around this insight
		List<InsightActivity> activities = InsightActivity.find("insight = ? and user != ?", insight, this).fetch();
		for( InsightActivity activity : activities ) {
			activity.notEmpty = true;
			activity.updated = new Date();
			if(change) {
				activity.voteChangeCount++;
			} else {
				if(voteState.equals(State.AGREE)) {
					activity.newAgreeCount++;
				} else {
					activity.newDisagreeCount++;					
				}
			}
			activity.save();
		}

	}

	/**
	 * Tells if the given insight is already in the current user followed
	 * insight list.
	 * 
	 * @param insight
	 * @return
	 */
	public boolean isFollowingInsight(Insight insight) {
		if (followedInsights.contains(insight)) {
			return true;
		}
		return false;
	}

	/**
	 * Call this method to add the given insight in the current user followed
	 * insight list
	 * 
	 * @param insightId
	 */
	public void startFollowingThisInsight(Long insightId)
			throws UserIsAlreadyFollowingInsightException {
		Insight insight = Insight.findById(insightId);

		// If we are already following the insight throw a business exception
		if (isFollowingInsight(insight) == true) {
			throw new UserIsAlreadyFollowingInsightException();
		}

		followedInsights.add(insight);
		save();
		insight.followers.add(this);
		insight.save();
		
		// update the activities around this insight
		List<InsightActivity> activities = InsightActivity.find("insight = ? and user != ?", insight, this).fetch();
		for( InsightActivity activity : activities ) {
			activity.notEmpty = true;
			activity.updated = new Date();
			activity.newFavoriteCount++;
			activity.save();
		}

	}

	/**
	 * 
	 * @param insightId
	 */
	public void stopFollowingThisInsight(Long insightId) {
		Insight insight = Insight.findById(insightId);

		// If we were not following the insight just do nothing ...
		if (isFollowingInsight(insight) == false) {
			return;
		}

		followedInsights.remove(insight);
		save();
		insight.followers.remove(this);
		insight.save();
	}

	/**
	 * is this user following the given user
	 * 
	 * @param user
	 *            to check
	 */
	public boolean isFollowingUser(User user) {
		if (followedUsers.contains(user)) {
			return true;
		}
		return false;
	}

	/**
	 * This user start following the given user
	 * 
	 * @param user
	 *            : User to follow
	 */
	public void startFollowingThisUser(User user) {
		if (isFollowingUser(user) == true) {
			return;
		}
		followedUsers.add(user);
		save();
		user.followers.add(this);
		user.save();
	}

	/**
	 * This user stops following the given user
	 * 
	 * @param insightId
	 */
	public void stopFollowingThisUser(User user) {
		if (isFollowingUser(user) == false) {
			return;
		}
		followedUsers.remove(user);
		save();
		user.followers.remove(this);
		user.save();
	}

	public void computeScores() {
		this.computeUserScore();
		for (Category category : Category.getAllCategories()) {
			this.computeCategoryScore(category);
		}
	}

	public void computeUserScore() {
		// TODO: insert here the score computation algorithm
		this.score = Math.random();
	}

	public void computeCategoryScore(Category category) {
		// look if this user has a score for this category
		boolean newCategory = true;
		for (UserCategoryScore userCatScore : categoryScores) {
			if (userCatScore.category == category) {
				newCategory = false;
				userCatScore.score = Math.random();
				// TODO: insert here the score computation algorithm for a given
				// category
			}
		}
		// if not, create the link between user and category
		if (newCategory) {
			UserCategoryScore newUserCatScore = new UserCategoryScore(this,
					category);
			newUserCatScore.score = Math.random();
			categoryScores.add(newUserCatScore);
		}
	}

	/**
	 * get the list of the n last insights of this User (insights he voted for)
	 * 
	 * @param n : the maximum number of votes to return
	 * @return: the list of n most recent active insights of this user
	 */
	public List<Insight> getLastInsights(int n) {
		return Vote.find(
				"select i from Insight i " + "join i.votes v "
						+ "join v.user u "
						+ "where v.status = :status and u.id=:userId "
						+ "order by v.creationDate DESC").bind("status",
				Status.ACTIVE).bind("userId", this.id).fetch(n);
	}
	
	/**
	 * get the list of most relevant InsightActivity around the user (for now, they are only the most recent) 
	 * @param n : maximum number of item to return 
	 */
	public List<InsightActivity> getInsightActivity(int n) {
		return InsightActivity.find("user = ? and notEmpty is true order by updated DESC", this).fetch(n);
	}
	
	/**
	 * Reset the stored recorded insight activity for a given user
	 */
	public void resetInsightActivity() {
		List<InsightActivity> activities = InsightActivity.find("user = ? and notEmpty is true order by updated DESC", this).fetch();
		for( InsightActivity activity : activities ) {
			activity.notEmpty = false;
			activity.updated = new Date();
			activity.newFavoriteCount 	= 0;
			activity.newAgreeCount 		= 0;
			activity.newDisagreeCount 	= 0;
			activity.voteChangeCount 	= 0;
			activity.save();
		}
	}
	
	/**
	 * the user reads this insight (if it has been shared, remove it from shared insights)
	 */
	public void readInsight(Insight insight) {
		InsightShare insightShare = InsightShare.findByUserAndInsight(this, insight);
		if(insightShare != null) {
			insightShare.hasBeenRead = true;
			insightShare.save();
		}
	}

	public boolean invite(String email, String message) {
		if(invitationsLeft != 0) {
			// Create a promocode
			String uuid = Codec.UUID();
			try {
				Promocode code = new Promocode(uuid, 1, (new SimpleDateFormat("yyyy/MM/dd")).parse("2012/12/31") );
				code.save();
			} catch (ParseException e) {
				Logger.error("Cannot create promocode");
				return false;
			}
			
			// Create the invitation
			Invitation invitation = new Invitation(this, email, message, uuid);
			invitation.save();

			// create the task for mail sending
			InvitationMailTask task = new InvitationMailTask(invitation, this.uiLanguage.label);
			task.save();
			
			invitationsLeft--;
			save();
			return true;
		}
		return false;
	}
	
	/**
	 * Share an insight with another beansight's user
	 * @param toUser : the user to send the insight to
	 * @param insight : the insight to share
	 */
	public boolean shareInsight(User toUser, Insight insight) throws NotFollowingUserException, InsightAlreadySharedException {
		// check you are following the user
		if( !this.isFollowingUser(toUser) ) {
			throw new NotFollowingUserException();
		}
		
		// check if you haven't send it already
		long shared = InsightShare.count("fromUser = ? and toUser = ? and insight = ?", this, toUser, insight);
		if(shared != 0) {
			throw new InsightAlreadySharedException();
		}
		
		InsightShare share = new InsightShare(this, toUser, insight);
		share.save();
		
		this.shared.add(share);
		this.save();

		return true;
	}
	
	public void addInvitations(long invitationNumber) {
		this.invitationsLeft += invitationNumber;
		save();
	}

	/**
	 * Return the insights that have been shared to this user.
	 * @param number : maximum number to return
	 */
	public List<Insight> getSharedInsights(int number) {
		List<InsightShare> shares = InsightShare.findSharedToUser(this, number);
		List<Insight> sharedInsights = new ArrayList<Insight>();
		for(InsightShare share : shares) {
			sharedInsights.add(share.insight);
		}
		return sharedInsights;
	}

	public InsightResult getSuggestedInsights(int from, int number, Filter filter) {
		// This is totally temporary.
		InsightResult result = Insight.findLatest(from, number, filter);
		
		List<Insight> sharedInsights = this.getSharedInsights(NUMBER_SHAREDINSIGHTS_SUGGEDTEDINSIGHTS);
		sharedInsights.addAll(result.results);
		result.results = sharedInsights;
		
		return result;
	}
	
	public boolean sendMessage(User user, String content) {
		Message message = new Message(this, user, content);
		message.save();

		MessageMailTask task = new MessageMailTask(message);
		task.save();
		
		return true;
	}
	
	/** change this user's password */
	public void changePassword(String newPassword) {
		this.password = Crypto.passwordHash(newPassword);
		this.save();
	}
	
	public static UserResult search(String userNameQuery, int from, int pageSize) {
		Query q = Search.search("userName:" + userNameQuery + "*" , User.class);
		q.page(from, pageSize);
		List<User> usersResult = q.fetch();
		UserResult userResult = new UserResult(usersResult, usersResult.size());
		
		return userResult;
	}
	
	public static class UserResult {
		/** users search's results */
		public List<User> results;
		/** the total number of results */
		public long count;
		
		public UserResult(List<User> results, long count) {
			super();
			this.results = results;
			this.count = count;
		}
	}

}
