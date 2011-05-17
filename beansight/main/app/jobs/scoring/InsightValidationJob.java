package jobs.scoring;

import java.util.Date;
import java.util.List;

import models.Insight;
import models.PeriodEnum;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("2h")
public class InsightValidationJob extends Job {

	public static final int INSIGHT_NUMBER_TO_PROCESS = 10;
	public static final int HOURS_JOB_EXECUTION = 2;

	private Date computeDate = null;
	private boolean runScoreJob = false;
	
	private int page = 1;
	public BuildInsightValidationAndUserScoreJob parentJob;
	
	/**
	 * default constructor : runs the job only to validate insights
	 */
	public InsightValidationJob() {
		this.computeDate = null;
		this.runScoreJob = false;
		this.parentJob = null;
	}
	
	public InsightValidationJob(Date dateToCompute, boolean runScoreJob, BuildInsightValidationAndUserScoreJob parentJob) {
		this.computeDate = dateToCompute;
		this.runScoreJob = runScoreJob;
		this.parentJob = parentJob;
	}
	
    @Override
    public void doJob() throws Exception {
    	Logger.info("InsightValidationJob begin");
    	Logger.debug("computeDate=%s", computeDate);

    	// Validate Insights and compute score of voters
    	
    	List<Insight> insights = Insight.findInsightsToValidate(page, INSIGHT_NUMBER_TO_PROCESS);
    	Logger.debug("InsightValidationJob, %s insights found for validation" , insights.size());
    	
    	// no more insight to validate and compute voter scores
    	// then start to compute category and user scores
    	if (insights.isEmpty()) {
    		Logger.info("InsightValidationJob end");
    		if (runScoreJob) {
    			new ComputeCategoryAndUserScoreHistoJob(computeDate, 1, parentJob).now();
    			return;
    		} else {
        		return;
        	}
    	} 
    	
		Logger.info("InsightValidationJob: page " + page);
		
		for (Insight insight : insights) {
            Logger.debug("Validation of Insight: " + insight.content);
            insight.validate();
			insight.computeVoterScores();
		}
        
		page++;
		
		// process next page
		this.now();
		
    }
}
