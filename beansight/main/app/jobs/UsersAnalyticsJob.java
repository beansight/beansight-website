package jobs;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import models.Insight;
import models.Trend;
import models.User;
import models.analytics.UserInsightDailyCreation;
import models.analytics.UserInsightDailyVote;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

// job starts every days at midnight
@On("0 30 0 * * ?")
public class UsersAnalyticsJob extends Job {

    @Override
    public void doJob() {
    	Logger.info("UsersAnalyticsJob begin");
    	
    	// since this job starts after midnight we are calculating how many insights 
    	// have been created for yesterday (and potentially for previous days if 
    	// it's the first time the job running ...)
    	DateTime startOfDayForCalculation = new DateTime(new DateMidnight()).minusDays(1);
    	
    	doCalculationForUserInsightDailyCreation(startOfDayForCalculation);
		
    	doCalculationForUserInsightDailyVote(startOfDayForCalculation);
    	
        Logger.info("UsersAnalyticsJob end");
    }
    
    
    private void doCalculationForUserInsightDailyCreation(DateTime startOfDayForCalculation) {
    	if (UserInsightDailyCreation.findIfAnalyticExistsForDate(startOfDayForCalculation.toDate()) == true) {
    		return; // stop
    	}
    	DateTime endOfDayForCalculation = startOfDayForCalculation.plusDays(1).minusSeconds(1);
		List<Object[]> rows = User.find("select i.creator, count(i.id) from Insight i where i.creationDate between ? and  ? group by i.creator.id", startOfDayForCalculation.toDate(), endOfDayForCalculation.toDate()).fetch();
		
		for (Object[] row : rows) {
			UserInsightDailyCreation analytic = new UserInsightDailyCreation(startOfDayForCalculation.toDate(), (User)row[0], (Long)row[1]);
			analytic.save();
		}
		
		// recursive call to create previous records if they don't exist yet
		startOfDayForCalculation = startOfDayForCalculation.minusDays(1);
		doCalculationForUserInsightDailyCreation(startOfDayForCalculation);
    }
    
    
    private void doCalculationForUserInsightDailyVote(DateTime startOfDayForCalculation) {
    	if (UserInsightDailyVote.findIfAnalyticExistsForDate(startOfDayForCalculation.toDate()) == true) {
    		return; // stop
    	}
    	DateTime endOfDayForCalculation = startOfDayForCalculation.plusDays(1).minusSeconds(1);
		List<Object[]> rows = User.find("select v.user, count(v.id) from Vote v where v.creationDate between ? and  ? group by v.user.id", startOfDayForCalculation.toDate(), endOfDayForCalculation.toDate()).fetch();
		
		for (Object[] row : rows) {
			UserInsightDailyVote analytic = new UserInsightDailyVote(startOfDayForCalculation.toDate(), (User)row[0], (Long)row[1]);
			analytic.save();
		}
		
		// recursive call to create previous records if they don't exist yet
		startOfDayForCalculation = startOfDayForCalculation.minusDays(1);
		doCalculationForUserInsightDailyVote(startOfDayForCalculation);
    }
}
