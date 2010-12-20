package models;

import play.*;
import play.db.jpa.*;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.*;

@Entity
public class FollowNotificationTask extends MailTask {

	@ManyToOne
	public User follower;
	@ManyToOne
	public User followed;
	
	public FollowNotificationTask(String to, User follower, User followed) {
		super(to);
		this.follower = follower;
		this.followed = followed;
	}
}
