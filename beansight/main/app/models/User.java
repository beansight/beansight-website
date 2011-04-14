package models;

import helpers.ImageHelper;
import helpers.UserCount;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import models.Insight.InsightResult;
import models.Vote.State;
import models.Vote.Status;
import models.analytics.UserClientInfo;
import models.analytics.UserExpertVisit;
import models.analytics.UserInsightDailyCreation;
import models.analytics.UserInsightDailyVote;
import models.analytics.UserInsightSearchVisit;
import models.analytics.UserInsightVisit;
import models.analytics.UserListExpertsVisit;
import models.analytics.UserListInsightsVisit;
import models.analytics.UserPromocodeCampaign;
import notifiers.Mails;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.annotations.Index;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import play.Logger;
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
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.InsightAlreadySharedException;
import exceptions.InsightWithSameUniqueIdAndEndDateAlreadyExistsException;
import exceptions.InvitationException;
import exceptions.NotFollowingUserException;
import exceptions.UserIsAlreadyFollowingInsightException;

@Entity
@Indexed
public class User extends Model {

	public static final int NUMBER_SHAREDINSIGHTS_SUGGEDTEDINSIGHTS = 2;

	public static final int DESCRIPTION_MAXLENGTH = 120;
	public static final int REALNAME_MAXLENGTH = 30;
	public static final long INVITATION_NUMBER = 5;
	
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
	
//	public String oauthToken;
//	public String oauthSecret;

	/** facebook specific informations */
    public Long facebookUserId;
    public String facebookScreenName;
    
    /** true if the user account creation process went well to the end */
    public boolean isPromocodeValidated;
    
	@Lob
    public String description;
	
	/** Language the user wants his UI to be displayed in */
    @ManyToOne(fetch=FetchType.LAZY)
	public Language uiLanguage;
	/** Main language of this user: insights of this language are displayed in the timeline and when he creates an insight, this one is by default */
    @ManyToOne(fetch=FetchType.LAZY)
	public Language writtingLanguage;
	/** second language of this user (null if none): insights in this language will also be displayed in his timeline */
    @ManyToOne(fetch=FetchType.LAZY)
	public Language secondWrittingLanguage;
    
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
	public Double score;
	
	/** the last time this user's score has been computed */
	public Date lastScoreUpdate;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@OrderBy("scoreDate DESC")
	public List<UserScoreHistoric> userScoreHistorizedList;
	
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
	
	// Settings
	/** Should this user receive a mail when another user is following him */
	public boolean followMail;
	/** Should this user receive a mail when he receives a private message */
	public boolean messageMail;
	/** Should this user receive a mail when another user comments on an insight he created */
	public boolean commentCreatedMail;
	/** Should this user receive a mail when another user comments on an insight he added to his favorites */
	public boolean commentFavoriteMail;
	/** Should this user receive a mail when another user comments an insight he commented on */
	public boolean commentCommentMail;
	/** Should this user receive a mail when another user mentions him in a comment */
	public boolean commentMentionMail;
	/** Should this user receive a mail when another usershare an insight with him */
	public boolean insightShareMail;
	
	/** Should this user receive a mail every week of upcoming events */
	public boolean upcomingNewsletter;
	/** Should this user receive a mail every week of prediction statuses */
	public boolean statusNewsletter;
	
	
	public User(String email, String userName, String password) {
		if (!User.isUsernameAvailable(userName)) {
			throw new RuntimeException(Messages.get("registerusernameexist"));
		}
		if (email != null && !email.equals("") && !User.isEmailAvailable(email)) {
			throw new RuntimeException(Messages.get("registeremailexist"));
		}
		Logger.info("New User: " + userName);
		this.email = email;
		this.password = Crypto.passwordHash(password);
		this.userName = userName;
		
		this.emailConfirmed = false;
		this.isPromocodeValidated = false;
		this.uuid = Codec.UUID();
		
		String lang = Lang.get();
		// if no language, then english
		if( lang == null || lang.equals("") ) { lang = "en"; }

		Language language = Language.findByLabelOrCreate(lang);

		this.uiLanguage = language;
		this.writtingLanguage = language;
		this.secondWrittingLanguage = null;
		
		this.votes = new ArrayList<Vote>();
		this.createdInsights = new ArrayList<Insight>();
		this.followedInsights = new ArrayList<Insight>();
		this.followedUsers = new ArrayList<User>();
		this.crdate = new Date();
		
		this.shared = new ArrayList<InsightShare>();
		
		this.score = null;
		this.userScoreHistorizedList = new ArrayList<UserScoreHistoric>();
		
		this.followMail = true;
		this.messageMail = true;
		this.commentCreatedMail = true;
		this.commentFavoriteMail = true;
		this.commentCommentMail = true;
		this.commentMentionMail = true;
		this.insightShareMail = true;
		
		this.upcomingNewsletter = true;
		this.statusNewsletter = true;
		
		this.invitationsLeft = INVITATION_NUMBER;
	}

