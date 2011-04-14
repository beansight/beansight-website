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

	@Enumerated(EnumType.STRING)
	public PeriodEnum period;
	
	/** Score of the user in the pointed category */
	public Double score;

	/** normalized score of this user in this category (between 0 and 1) */
	public Double normalizedScore;
	
	@ManyToOne
	public UserScoreHistoric historic;
	
	@ManyToOne
	public Category category;

	/** Date of the last update of the score */
	public Date lastupdate;

	public UserCategoryScore(User user, Category category, UserScoreHistoric userScoreHisto, PeriodEnum period) {
		this.category = category;
		this.score = null;
		this.normalizedScore = null;
		this.historic = userScoreHisto;
		this.historic.categoryScores.add(this);
		this.period = period;
	}

	public void computeNormalizedScore() {
		if (this.score != null) {
			if(this.category.scoreMax > this.category.scoreMin) {
				this.normalizedScore = (this.score - this.category.scoreMin) / (this.category.scoreMax - this.category.scoreMin);
			} else {
				this.normalizedScore = null;
			}
		}
	}

}
