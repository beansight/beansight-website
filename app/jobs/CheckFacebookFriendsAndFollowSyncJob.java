package jobs;

import helpers.TimeHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.FacebookFriend;
import models.Insight;
import models.User;
import models.Vote;
import models.analytics.DailyTotalComment;
import models.analytics.DailyTotalInsight;
import models.analytics.DailyTotalVote;
import models.analytics.InsightDailyVote;
import models.analytics.UserInsightDailyCreation;
import models.analytics.UserInsightDailyVote;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;


/**
 * It seems that we have a bug but currently we can't find out where/when it happens 
 * So this job allow to check if your hare following/hiding a facebook friend
 * then the beansight user should be followed/not followed
 * 
 * @author jb
 *
 */
public class CheckFacebookFriendsAndFollowSyncJob extends Job {

    @Override
    public void doJob() {
    	long startTime = System.currentTimeMillis();
    	Logger.info("CheckFacebookFriendsAndFollowSyncJob started");
		List<FacebookFriend> friends = FacebookFriend.find("isBeansightUser is true").fetch();
		for (FacebookFriend fbFriend : friends) {
			if (fbFriend.isAdded) {
				if (fbFriend.user.isFollowingUser(fbFriend.beansightUserFriend) == false) {
					Logger.debug("%s should be following %s",fbFriend.user, fbFriend.beansightUserFriend);
					fbFriend.user.startFollowingThisUser(fbFriend.beansightUserFriend, false);
					fbFriend.user.save();
					Logger.debug("Corrected : %s is now following %s",fbFriend.user, fbFriend.beansightUserFriend);
				}
			}
			if (fbFriend.isHidden) {
				if (fbFriend.user.isFollowingUser(fbFriend.beansightUserFriend) == true) {
					Logger.debug("%s should NOT be following %s",fbFriend.user, fbFriend.beansightUserFriend);
					fbFriend.user.stopFollowingThisUser(fbFriend.beansightUserFriend);
					fbFriend.user.save();
					Logger.debug("Corrected : %s has stop following %s",fbFriend.user, fbFriend.beansightUserFriend);
				}
			}
		}
		Logger.info("CheckFacebookFriendsAndFollowSyncJob finished (execution time : %s seconds)", (System.currentTimeMillis() - startTime)/1000);
    }
    
}
