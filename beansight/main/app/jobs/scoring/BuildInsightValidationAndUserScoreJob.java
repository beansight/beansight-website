package jobs.scoring;

import helpers.TimeHelper;

import java.util.Date;
import java.util.List;

import models.Insight;
import models.PeriodEnum;
import models.User;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

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
//@On("0 0 3 * * ?")
@Every("1h")
public class BuildInsightValidationAndUserScoreJob extends Job {

	private Date fromDate = null;
	private Date toDate = null;
	public boolean runNow = false;
	
	/**
	 * default constructor : runs the job for the previous day
	 */
	public BuildInsightValidationAndUserScoreJob() {
		this.fromDate = new DateMidnight().minusDays(1).toDate();
		this.toDate =  new DateMidnight().minusDays(1).toDate();
		Logger.info("BuildInsightValidationAndUserScoreJob created : from %s to %s", fromDate, toDate);
	}
	
	/**
	 * Runs the validation and the score computation for every day of the given period of time.
	 * @param fromDate : the first date 
	 * @param toDate : the last day
	 * 
	 * Example : fromDate = 12 and endDate = 13
	 * The whole process will be run twice : one for the 12. and one for the 13.
	 */
	public BuildInsightValidationAndUserScoreJob(Date fromDate, Date toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
		Logger.info("BuildInsightValidationAndUserScoreJob created with params : from %s to %s", fromDate, toDate);
	}
	
	public void incrementFromDate() {
		fromDate = new Date(fromDate.getTime() + 24l*60l*60l*1000l);
	}
	
    @Override
    public void doJob() throws Exception {
    	Logger.info("BuildInsightValidationAndUserScoreJob doJob");
    	// TEMP
    	if (runNow == false) {
			if(!TimeHelper.hourAndDayCheck(4, null)) {
				Logger.info("BuildInsightValidationAndUserScoreJob quit");
				return;
			}
    	}
    	
    	Logger.info("BuildInsightValidationAndUserScoreJob begin from %s to %s", fromDate, toDate);
    	
    	// if fromDate is before toDate
    	if (fromDate.before(toDate) || fromDate.equals(toDate)) {
	    	Logger.info("********************");
	    	Logger.info("date=" + fromDate);
	    	Logger.info("********************");
	    	
	    	new InsightValidationJob(fromDate, true, this).now();
	    	
    	}
    	
    }
}
