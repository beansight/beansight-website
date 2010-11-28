package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import models.Vote.Status;

import play.Logger;
import play.data.validation.Required;
import play.db.jpa.JPASupport;
import play.db.jpa.Model;
import play.modules.search.*;
import play.modules.search.Search.Query;

@Indexed
@Entity
public class Insight extends Model {

	@ManyToOne
	@Required
	public User creator;

	/** the date this insight has been created by its creator */
	// TODO index this (unfortunately simply adding @Field transforms the date
	// in string)
	public Date creationDate;

	/** the date this insight is ending, defined by its creator */
	// TODO index this
	@Required
	public Date endDate;

	/** Content of the insight, a simple text describing the idea */
	@Field
	@Required
	public String content;

	/** Every vote of the current insight */
	@OneToMany(mappedBy = "insight", cascade = CascadeType.ALL)
	public List<Vote> votes;

	/** Every tag of the current insight */
	@ManyToMany(mappedBy = "insights", cascade = CascadeType.ALL)
	@Field
	public List<Tag> tags;

	@ManyToOne
	@Field
	@Required
	/** Category of this insight */
	public Category category;

	/** Users who follow the current insight */
	@ManyToMany(mappedBy = "followedInsights", cascade = CascadeType.ALL)
	public List<User> followers;

	/** Comments made to current insight */
	@OneToMany(mappedBy = "insight", cascade = CascadeType.ALL)
	public List<Comment> comments;

	// model denormalization :
	// having to count agree and disagree each time you need to access an
	// insight is a performance killer
	/**
	 * current number of active "agree" votes (if someone changed his mind, it
	 * is not counted)
	 */
	public long agreeCount;
	/**
	 * current number of active "disagree" votes (if someone changed his mind,
	 * it is not counted)
	 */
	public long disagreeCount;
	/** the last time when someone voted for the insight */
	public Date lastUpdated;

	@OneToMany(mappedBy = "insight", cascade = CascadeType.ALL)
	@OrderBy(value = "trendDate")
	public List<Trend> trends;
	
	/**
	 * Create an insight
	 * 
	 * @param creator
	 * @param content
	 *            : content text of this insight
	 * @param endDate
	 *            : date this insight is supposed to end
	 * @param category
	 *            : the category of the insight
	 */
	public Insight(User creator, String content, Date endDate, Category category) {
		this.creator = creator;
		this.creationDate = new Date();
		this.endDate = endDate;
		this.content = content;
		this.followers = new ArrayList<User>();
		this.comments = new ArrayList<Comment>();
		this.category = category;
		this.trends = new ArrayList<Trend>();
		this.lastUpdated = new Date();
	}

	/**
	 * Tells if the current insight was created by the given User
	 * 
	 * @param user
	 * @return
	 */
	public boolean isCreator(User user) {
		if (creator.equals(user)) {
			return true;
		}
		return false;
	}

	/**
	 * a user adds tags from an input string.
	 * 
	 * @param tagLabelList
	 *            : list of tag labels separated by commas and spaces
	 * @param user
	 *            : the user adding the tag
	 */
	public void addTags(String tagLabelList, User user) {
		String[] labelArray = tagLabelList.split(",");

		for (int i = 0; i < labelArray.length; i++) {
			String label = labelArray[i].trim();
			this.addTag(label, user);
		}
	}

