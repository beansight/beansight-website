package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
	
	/** the language in which this featured topic should be displayed */
	@ManyToOne(fetch=FetchType.LAZY)
	public Language language;
	
	public String toString() {
	    return topic.label;
	}
	
	/**
	 * Create a featuredtopic
	 * it will start immediately to be featured, and stop 2 weeks later
	 */
	public FeaturedTopic(Topic topic, Language language) {
		this.topic = topic;
		this.startDate = new Date();
		this.endDate = new DateTime().plusWeeks(2).toDate();
		this.language = language;
	}
	
	/**
	 * return all the featured topics that are currently active in the current languages
	 * @return
	 */
	public static List<FeaturedTopic> findActive(List<Language> languages) {
		List<FeaturedTopic> result = new ArrayList<FeaturedTopic>();
		List<FeaturedTopic> featuredTopics = FeaturedTopic.find("endDate > ? order by startDate desc", new Date()).fetch();

		for (FeaturedTopic featured : featuredTopics) {
			if(languages.contains(featured.language)) {
				result.add(featured);
			}
		}
		
		return result;
	}

}
