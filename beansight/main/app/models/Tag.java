package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

@Entity
public class Tag extends Model {

	public String label;

	@ManyToMany(cascade = CascadeType.ALL)
	public List<User> users;

	@ManyToMany(cascade = CascadeType.ALL)
	public List<Insight> insights;

	/** the date this vote has been made */
	public Date creationDate;

	public Tag(String label, Insight insight, User user) {
		this.users = new ArrayList<User>();
		this.users.add(user);
		this.insights = new ArrayList<Insight>();
		this.insights.add(insight);
		this.label = label;
		this.creationDate = new Date();
	}

	public static String listToIdString(Set<Tag> tags) {
		StringBuffer buffer = new StringBuffer();
        Iterator<Tag> iter = tags.iterator();
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
	
	public String toString() {
	    return label;
	}


}
