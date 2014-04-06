package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class CommentMentionNotification extends Model {

	@ManyToOne
	public Insight insight;
	
	@ManyToOne
	public User toUser;
	@ManyToOne
	public User fromUser;
	
	@ManyToOne
	public Comment comment;
	
	public CommentMentionNotification(Insight insight, User from, User to, Comment comment) {
		this.insight = insight;
		this.toUser = to;
		this.fromUser = from;
		this.comment = comment;
	}

}
