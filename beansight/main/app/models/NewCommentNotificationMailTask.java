package models;

import play.*;
import play.db.jpa.*;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.*;

@Entity
public class NewCommentNotificationMailTask extends MailTask {

	@OneToOne
	public NewCommentNotification notification;
	
	public NewCommentNotificationMailTask(NewCommentNotification notification) {
		super(notification.toUser.email, notification.comment.user.uiLanguage.label);
		this.notification = notification;
	}
}
