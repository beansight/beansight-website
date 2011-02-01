package models;

import play.*;
import play.db.jpa.*;
import play.i18n.Messages;

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
	
	/** email address this mail should be sent to */
	public String sendTo;
	
	/** language in which teh mail should be sent */
	public String language;
	
	
	public MailTask(String to) {
		this.sendTo = to;
		this.language = "";
		this.attempt = 0;
		this.sent = false;
		this.created = new Date();
	}
	
	public MailTask(String to, String lang) {
		this.sendTo = to;
		this.language = lang;
		this.attempt = 0;
		this.sent = false;
		this.created = new Date();
	}
	
	public Date getCreated() {
		return created;
	}
}
