package jobs;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Insight;
import models.User;
import models.Vote;
import models.analytics.DailyTotalVote;
import models.analytics.InsightDailyVote;
import models.analytics.UserInsightDailyCreation;
import models.analytics.UserInsightDailyVote;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

// job starts every days at midnight
@On("0 30 0 * * ?")
public class AnalyticsJob extends Job {

    @Override
    public void doJob() {
    	Logger.info("AnalyticsJob begin");
    	
    	// since this job starts after midnight we are calculating how many insights 
    	// have been created for yesterday (and potentially for previous days if 
    	// it's the first time the job running ...)
    	DateTime startOfDayForCalculation = new DateTime(new DateMidnight()).minusDays(1);
    	
    	doCalculationForUserInsightDailyCreation(startOfDayForCalculation);
		
    	doCalculationForUserInsightDailyVote(startOfDayForCalculation);
    	
    	doCalculationForInsightDailyVote(startOfDayForCalculation);
    	
    	DailyTotalVote.compute(startOfDayForCalculation);
    	
        Logger.info("AnalyticsJob end");
    }
    
    
    private void doCalculationForUserInsightDailyCreation(DateTime startOfDayForCalculation) {
    	if (UserInsightDailyCreation.findIfAnalyticExistsForDate(startOfDayForCalculation.toDate()) == true) {
    		return; // stop
    	}
    	DateTime endOfDayForCalculation = startOfDayForCalculation.plusDays(1).minusSeconds(1);
		List<Object[]> rows = Insight.find("select i.creator, count(i.id) from Insight i where i.creationDate between ? and  ? group by i.creator.id", startOfDayForCalculation.toDate(), endOfDayForCalculation.toDate()).fetch();
		
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
		List<Object[]> rows = Vote.find("select v.user, count(v.id) from Vote v where v.creationDate between ? and  ? group by v.user.id", startOfDayForCalculation.toDate(), endOfDayForCalculation.toDate()).fetch();
		
		for (Object[] row : rows) {
			UserInsightDailyVote analytic = new UserInsightDailyVote(startOfDayForCalculation.toDate(), (User)row[0], (Long)row[1]);
			analytic.save();
		}
		
		// recursive call to create previous records if they don't exist yet
		startOfDayForCalculation = startOfDayForCalculation.minusDays(1);
		doCalculationForUserInsightDailyVote(startOfDayForCalculation);
    }
    
    private void doCalculationForInsightDailyVote(DateTime startOfDayForCalculation) {
    	if (InsightDailyVote.findIfAnalyticExistsForDate(startOfDayForCalculation.toDate()) == true) {
    		return; // stop
    	}
    	DateTime endOfDayForCalculation = startOfDayForCalculation.plusDays(1).minusSeconds(1);

    	// create the date in java.util.Date to avoid creating it each time in the loops
    	Date startDate = startOfDayForCalculation.toDate();

    	//
		// Retrieving disagree count vote for insights for date
		//
		String q1 = "select v.insight, count(v.id) " +
				"from Vote v " +
				"where v.creationDate between :start and :end " +
				"and v.state = 0 " +
				"group by v.insight.id"; 

		
		Map<Long, InsightDailyVote> resultsAggregatorMap = new HashMap<Long, InsightDailyVote>();
		List<Object[]> r1 = Insight.find(q1).bind("start", startOfDayForCalculation.toDate()).bind("end", endOfDayForCalculation.toDate()).fetch();
		for (Object[] o : r1) {
			System.out.println(o[0] + ":" + o[1]);
			Insight insight = (Insight)o[0];
			Long disagreeCount = (Long)o[1];
			resultsAggregatorMap.put(insight.id, new InsightDailyVote(startDate, insight, 0, disagreeCount));
		}
		
		//
		// updating with agree count vote for this insight for date
		//
		String q2 = "select v.insight, count(v.id) " +
				"from Vote v " +
				"where v.creationDate between :start and :end " +
				"and v.state = 1 " +
				"group by v.insight.id"; 
		
		List<Object[]> r2 = Insight.find(q2).bind("start", startOfDayForCalculation.toDate()).bind("end", endOfDayForCalculation.toDate()).fetch();
		for (Object[] o : r2) {
			Insight insight = (Insight)o[0];
			Long agreeCount = (Long)o[1];
			
			// search if InsightDailyVote exists to merge agreeCount  
			InsightDailyVote insightDailyVote = resultsAggregatorMap.get(insight.id);
			if (insightDailyVote == null) {
				insightDailyVote = new InsightDailyVote(startDate, insight, agreeCount, 0);
			} else {
				insightDailyVote.agreeCount = agreeCount;
			}
			
		}
		
		// save in database
		for (InsightDailyVote analytic : resultsAggregatorMap.values()) {
			analytic.save();
		}
		
		// recursive call to create previous records if they don't exist yet
		startOfDayForCalculation = startOfDayForCalculation.minusDays(1);
		doCalculationForInsightDailyVote(startOfDayForCalculation);
    }
}
