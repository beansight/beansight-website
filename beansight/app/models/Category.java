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
public class Category extends Model {

	public String label;

	/** Insights of the category */
	@OneToMany(mappedBy="category", cascade = CascadeType.ALL)
	public List<Insight> insights;
	
	public Category(String label) {
		this.label = label;
	}
	
	public static List<Category> getAllCategories() {
		return Category.findAll();
	}
	
	public String toString() {
	    return label;
	}


}
