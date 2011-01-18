package jobs;

import java.util.Date;
import java.util.List;

import models.Insight;
import models.Trend;
import models.Vote;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("2h")
public class InsightValidationJob extends Job {

	public static final int INSIGHT_NUMBER_TO_PROCESS = 100;
	
    @Override
    public void doJob() throws Exception {
    	Logger.info("InsightValidationJob begin");
    	
    	int page = 1;
    	List<Insight> insights = Insight.findNotValidated(page, INSIGHT_NUMBER_TO_PROCESS);
    	while(insights.size() > 0) {
    		validateInsights(insights);
            Logger.info("InsightValidationJob: page " + page);
    		page++;
    		insights = Insight.findNotValidated(page, INSIGHT_NUMBER_TO_PROCESS);
    	}
    	
        Logger.info("InsightValidationJob end");
    }
    
    
    private void validateInsights(List<Insight> insights) {
        for (Insight insight : insights) {
            Logger.info("Validation of Insight: " + insight.content);

            double score = 0.5;
            
            try {
	            // score = ( sum position * DT ) / ( sum DT )
	            // DT = (timestamp position) - (timestamp creation) 
	            // position = 1 if agree, 0 if disagree

            	double num = 0;
	        	double denum = 0;
	        	
	            for(Vote vote : insight.votes) {
	            	double dt = vote.creationDate.getTime() - insight.creationDate.getTime();
	            	if(vote.state.equals(Vote.State.AGREE)) {
	            		num += dt;
	            	}
	            	denum += dt;
	            }
	            
	            score = num / denum;
            } catch(Exception e) {
            	// if something goes wrong, result = cannot decide.
            	score = 0.5;
            }
            
            insight.validationScore = score;
            insight.validated = true;
            
            insight.save();
        }
    }
    
}
