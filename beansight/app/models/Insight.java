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

	/** Every vote of the current insight */
	@OneToMany(mappedBy = "insight", cascade = CascadeType.ALL)
	public List<Vote> votes;
	
	/** Every tag of the current insight */
	@OneToMany(mappedBy = "insight", cascade = CascadeType.ALL)
	public List<Tag> tags;

	/** Users who follow the current insight */
	@ManyToMany(mappedBy = "followedInsights", cascade = CascadeType.ALL)
	public List<User> followers;

	/*
	model denormalization : 
	having to count agree and disagree each time you need to access an insight is a performance killer 
	*/
	/** current number of active "agree" votes (if someone changed his mind, it is not counted) */
	public long agreeCount;
	/** current number of active "disagree" votes (if someone changed his mind, it is not counted) */
	public long disagreeCount;
	
	public Insight(User creator, String content, Date endDate) {
		this.creator = creator;
		this.creationDate = new Date();
		this.endDate = endDate;
		this.content = content;
	}

	
	public boolean isCreator(User user) {
		if(creator.equals(user))
			return true;
		return false;
	}
	
	
	public String toString() {
		return content;
	}
	

}
