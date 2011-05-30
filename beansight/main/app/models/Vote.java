package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import play.Logger;
import play.db.jpa.Model;

@Entity
public class Vote extends Model {

	public enum State {
		DISAGREE,     // 0
		AGREE           // 1
	}

	public enum Status {
		HISTORIZED,   // 0
		ACTIVE            // 1
	}

	@ManyToOne(fetch=FetchType.LAZY)
	public User user;

	@ManyToOne(fetch=FetchType.LAZY)
	public Insight insight;

	/** the date this vote has been made */
	public Date creationDate;

	/** agree or disagree */
	public State state;

	/** active or historized */
	public Status status;

	public Vote(User user, Insight insight, State state) {
		this.user = user;
		this.insight = insight;
		this.state = state;
		this.creationDate = new Date();
		this.status = Status.ACTIVE;
	}

	public String toString() {
		return state.toString();
	}

	/**
	 * Call this to test if someone has already vote for an insight.
	 * 
	 * @param userId
	 * @param insightId
	 * @return true if user has already vote, false otherwise.
	 */
	public static boolean hasUserVotedForInsight(Long userId, String insightUniqueId) {
		Long count = find(
				"select count(*) from Vote v join v.user u join v.insight i "
						+ "where u.id=:userId and v.status = :status "
						+ "and i.uniqueId=:insightUniqueId").bind("userId", userId)
				.bind("status", Status.ACTIVE).bind("insightUniqueId", insightUniqueId)
				.first();
		if (count == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * Call this to get the last vote a user made for an insight
	 * 
	 * @param userId
	 * @param insightId
	 * @return
	 */
	public static Vote findLastVoteByUserAndInsight(Long userId, String insightUniqueId) {
		
		Logger.info("id: " + userId);
		
		Vote vote = find(
				"select v from Vote v join v.user u join v.insight i "
						+ "where u.id=:userId and v.status = :status "
						+ "and i.uniqueId=:insightUniqueId").bind("userId", userId)
				.bind("status", Status.ACTIVE).bind("insightUniqueId", insightUniqueId)
				.first();
		return vote;
	}

	public static List<Vote> findVotesByUserAndInsight(Long userId,
			String insightUniqueId) {
		List<Vote> votes = find(
				"select v from Vote v join v.user u join v.insight i "
						+ "where u.id=:userId "
						+ "and i.uniqueId=:insightUniqueId order by v.creationDate desc")
				.bind("userId", userId).bind("insightUniqueId", insightUniqueId).fetch();
		return votes;
	}
}
