package jobs.scoring;

import helpers.TimeHelper;

import java.util.Date;
import java.util.List;

import org.joda.time.DateMidnight;

import models.Insight;
import models.PeriodEnum;
import models.User;
import models.job.ComputeScoreForUsersTask;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

//run at 4 in the morning every days
//@On("0 0 4 * * ?")
@Every("1h")
public class ScoresComputationInitJob extends Job {

	public boolean runNow = false;
	
	@Override
	public void doJob() throws Exception {
		
    	Logger.info("ScoresComputationInitJob doJob");
    	// FIXME TEMP : we should use @On("0 0 4 * * ?") but since there is a bug in Play 1.1.1 we use this trick
    	if (runNow == false) {
    		Logger.info("ScoresComputationInitJob : runNow=false");
			if(!TimeHelper.hourAndDayCheck(4, null)) {
				Logger.info("ScoresComputationInitJob : not 4 AM");
				Logger.info("ScoresComputationInitJob quit");
				return;
			}
    	}
		
    	// foremost we want to be sure that there's not already some tasks to process
    	// and may be there are being process right now !? If yes we delete all the ScoresToComputeTask
    	// and we rerun the ScoresComputationInitJob with a delay to be sure that no more Job are iterating
    	// over the ScoresToComputeTask
    	if (ComputeScoreForUsersTask.count() > 0) {
    		Logger.info("their is already some ScoresToComputeTask in DB that are processed. They will be deleted and ScoresComputationInitJob will be rerun un 60 s");
    		ComputeScoreForUsersTask.deleteAll();
    		ScoresComputationInitJob scoresComputationInitJob = new ScoresComputationInitJob();
    		scoresComputationInitJob.runNow = true;
    		scoresComputationInitJob.in(60);
    		return;
    	}
    	
		// First : insure that all insights that should be validated are validated :
		
		Insight.validateAllInsights();
		
		// Second : create all ScoreToComputeTask in database :
		
		//( we compute score for yesterday because we want score for ended day only)
		Date toDate = new DateMidnight().minusDays(1).toDate();
		ComputeScoreForUsersTask.createTasksForDate(toDate, PeriodEnum.THREE_MONTHS);
		
		// start processing ScoresToComputeTask now : 
		new ScoresComputationJob().now();
	}
	
}
