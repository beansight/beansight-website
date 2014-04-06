package models.analytics;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Tag;
import models.User;

@Entity
public class UserTopicVisit extends UserVisit {

	@ManyToOne
	public Tag topic;

	public UserTopicVisit(Date creationDate, User user, UserClientInfo userClientInfo, Tag topic) {
		super(creationDate, user, userClientInfo);
		this.topic = topic;
	}
	
}
