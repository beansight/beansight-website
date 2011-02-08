package models.analytics;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import models.User;

import java.util.*;

@MappedSuperclass
public class UserVisit extends Model {

	/** the date of the visit on the page */
	public Date timestamp;
	
	/** user visiting the page */
	@ManyToOne
	public User user;
	
	/** ip of the user */
	public String ip;
	
	/** user-agent of the visiting user */
	public String userAgent;
	
	/** the id of the application used (example: web-desktop if used from beansight.com) */
	public String application;
	
	public UserVisit(Date timestamp, User user, String ip, String userAgent, String application) {
		super();
		this.timestamp = timestamp;
		this.user = user;
		this.ip = ip;
		this.userAgent = userAgent;
		this.application = application;
	}
	
}
