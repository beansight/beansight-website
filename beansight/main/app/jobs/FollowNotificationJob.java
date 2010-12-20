package jobs;

import java.util.Date;
import java.util.List;

import notifiers.Mails;

import models.FollowNotificationTask;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("5min")
public class FollowNotificationJob extends Job {
	
	/** Number of task email this job can send in his 5 minutes */
	public static final int NUM_TASK = 10;
	/** How many milliseconds it has to wait between each mail sending */
	public static final int WAIT_TIME = 15000;
	
    @Override
    public void doJob() throws Exception {
    	List<FollowNotificationTask> tasks = FollowNotificationTask.find("sent is false and attempt < 5").fetch(NUM_TASK);

    	for( FollowNotificationTask task : tasks) {
	    	if(task != null) {
	            try {
			    	Mails.followNotification(task);
			    	task.sent = true;
					task.save();
		        } catch (Exception e) {
		            Logger.error(e, "Mail error");
		        }
				Thread.sleep(WAIT_TIME);
	    	}
    	}
    	
    }
}