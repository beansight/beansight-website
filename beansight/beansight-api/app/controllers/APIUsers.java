package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Category;
import models.Insight.InsightResult;
import models.User;
import models.UserCategoryScore;
import models.UserInsightsFilter;
import play.mvc.Router;
import controllers.APIInsights.InsightItemResult;

public class APIUsers extends APIController {

	
	/**
	 * 
	 * @param userName
	 */
	public static void profil(String userName) {
		User user = User.findByUserName(userName);
		Profil profil = new Profil();
		profil.userName = user.userName;
		profil.description = user.description;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userName", userName);
		map.put("code", user.avatarHashCode());
		profil.avatarSmall = Router.getFullUrl("Application.showAvatarSmall", map);
		profil.avatarMedium = Router.getFullUrl("Application.showAvatarMedium", map);
		profil.avatarLarge = Router.getFullUrl("Application.showAvatarLarge", map);
		profil.successfulPredictionsCount = user.successfulPredictionCount;
		List<UserCategoryScore> userCategorieScores = user.getLatestCategoryScores();
		for (UserCategoryScore userCategorieScore : userCategorieScores) {
			profil.scores.add(new String[] {userCategorieScore.category.label, userCategorieScore.normalizedScore.toString()});
		}
		
		renderAPI(profil);
	}
	
	
	/**
	 * 
	 * @param userName
	 */
	public static void friends(String userName) {
		User user = User.findByUserName(userName);
		
		List<String> friends = new ArrayList<String>();
		for (User friend : user.followedUsers) {
			friends.add(friend.userName);
		}
		
		renderAPI(friends);
	}
	
	
	/**
	 * 
	 * @param userName
	 */
	public static void followers(String userName) {
		User user = User.findByUserName(userName);
		
		List<String> followers = new ArrayList<String>();
		for (User follower : user.followers) {
			followers.add(follower.userName);
		}
		
		renderAPI(followers);
	}
	
	public static void insights(String userName, Integer from, Long cat, String filterVote) {
		if (from == null) {
			from = 1;
		}
		if (cat == null) {
			cat = 0l;
		}
		if (filterVote == null || filterVote.trim().equals("")) {
			filterVote = "all";
		}
		
		User user = User.findByUserName(userName);
		InsightResult result = getFilteredUserInsightsList(from, Application.NUMBER_INSIGHTS_INSIGHTPAGE, cat, user, filterVote);
		
		InsightItemResult insightItemResult = new InsightItemResult(result.results);
		
		renderAPI(insightItemResult);
	}
	
	
	private static InsightResult getFilteredUserInsightsList(int from, int numberInsights, long cat, User user, String filterVote) {
		UserInsightsFilter filter = new UserInsightsFilter();

		filter.user = user;
		filter.filterVote = filterVote;
		
		Category category = Category.findById(cat);
		if(category != null) {
			filter.categories.add(category);
		}
		
		InsightResult result = user.getLastInsights(from, numberInsights, filter);

		return result;
	}

	
	// --------------
	
	public static class Profil {
		public String userName;
		public String description;
		public String avatarSmall;
		public String avatarMedium;
		public String avatarLarge;
		public int successfulPredictionsCount;
		public List<String[]> scores = new ArrayList<String[]>();
	}

}