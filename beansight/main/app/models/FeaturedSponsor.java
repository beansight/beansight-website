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
 * A sponsor selected by an admin as being "featured".
 */
@Entity
public class FeaturedSponsor extends Model {

	/** Who is paying? */
	@ManyToOne
	public User sponsor;

	/** a list of insights to feature */
	@ManyToMany
	public List<Insight> insights;
	
	/** the date this sponsor should be featured */
	public Date startDate;
	/** the date this sponsor should stop being featured */
	public Date endDate;

	/** the language in which this featured sponsor should be displayed */
	@ManyToOne(fetch=FetchType.LAZY)
	public Language language;

	public String toString() {
	    return sponsor.userName + " - " + startDate;
	}
	
	public FeaturedSponsor(User sponsor, List<Insight> insights, Language language) {
		this.sponsor = sponsor;
		this.insights = insights;
		this.startDate = new Date();
		this.endDate = new DateTime().plusMonths(1).toDate();
		this.language = language;
	}

	public FeaturedSponsor(User sponsor, List<Insight> insights, Date startDate, Date endDate, Language language) {
		this.sponsor = sponsor;
		this.insights = insights;
		this.startDate = startDate;
		this.endDate = endDate;
		this.language = language;
	}

	/**
	 * return all the featured sponsor that are currently active
	 * @return
	 */
	public static List<FeaturedSponsor> findActive(Language language) {
		Date today = new Date();
		return FeaturedSponsor.find("startDate < ? and endDate > ? and language = ? order by startDate desc", today, today, language).fetch();
	}

}
