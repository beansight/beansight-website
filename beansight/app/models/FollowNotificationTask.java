package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class FollowNotificationTask extends MailTask {

	@OneToOne
	public User follower;
	
	public FollowNotificationTask(User to, User follower) {
		super(to);
		this.follower = follower;
	}
    
}
