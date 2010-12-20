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
public class UserCategoryScore extends Model {

	/** Score of the user in the pointed category */
	public double score;
	
	@ManyToOne
	public User user;
	
	@ManyToOne
	public Category category;

	/** Date of the last update of the score */
	public Date lastupdate;
	
	public UserCategoryScore(User user, Category category) {
		this.user = user;
		this.category = category;
	}
}
