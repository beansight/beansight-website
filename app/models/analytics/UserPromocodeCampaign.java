package models.analytics;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Promocode;
import models.User;


/**
 * recording which promocode a user have used to get an account will give us analytics
 * about the efficiency of each campaign
 * 
 */
@Entity
public class UserPromocodeCampaign extends UserVisit {

	@ManyToOne
	public Promocode promocode;
	
	public UserPromocodeCampaign(Date creationDate, User user, UserClientInfo userClientInfo, Promocode promocode) {
		super(creationDate, user, userClientInfo);
		this.promocode = promocode;
	}
	
}
