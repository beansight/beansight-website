package models.analytics;

import java.util.Date;

import javax.persistence.Entity;

import models.User;

@Entity
public class UserInsightSearchVisit extends UserVisit {

	public String searchKeyWords;
	
	public UserInsightSearchVisit(Date timestamp, User user, UserClientInfo userClientInfo, String searchKeyWords) {
		super(timestamp, user, userClientInfo);
		this.searchKeyWords = searchKeyWords;
	}
	
}
