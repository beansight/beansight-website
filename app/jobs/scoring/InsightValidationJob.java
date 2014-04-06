package jobs.scoring;

import java.util.Date;
import java.util.List;

import models.Insight;
import models.PeriodEnum;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;

/**
 * This Job validates insights
 * @author jb
 *
 */
@Every("2h")
public class InsightValidationJob extends Job {

	public static final int INSIGHT_NUMBER_TO_PROCESS = 10;

	
	/**
	 * default constructor : runs the job only to validate insights
	 */
	public InsightValidationJob() {
	}
	

	
    @Override
    public void doJob() throws Exception {
    	Logger.info("InsightValidationJob running");

    	List<Insight> insights = Insight.findInsightsToValidate(1, INSIGHT_NUMBER_TO_PROCESS);
    	
    	if (!insights.isEmpty()) {
    		for (Insight insight : insights) {
                Logger.debug("Validation of Insight: " + insight.content);
                insight.validate();
    			insight.computeVoterScores();
    		}
    		
    		// This job validates insights by bunch of INSIGHT_NUMBER_TO_PROCESSinsights.
    		// Once the bunch of INSIGHT_NUMBER_TO_PROCESS insights is validated it
    		// schedules itself to run again in 10 seconds : this way each bunch of
    		// validated insights is committed in db and jvm's memory taken by this job
    		// is released, avoiding to take too much cpu and memory
    		
    		this.in(10);
    		
    	} 
    	
    	Logger.info("InsightValidationJob : no more insight to validate");

    }
}
