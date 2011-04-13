package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;

import play.db.jpa.Model;

@Entity
@Table(
		uniqueConstraints=@UniqueConstraint(columnNames={"scoreDate","user_id"}, name="uniqueUserScoreByDate")
)
public class UserScoreHistoric extends Model {
	
	public Date scoreDate;
	
	@ManyToOne
	public User user;
	
 	@OneToMany(mappedBy = "historic", cascade = CascadeType.ALL)
	@OrderBy("normalizedScore DESC")
	public List<UserCategoryScore> categoryScores;

// 	@OneToMany(mappedBy = "historic", cascade = CascadeType.ALL)
//	@OrderBy("score DESC")
//	public List<UserInsightScore> insightScores;
 	
	public UserScoreHistoric(Date scoreDate, User user) {
		super();
		this.scoreDate = scoreDate;
		this.user = user;
		this.categoryScores = new ArrayList<UserCategoryScore>();
//		this.insightScores = new ArrayList<UserInsightScore>();
	}
	
    
}
