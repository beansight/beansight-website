package models;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

	public static Category findByLabel(String label) {
		return Category.find("byLabel", label).first();
	}
	
	public String toString() {
	    return label;
	}
	
	public static String listToIdString(Set<Category> categories) {
        StringBuffer buffer = new StringBuffer();
        Iterator<Category> iter = categories.iterator();
        while (iter.hasNext()) {
        	buffer.append("'");
            buffer.append(iter.next().id);
            buffer.append("'");
            if (iter.hasNext()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
	}

}
