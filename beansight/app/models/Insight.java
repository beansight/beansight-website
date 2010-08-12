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

	/** Comments made to current insight */
	@OneToMany(mappedBy="insight", cascade = CascadeType.ALL)
	public List<Comment> comments;
	
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

	/**
	 * Tells if the current insight was created by the given User
	 * 
	 * @param user
	 * @return
	 */
	public boolean isCreator(User user) {
		if(creator.equals(user))
			return true;
		return false;
	}
	
	/**
	 * Add a comment to the current insight
	 * @param content
	 * @param user
	 */
	public void addComment(String content, User user) {
		Comment comment = new Comment(user, this, content);
		comment.save();
	}
	
	public String toString() {
		return content;
	}
	

}
