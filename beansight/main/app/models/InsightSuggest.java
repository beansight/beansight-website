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
public class InsightSuggest extends Model {

	private static final double FOLLOWED_USER_VOTE_SCORE = 10.0;
	private static final double FOLLOWED_USER_CREATE_SCORE = 20.0; // +10 because when we create we vote
	private static final double FOLLOWED_TAG_SCORE = 5.0;
	
	/** date this suggestion was created */
	public Date created;
	/** last time this suggestion was updated */ 
	public Date updated;
	
	/** user concerned by this suggestion */
	@ManyToOne(fetch=FetchType.LAZY)
	public User user;

	/** insight concerned by this suggestion */
	@ManyToOne(fetch=FetchType.LAZY)
	public Insight insight;

	/** Date the insight ends (same as insight.endDate) */
	public Date endDate;
	
	public double score;
	
	/** This suggestion is displayed because these user you are following voted on the insight */
	@ManyToMany(fetch=FetchType.LAZY)
	public List<User> becauseFollowedUserVoted;
	
	/** This suggestion is displayed because this user created it */
	public boolean becauseFollowedUserCreated;
	
	/** This suggestion is displayed because the insight has thsese tags, followed by the user */
	@ManyToMany(fetch=FetchType.LAZY)
	public List<Tag> becauseFollowedTag;
	
	
	public InsightSuggest(User user, Insight insight) {
		this.created 	= new Date();
		this.updated 	= new Date();
		this.user 		= user;
		this.insight 	= insight;
		this.endDate 	= insight.endDate;
		this.score 		= 0;
		this.becauseFollowedUserVoted 	= new ArrayList<User>();
		this.becauseFollowedTag 		= new ArrayList<Tag>();
	}
	
	/** Clear this insight activity (set everything to 0) */
	public void reset() {
		this.updated = new Date();
	}
	
	private void updated() {
		this.updated = new Date();
	}
	
	
	public void addBecauseFollowedUserVoted(User u) {
		this.becauseFollowedUserVoted.add(u);
		this.score += FOLLOWED_USER_VOTE_SCORE;
		updated();
	}

	public void addBecauseFollowedTag(Tag t) {
		this.becauseFollowedTag.add(t);
		this.score += FOLLOWED_TAG_SCORE;
		updated();
	}
	
	public void addBecauseFollowedUserCreated(User u) {
		this.becauseFollowedUserCreated = true;
		this.score += FOLLOWED_USER_CREATE_SCORE;
		updated();
	}
	
	public static InsightSuggest findByUserAndInsight(User user, Insight insight) {
		return InsightSuggest.find("byUserAndInsight", user, insight).first();
	}
	
	public static InsightSuggest findByUserAndInsightOrCreate(User user, Insight insight) {
		InsightSuggest suggest = findByUserAndInsight(user, insight);
		if(suggest == null) {
			suggest = new InsightSuggest(user, insight);
			suggest.save();
		}
		return suggest;
	}
	
	
	
}
