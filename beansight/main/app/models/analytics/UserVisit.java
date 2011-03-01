package models.analytics;

import java.util.Date;

import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import models.User;
import play.db.jpa.Model;

@MappedSuperclass
public class UserVisit extends Model {

	/** the date of the visit on the page */
	public Date creationDate;
	
	/** user visiting the page */
	@ManyToOne
	public User user;
	
	/** ip of the user */
	public String ip;
	
	/** user-agent of the visiting user */
	@Lob
	public String userAgent;
	
	/** the id of the application used (example: web-desktop if used from beansight.com) */
	public String application;
	
	public UserVisit(Date creationDate, User user, UserClientInfo userClientInfo) {
		super();
		this.creationDate = creationDate;
		this.user = user;
		this.ip = userClientInfo.ip;
		this.userAgent = userClientInfo.userAgent;
		this.application = userClientInfo.application;
	}
	
}
