package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@MappedSuperclass
/**
 * Generic class that provide basic functions for a mail task
 */
public class MailTask extends Model {
	/** creation date of this task */ 
	private Date created;
	
	/** Does this mail have been sent? */
	public boolean sent;
	
	/** Number of time the system tried to send this mail */
	public int attempt;
	
	/** User this mail should be sent to */
	@ManyToOne
	public User to;
	
	public MailTask(User to) {
		this.to = to;
		this.attempt = 0;
		this.sent = false;
		this.created = new Date();
	}
	
	public Date getCreated() {
		return created;
	}
}
