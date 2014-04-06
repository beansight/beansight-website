package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class UserInsightScore extends Model {

	public Double score;
	
	@ManyToOne
	public User user;
	
	
	@ManyToOne
	public Insight insight;

	public Date lastUpdate;
	
	public UserInsightScore(User user, Insight insight) {
		this.user = user;
		this.insight = insight;
	}
}
