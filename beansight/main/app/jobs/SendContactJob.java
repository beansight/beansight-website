package jobs;

import java.util.Date;
import java.util.List;

import notifiers.Mails;

import models.ContactMailTask;
import models.MessageMailTask;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

// FIXME : all mail jobs start at the same time : not good 
@Every("30s")
public class SendContactJob extends Job {
	
	/** Number of task email this job can send in his 5 minutes */
	public static final int NUM_TASK = 10;
	/** How many milliseconds it has to wait between each mail sending */
	public static final int WAIT_TIME = 15000;
	
    @Override
    public void doJob() throws Exception {
    	List<ContactMailTask> tasks = ContactMailTask.find("sent is false and attempt < 5").fetch(NUM_TASK);

    	for( ContactMailTask task : tasks) {
	    	if(task != null) {
	            try {
			    	Mails.contact(task);
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