package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Comment extends Model {

	public String content;

	@ManyToOne
	public User user;

	@ManyToOne
	public Insight insight;

	/** the date this comment has been made */
	public Date creationDate;

	public Comment(User user, Insight insight, String content) {
		this.user = user;
		this.insight = insight;
		this.content = content;
		this.creationDate = new Date();
	}
	
	public String toString() {
	    return content;
	}


}
