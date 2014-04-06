package jobs.scoring;

import java.util.Date;
import java.util.List;

import org.joda.time.DateMidnight;

import models.Insight;
import models.User;
import models.job.ComputeScoreForUsersTask;
import play.jobs.Every;
import play.jobs.Job;

/**
 * This Job is ran from ScoresComputationInitJob because ScoresComputationInitJob is
 * the job that is scheduled to execute every days to compute every days scores.
 * 
 *  BUT this job can also be run from anywhere the only condition is to have 
 *  ComputeScoreForUsersTask available in database : see ComputeScoreForUsersTask
 *  to know how to create new task to be executed by this job.
 * 
 * 
 * @author jb
 *
 */
public class ScoresComputationJob extends Job {

	@Override
	public void doJob() throws Exception {
		ComputeScoreForUsersTask task = ComputeScoreForUsersTask.find("order by computeDate asc").first();
		if (task != null) {
			task.computeScoresForTask();
			
			// task has finished, delete it so we don't compute it again
			task.delete();
			
			new ScoresComputationJob().in(1); 
		}
	}
	
}
