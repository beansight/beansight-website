package models.analytics;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.joda.time.DateMidnight;

import models.User;
import play.db.jpa.Model;

@Entity
public class UserInsightDailyVote extends Model {

	@Transient
	private static Date NO_ANALYTIC_BEFORE_DATE = new DateMidnight(2011, 1, 26).toDate();
	
	/** the date for which this analytics has been created */
	public Date forDate;
	
	@ManyToOne
	public User user;
	
	// number of vote the "user" has made for date "forDate"
	public long count;
	
	public UserInsightDailyVote(Date forDate, User user, long count) {
		super();
		this.forDate = forDate;
		this.user = user;
		this.count = count;
	}
	
	public static boolean findIfAnalyticExistsForDate(Date date) {
		// their is no prediction before 26 january 2011
		if (date.before(NO_ANALYTIC_BEFORE_DATE)) {
			return true;
		}
		if (UserInsightDailyVote.count("forDate = ?", date) > 0) {
			return true;
		}
		return false;
	}
}
