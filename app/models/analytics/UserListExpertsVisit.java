package models.analytics;

import java.util.Date;

import javax.persistence.Entity;

import models.User;

@Entity
public class UserListExpertsVisit extends UserVisit {

	public UserListExpertsVisit(Date creationDate, User user, UserClientInfo userClientInfo) {
		super(creationDate, user, userClientInfo);
	}
	
}
