package models.analytics;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.User;

@Entity
public class UserExpertVisit extends UserVisit {

	@ManyToOne
	public User expert;

	public UserExpertVisit(Date creationDate, User user, UserClientInfo userClientInfo, User expert) {
		super(creationDate, user, userClientInfo);
		this.expert = expert;
	}
	
}
