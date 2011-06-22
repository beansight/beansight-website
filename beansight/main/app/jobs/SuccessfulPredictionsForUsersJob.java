package jobs;

import helpers.TimeHelper;

import java.util.List;

import models.User;

import play.Logger;
import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;

@Every("50min")
public class SuccessfulPredictionsForUsersJob extends Job {

	public boolean runNow = false;
	
	private static Integer BLOCK_SIZE = 20;
	
	@Override
	public void doJob() throws Exception {
		
    	// FIXME TEMP : we should use @On("0 0 6 * * ?") but since there is a bug in Play 1.1.1 we use this trick
    	if (runNow == false) {
    		Logger.info("SucessfulPredictionsForUsersJob : runNow=false");
			if(!TimeHelper.hourAndDayCheck(6, null)) {
				Logger.info("SucessfulPredictionsForUsersJob : not 6 AM");
				Logger.info("SucessfulPredictionsForUsersJob quit");
				return;
			}
    	}
    	
    	Logger.info("SucessfulPredictionsForUsersJob doJob");
    	
    	Integer page = (Integer)Cache.get("SuccessfulPredictionsForUsersJob.page");
    	if (page == null) {
    		page = 1;
    	}
    	Cache.safeDelete("SuccessfulPredictionsForUsersJob.page");
    	
		List<User> users = User.find("order by id").fetch(page, BLOCK_SIZE);
		for (User u : users) {
			u.computeSuccessfulPredictionCount();
			u.save();
		}
		
		Logger.info("SucessfulPredictionsForUsersJob : block from %s computed", page);
		
		Integer nextPage = page + 1;
		users = User.find("order by id").fetch(nextPage, BLOCK_SIZE);
		if (users.size() > 0) {
			Cache.safeAdd("SuccessfulPredictionsForUsersJob.page", nextPage, "10mn");
			SuccessfulPredictionsForUsersJob job = new SuccessfulPredictionsForUsersJob();
			job.runNow = true;
			job.in(2);
			return;
		} else {
			Logger.info("SucessfulPredictionsForUsersJob no more users to process");
		}

		
		Logger.info("SucessfulPredictionsForUsersJob finished");
	}
	
}
