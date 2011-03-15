package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class InvitedSubscribedNotification extends Model {

	@ManyToOne
	public User notifiedUser;
	@ManyToOne
	public User subscribedUser;
	
	public InvitedSubscribedNotification(User notifiedUser, User subscribedUser) {
		this.notifiedUser = notifiedUser;
		this.subscribedUser = subscribedUser;
	}

}
