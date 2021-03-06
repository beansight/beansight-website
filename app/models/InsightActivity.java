package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;


/**
 * The activity that happened on a given insight for a given user
 * InsightActivities are created every time a user starts following an insight.
 */
@Entity
public class InsightActivity extends Model {

	/** date this activity was created */
	public Date created;
	/** last time this activity was updated */ 
	public Date updated;
	
	/** Dos this activity contains information? */
	public boolean notEmpty;

	
	/** user concerned by this activity */
	@ManyToOne(fetch=FetchType.LAZY)
	public User user;

	@ManyToOne(fetch=FetchType.LAZY)
	public Insight insight;

	/** Number of actions performed on this insight (change, agree, disagree, comment...) */
	public long totalCount;
	
	/** since the last time it has been reseted, how many times users changed their vote ? */
	public long voteChangeCount;
	/** since the last time it has been reseted, how many new votes are "agree" ?*/
	public long newAgreeCount;
	/** since the last time it has been reseted, how many new votes are "disagree" ?*/
	public long newDisagreeCount;
	/** since the last time it has been reseted, how many times this insight has been put into favorites ? */
	public long newFavoriteCount;
	/** since the last time it has been reseted, how many times this insight has been commented (not used yet) */
	public long newCommentCount;
	
	
	public InsightActivity(User user, Insight insight) {
		this.created = new Date();
		this.updated = new Date();
		this.user = user;
		this.insight = insight;
	}
	
	/** Clear this insight activity (set everything to 0) */
	public void reset() {
		this.notEmpty = false;
		this.updated = new Date();
		
		this.totalCount = 0;
		this.voteChangeCount = 0;
		this.newAgreeCount = 0;
		this.newDisagreeCount = 0;
		this.newFavoriteCount = 0;
		this.newCommentCount = 0;
	}
	
	public void incrementVoteChangeCount() {
		this.voteChangeCount++;
		this.totalCount++;
		updated();
	}
	public void incrementNewAgreeCount() {
		this.newAgreeCount++;
		this.totalCount++;
		updated();
	}
	public void incrementNewDisagreeCount() {
		this.newDisagreeCount++;
		this.totalCount++;
		updated();
	}
	public void incrementNewFavoriteCount() {
		this.newFavoriteCount++;
		this.totalCount++;
		updated();
	}
	public void incrementNewCommentCount() {
		this.newCommentCount++;
		this.totalCount++;
		updated();
	}
	
	private void updated() {
		this.notEmpty = true;
		this.updated = new Date();
	}
	
}
