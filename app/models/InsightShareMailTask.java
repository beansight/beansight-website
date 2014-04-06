package models;

import play.*;
import play.db.jpa.*;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.*;

@Entity
public class InsightShareMailTask extends MailTask {

	@OneToOne
	public InsightShare share;
	
	public InsightShareMailTask(InsightShare share) {
		super(share.toUser.email, share.toUser.uiLanguage.label);
		this.share = share;
	}
}
