package jobs;

import java.util.Date;
import java.util.List;

import notifiers.Mails;

import models.ContactMailTask;
import models.MessageMailTask;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.mvc.Scope;

// FIXME : all mail jobs start at the same time : not good 
@Every("10min")
public class SendContactJob extends Job {
	
	/** Number of task email this job can send in his 5 minutes */
	public static final int NUM_TASK = 10;
	/** How many milliseconds it has to wait between each mail sending */
	public static final int WAIT_TIME = 15000;
	
    @Override
    public void doJob() throws Exception {
    	// TODO : this is a hack to make the reverse rout work when calling a mail from Job (http://groups.google.com/group/play-framework/browse_thread/thread/2127472d7df42aff)
    	Scope.RouteArgs.current.set(new Scope.RouteArgs());
    	
    	List<ContactMailTask> tasks = ContactMailTask.find("sent is false and attempt < 5").fetch(NUM_TASK);

    	for( ContactMailTask task : tasks) {
	    	if(task != null) {
	            try {
			    	Mails.contact(task);
			    	task.sent = true;
					task.save();
		        } catch (Throwable e) {
		            Logger.error(e, "Mail error");
		        }
				Thread.sleep(WAIT_TIME);
	    	}
    	}
    	
    }
}