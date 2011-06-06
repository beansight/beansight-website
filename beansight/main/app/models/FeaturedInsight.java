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
 * An insight is selected by an admin as being "featured". (can be used to feature a sponsored insight for example)
 */
@Entity
public class FeaturedInsight extends Model {

	/** a list of insights to feature */
	@ManyToOne
	public Insight insight;
	
	/** the date this sponsor should be featured */
	public Date startDate;
	/** the date this sponsor should stop being featured */
	public Date endDate;

	public String toString() {
	    return insight.content  + " - " + startDate;
	}
	
	public FeaturedInsight(Insight insight) {
		this.insight = insight;
		this.startDate = new Date();
		this.endDate = new DateTime().plusWeeks(1).toDate();
	}

	public FeaturedInsight(Insight insight, Date startDate, Date endDate) {
		this.insight = insight;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	/**
	 * return all the featured insights that are currently active
	 * @return
	 */
	public static List<FeaturedInsight> findActive(List<Language> langs) {
		Date today = new Date();
		return FeaturedInsight.find("startDate < :startDate and endDate > :endDate and insight.lang in (:langs) order by startDate desc").bind("startDate", today).bind("endDate", today).bind("langs", langs).fetch();
	}

}
