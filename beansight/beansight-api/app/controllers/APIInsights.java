package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Category;
import models.Comment;
import models.Filter;
import models.Filter.FilterVote;
import models.Insight;
import models.Insight.InsightResult;
import models.Language;
import models.Tag;
import models.User;
import models.Vote;
import models.Vote.State;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import exceptions.CannotVoteTwiceForTheSameInsightException;

public class APIInsights extends APIController {

	static SimpleDateFormat dateFormatFrench = new SimpleDateFormat("dd/MM/yyyy");
	
	public static class InsightItem {
		public String 	id;
		public String 	content;
		public Long 	creationDate;
		public Long 	endDate;
		public String 	creator;
		public Long   	category;
		public Long	  	agreeCount;
		public Long	  	disagreeCount;
		public Long	  	commentCount;
		public String 	lastCurrentUserVote;
		
		public InsightItem(Insight insight) {
			id = insight.uniqueId;
			content = insight.content;
			creationDate = insight.creationDate.getTime();
			endDate = insight.endDate.getTime();
			creator = insight.creator.userName;
			category = insight.category.id;
			agreeCount = insight.agreeCount;
			disagreeCount = insight.disagreeCount;
			commentCount = (long) insight.comments.size();
			
			User currentUser = getUserFromAccessToken();
			if (currentUser != null) {
				Vote lastUserVote = Vote.findLastVoteByUserAndInsight(currentUser.id, insight.uniqueId);
				if (lastUserVote != null) {
					if (lastUserVote.state.equals(State.AGREE)) {
						lastCurrentUserVote = "agree";
					} else {
						lastCurrentUserVote = "disagree";
					}
				} else {
					lastCurrentUserVote = "non-voted";
				}
			}
		}
		
		public static List<InsightItem> insightListToInsightItemList(List<Insight> insightList) {
			List<InsightItem> insights = new ArrayList();
			for (Insight insight : insightList) {
				InsightItem insightItem = new InsightItem(insight);
				insights.add(insightItem);
			}
			return insights;
		}
	}
	
	public static class InsightDetail {
		public String 	id;
		public String 	content;
		public Long 	creationDate;
		public Long 	endDate;
		public String 	creator;
		public Long   	category;
		public Long	  	agreeCount;
		public Long	  	disagreeCount;
		public Long	  	commentCount;
		public String 	lastCurrentUserVote;
		public Double	occurenceScore;
		public boolean 	validated;
		public List<String> tags = new ArrayList<String>();
		
		public InsightDetail(Insight insight) {
			id = insight.uniqueId;
			content = insight.content;
			creationDate = insight.creationDate.getTime();
			endDate = insight.endDate.getTime();
			creator = insight.creator.userName;
			category = insight.category.id;
			agreeCount = insight.agreeCount;
			disagreeCount = insight.disagreeCount;
			commentCount = (long) insight.comments.size();
			validated = insight.validated;
			if (insight.validated) {
				occurenceScore = insight.getValidationScore();
			} else {
				occurenceScore = insight.occurenceScore;
			}
			
			for(Tag tag : insight.tags) {
				tags.add(tag.label);
			}
			
			User currentUser = getUserFromAccessToken();
			if (currentUser != null) {
				Vote lastUserVote = Vote.findLastVoteByUserAndInsight(currentUser.id, insight.uniqueId);
				if (lastUserVote != null) {
					if (lastUserVote.state.equals(State.AGREE)) {
						lastCurrentUserVote = "agree";
					} else {
						lastCurrentUserVote = "disagree";
					}
				}
			}
		}
		
	}
	
	
	public static class InsightComment {
		public String 	author;
		public Long 	creationDate;
		public String 	content;
		
		public static InsightComment commentToInsightComment(Comment comment) {
			InsightComment insightComment = new InsightComment();
			insightComment.content = comment.content;
			insightComment.creationDate = comment.creationDate.getTime();
			insightComment.author = comment.user.userName;
			return insightComment;
		}
		
		
		public static List<InsightComment> commentListToInsightCommentList(List<Comment> commentList) {
			List<InsightComment> insightCommentList = new ArrayList<APIInsights.InsightComment>();
			for (Comment comment : commentList) {
				insightCommentList.add(commentToInsightComment(comment));
			}
			return insightCommentList;
		}
	}
	
