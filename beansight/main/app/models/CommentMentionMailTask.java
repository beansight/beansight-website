package models;

import play.*;
import play.db.jpa.*;
import play.i18n.Messages;

import javax.persistence.*;

import notifiers.Mails;

import java.util.*;
import java.util.concurrent.Future;

@Entity
public class CommentMentionMailTask extends MailTask {

	@OneToOne
	public CommentMentionNotification commentNotificationMsg;
	
	public CommentMentionMailTask(CommentMentionNotification commentNotificationMsg) {
		super(commentNotificationMsg.toUser.email, commentNotificationMsg.toUser.uiLanguage.label);
		this.commentNotificationMsg = commentNotificationMsg;
	}
}
