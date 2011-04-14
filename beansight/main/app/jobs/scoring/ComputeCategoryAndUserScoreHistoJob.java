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

public class ComputeCategoryAndUserScoreHistoJob extends Job {

	private Date computeDate = null;
	private int pageToProcess = 1;
	public BuildInsightValidationAndUserScoreJob parentJob = null;
	
	public ComputeCategoryAndUserScoreHistoJob(Date dateToCompute, int pageToProcess, BuildInsightValidationAndUserScoreJob parentJob) {
		this.computeDate = dateToCompute;
		this.parentJob = parentJob;
		this.pageToProcess = pageToProcess;
	}
	
    @Override
    public void doJob() throws Exception {
    	// Calculate for all users having voted for at least one insight (prediction) during the period
    	Date from = new Date(computeDate.getTime() - PeriodEnum.THREE_MONTHS.getTimePeriod());
    	List<User> usersToUpdate = User.find("select distinct u from User u join u.votes v join v.insight i " +
    			"where i.hidden is false and i.endDate between :fromDate and :endDate")
    			.bind("fromDate", from)
    			.bind("endDate", this.computeDate)
    			.fetch(pageToProcess, 5);
    	
    	// if no more user to update 
    	if (usersToUpdate.isEmpty()) {
    		// then move to next date if their is a parent job 
    		if (parentJob != null) {
	    		parentJob.incrementFromDate();
	    		parentJob.now();
    		}
    		return;
    	}
    	
    	int i = 1;
    	for(User u : usersToUpdate) {
    		Logger.info("updating user, page " + pageToProcess + " : " + i + "/" + usersToUpdate.size());
    		u.computeCategoryScores(computeDate, PeriodEnum.THREE_MONTHS);
    		u.computeUserScore(computeDate, PeriodEnum.THREE_MONTHS);
    		i++;
    	}
    	
    	// continue to next page
    	pageToProcess++;
    	this.now();
        
    }
}
