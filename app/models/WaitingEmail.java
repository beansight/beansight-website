package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class WaitingEmail extends Model {

	public String email;
	public boolean invitationSent;

	/** the date this email has been saved */
	public Date emailDate;

	public WaitingEmail(String email) {
		this.email = email;
		invitationSent = false;
		this.emailDate = new Date();
	}
	
	public String toString() {
	    return email;
	}


}
