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

import org.joda.time.DateTime;

import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

/**
 * A topic selected by an admin as being "featured".
 */
@Entity
public class FeaturedTopic extends Model {
	@ManyToOne
	public Topic topic;

	/** the date the topic should be featured */
	public Date startDate;

	/** the date the topic should stop being featured */
	public Date endDate;
	
	public String toString() {
	    return topic.label;
	}
	
	/**
	 * Create a featuredtopic
	 * it will start immediately to be featured, and stop 2 weeks later
	 */
	public FeaturedTopic(Topic topic) {
		this.topic = topic;
		this.startDate = new Date();
		this.endDate = new DateTime().plusWeeks(2).toDate();
	}
	
	/**
	 * return all the featured topics that are currently active
	 * @return
	 */
	public static List<FeaturedTopic> findActive() {
		return FeaturedTopic.find("endDate > ? order by startDate desc", new Date()).fetch();
	}

}
