package models;

import play.*;
import play.db.jpa.*;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.*;

@Entity
public class CommentNotificationMailTask extends MailTask {

	@OneToOne
	public CommentNotificationMessage commentNotificationMsg;
	
	public CommentNotificationMailTask(CommentNotificationMessage commentNotificationMsg) {
		super(commentNotificationMsg.toUser.email, commentNotificationMsg.toUser.writtingLanguage.label);
		this.commentNotificationMsg = commentNotificationMsg;
	}
}
