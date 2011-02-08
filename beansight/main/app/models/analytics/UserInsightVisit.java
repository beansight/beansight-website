package models.analytics;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import models.Insight;
import models.User;

import java.util.*;

@Entity
public class UserInsightVisit extends UserVisit {

	@ManyToOne
	public Insight insight;

	public UserInsightVisit(Date timestamp, User user, String ip, String userAgent, String application, Insight insight) {
		super(timestamp, user, ip, userAgent, application);
		this.insight = insight;
	}
	
}
