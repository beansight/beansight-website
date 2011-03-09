package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class NewCommentNotification extends Model {

	@ManyToOne
	public User toUser;
	@ManyToOne
	public Comment comment;
	
	public NewCommentNotification(User toUser, Comment comment) {
		this.toUser = toUser;
		this.comment = comment;
	}

}