	/**
	 * Add a tag from a given label string, will check if tag already exists for
	 * this insight
	 * 
	 * @param label
	 *            : the label of the tag (will not be processed)
	 * @param user
	 *            : the user adding the tag
	 */
	private void addTag(String label, User user) {
		// TODO call here a method to normalize the label

		// check if this tag already exist for this insight
		boolean foundTag = false;
		if (this.tags != null) {
			for (Tag storedTag : this.tags) {
				if (storedTag.label.equalsIgnoreCase(label)) {
					storedTag.users.add(user);
					storedTag.save();
					foundTag = true;
					break;
				}
			}
		}
		// if not, check if this tag already exist on the website
		if (!foundTag) {
			Tag existTag = Tag.find("byLabel", label).first();
			if (existTag == null) {
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
	 * 
	 * @param content
	 * @param user
	 */
	public Comment addComment(String content, User user) {
		Comment comment = new Comment(user, this, content);
		comment.save();
		return comment;
	}

	/**
	 * get the list of the n last active votes for this Insight
	 * 
	 * @param n
	 *            : the maximum number of votes to return
	 * @return: the list of n most recent active votes
	 */
	public List<Vote> getLastVotes(int n) {
		return Vote.find(
				"select v from Vote v " + "join v.insight i "
						+ "where v.status = :status and i.id=:insightId "
						+ "order by v.creationDate DESC").bind("status",
				Status.ACTIVE).bind("insightId", this.id).fetch(n);
	}

	/**
	 * Performs a search action
	 * 
	 * @param query
	 *            : the search query
	 * @param from
	 *            : index of the first item to be returned
	 * @param number
	 *            : number of items to return
	 * @param category
	 *            : the category to restrict the search to (null
	 * @return : an object containing the result list and the total result
	 *         number
	 */
	public static InsightResult search(String query, int from, int number,
			Category category) {
		// TODO Steren : this query string construction is temporary, we should
		// better handle this
		String fullQueryString = "(content:" + query + " OR tags:" + query
				+ ") ";
		if (category != null) {
			fullQueryString += " AND category:" + category.label;
		}

		Query q = Search.search(fullQueryString, Insight.class);

		// create the result object
		InsightResult result = new InsightResult();
		result.count = q.count();

		// restrict to a sub group
		q.page(from, number);

		result.results = q.fetch();

		return result;
	}

	/**
	 * 
	 * @param from
	 * @param number
	 * @param category
	 * @return
	 */
	public static InsightResult getLatest(int from, int number,
			Category category) {

		String query = "";
		if (category != null) {
			query += "select i from Insight i join i.category c where c.id="
					+ category.id;
		}
		query += " order by creationDate DESC";

		InsightResult result = new InsightResult();
		// TODO : return total number using count ?
		// result.count = Insight.count(query);

		List<Insight> insights = Insight.find(query).from(from).fetch(number);
		result.results = insights;

		return result;
	}

	public void createTrendSnapshot() {
	    addTrend(new Trend(new Date(), this, this.agreeCount, this.disagreeCount));
	}
	
	public void addTrend(Trend trend) {
	    trends.add(trend);
	}
	
    public static long getTrendCountForInsight(long insightId) {
        return find("select count(t) from Trend t join t.insight i where i.id = :insightId").bind("insightId", insightId).first();
   }
	
    /**
     * 
     * 
     * @param horizontalDefinition Number of horizontal value that will be used to create the charts
     * @return
     */
    public List<Double> getAgreeRatioTrends(long horizontalDefinition) {
        long trendsCount = getTrendCountForInsight(this.id);
                
        List<Double> agreeTrends;
        if (trendsCount <= horizontalDefinition) {
            agreeTrends = find("select t.agreeRatio from Trend t join t.insight i where i.id = :insightId order by t.trendDate").bind("insightId", this.id)
                    .fetch();
        } else {
            long incrementSize = (trendsCount - 2) / horizontalDefinition;
            List<Long> indexList = new ArrayList<Long>((int)horizontalDefinition);
            for (int i = 1 ; i<horizontalDefinition ; i++) {
                indexList.add(i * incrementSize + 1);
            }
            
            agreeTrends = find(
                    "select t.agreeRatio from Trend t join t.insight i where i.id = :insightId and t.relativeIndex in (:indexList) order by t.trendDate")
                    .bind("insightId", this.id).bind("indexList", indexList).fetch();
        }

        return agreeTrends;
    }
    
	public static class InsightResult {

		public List<Insight> results;
		public long count;
	}

	public String toString() {
		return content;
	}
}