	public String toString() {
		return userName;
	}
	
	/**
	 * Return the Best users (comparing their scores)
	 */
	public static List<User> findBest(int from, int number) {
		return User.find("score is not null order by score DESC").from( from ).fetch( number );
	}

	public void setUserName(String userName) {
		if( User.isUsernameAvailable(userName) ) {
			this.userName = userName;
		}
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
		if (userName != null && !userName.trim().equals("") && User.count("byUserNameLike", userName.toLowerCase()) == 0) {
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
//		User user = Cache.get(userName, User.class);
//		if (user == null) {
//			user = find("userName = ?", userName).first();
//			Cache.set(userName, user, "2s");
//		}
//		return user;
		return find("userName = ?", userName).first();
	}

	/**
	 * Static method to get a User instance given his username
	 * 
	 * @param userName
	 * @return
	 */
	public static User findByEmail(String email) {
//		User user = Cache.get(email, User.class);
//		if (user == null) {
//			user = find("email = ?", email).first();
//			Cache.set(email, user, "2s");
//		}
//		return user;
		return find("email = ?", email).first();
	}
	   
	/**
     * Static method to get a User instance given his twitterUserId
     * 
     * @param userName
     * @return
     */
    public static User findByTwitterUserId(String twitterUserId) {
//		User user = Cache.get(twitterUserId, User.class);
//		if (user == null) {
//			user = find("twitterUserId = ?", twitterUserId).first();
//			Cache.set(twitterUserId, user, "2s");
//		}
//		return user;
    	return find("twitterUserId = ?", twitterUserId).first();
    }
	
    /**
     * Static method to get a User instance given his facebookUserId
     * 
     * @param userName
     * @return
     */
    public static User findByFacebookUserId(Long facebookUserId) {
//		User user = Cache.get(facebookUserId.toString(), User.class);
//		if (user == null) {
//			user = find("facebookUserId = ?", facebookUserId).first();
//			Cache.set(facebookUserId.toString(), user, "2s");
//		}
//		return user;
    	return find("facebookUserId = ?", facebookUserId).first();
    }
    

	/**
	 * Call this method to create a new insight that will be automatically owned
	 * by the current user.
	 * 
	 * @param insightContent : the content text of this insight
	 * @param endDate : date this insight should end
	 * @param tagLabelList : a comma separated list of tags
	 * @param voteState : the state of the vote, null if no vote.
	 * @throws InsightWithSameUniqueIdAndEndDateAlreadyExistsException 
	 */
	public Insight createInsight(String insightContent, Date endDate, String tagLabelList, long categoryId, String lang, State voteState) throws InsightWithSameUniqueIdAndEndDateAlreadyExistsException {
		if(lang == null) { // if lang is not specified, use the language from the user's preferred insight language
			lang = this.writtingLanguage.label;
		}
		
		Category category = Category.findById(categoryId);
		if (category == null) {
			throw new RuntimeException("Category with id" + categoryId + " doesn't exist.");
		}
		
		Language language = Language.findByLabelOrCreate(lang);

		Insight i = new Insight(this, insightContent, endDate, category, language);
		i.save();

		i.addTags(tagLabelList, this);

		this.createdInsights.add(i);

		if(voteState != null) {
			try {
				this.voteToInsight(i.uniqueId, voteState);
			} catch (CannotVoteTwiceForTheSameInsightException e) {
				// do nothing
			}
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
				activity.incrementVoteChangeCount();
			} else {
				if(voteState.equals(State.AGREE)) {
					activity.incrementNewAgreeCount();
				} else {
					activity.incrementNewDisagreeCount();					
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
		Long count = Insight.find("select count(i) from Insight i join i.followers u where u=:user and i=:insight").bind("user", this).bind("insight", insight).first();
//		if (followedInsights.contains(insight)) {
		if (count > 0) {
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
			activity.incrementNewFavoriteCount();
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
		Long count = find("select count(u) from User u join u.followers f where u=:user and f=:followed").bind("user", user).bind("followed", this).first();
		if (count > 0) {
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
		
		// if the user accepts it, send a mail
		if(this.followMail) {
			FollowNotificationTask mail = new FollowNotificationTask(this, user);
			mail.save();
		}
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

	public void computeCategoryScores(Date computeDate, PeriodEnum period) {
		for (Category category : Category.getAllCategories()) {
			this.computeCategoryScore(category, computeDate, period);
		}
	}

	/**
	 * compute the global score for this user
	 */
	public void computeUserScore(Date computeDate, PeriodEnum period) {
//		UserScoreHistorized userScoreHistorized = UserScoreHistorized.find("scoreDate=:scoreDate and user=:user").bind("scoreDate", computeDate).bind("user", this).first();
//		List<UserCategoryScore> categoryScores = userScoreHistorized.categoryScores;
		List<UserCategoryScore> categoryScores = UserCategoryScore.find("historic.scoreDate=:scoreDate and historic.user=:user and period=:period").bind("scoreDate", computeDate).bind("user", this).bind("period", period).fetch();
		Double score = null;
		for(UserCategoryScore catScore : categoryScores) {
			if (catScore.score != null) {
				if (score != null) {
					score += catScore.score;
				} else {
					score = catScore.score;
				}
			}
			
		}
		this.score = score;
		this.lastScoreUpdate = new Date();
		this.save();
	}

	/**
	 * Computes the score of this user is the given category (based on the score this user has on all insights in this category)
	 * @param category
	 */
	public void computeCategoryScore(Category category, Date computeDate, PeriodEnum period) {
		UserScoreHistoric userScoreHistorized = UserScoreHistoric.find("scoreDate=:scoreDate and user=:user").bind("scoreDate", computeDate).bind("user", this).first();
//		UserScoreHistorized userScoreHistorized = UserScoreHistorized.find("select h from UserScoreHistorized h " +
//				"join h.categoryScores cs " +
//				"where cs.period = :period and h.scoreDate=:scoreDate and h.user=:user")
//				.bind("period", period).bind("scoreDate", computeDate).bind("user", this).first();
		if (userScoreHistorized==null) {
			userScoreHistorized = new UserScoreHistoric(computeDate, this);
			userScoreHistorized.save();
		}
//		List<UserCategoryScore> categoryScores = userScoreHistorized.categoryScores;
		List<UserCategoryScore> categoryScores = UserCategoryScore.find("select cs from UserCategoryScore cs " +
				"where cs.historic.scoreDate = :scoreDate and cs.historic.user = :user and cs.period = :period")
				.bind("scoreDate", computeDate)
				.bind("user", this)
				.bind("period", period).fetch();
		
		UserCategoryScore catScore = null;
		
		// look if this user has a score for this category
		boolean newCategory = true;
		for (UserCategoryScore userCatScore : categoryScores) {
			if (userCatScore.category == category) {
				newCategory = false;
				catScore = userCatScore;
			}
		}
		if (newCategory) { // if not, create the link between user and category
			catScore = new UserCategoryScore(this, category, userScoreHistorized, period);
		}

		// compute the score for this category :
		Double score = null;
		List<UserInsightScore> insightScores;
		// select all userInsightScore that are in the date range for the score computation
		Date fromDate = new Date(computeDate.getTime() - period.getTimePeriod());
		insightScores = UserInsightScore.find("select i from UserInsightScore i "
				+"where i.user=:usertraite and i.insight.endDate between :fromDate and :toDate "
				+ "and i.insight.category=:cattraite ")
				.bind("fromDate",fromDate)
				.bind("toDate",computeDate)
				.bind("usertraite",this)
				.bind("cattraite", category).fetch();
		for(UserInsightScore insightScore : insightScores){
			if (insightScore.score != null) {
				if (score == null) {
					score = 0d;
				}
				score += insightScore.score;
			}
		}

		catScore.score=score;
		catScore.lastupdate = new Date();
		
		// check if the user has become the best or worst in this category
		if (score != null) {
			if (score > category.scoreMax) {
				category.scoreMax = score;
				category.save();
				category.computeAllNormalizedScores();
			} else if (score < category.scoreMin) {
				category.scoreMin = score;
				category.save();
				category.computeAllNormalizedScores();
			}
		}
		
		catScore.computeNormalizedScore();

		userScoreHistorized.save();
		catScore.save();
		this.save();
	}

	/**
	 * get the list of the n last insights of this User (insights he voted for)
	 * 
	 * @param n : the maximum number of votes to return
	 * @return: the list of n most recent active insights of this user
	 */
	public InsightResult getLastInsights(int from, int number, UserInsightsFilter filter) {
		InsightResult result = new InsightResult();
		result.results = Vote.find(
				"select i from Insight i " 
				+ filter.generateJPAQueryFromClause()
				+ "where "
				+ filter.generateJPAQueryWhereClause()
				+ filter.generateJPAQueryOrderByClause())
				.from(from).fetch(number);
		return result;
	}
	
	public List<Vote> getVotesToInsight(Insight insight) {
		return User.find(
				"select v from Vote v "
						+ "where v.user=:userId and v.insight=:insightId "
						+ "and v.creationDate < :date "
						+ "order by v.creationDate ASC").bind("userId", this).bind("insightId", 
					insight).bind("date",insight.endDate).fetch();
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
		List<InsightActivity> activities = InsightActivity.find("user = ? and notEmpty is true order by totalCount DESC", this).fetch();
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
	
	/**
	 * This user visits the page of this insight
	 * @param insight : the insight
	 * @param ip : the current IP of this connected user
	 * @param userAgent : the current user-agent of this connected user
	 * @param application : the application id of this connected user
	 */
	public void visitInsight(Insight insight, UserClientInfo userClientInfo) {
		UserInsightVisit visit = new UserInsightVisit(new Date(), this, userClientInfo, insight);
		visit.save();
	}

	/**
	 * This user visits the insights list page
	 * @param ip : the current IP of this connected user
	 * @param userAgent : the current user-agent of this connected user
	 * @param application : the application id of this connected user
	 */
	public void visitInsightsList(UserClientInfo userClientInfo) {
		UserListInsightsVisit visit = new UserListInsightsVisit(new Date(), this, userClientInfo);
		visit.save();
	}
	
	/**
	 * This user visits the profil's page of this expert
	 * @param expert : the expert
	 * @param ip : the current IP of this connected user
	 * @param userAgent : the current user-agent of this connected user
	 * @param application : the application id of this connected user
	 */
	public void visitExpert(User expert, UserClientInfo userClientInfo) {
		UserExpertVisit visit = new UserExpertVisit(new Date(), this, userClientInfo, expert);
		visit.save();
	}
	
	/**
	 * This user visits the experts list page
	 * @param ip : the current IP of this connected user
	 * @param userAgent : the current user-agent of this connected user
	 * @param application : the application id of this connected user
	 */
	public void visitExpertsList(UserClientInfo userClientInfo) {
		UserListExpertsVisit visit = new UserListExpertsVisit(new Date(), this, userClientInfo);
		visit.save();
	}

	/**
	 * This user searchs for insight
	 * @param ip : the current IP of this connected user
	 * @param userAgent : the current user-agent of this connected user
	 * @param application : the application id of this connected user
	 * @param searchKeyWords : the key words used to search insights
	 */
	public void visitInsightsSearch(String searchKeyWords, UserClientInfo userClientInfo) {
		UserInsightSearchVisit visit = new UserInsightSearchVisit(new Date(), this, userClientInfo, searchKeyWords);
		visit.save();
	}
	
	/**
	 * recording which promocode a user have used to get an account will give us analytics
	 * about the efficiency of each campaign
	 * @param userClientInfo
	 * @param promocode
	 */
	public void recordPromocodeUsedToCreateAccount(UserClientInfo userClientInfo, Promocode promocode) {
		UserPromocodeCampaign visit = new UserPromocodeCampaign(new Date(), this, userClientInfo, promocode);
		visit.save();
	}
	
	public void invite(String email, String message) throws InvitationException {
		if(invitationsLeft != 0) {
			// Create the invitation
			Invitation invitation = new Invitation(this, email, message);
			invitation.save();

			// create the task for mail sending
			InvitationMailTask task = new InvitationMailTask(invitation, this.uiLanguage.label);
			task.save();
			
			invitationsLeft--;
			save();
		} else {
			throw new InvitationException("You don't have any available invitation");
		}
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

		if(toUser.insightShareMail) {
			InsightShareMailTask mailTask = new InsightShareMailTask(share);
			mailTask.save();
		}
		
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

	/**
	 * @param user : the user this message should be sent to
	 * @param content : text content of this message
	 * @return
	 */
	public boolean sendMessage(User user, String content) {
		Message message = new Message(this, user, content);
		message.save();

		// send a mail if this user accepts it
		if(this.messageMail) {
			MessageMailTask task = new MessageMailTask(message);
			task.save();
		}
		
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
	
	/**
	 * override default behavior to remove from results navigation the hidden insights
	 * @return
	 */
	public List<Insight> getNotHiddenFollowedInsights() {
		List<Insight> fInsights = Insight.find("select i from User u join u.followedInsights i where u.id = ? and i.hidden is false", this.id).fetch();
		return fInsights;
	}
	
	public String avatarHashCode() {
		if (avatarSmall != null) {
			return String.valueOf(avatarSmall.hashCode());
		} 
		return "0";
	}
	
//	public TokenPair getTokenPair() {
//		return new TokenPair(this.oauthToken, this.oauthSecret);
//	}
	
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

//	public void setTokenPair(TokenPair tokens) {
//		this.oauthSecret = tokens.secret;
//		this.oauthToken = tokens.token;
//	}
	
	/**
	 * @return the score of this user for this insight
	 */
//	public UserInsightScore getInsightScore(Insight insight, Date computeDate, PeriodEnum period) {
	public UserInsightScore getInsightScore(Insight insight) {
//		return UserInsightScore.find("select i from UserInsightScore i " +
//				"where i.historic.scoreDate = :computeDate and i.insight = :insight and i.historic.user=:usertraite and i.period = :period")
//				.bind("computeDate", computeDate)
//				.bind("insight", insight)
//				.bind("usertraite",this)
//				.bind("period", period).first();
		return UserInsightScore.find("select i from UserInsightScore i "
				+"where i.user=:usertraite "
				+ "and i.insight=:insighttraite").bind("usertraite",this).bind("insighttraite", insight).first();
	}
	
	/**
	 * @return list of users which score needs to be updated because it changed since the given "since" date 
	 */
	public static List<User> findUsersToUpdateScore(Date since) {
		return User.find("select distinct i.user from UserInsightScore i where i.lastUpdate > ?", since).fetch();
	}
	
    public static String createNewAvailableUserName(String firstName) {
    	int firstNameMaxSize = 14;
    	
    	String userName = firstName.replace(" ", "").replace("-", "");
    	
    	if (userName.length() < firstNameMaxSize) {
    		firstNameMaxSize = userName.length();
    	}
    	
    	userName = userName.substring(0, firstNameMaxSize);
    	
    	for (int i=1; i<100; i++) {
	    	if (User.isUsernameAvailable(userName)) {
	    		return userName;
	    	} else {
	    		userName = userName + i;
	    	}
    	}
    	
    	// This should never happen but like that wee still return a string
    	return RandomStringUtils.randomAlphabetic(10);
    }

	/**
	 * every users that didn't have validated their account with a promocode 
	 * before the provided date will be deleted
	 * 
	 * @param date
	 * @param delete : si false ne supprime pas
	 */
	public static List<User> removeCreatedAccountWithNoInvitationBefore(Date date, boolean delete) {
		List<User> results = new ArrayList<User>();
		List<User> users = User.find("select u from User u where u.isPromocodeValidated is false and u.crdate < ?", date).fetch();
    	for (User user : users) {
    		long count = UserListInsightsVisit.count("user = ?", user);
    		if (count == 0) {
    			// if the user has been visited in expert profil first we need to delete the visit on him
    			long expertVisitCount = UserExpertVisit.count("expert = ?", user);
    			if (expertVisitCount > 0) {
    				UserExpertVisit.delete("expert = ?", user);
    			}
	    		if (user.email != null && !user.email.trim().equals("") && WaitingEmail.count("email = ?", user.email) == 0) {
	    			if (delete == true) {
		    			WaitingEmail waitingEmail = new WaitingEmail(user.email);
		    			waitingEmail.save();
		    			Logger.info("removeCreatedAccountWithNoInvitationBefore : adding user to WaitingEmail with email : " + user.email);
	    			}
	    		}
	    		
	    		if (delete == true) {
	    			Logger.info("removeCreatedAccountWithNoInvitationBefore : deleting user with userName : " + user.userName + " and email = " + user.email);
	    			user.delete();
	    		} 
	    		results.add(user);
	    		
    		} else {
    			Logger.info("removeCreatedAccountWithNoInvitationBefore : UserListInsightsVisit is referencing the use, so it cannot delete user with userName : " + user.userName + " and email = " + user.email);
    		}
    	}
    	return results;
	}
	
	public static List<UserCount> findBestVoters(int number) {
		// A single SQL query could have been used for the following code
		List<UserInsightDailyVote> stats = UserInsightDailyVote.find("forDate > ?", new DateTime().minusWeeks(1).toDate()).fetch();
		// Compute the number of vote per user
		Map<User, Long> userCountMap = new HashMap<User, Long>();
		for(UserInsightDailyVote stat : stats ) {
			Long count = new Long(0);
			if(userCountMap.containsKey(stat.user)) {
				count = userCountMap.get(stat.user);
			}
			userCountMap.put(stat.user, count + stat.count);
		}
		// Create an ordered users and count
		List<UserCount> userCounts = new ArrayList<UserCount>();
		for ( Map.Entry<User, Long> entry : userCountMap.entrySet() ) {
			userCounts.add( new UserCount(entry.getKey(), entry.getValue()) );
		}
		// sort by count
		Collections.sort( userCounts );
		// reverse order
		Collections.reverse( userCounts );
		return userCounts.subList(0, Math.min(userCounts.size(), number));
	}
	
	public static List<UserCount> findBestCreators(int number) {
		// A single SQL query could have been used for the following code
		List<UserInsightDailyCreation> stats2 = UserInsightDailyCreation.find("forDate > ?", new DateTime().minusWeeks(1).toDate()).fetch();
		// Compute the number of vote per user
		Map<User, Long> userCountMap2 = new HashMap<User, Long>();
		for(UserInsightDailyCreation stat : stats2 ) {
			Long count = new Long(0);
			if(userCountMap2.containsKey(stat.user)) {
				count = userCountMap2.get(stat.user);
			}
			userCountMap2.put(stat.user, count + stat.count);
		}
		// Create an ordered users and count
		List<UserCount> userCounts2 = new ArrayList<UserCount>();
		for ( Map.Entry<User, Long> entry : userCountMap2.entrySet() ) {
			userCounts2.add( new UserCount(entry.getKey(), entry.getValue()) );
		}
		// sort by count
		Collections.sort( userCounts2 );
		// reverse order
		Collections.reverse( userCounts2 );
		return userCounts2.subList(0, Math.min(userCounts2.size(), number));
	}
	
	public List<UserCategoryScore> getCategoryScores(Date date, PeriodEnum period) {
		List<UserCategoryScore> categoryScores = UserCategoryScore.find("select cs from UserCategoryScore cs " +
			"where cs.historic.user = :user and cs.historic.scoreDate = :scoreDate and cs.period = :period")
			.bind("user", this)
			.bind("scoreDate", date)
			.bind("period", period)
			.fetch();
		return categoryScores;
//		UserScoreHistorized historizedCategoryScores = UserScoreHistorized.find("scoreDate=:scoreDate and user=:user").bind("scoreDate", new DateMidnight().toDate()).bind("user", this).first();
//		if ( historizedCategoryScores!=null && historizedCategoryScores.categoryScores !=null) {
//			return historizedCategoryScores.categoryScores;
//		}
//		return new ArrayList<UserCategoryScore>();
	}

	
	public List<Object[]> getScoreTimelineByCategory(CategoryEnum categoryEnum, PeriodEnum period) {
		List<Object[]> categoryScores = UserCategoryScore.find("select cs.historic.scoreDate, cs.normalizedScore from UserCategoryScore cs " +
			"where cs.historic.user = :user and cs.period = :period and cs.category.id = :catId and cs.score is not null")
			.bind("user", this)
			.bind("period", period)
			.bind("catId", categoryEnum.getId())
			.fetch();
		return categoryScores;
	}
}
