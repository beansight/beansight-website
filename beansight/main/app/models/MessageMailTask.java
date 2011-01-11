package models;

import play.*;
import play.db.jpa.*;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.*;

@Entity
public class MessageMailTask extends MailTask {

	@OneToOne
	public Message message;
	
	public MessageMailTask(Message message) {
		super(message.toUser.email);
		this.message = message;
	}
}
