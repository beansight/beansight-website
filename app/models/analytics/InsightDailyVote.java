package models.analytics;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.joda.time.DateMidnight;

import models.Insight;
import models.User;
import play.db.jpa.Model;

@Entity
public class InsightDailyVote extends Model {

	@Transient
	private static Date NO_ANALYTIC_BEFORE_DATE = new DateMidnight(2011, 1, 26).toDate();
	
	/** the date for which this analytics has been created */
	public Date forDate;
	
	@ManyToOne
	public Insight insight;
	
	public Long agreeCount;
	
	public Long disagreeCount;
	
	public InsightDailyVote(Date forDate, Insight insight, long agreeCount, long disagreeCount) {
		super();
		this.forDate = forDate;
		this.insight = insight;
		this.agreeCount = agreeCount;
		this.disagreeCount = disagreeCount;
	}
	
	public Long getTotalVote() {
		return agreeCount + disagreeCount;
	}
	
	public static boolean findIfAnalyticExistsForDate(Date date) {
		// their is no prediction before 26 january 2011
		if (date.before(NO_ANALYTIC_BEFORE_DATE)) {
			return true;
		}
		if (InsightDailyVote.count("forDate = ?", date) > 0) {
			return true;
		}
		return false;
	}
}
