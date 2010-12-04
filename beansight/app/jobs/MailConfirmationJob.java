package jobs;

import java.util.Date;
import java.util.List;

import notifiers.Mails;

import models.Insight;
import models.MailConfirmTask;
import models.Trend;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("15s")
public class MailConfirmationJob extends Job {
	
    @Override
    public void doJob() throws Exception {
    	MailConfirmTask task = MailConfirmTask.find("sent is false and attempt < 5").first(); // "find("sent is false"
    	if(task != null) {
	    	Mails.confirmation(task);
	    	task.attempt++;
	    	task.sent = true;
			task.save();
    	}
    	
    }
}