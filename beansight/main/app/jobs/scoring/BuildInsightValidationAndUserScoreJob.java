package jobs.scoring;

import java.util.Date;
import java.util.List;

import models.Insight;
import models.PeriodEnum;
import models.User;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

/**
 * This Job is used to create the score for each category and to historize these scores
 * 
 * @author jb
 *
 */
// run at 3 in the morning every days
@On("0 0 3 * * ?")
public class BuildInsightValidationAndUserScoreJob extends Job {

	private Date fromDate = null;
	private Date toDate = null;
	
	public BuildInsightValidationAndUserScoreJob(Date fromDate, Date toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
	}
	
	public void incrementFromDate() {
		fromDate = new Date(fromDate.getTime() + 24l*60l*60l*1000l);
	}
	
    @Override
    public void doJob() throws Exception {
    	Logger.info("BuildInsightValidationAndUserScoreJob begin");
    	
    	if (fromDate.before(toDate)) {
	    	Logger.info("********************");
	    	Logger.info("date=" + fromDate);
	    	Logger.info("********************");
	    	
	    	new InsightValidationJob(fromDate, PeriodEnum.THREE_MONTHS, true, this).now();
	    	
    	}
    	
    }
}
