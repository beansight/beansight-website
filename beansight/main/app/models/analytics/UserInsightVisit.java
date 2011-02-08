package models.analytics;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Insight;
import models.User;

@Entity
public class UserInsightVisit extends UserVisit {

	@ManyToOne
	public Insight insight;

	public UserInsightVisit(Date timestamp, User user, UserClientInfo userClientInfo, Insight insight) {
		super(timestamp, user, userClientInfo);
		this.insight = insight;
	}
	
}
