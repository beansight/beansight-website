package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Category;
import models.Filter;
import models.Filter.FilterType;
import models.Insight;
import models.Insight.InsightResult;
import models.Language;
import models.User;
import models.UserCategoryScore;
import models.Vote;
import models.Vote.State;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.libs.WS;
import play.mvc.Router;
import controllers.CurrentUser;
import exceptions.CannotVoteTwiceForTheSameInsightException;

public class APIUsers extends APIController {

	
	
	
	
	public static void profil(String userName) {
		User user = User.findByUserName(userName);
		Profil profil = new Profil();
		profil.userName = user.userName;
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
	
	
	public static void friends(String userName) {
		User user = User.findByUserName(userName);
		
		List<String> friends = new ArrayList<String>();
		for (User friend : user.followedUsers) {
			friends.add(friend.userName);
		}
		
		renderAPI(friends);
	}
	
	
	public static void followers(String userName) {
		User user = User.findByUserName(userName);
		
		List<String> followers = new ArrayList<String>();
		for (User follower : user.followers) {
			followers.add(follower.userName);
		}
		
		renderAPI(followers);
	}
	
	
	
	
	
	public static class Profil {
		public String userName;
		public String avatarSmall;
		public String avatarMedium;
		public String avatarLarge;
		public int successfulPredictionsCount;
		public List<String[]> scores = new ArrayList<String[]>();
	}
	
}
