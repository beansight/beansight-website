package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Index;

import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

@Entity
public class Event extends Model {

	public String title;
	
	@Lob
	public String message;
	
	@Index(name = "EVENT_UNIQUE_ID_IXD")
	public String uniqueId;
	
	@ManyToMany(cascade = CascadeType.ALL)
	public List<Insight> insights;

	public Date creationDate;
	public Date endDate;
	
	public Event(String title, String message, String uniqueId, List<Insight> insights) {
		this.title = title;
		this.message = message;
		this.uniqueId = uniqueId;
		this.insights = insights;
		this.creationDate = new Date();
		this.endDate = new Date();
	}
	
	public String toString() {
	    return title;
	}

}
