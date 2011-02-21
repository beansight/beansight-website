package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import jregex.Pattern;
import jregex.Replacer;

import org.hibernate.annotations.Index;

import play.db.jpa.Model;

@Entity
public class Comment extends Model {

	@Lob
	public String content;

	@ManyToOne
	public User user;

	@ManyToOne
	public Insight insight;

	/** the date this comment has been made */
	@Index (name = "COMMENTS_CRDATE_IDX")
	public Date creationDate;

	/** is this comment hidden ? */
	public boolean hidden;
	
	public Comment(User user, Insight insight, String content) {
		this.user = user;
		this.insight = insight;
		this.content = content;
		this.creationDate = new Date();
		this.hidden = false;
	}
	
	public String toString() {
	    return content;
	}


}
