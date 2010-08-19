package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Vote extends Model {

	public enum State {
		AGREE, DISAGREE
	}

	@ManyToOne
	public User user;

	@ManyToOne
	public Insight insight;

	/** the date this vote has been made */
	public Date creationDate;

	/** agree or disagree */
	public State state;
	
	public Vote(User user, Insight insight, State state) {
		this.user = user;
		this.insight = insight;
		this.state = state;
		this.creationDate = new Date();
	}
	
	public String toString() {
	    return state.toString();
	}

	/**
	 * Call this to test if someone has already vote for an insight.
	 * @param userId
	 * @param insightId
	 * @return true if user has already vote, false otherwise.
	 */
	public static boolean hasUserVotedForInsight(Long userId, Long insightId) {
		Long count = find("select count(*) from Vote v join v.user u join v.insight i where u.id=:userId and i.id=:insightId").bind("userId", userId).bind("insightId", insightId).first();
		if (count==0) {
			return false;
		}
		return true;
	}
	
	/**
	 * Call this to get the last vote a user made for an insight 
	 * @param userId
	 * @param insightId
	 * @return
	 */
	public static Vote findLastVote(Long userId, Long insightId) {
		Vote vote = find("select v from Vote v join v.user u join v.insight i where u.id=:userId and i.id=:insightId order by v.creationDate desc").bind("userId", userId).bind("insightId", insightId).first();
		return vote;
	}
}
