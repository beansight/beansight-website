package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Vote extends Model {

	@ManyToOne
	public User user;

	@ManyToOne
	public Insight insight;

	/** the date this vote has been made */
	public Date creationDate;

}
