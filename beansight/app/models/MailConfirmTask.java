package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class MailConfirmTask extends MailTask {

	public MailConfirmTask(User to) {
		super(to);
	}
    
}
