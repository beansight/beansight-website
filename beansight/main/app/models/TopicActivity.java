package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;


/**
 * The activity that happened on a given topic a user has favorited
 */
@Entity
public class TopicActivity extends Model {

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
	public Topic topic;

	/** Number of actions performed on this topic (change, agree, disagree, comment...) */
	public long totalCount;

	/** since the last time it has been reseted, how many insights have been created in this topic ? */
	public long newInsightCount;
	
	public TopicActivity(User user, Topic topic) {
		this.created = new Date();
		this.updated = new Date();
		this.user = user;
		this.topic = topic;
	}

	/** Clear this insight activity (set everything to 0) */
	public void reset() {
		this.notEmpty = false;
		this.updated = new Date();
		
		this.totalCount = 0;
		this.newInsightCount = 0;
	}
	
	public void incrementNewInsightCount() {
		this.newInsightCount++;
		this.totalCount++;
		
		this.notEmpty = true;
		this.updated = new Date();
	}
	
}
