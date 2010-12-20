package models;

import play.*;
import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class Invitation extends Model {
    
	@ManyToOne
	@Required
	public User invitor;
	
	@Email
	@Required
	public String invitedEmail;
	
	@Lob
	public String message;

	public Invitation(User invitor, String invitedEmail, String message) {
		this.invitor = invitor;
		this.invitedEmail = invitedEmail;
		this.message = message;
	}
	
	
	
}
