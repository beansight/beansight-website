package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.UserIsAlreadyFollowingInsightException;

import models.Vote.State;
import models.Vote.Status;

import play.db.jpa.FileAttachment;
import play.db.jpa.Model;
import play.libs.Crypto;
import play.modules.search.*;

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
	
    //use the @Embedded annotation to store avatars in the database
    public FileAttachment avatar;
	
	/** Date the user created his account */
	private Date crdate; // private because must be read-only.
	
	/** the global score for this user */
	@Field
	public double score;
	
	/** list of scores of this users in all the categories */
	@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	public List<UserCategoryScore> categoryScores;
	
	/** list of insights created by this user */
	@OneToMany(mappedBy="creator", cascade=CascadeType.ALL)
	public List<Insight> createdInsights;
	
	/** every votes of the current user */
	@OneToMany(cascade = CascadeType.ALL)
	public List<Vote> votes;

	/** the insights followed by this user */
	@ManyToMany(cascade = CascadeType.ALL)
	public List<Insight> followedInsights;

	/** the users followed by this user */
	@ManyToMany(cascade = CascadeType.ALL)
	public List<User> followedUsers;
	
	/** the users you follow this user */
	@ManyToMany(mappedBy="followedUsers", cascade = CascadeType.ALL)
	public List<User> followers;
	
	/** the comments  */
	@OneToMany(mappedBy="user", cascade = CascadeType.ALL)
	public List<Comment> comments;
	
    public User(String email, String userName, String password) {
        this.email = email;
        this.password = Crypto.passwordHash(password);
        this.userName = userName;
        this.votes = new ArrayList<Vote>();
        this.createdInsights = new ArrayList<Insight>();
        this.followedInsights = new ArrayList<Insight>();
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
    	User user = find("email=? and password=?", email, Crypto.passwordHash(password)).first();
    	if (user!=null) {
    		return true;
    	}

    	return false;
    }
	
    public Date getCrdate() {
		return crdate;
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
     * Call this method to create a new insight that will be
     * automatically owned by the current user.
     * 
     * @param insightContent: the content text of this insight
     * @param endDate: date this insight should end 
     * @param tagLabelList: a comma separated list of tags
     */
    public Insight createInsight(String insightContent, Date endDate, String tagLabelList, long cateopryId) {
    	
    	Category category = Category.findById(cateopryId);
    	// exception if null
    	
    	Insight i = new Insight(this, insightContent, endDate, category);
    	i.save();
    	i.addTags(tagLabelList, this);
    	
    	this.createdInsights.add(i);
    	// agree with this insight
    	try {
			this.voteToInsight(i.id, State.AGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			e.printStackTrace();
		}
    	
    	this.save();
    	
    	return i;
    }
    
    /**
     * Call this method to set a vote for one insight for the
     * current user.
     * It shouldn't be possible to vote twice for one insight.
     * 
     * @param insightId : id of the insight user is voting for.
     * @param voteState State.AGREE or State.DISAGREE
     */
	public void voteToInsight(Long insightId, State voteState) throws CannotVoteTwiceForTheSameInsightException  {
		Insight insight = Insight.findById(insightId);
		Vote vote = Vote.findLastVoteByUserAndInsight(this.id, insightId);
		if (vote != null) {
			// there are only idiots who do not change their minds
			if (vote.state.equals(voteState)) {
				// voting twice for the same side ... 
				throw new CannotVoteTwiceForTheSameInsightException();
			} else {
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
			insight.save();
			
		}
		
	}

	
	/**
	 * Tells if the given insight is already in the current user followed insight list. 
	 * @param insight
	 * @return
	 */
	public boolean isFollowingInsight(Insight insight) {
		if(followedInsights.contains(insight)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Call this method to add the given insight in the current user followed insight list 
	 * @param insightId
	 */
	public void startFollowingThisInsight(Long insightId) throws UserIsAlreadyFollowingInsightException {
		Insight insight = Insight.findById(insightId);
		
		// If we are already following the insight throw a business exception
		if (isFollowingInsight(insight)==true) {
			throw new UserIsAlreadyFollowingInsightException();
		}
		
		followedInsights.add(insight);
		save();
		insight.followers.add(this);
		insight.save();
	}
	
	
	/**
	 * 
	 * @param insightId
	 */
	public void stopFollowingThisInsight(Long insightId) {
		Insight insight = Insight.findById(insightId);
		
		// If we were not following the insight just do nothing ...
		if (isFollowingInsight(insight)==false) {
			return;
		}
		
		followedInsights.remove(insight);
		save();
		insight.followers.remove(this);
		insight.save();
	}
	
	/**
	 * is this user following the given user 
	 * @param user to check
	 */
	public boolean isFollowingUser(User user) {
		if(followedUsers.contains(user)) {
			return true;
		}
		return false;
	}
	
	/**
	 * This user start following the given user 
	 * @param user: User to follow
	 */
	public void startFollowingThisUser(User user) {
		if(isFollowingUser(user)==true){
			return;
		}
		followedUsers.add(user);
		save();
		user.followers.add(this);
		user.save();
	}

	/**
	 * This user stops following the given user
	 * @param insightId
	 */
	public void stopFollowingThisUser(User user) {
		if(isFollowingUser(user)==false){
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
		for( UserCategoryScore userCatScore : categoryScores ) {
			if(userCatScore.category == category) {
				newCategory = false;
				userCatScore.score = Math.random();
				// TODO: insert here the score computation algorithm for a given category
			}
		}
		// if not, create the link between user and category
		if(newCategory) {
			UserCategoryScore newUserCatScore = new UserCategoryScore(this, category);
			newUserCatScore.score = Math.random();
			categoryScores.add(newUserCatScore);
		}
	}
}
