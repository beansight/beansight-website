package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Message extends Model {

	@ManyToOne
	public User toUser;
	@ManyToOne
	public User fromUser;
	public String content;
	
	public Message(User from, User to, String content) {
		this.toUser = to;
		this.fromUser = from;
		this.content = content;
	}

}
