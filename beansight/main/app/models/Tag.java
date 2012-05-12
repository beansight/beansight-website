package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import controllers.CRUD.Hidden;

import models.Insight.InsightResult;

import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

/**
 * A tag is a label linked to many insights. It represents a topic (exemple: "apple")
 * A tag can have many children tags.  Children tag are considered as "sub-topics" (exemple: "iphone", "iOS", "Steve Jobs")
 */
@Entity
public class Tag extends Model {

	public String label;
	
	/**
	 * true if this tag should be considered as deleted.
	 */
	public boolean deleted;

	@ManyToMany(cascade = CascadeType.ALL)
	public Set<Tag> children;
	
	/** the insights this tag refers to */
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Hidden
	public Set<Insight> insights;

	/** the date this vote has been made */
	public Date creationDate;

	public Tag(String label) {
		this.label = label;
		this.insights = new HashSet<Insight>();
		this.children = new HashSet<Tag>();
		this.creationDate = new Date();
	}
	
	public Tag(String label, Insight insight) {
		this.label = label;
		this.insights = new HashSet<Insight>();
		this.children = new HashSet<Tag>();
		this.insights.add(insight);
		this.creationDate = new Date();
	}

	/**
	 * Find the tag by label, or create it if not found.
	 * @param label : the label wanted
	 * @param insight : the insight associated with this tag. Can be null for no insight
	 * @return the found or created tag
	 */
	public static Tag findByLabelOrCreate(String label) {
		Tag resultTag = Tag.find("byLabel", label).first();
		if (resultTag == null) {
			// if null, then create it.
			resultTag = new Tag(label);
			resultTag.save();
		}
		
		return resultTag;
	}
	
	/**
	 * Get all the tags contained by this one. (this one, its children, the children of its children...)
	 */
	public List<Tag> getContainedTags() {
		List<Tag> result = new ArrayList<Tag>();
		addSubTags(result);
		return result;
	}
	/** reccursive method used to get all the sub-children of a tag */
	private void addSubTags(List<Tag> tags) {
		tags.add(this);
		for(Tag t : this.children) {
			if( !tags.contains(t) ) {
				t.addSubTags(tags);
			}
		}
	}
	
	/** Set this topic as a featured topic*/
	public void feature(Language language) {
		new FeaturedTag(this, language).save();
	}
	
	/** Set the endDate of all the Featured Topics to today */
	public void stopFeature(Language language) {
		List<FeaturedTag> tags = FeaturedTag.find("byTagAndLanguage", this, language).fetch();
		for(FeaturedTag tag : tags) {
			tag.endDate = new Date();
			tag.save();
		}
	}
	
	/** Used to generated SQL requests */
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
	
	
	public static Tag findByLabel(String label) {
		return Tag.find("label = ?", label).first();
	}
	
	public String toString() {
	    return label;
	}

	/**
	 * remove this tag from all the insights, set deleted to true
	 */
	public void remove() {
		Filter filter = new Filter();
		filter.tags.add(this);
		int n = 1000;
		InsightResult insightResult = Insight.findLatest(0, n, filter);
		while (insightResult.results != null) {
			for(Insight i : insightResult.results) {
				i.tags.remove(this);
				i.save();
				this.insights.remove(i);
			}
			this.deleted = true;
			this.save();
			n = n + 100;
			insightResult = Insight.findLatest(0, n, filter);
		}
	}

}
