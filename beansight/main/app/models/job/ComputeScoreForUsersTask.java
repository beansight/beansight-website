package models.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;

import models.PeriodEnum;
import models.User;

import org.joda.time.DateMidnight;

import play.Logger;
import play.db.jpa.Model;

@Entity
public class ComputeScoreForUsersTask extends Model {
	public static int PAGE_SIZE = 20;
	
	public Date computeDate;
	
	@ManyToMany
	public List<User> users;
	
	/** period in millisecond */
	@Enumerated(EnumType.STRING)
	public PeriodEnum period; 
	
	
	/**
	 * Create a Task that will be run by the ScoresComputationJob
	 * @param computeDate : the date for which we want to compute the scores
	 * @param period : the period used to compute the scores
	 */
	public ComputeScoreForUsersTask(Date computeDate, PeriodEnum period) {
		super();
		this.computeDate = computeDate;
		this.period = period;
		this.users = new ArrayList<User>();
	}

	
	/**
	 * Call this method to create ScoresToComputeTask in database.
	 * This will create tasks for each date starting from fromDate to toDate and for a given period
	 * For example if you want to compute users scores for the last 3 month and only for June 1 2011 you will
	 * call it this way : createTasksFromToDate(new Date("01/06/2010"), new Date("01/06/2010"), PeriodEnum.THREE_MONTHS)
	 * Then to start 
	 * @param fromDate
	 * @param toDate
	 */
	public static void createTasksForDate(Date date, PeriodEnum period) {
		Date fromDate = new Date(date.getTime() - period.getTimePeriod());
		// get the number of users that are concerned by the score computation
		Long count = User.find("select count(distinct u) from User u join u.votes v join v.insight i " +
			"where i.hidden is false and i.endDate between ? and ?",
			fromDate, 
			date).first(); 
		
		Logger.info("ScoresComputationInitJob : %s user to compute for %s", count, date);
		
		int pageTotal = Math.round(count/ComputeScoreForUsersTask.PAGE_SIZE + 0.5f);
		for (int currentPage = 1; currentPage < pageTotal; currentPage++) {
	    	List<User> usersToUpdate = User.find("select distinct u from User u join u.votes v join v.insight i " +
	    			"where i.hidden is false and i.endDate between :fromDate and :endDate")
	    			.bind("fromDate", fromDate)
	    			.bind("endDate", date)
	    			.fetch(currentPage, PAGE_SIZE);
			
	    	// Create a task and add users to it
			ComputeScoreForUsersTask task = new ComputeScoreForUsersTask(date, period);
			for (User user : usersToUpdate) {
				task.users.add(user);				
			}
			task.save();
		}
	}
	
	/**
	 * This is a convenient method to create tasks to compute scores for more than one date.
	 * For example if you want to compute user's scores for 1st june 2011 and 2nd june 2011
	 * using the last 3 months of ended insights you will call the method this way :
	 * createTasksBetweenTwoDates(new Date("01/06/2011"), new Date("02/06/2011"), PeriodEnum.THREE_MONTHS) 
	 * @param fromDate
	 * @param toDate
	 * @param period
	 */
	public static void createTasksBetweenTwoDates(Date fromDate, Date toDate, PeriodEnum period) {
		DateMidnight currentDate = new DateMidnight(fromDate);
		DateMidnight lastDate = new DateMidnight(toDate);
		while (lastDate.isEqual(currentDate) || lastDate.isAfter(currentDate)) {
			ComputeScoreForUsersTask.createTasksForDate(currentDate.toDate(), period);
			currentDate = currentDate.plusDays(1);
		}
	}
	
	/**
	 * Call this method to compute the user's scores for this Task 
	 * It takes all the users associated on the task intance and compute their score 
	 * for the computeDate and period task's attributes.
	 */
	public void computeScoresForTask() {
		Logger.info("ScoresToComputeTask.computeScoresForTask(computeDate:%s)", this.computeDate);
    	
    	for(User u : this.users) {
    		Logger.debug("updating user score for userName : %s", u.userName);
    		u.computeCategoryScores(this.computeDate, this.period);
    		u.computeUserScore(this.computeDate, this.period);
    	}
	}
	
	/**
	 * If you want to remove all task waiting in DB to be executed then call
	 * flushAll !
	 */
	public static void flushAll() {
		List<ComputeScoreForUsersTask> list = ComputeScoreForUsersTask.findAll();
		for (ComputeScoreForUsersTask task : list) {
			task.delete();
		}
	}
}
