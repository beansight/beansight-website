package models;

import helpers.ImageHelper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import models.Vote.State;
import models.Vote.Status;
import models.oauthclient.Credentials;
import play.Logger;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.FileAttachment;
import play.db.jpa.Model;
import play.i18n.Lang;
import play.libs.Codec;
import play.libs.Crypto;
import play.modules.search.Field;
import play.modules.search.Indexed;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.UserIsAlreadyFollowingInsightException;

@Entity
@Indexed
public class User extends Model {

	@Field
	public String userName;
	@Field
	public String firstName;
	@Field
	public String lastName;
	public String password;
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
	
	
	/** Language the user wants his UI to be displayed in */
	public String uiLanguage;
	/** Language the user is writing insights in */
	public String writtingLanguage;
	
	/** How many invitations this user can send, -1 for infinity*/
	public long invitationsLeft;
	
	// use the @Embedded annotation to store avatars in the database
	public FileAttachment avatar;

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

	public User(String email, String userName, String password) {
		Logger.info("New User: " + userName);
		this.email = email;
		this.password = Crypto.passwordHash(password);
		this.userName = userName;
		
		this.emailConfirmed = false;
		this.uuid = Codec.UUID();
		
		String lang = Lang.get();
		if( lang == null || lang.equals("") ) { lang = "en"; }
		this.uiLanguage = lang;
		this.writtingLanguage = lang;
		
		this.votes = new ArrayList<Vote>();
		this.createdInsights = new ArrayList<Insight>();
		this.followedInsights = new ArrayList<Insight>();
		this.followedUsers = new ArrayList<User>();
		this.crdate = new Date();

		this.score = 0;
		this.categoryScores = new ArrayList<UserCategoryScore>();
		
	}

	public String toString() {
		return userName;
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

	
	public void updateAvatar(File originalImage) {
		File originalImageCopy = new File(FileAttachment.getStore(),
				"originalImage_" + this.id);
		originalImage.renameTo(originalImageCopy);
		// Default is we resize the originalImage without any modification.
		// Can be cropped later if necessary since we keep the original
		File resizedOriginalImage = new File(Play.getFile("tmp") + "/resizedOriginalImageTmp_" + this.id);
		ImageHelper.resizeRespectingRatio(originalImageCopy, resizedOriginalImage, 60, 60);
		this.avatar.set(resizedOriginalImage);
		this.saveAttachment();
		resizedOriginalImage.deleteOnExit();
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
			lang = this.writtingLanguage;
		}
		
		Category category = Category.findById(cateopryId);
		// TODO exception if null

		Insight i = new Insight(this, insightContent, endDate, category, lang);
		i.save();
		i.addTags(tagLabelList, this);

		this.createdInsights.add(i);
		// agree with this insight
		try {
			this.voteToInsight(i.id, State.AGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			e.printStackTrace();
		}

		// store the given language as the default language for the user
		this.writtingLanguage = lang;
		
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
	public void voteToInsight(Long insightId, State voteState) throws CannotVoteTwiceForTheSameInsightException {
		Insight insight = Insight.findById(insightId);

		boolean change = false;
		Vote vote = Vote.findLastVoteByUserAndInsight(this.id, insightId);
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
			InvitationMailTask task = new InvitationMailTask(invitation);
			task.save();
			
			invitationsLeft--;
			save();
			return true;
		}
		return false;
	}
	
	public void addInvitations(long invitationNumber) {
		this.invitationsLeft += invitationNumber;
		save();
	}

	public boolean sendMessage(User user, String content) {
		Message message = new Message(this, user, content);
		message.save();

		MessageMailTask task = new MessageMailTask(message);
		task.save();
		
		return true;
	}
	
}
