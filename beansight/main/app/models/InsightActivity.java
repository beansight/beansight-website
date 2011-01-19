package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;


/**
 * The activity that happened on a given insight for a given user
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
	@ManyToOne
	public User user;

	@ManyToOne
	public Insight insight;
	
	/**since the creation date, how many times users changed their vote ? */
	public long voteChangeCount;
	/** since the creation date, how many new votes are "agree" ?*/
	public long newAgreeCount;
	/** since the creation date, how many new votes are "disagree" ?*/
	public long newDisagreeCount;
	/** since the creation date, how many times this insight has been put into favorites ? */
	public long newFavoriteCount;
	
	
	public InsightActivity(User user, Insight insight) {
		this.created = new Date();
		this.updated = new Date();
		this.user = user;
		this.insight = insight;
	}
}
