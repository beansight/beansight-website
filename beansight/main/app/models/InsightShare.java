package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
/**
 * information needed to share an insight from an user to another user.
 */
public class InsightShare extends Model {

	/** date when the share has been done */
	public Date created;
	
	/** user who will receive this Share */
	@ManyToOne
	public User toUser;

	/** user who sent this Share */
	@ManyToOne
	public User fromUser;
	
	@ManyToOne
	public Insight insight;
	
	/** Does this Share has been read ?*/
	public boolean read;
	
	public InsightShare(User fromUser, User toUser, Insight insight) {
		this.created = new Date();
		this.insight = insight;
		this.toUser = toUser;
		this.fromUser = fromUser;
		this.read = false;
	}
}
