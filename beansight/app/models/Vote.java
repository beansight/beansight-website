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

}
