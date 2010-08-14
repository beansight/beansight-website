package models;

import java.util.ArrayList;
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
	@ManyToMany(mappedBy = "insights", cascade = CascadeType.ALL)
	public List<Tag> tags;

	@ManyToOne
	/** Category of this insight */
	public Category category;
	
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
	
	/**
	 * Create an insight
	 * @param creator
	 * @param content: content text of this insight
	 * @param endDate: date this insight is supposed to end
	 * @param category: the category of the insight
	 */
	public Insight(User creator, String content, Date endDate, Category category) {
		this.creator = creator;
		this.creationDate = new Date();
		this.endDate = endDate;
		this.content = content;
		this.category = category;
	}

	/**
	 * Tells if the current insight was created by the given User
	 * 
	 * @param user
	 * @return
	 */
	public boolean isCreator(User user) {
		if(creator.equals(user)) {
			return true;
		}
		return false;
	}
	
	/**
	 * a user adds tags from an input string.
	 * @param tagLabelList: list of tag labels separated by commas and spaces
	 * @param user: the user adding the tag
	 */
	public void addTags(String tagLabelList, User user) {
    	String[] labelArray = tagLabelList.split(",");
		
		for( int i=0; i < labelArray.length; i++ ) {
			String label = labelArray[i].trim();
			this.addTag(label, user);
		}
	}
	
	/**
	 * Add a tag from a given label string, will check if tag already exists for this insight
	 * @param label: the label of the tag (will not be processed)
	 * @param user: the user adding the tag
	 */
	private void addTag(String label, User user) {
		// TODO call here a method to normalize the label

		// check if this tag already exist for this insight
		boolean foundTag = false;
		if(this.tags != null) {
			for(Tag storedTag : this.tags) {
				if(storedTag.label.equals(label)) {
					storedTag.users.add(user);
					storedTag.save();
					foundTag = true;
					break;
				}
			}
		}
		// if not, check if this tag already exist on the website
		if(!foundTag) {
			Tag existTag = Tag.find("byLabel", label).first();
			if(existTag == null) {
				// if null, then create it.
				Tag newTag = new Tag(label, this, user);
				newTag.save();
			} else {
				System.out.println("FFFOUND:" + existTag.label);
				// if found, then associate with this insight and this user.
				existTag.insights.add(this);
				existTag.users.add(user);
				existTag.save();
			}
		}
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
