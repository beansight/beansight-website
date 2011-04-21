package jobs.weeklymailing;

import java.util.List;

import models.Insight;
import models.WeeklyMailingTask;
import notifiers.Mails;
import play.Logger;
import play.jobs.Job;
import play.jobs.On;

/**
 * 
 * @author jb
 *
 */
// every friday at 6 AM
@On("0 0 6 ? * FRI")
public class WeeklyMailingSenderJob extends Job {

	/** Number of task email this job can send  */
	public static final int NUM_TASK = 10;
	
	@Override
	public void doJob() throws Exception {
		Logger.info("starting WeeklyMailingSenderJob");
		
		List<WeeklyMailingTask> mailTasks = WeeklyMailingTask.find("sent is false and attempt < 5").fetch(NUM_TASK);
		
		for (WeeklyMailingTask task : mailTasks) {
			if( Mails.weeklyMailing(task) ) {
		    	task.sent = true;
				task.save();
			}
		}
		
		long count = WeeklyMailingTask.count("sent is false and attempt < 5") ;
		if (count > 0) {
			this.in(3*60);
			Logger.info("Still %s WeeklyMailingTask to email, scheduling another WeeklyMailingSenderJob in 3 minutes", count);
		} 
		
		Logger.info("ending WeeklyMailingSenderJob");
	}
	
}
