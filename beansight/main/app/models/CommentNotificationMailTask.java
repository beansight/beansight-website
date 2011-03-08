package models;

import play.*;
import play.db.jpa.*;
import play.i18n.Messages;

import javax.persistence.*;

import notifiers.Mails;

import java.util.*;
import java.util.concurrent.Future;

@Entity
public class CommentNotificationMailTask extends MailTask {

	@OneToOne
	public CommentNotificationMessage commentNotificationMsg;
	
	public CommentNotificationMailTask(CommentNotificationMessage commentNotificationMsg) {
		super(commentNotificationMsg.toUser.email, commentNotificationMsg.toUser.writtingLanguage.label);
		this.commentNotificationMsg = commentNotificationMsg;
	}
}
