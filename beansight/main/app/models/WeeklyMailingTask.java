package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
public class WeeklyMailingTask extends MailTask {

//	public List<Long> previousWeekInsightIds = null;
//	public List<Long> nextWeekInsightIds = null;
	
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
		
//		this.previousWeekInsightIds = getIds(previousWeekInsights);
//		this.nextWeekInsightIds = getIds(nextWeekInsights);
	}
	
	
//	public List<Insight> getPreviousWeekInsights() {
//		return Insight.find("id in :insightIds")
//			.bind("insightIds", previousWeekInsightIds)
//			.fetch();
//	}
//	
//	
//	public List<Insight> getNextWeekInsights() {
//		return Insight.find("id in :insightIds")
//			.bind("insightIds", nextWeekInsightIds)
//			.fetch();
//	}
	
//	private List<Long> getIds(List<Insight> insights) {
//		List<Long> insightIds = new ArrayList<Long>();
//		for (Insight insight : insights) {
//			insightIds.add(insight.id);
//		}
//		return insightIds;
//	}
}
