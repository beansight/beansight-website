package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;


/**
 * The activity that happened on a given user a user has favorited
 */
@Entity
public class UserActivity extends Model {

	/** date this activity was created */
	public Date created;
	/** last time this activity was updated */ 
	public Date updated;
	
	/** Does this activity contains information? */
	public boolean notEmpty;
	
	/** user concerned by this activity */
	@ManyToOne(fetch=FetchType.LAZY)
	public User user;

	@ManyToOne(fetch=FetchType.LAZY)
	public User followedUser;

	/** Number of actions performed on this topic (change, agree, disagree, comment...) */
	public long totalCount;

	/** since the last time it has been reseted, how many insights this user has created ? */
	public long newInsightCount;
	/** since the last time it has been reseted, how many votes this user has made ? */
	public long newVoteCount;
	/** since the last time it has been reseted, how many times this user changed his mind ? */
	public long voteChangeCount;
	
	public UserActivity(User user, User followedUser) {
		this.created = new Date();
		this.updated = new Date();
		this.user = user;
		this.followedUser = followedUser;
		
		this.totalCount = 0;
		this.newInsightCount = 0;
		this.newVoteCount = 0;
		this.voteChangeCount = 0;
	}

	/** Clear this insight activity (set everything to 0) */
	public void reset() {
		this.notEmpty = false;
		this.updated = new Date();
		
		this.totalCount = 0;
		this.newInsightCount = 0;
		this.newVoteCount = 0;
		this.voteChangeCount = 0;
	}
	
	public void incrementNewInsightCount() {
		this.newInsightCount++;
		this.totalCount++;
		updated();
	}

	public void incrementNewVoteCount() {
		this.newVoteCount++;
		this.totalCount++;
		updated();
	}
	
	public void incrementVoteChangeCount() {
		this.voteChangeCount++;
		this.totalCount++;
		updated();
	}
	
	private void updated() {
		this.notEmpty = true;
		this.updated = new Date();
	}
}
