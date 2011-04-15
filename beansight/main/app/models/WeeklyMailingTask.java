package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
public class WeeklyMailingTask extends MailTask {

	@ManyToOne
	public User user;
	
	@ManyToMany
	@JoinTable(name="WeeklyMailingTask_PreviousWeekInsights")
	public List<Insight> previousWeekInsights = null;
	
	@ManyToMany
	@JoinTable(name="WeeklyMailingTask_NextWeekInsights")
	public List<Insight> nextWeekInsights = null;
	
	public WeeklyMailingTask(User userToSendMail, List<Insight> previousWeekInsights, List<Insight> nextWeekInsights) {
		super(userToSendMail.email, userToSendMail.uiLanguage.label);
		this.user = userToSendMail;
		this.previousWeekInsights = previousWeekInsights;
		this.nextWeekInsights = nextWeekInsights;
		
	}
}
