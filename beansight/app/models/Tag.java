package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Tag extends Model {

	public String label;

	@ManyToOne
	public User user;

	@ManyToOne
	public Insight insight;

	/** the date this vote has been made */
	public Date creationDate;

	public Tag(User user, Insight insight, String label) {
		this.user = user;
		this.insight = insight;
		this.label = label;
		this.creationDate = new Date();
	}
	
	public String toString() {
	    return label;
	}


}
