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
public class FeaturedTag extends Model {
	@ManyToOne
	public Tag tag;

	/** the date the topic should be featured */
	public Date startDate;

	/** the date the topic should stop being featured */
	public Date endDate;
	
	/** the language in which this featured topic should be displayed */
	@ManyToOne(fetch=FetchType.LAZY)
	public Language language;
	
	public String toString() {
	    return tag.label;
	}
	
	/**
	 * Create a featuredtopic
	 * it will start immediately to be featured, and stop 2 weeks later
	 */
	public FeaturedTag(Tag topic, Language language) {
		this.tag = topic;
		this.startDate = new Date();
		this.endDate = new DateTime().plusWeeks(2).toDate();
		this.language = language;
	}
	
	/**
	 * return all the featured topics that are currently active in the current languages
	 * @return
	 */
	public static List<FeaturedTag> findActive(List<Language> languages) {
		List<FeaturedTag> result = new ArrayList<FeaturedTag>();
		List<FeaturedTag> FeaturedTags = FeaturedTag.find("endDate > ? order by startDate desc", new Date()).fetch();

		for (FeaturedTag featured : FeaturedTags) {
			if(languages.contains(featured.language)) {
				result.add(featured);
			}
		}
		
		return result;
	}

}
