package jobs;

import java.util.Date;
import java.util.List;

import notifiers.Mails;

import models.FollowNotificationTask;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.mvc.Scope;

@Every("5min")
public class SendFollowNotificationMailsJob extends Job {
	
	/** Number of task email this job can send in his 5 minutes */
	public static final int NUM_TASK = 10;
	
    @Override
    public void doJob() throws Exception {
    	// TODO : this is a hack to make the reverse rout work when calling a mail from Job (http://groups.google.com/group/play-framework/browse_thread/thread/2127472d7df42aff)
    	Scope.RouteArgs.current.set(new Scope.RouteArgs());
    	
    	List<FollowNotificationTask> tasks = FollowNotificationTask.find("sent is false and attempt < 5").fetch(NUM_TASK);

    	for( FollowNotificationTask task : tasks) {
	    	if(task != null) {
	            try {
			    	if ( Mails.followNotification(task) ) {
				    	task.sent = true;
						task.save();
			    	}
		        } catch (Throwable e) {
		            Logger.error(e, "Mail error");
		        }
	    	}
    	}
    	
    }
}