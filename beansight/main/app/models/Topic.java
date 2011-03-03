package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

/**
 * A topic is a group of tags
 */
@Entity
public class Topic extends Model {
	public String label;

	@ManyToMany(cascade = CascadeType.ALL)
	public List<Tag> tags;

	/** the date this vote has been made */
	public Date creationDate;

	/** the user who initially created the topic */
	@ManyToOne
	public User creator;
	
	public Topic(String label, List<Tag> tags, User creator) {
		this.label = label;
		this.tags = tags;
		this.creator = creator;
		this.creationDate = new Date();
	}
	
	
	public String toString() {
	    return label;
	}

	public static Topic findByLabel(String label) {
		return Topic.find("byLabel", label).first();
	}

}
