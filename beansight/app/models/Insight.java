package models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Insight extends Model {

	@ManyToOne
	public User creator;

	/** the date this insight has been created by its creator */
	public Date creationDate;

	/** the date this insight is ending, defined by its creator */
	public Date endDate;

	/** Content of the insight, a simple text describing the idea */
	public String content;

	/** Every votes of the current insight */
	@OneToMany(mappedBy = "insight", cascade = CascadeType.ALL)
	public List<Vote> votes;

	/** Users who follow the current insight */
	@ManyToMany(mappedBy = "followedInsights", cascade = CascadeType.ALL)
	public List<User> followers;

	
	public Insight(User creator, String content, Date endDate) {
		super();
		this.creator = creator;
		this.endDate = endDate;
		this.content = content;
	}
	
}
