package jobs.weeklymailing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateMidnight;

import models.Insight;
import models.User;
import models.WeeklyMailingTask;
import play.Logger;
import play.jobs.Job;
import play.jobs.On;

/**
 * This Job select users having insights that have been validated previous week
 * and insights that will be validated incoming week.
 * 
 * It creates WeeklyMailingTask objects in database. These objects will be readed 
 * in another job (WeeklyMailingSenderJob) that will send these by email.
 * 
 * @author jb
 *
 */
// Every Friday at 5 AM
@On("0 0 5 ? * FRI")
public class WeeklyMailingJob extends Job {

	@Override
	public void doJob() throws Exception {
		Logger.info("starting WeeklyMailingJob");
		
		DateMidnight pivotDate = new DateMidnight().minus(Insight.VALIDATION_HOUR_NUMBER*60l*60l);
		DateMidnight previousWeekFromDate = pivotDate.minusDays(7);
		DateMidnight nextWeekToDate = pivotDate.plusDays(7);
		
		// find all users having a voted insight within the provided from and to date:
		// note : don't select users having unsubscribe to this newsletter
		List<User> users = User.find("select distinct v.user from Vote v " +
				"where v.insight.endDate between :fromDate and :toDate " +
				"and v.insight.hidden is false " +
				"and v.user.statusNewsletter is true ")
				.bind("fromDate", previousWeekFromDate.toDate())
				.bind("toDate", nextWeekToDate.toDate())
				.fetch();
		
		Logger.debug("%s users selected for WeeklyMailingJob", users.size());
		
		for (User user : users) {
			List<Insight> userPreviousWeekInsights = user.getVotedInsights(true, previousWeekFromDate.toDate(), pivotDate.toDate());
			List<Insight> userNextWeekInsights = user.getVotedInsights(false, pivotDate.toDate(), nextWeekToDate.toDate());
			
			Logger.debug("%s has %s for previous week and %s for next week", user.userName, userPreviousWeekInsights.size(), userNextWeekInsights.size());
			
			WeeklyMailingTask weeklyMailingTask = new WeeklyMailingTask(user, userPreviousWeekInsights, userNextWeekInsights); 
			weeklyMailingTask.save();
		}
		
		Logger.info("ending WeeklyMailingJob");
	}

}
