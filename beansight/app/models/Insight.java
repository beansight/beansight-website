package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Insight extends Model {

	@ManyToOne
	public User creator;

	/** the date this insight has been created by its creator */
	public Date creationDate;

	/** the date this insight is ending, defined by its creator */
	public Date endDate;

	/** Content of the insight, a simple text describing the idea */
	public String content;

	/** Every votes of the current insight */
	@OneToMany(mappedBy = "insight", cascade = CascadeType.ALL)
	public List<Vote> votes;
	// TODO : to reduce the number of queries, we may need to store the positive and negative vote number.
	
	/** Users who follow the current insight */
	@ManyToMany(mappedBy = "followedInsights", cascade = CascadeType.ALL)
	public List<User> followers;

	public Insight(User creator, String content, Date endDate) {
		this.creator = creator;
		this.creationDate = new Date();
		this.endDate = endDate;
		this.content = content;
	}

	/** get the number of agreed for this insight */
	public long getPositiveVoteNumber() {
		return Vote.count("insight = ? and state = ?", this, Vote.State.AGREE);
	}
	
	/** get the number of disagreed for this insight */
	public long getNegativeVoteNumber() {
		return Vote.count("insight = ? and state = ?", this, Vote.State.DISAGREE);
	}
	
	public String toString() {
		return content;
	}
	

}
