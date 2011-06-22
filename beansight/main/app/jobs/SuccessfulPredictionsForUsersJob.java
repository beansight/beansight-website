package jobs;

import helpers.TimeHelper;

import java.util.List;

import models.User;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("50min")
public class SuccessfulPredictionsForUsersJob extends Job {

	public boolean runNow = false;
	
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
		List<User> users = User.findAll();
		for (User u : users) {
			u.computeSuccessfulPredictionCount();
			u.save();
		}
		
		Logger.info("SucessfulPredictionsForUsersJob finished");
	}
	
}
