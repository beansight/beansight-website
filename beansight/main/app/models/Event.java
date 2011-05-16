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
	@Lob
	public String callToAction;

	public String shareCallToAction;
	
	@Index(name = "EVENT_UNIQUE_ID_IXD")
	public String uniqueId;
	
	
	public String imageThumbURL;
	public String imageBackgroundLeftURL;
	public String imageBackgroundRightURL;
	
	@ManyToOne
	public Topic topic;
	
	@ManyToOne
	public Insight insight1;
	public String insight1Tagline;
	@ManyToOne
	public Insight insight2;
	public String insight2Tagline;
	@ManyToOne
	public Insight insight3;
	public String insight3Tagline;
	@ManyToOne
	public Insight insight4;
	public String insight4Tagline;
	@ManyToOne
	public Insight insight5;
	public String insight5Tagline;
	

	public Date creationDate;
	public Date endDate;
	
	public List<InsightLine> getInsights() {
		List<InsightLine> list = new ArrayList<InsightLine>();
		if(insight1 != null) {
			list.add(new InsightLine(insight1, insight1Tagline));
		}
		if(insight2 != null) {
			list.add(new InsightLine(insight2, insight2Tagline));
		}
		if(insight3 != null) {
			list.add(new InsightLine(insight3, insight3Tagline));
		}
		if(insight4 != null) {
			list.add(new InsightLine(insight4, insight4Tagline));
		}
		if(insight5 != null) {
			list.add(new InsightLine(insight5, insight5Tagline));
		}
		return list;
	}
	
	public String toString() {
	    return title;
	}
	
	public class InsightLine {
		public Insight insight;
		public String tagLine;
		public InsightLine(Insight insight, String tagLine) {
			this.insight = insight;
			this.tagLine = tagLine;
		}
		
	}
	
}
