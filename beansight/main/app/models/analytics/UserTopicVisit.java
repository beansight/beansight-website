package models.analytics;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Topic;
import models.User;

@Entity
public class UserTopicVisit extends UserVisit {

	@ManyToOne
	public Topic topic;

	public UserTopicVisit(Date creationDate, User user, UserClientInfo userClientInfo, Topic topic) {
		super(creationDate, user, userClientInfo);
		this.topic = topic;
	}
	
}
