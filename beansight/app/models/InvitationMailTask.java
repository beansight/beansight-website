package models;

import play.*;
import play.db.jpa.*;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.*;

@Entity
public class InvitationMailTask extends MailTask {

	@OneToOne
	public Invitation invitation;
	
	public InvitationMailTask(Invitation invitation) {
		super(invitation.invitedEmail);
		this.invitation = invitation;
	}
}
