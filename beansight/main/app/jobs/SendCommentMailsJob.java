package jobs;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import notifiers.Mails;

import models.CommentMentionMailTask;
import models.FollowNotificationTask;
import models.NewCommentNotificationMailTask;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.mvc.Scope;

@Every("10s")
public class SendCommentMailsJob extends Job {
	
	/** Number of task email this job can send in his 5 minutes */
	public static final int NUM_TASK = 10;
		
    @Override
    public void doJob() throws Exception {
    	// TODO : this is a hack to make the reverse rout work when calling a mail from Job (http://groups.google.com/group/play-framework/browse_thread/thread/2127472d7df42aff)
    	Scope.RouteArgs.current.set(new Scope.RouteArgs());
    	
    	// Comment mention
    	List<CommentMentionMailTask> tasks = CommentMentionMailTask.find("sent is false and attempt < 5").fetch(NUM_TASK);
    	for( CommentMentionMailTask task : tasks) {
	    	if(task != null) {
	            try {
			    	if ( Mails.commentMention(task)) {
				    	task.sent = true;
						task.save();
			    	}
		        } catch (Throwable e) {
		            Logger.error(e, "Mail error");
		        }
	    	}
    	}
    	
    	// New Comment notifications
    	List<NewCommentNotificationMailTask> newCommentTasks = NewCommentNotificationMailTask.find("sent is false and attempt < 5").fetch(NUM_TASK);
    	for( NewCommentNotificationMailTask task : newCommentTasks) {
	    	if(task != null) {
	            try {
			    	if ( Mails.newCommentNotification(task)) {
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