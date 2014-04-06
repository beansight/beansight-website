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

	public UserInsightVisit(Date creationDate, User user, UserClientInfo userClientInfo, Insight insight) {
		super(creationDate, user, userClientInfo);
		this.insight = insight;
	}
	
}
