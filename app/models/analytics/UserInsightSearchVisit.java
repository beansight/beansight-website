package models.analytics;

import java.util.Date;

import javax.persistence.Entity;

import models.User;

@Entity
public class UserInsightSearchVisit extends UserVisit {

	public String searchKeyWords;
	
	public UserInsightSearchVisit(Date creationDate, User user, UserClientInfo userClientInfo, String searchKeyWords) {
		super(creationDate, user, userClientInfo);
		this.searchKeyWords = searchKeyWords;
	}
	
}
