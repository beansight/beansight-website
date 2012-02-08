package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import models.Filter.SortBy;
import models.Insight.InsightResult;

import java.util.*;


/**
 * Insight suggestion for a user. Sort by score to get the higher suggested insights
 * Every time someone or something followed by this user is updated, this objects gets point in its score
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
	
	/** main parameter */
	public double score;
	
	/** This suggestion is displayed because these user you are following voted on the insight */
	@ManyToMany(fetch=FetchType.LAZY)
	public List<User> becauseFollowedUserVoted;
	
	/** This suggestion is displayed because this user created it */
	public boolean becauseFollowedUserCreated;
	
	/** This suggestion is displayed because the insight has these tags that you follow */
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
		// TODO
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
	
	public static List<InsightSuggest> findByUser(int from, int number, Filter filter, User user) {
        String query = "select insightsuggest from InsightSuggest insightsuggest "
		        		+ "where insightsuggest.insight.hidden is false "
		        		+ "and insightsuggest.user is :user "
		        		+ "and insightsuggest.insight.endDate >= :currentDate "
		        		//+ filter.generateJPAQueryWhereClause(SortBy.INCOMING)
		        		+ "order by insightsuggest.score DESC";

        return InsightSuggest.find(query).bind("user", user).bind("currentDate", new Date()).from(from).fetch(number);
		
	}
	
	/**
	 * transforms a list of InsightSuggets to an InsightResult object
	 * @param suggests
	 * @return
	 */
	public static InsightResult toInsightList(List<InsightSuggest> suggests) {
		List<Insight> insights = new ArrayList<Insight>();
		
		for(InsightSuggest suggest : suggests ) {
			insights.add(suggest.insight);
		}
		
		InsightResult result = new InsightResult();
		result.results = insights;
		
		return result;
	}
	
}
