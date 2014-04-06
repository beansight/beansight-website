package models;

import play.*;
import play.data.validation.Required;
import play.db.jpa.*;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.*;

@Entity
public class ContactMailTask extends MailTask {

	@Required
	public String name;
	
	@Required
	public String fromEmail;
	
	@Required
	public String subject;
	
	@Required
	@Lob
	public String message;
	
	public ContactMailTask(String name, String from, String to, String subject, String message) {
		super(to);
		this.name = name;
		this.fromEmail = from;
		this.subject = subject;
		this.message = message;
	}
	
}
