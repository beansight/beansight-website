package models.analytics;

import java.util.Date;

import javax.persistence.Entity;

import models.User;

@Entity
public class UserListExpertsVisit extends UserVisit {

	public UserListExpertsVisit(Date timestamp, User user, UserClientInfo userClientInfo) {
		super(timestamp, user, userClientInfo);
	}
	
}
