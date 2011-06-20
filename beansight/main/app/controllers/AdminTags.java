package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import models.FeaturedTag;
import models.Insight;
import models.InsightActivity;
import models.Language;
import models.Tag;
import models.TagActivity;
import models.User;
import models.UserActivity;
import play.modules.search.Search;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Administration features that concern Tags (and topic)
 *
 */
@With(Secure.class)
@Check("admin")
public class AdminTags extends Controller {
	
	/** main tags page */
	public static void tags() {
		List<Tag> tags = Tag.find("order by creationDate desc").fetch(50);
		
		List<Language> languages = Language.findAll();
		List<FeaturedTag> featuredTags = FeaturedTag.findActive(languages);
		
		render(tags, featuredTags);
	}
	
	public static void showTagByLabel(String label) {
		Tag tag = Tag.findByLabel(label);
		notFoundIfNull(tag);
		showTag(tag.id);
	}
	
	/** show a given tag */
	public static void showTag(Long tagId) {
		Tag tag = Tag.findById(tagId);
		notFoundIfNull(tag);
		render(tag);
	}
	
	/** rename a given tag */
	public static void rename(Long tagId, String label) {
		Tag tag = Tag.findById(tagId);
		notFoundIfNull(tag);
		
		tag.label = label;
		tag.save();

		showTag(tagId);
	}
	
	/**
	 * use when a tag needs to be cut in more tags (for example if it was not comma seaprated in teh begening
	 * @param tagId
	 * @param tagsCutted : add a comma where the tags need to be cutted
	 */
	public static void cutTag(Long tagId, String tagsCutted) {
		Tag tag = Tag.findById(tagId);
		notFoundIfNull(tag);
		
		String[] labelArray = tagsCutted.split(",");
		for (int i = 0; i < labelArray.length; i++) {
			String label = labelArray[i].trim();
			Tag newTag = Tag.findByLabelOrCreate(label);
			
			for(Insight insight : new HashSet<Insight>(tag.insights)) { // nb: create a Set because tag.insights cannot be cast to a List (is a PersistentSet)
				newTag.insights.add(insight);
			}
			
			newTag.save();
		}
		// remove the old tag
		tag.remove();
		
		tags();
	}
	
	public static void deleteTag(Long tagId) {
		Tag tag = Tag.findById(tagId);
		notFoundIfNull(tag);

		tag.remove();
		Search.unIndex(tag);
		
		tags();
	}
	
	/**
	 * transforms the given tagLabelList into tags and set it to the children of the given tag
	 * @param tagId
	 * @param tagLabelList : a comma separated list of tags
	 */
	public static void addChildren(Long tagId, String tagLabelList) {
		Tag tag = Tag.findById(tagId);
		notFoundIfNull(tag);
		
		// clear the children
		tag.children.clear();
		
		// add the new ones
		String[] labelArray = tagLabelList.split(",");
		for (int i = 0; i < labelArray.length; i++) {
			String label = labelArray[i].trim();
			Tag newTag = Tag.findByLabelOrCreate(label);
			tag.children.add(newTag);
		}
		tag.save();
		
		showTag(tagId);
	}
	
	/** feature the given topic */
	public static void featureTag(Long tagId, String lang) {
		Tag tag = Tag.findById(tagId);
		Language language = Language.findByLabelOrCreate(lang);
		tag.feature(language);
		renderText("Topic "+ tag.label +" was featured in language " + language.label);
	}
	
	/** stop featuring the given topic */
	public static void stopFeatureTag(Long tagId, String lang) {
		Tag tag = Tag.findById(tagId);
		Language language = Language.findByLabelOrCreate(lang);
		tag.stopFeature(language);
		renderText("Topic "+ tag.label +" is not featured anymore.");
	}

	/**
	 * Admin only: Call this method to replace all activities with new activities based on the "following" information for Users, Topics and Insights
	 */
	public static void CopyFavoriteToActivity() {
		TagActivity.deleteAll();
		UserActivity.deleteAll();
		InsightActivity.deleteAll();
		
		List<User> users = User.findAll(); 
		for(User user : users) {
			for(Insight insight : user.followedInsights) {
				new InsightActivity(user, insight).save();
			}
			for(Tag topic : user.followedTopics) {
				new TagActivity(user, topic).save();
			}
			for(User followedUser : user.followedUsers) {
				new UserActivity(user, followedUser).save();
			}
		}
		
		renderText("All activities for all users generated");
	}
	
}