	// TODO : what if the content evolves between two calls ?
	// Maybe a better solution would be to give the uniqueId of the latest
	// downloaded insight
	/**
	 * Get a list of insights<br/>
	 * <b>response:</b> <code>[{id, content, creationDate, endDate, creator, category, agreeCount, disagreeCount, commentCount, lastCurrentUserVote}, ...]</code>
	 * 
	 * @param from
	 *            index of the first insight to return, default = 0
	 * @param number
	 *            number of insights to return, default = 20
	 * 
	 * @param sort
	 *            possible values : ["updated", "trending", "incoming"]
	 *            (String), default = "updated"
	 * @param category
	 *            id of the category to restrict to, default = null
	 * @param vote
	 *            filter by vote state, possible values : ["all", "voted",
	 *            "non-voted"] (String), default = "all"
	 * @param topic
	 *            String of the topic, default = null
	 * @param closed
	 *            true to return closed insights, default = false
	 */
	public static void list(@Min(0) Integer from,
			@Min(1) @Max(100) Integer number, String sort, Integer category,
			String vote, String topic, Boolean closed) {

		if (validation.hasErrors()) {
			badRequest();
		}
		if (from == null) {
			from = 0;
		}
		if (number == null) {
			number = 20;
		}
		if (sort == null) {
			sort = "updated";
		} else {
			if(!sort.equals("updated") && !sort.equals("trending") && !sort.equals("incoming")) {
				badRequest();
			}
		}
		if (vote == null) {
			vote = "all";
		} else {
			if( !vote.equals("all") && !vote.equals("voted") && !vote.equals("non-voted")) {
				badRequest();
			}
		}
		if (closed == null) {
			closed = false;
		}

		InsightResult result = null;
		Filter filter = new Filter();
		filter.languages.add(Language.findByLabelOrCreate("en"));
		filter.languages.add(Language.findByLabelOrCreate("fr"));

		filter.user = getUserFromAccessToken();
		if( vote.equals("voted") ) {
			filter.vote = FilterVote.VOTED;
		} else if ( vote.equals("non-voted") ) {
			filter.vote = FilterVote.NONVOTED;
		} else {
			filter.vote = FilterVote.ALL;
		}
		
		if(topic != null) {
			Tag top = Tag.findByLabel(topic);
			if(top != null) {
				for(Tag tag : top.getContainedTags()) {
					filter.tags.add(tag);
				}
			}
		}
		
		filter.closed = closed;
		
		if (category != null) {
			Category cat = Category.findById(category);
			if(cat != null) {
				filter.categories.add(cat);
			}
		}

		if (sort.equals("trending")) {
			result = Insight.findTrending(from, number, filter);
		} else if (sort.equals("incoming")) {
			result = Insight.findIncoming(from, number, filter);
		} else {
			result = Insight.findLatest(from, number, filter);
		}

		renderAPI(InsightItem.insightListToInsightItemList(result.results));
	}

	/**
	 * Get detailed information about a given insight<br/>
	 * <b>response:</b> <code>{id, content, creationDate, endDate, creator, category, agreeCount, disagreeCount, commentCount, lastCurrentUserVote, occurenceScore, validated, tags[]}</code>
	 * 
	 * @param id : unique ID of this insight
	 */
	public static void show(@Required String id) {
		if (validation.hasErrors()) {
			badRequest();
		}
		Insight insight = Insight.findByUniqueId(id);
		notFoundIfNull(insight);
		
		InsightDetail apiResult = new InsightDetail(insight);

		renderAPI(apiResult);
	}

	/**
	 * The current user agree a given insight<br/>
	 * <b>Authentication required</b><br/> 
	 * <b>response:</b> <code>{id, updatedAgreeCount, updatedDisagreeCount, voteState}</code>
	 * 
	 * @param id : unique ID of this insight
	 */
	public static void agree(@Required String id) {
		checkAccessToken();
		vote(id, State.AGREE);
	}

	/**
	 * The current user disagree a given insight<br/>
	 * <b>Authentication required</b><br/> 
	 * <b>response:</b> <code>{id, updatedAgreeCount, updatedDisagreeCount, voteState}</code>
	 * 
	 * @param id : unique ID of this insight
	 */
	public static void disagree(@Required String id) {
		checkAccessToken();
		vote(id, State.DISAGREE);
	}

	private static void vote(String insightUniqueId, State voteState) {
		User currentUser = getUserFromAccessToken();

		try {
			currentUser.voteToInsight(insightUniqueId, voteState);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			// its ok, do not show anything
		}

		Insight insight = Insight.findByUniqueId(insightUniqueId);

		Map<String, Object> jsonResult = new HashMap<String, Object>();
		jsonResult.put("id", insight.uniqueId);
		jsonResult.put("updatedAgreeCount", insight.agreeCount);
		jsonResult.put("updatedDisagreeCount", insight.disagreeCount);
		if (voteState.equals(State.AGREE)) {
			jsonResult.put("voteState", "agree");
		} else {
			jsonResult.put("voteState", "disagree");
		}

		renderAPI(jsonResult);
	}
	
	/**
	 * Get a list of all the categories <br/>
	 * <b>response:</b> <code>[{label, id}, ...]</code>
	 */
	public static void categories() {
		List<Category> categories = Category.findAll();
		
		List<Object[]> allCategories = new ArrayList<Object[]>();
		
		for (Category cat : categories) {
			allCategories.add(new Object[] {cat.label, cat.id});
		}
		
		renderAPI(allCategories);
	}

	/**
	 * Get a list of all the comments for a given insigh <br/>
	 * b>response:</b> <code>[{author, creationDate, content}, ...]</code>
	 * @param id
	 */
	public static void comments(@Required String id) {
		renderAPI(InsightComment.commentListToInsightCommentList(Insight.findByUniqueId(id).comments));
	}
	
}
