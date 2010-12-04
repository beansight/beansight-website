package jobs;

import java.util.Date;
import java.util.List;

import notifiers.Mails;

import models.FollowNotificationTask;
import play.jobs.Every;
import play.jobs.Job;

@Every("5min")
public class FollowNotificationJob extends Job {
	
    @Override
    public void doJob() throws Exception {
    	FollowNotificationTask task = FollowNotificationTask.find("sent is false and attempt < 5").first();
    	// TODO : do not take only the first one, but take a certain number, and send the emails with a delay between each sending.
    	if(task != null) {
    		task.attempt++;
	    	Mails.followNotification(task);
	    	task.sent = true;
			task.save();
    	}
    	
    }
}