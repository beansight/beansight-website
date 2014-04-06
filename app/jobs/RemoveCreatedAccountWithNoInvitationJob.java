package jobs;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Insight;
import models.User;
import models.Vote;
import models.WaitingEmail;
import models.analytics.DailyTotalComment;
import models.analytics.DailyTotalInsight;
import models.analytics.DailyTotalVote;
import models.analytics.InsightDailyVote;
import models.analytics.UserInsightDailyCreation;
import models.analytics.UserInsightDailyVote;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

/**
 * 
 * During private beta one needs an invitation to signup to beansight
 * But since facebook and twitter connect have been added for as signup service
 * this can generate user that go trought the signup process and finally can't 
 * provide any invitation code, leaving the database with a unactive user.
 * 
 * This Job deletes user who do not have provided an invitation code 1 one hour after 
 * their user creation. It also add provided email in the WaitingEmails table so
 * that we can send these people an invitation
 * 
 * @author jb
 *
 */
//@Every("1h")
public class RemoveCreatedAccountWithNoInvitationJob extends Job {

    @Override
    public void doJob() {
    	Logger.info("RemoveCreatedAccountWithNoInvitationJob begin");
    	
    	DateTime datetime = new DateTime();
    	datetime = datetime.minusHours(1);
    	
    	User.removeCreatedAccountWithNoInvitationBefore(datetime.toDate(), true);
    	
        Logger.info("RemoveCreatedAccountWithNoInvitationJob end");
    }
    
}
