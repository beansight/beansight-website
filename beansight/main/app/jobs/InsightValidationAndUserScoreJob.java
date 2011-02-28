package jobs;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import models.Insight;
import models.Trend;
import models.User;
import models.Vote;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("2h")
public class InsightValidationAndUserScoreJob extends Job {

	public static final int INSIGHT_NUMBER_TO_PROCESS = 100;
	public static final int HOURS_JOB_EXECUTION = 2;
	
    @Override
    public void doJob() throws Exception {
    	Logger.info("InsightValidationJob begin");

    	// Validate Insights and compute score of voters
    	int page = 1;
    	List<Insight> insights = Insight.findInsightsToValidate(page, INSIGHT_NUMBER_TO_PROCESS);
    	while(insights.size() > 0) {
    		for (Insight insight : insights) {
                Logger.info("Validation of Insight: " + insight.content);
                insight.validate();
    			insight.computeVoterScores();
    		}
    		
            Logger.info("InsightValidationJob: page " + page);
    		page++;
    		insights = Insight.findInsightsToValidate(page, INSIGHT_NUMBER_TO_PROCESS);
    	}
    	
    	// update voters score :
    	List<User> usersToUpdate = User.findUsersToUpdateScore( new DateTime().minusHours(HOURS_JOB_EXECUTION).toDate());
    	for(User u : usersToUpdate) {
    		u.computeCategoryScores();
    		u.computeUserScore();
    	}
    	
        Logger.info("InsightValidationJob end");
    }
}
