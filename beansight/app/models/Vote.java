package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

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

	@ManyToOne
	public User user;

	@ManyToOne
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
	public static boolean hasUserVotedForInsight(Long userId, Long insightId) {
		Long count = find(
				"select count(*) from Vote v join v.user u join v.insight i "
						+ "where u.id=:userId and v.status = :status "
						+ "and i.id=:insightId").bind("userId", userId)
				.bind("status", Status.ACTIVE).bind("insightId", insightId)
				.first();
		if (count == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * Call this to test if someone has already vote for an insight.
	 * 
	 * @param userId
	 * @param insightId
	 * @return null if did not voted, State if voted
	 */
	public static State whatVoteForInsight(Long userId, Long insightId) {
		Vote vote = find(
				"select v from Vote v join v.user u join v.insight i "
						+ "where u.id=:userId and v.status = :status "
						+ "and i.id=:insightId").bind("userId", userId)
				.bind("status", Status.ACTIVE).bind("insightId", insightId)
				.first();
		if(vote == null) {
			return null;
		}
		return vote.state;
	}

	/**
	 * Call this to get the last vote a user made for an insight
	 * 
	 * @param userId
	 * @param insightId
	 * @return
	 */
	public static Vote findLastVoteByUserAndInsight(Long userId, Long insightId) {
		Vote vote = find(
				"select v from Vote v join v.user u join v.insight i "
						+ "where u.id=:userId and v.status = :status "
						+ "and i.id=:insightId").bind("userId", userId)
				.bind("status", Status.ACTIVE).bind("insightId", insightId)
				.first();
		return vote;
	}

	public static List<Vote> findVotesByUserAndInsight(Long userId,
			Long insightId) {
		List<Vote> votes = find(
				"select v from Vote v join v.user u join v.insight i "
						+ "where u.id=:userId "
						+ "and i.id=:insightId order by v.creationDate desc")
				.bind("userId", userId).bind("insightId", insightId).fetch();
		return votes;
	}
}
