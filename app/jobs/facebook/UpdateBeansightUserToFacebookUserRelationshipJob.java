package jobs.facebook;

import java.util.List;

import models.FacebookFriend;
import models.FacebookUser;
import models.User;
import play.jobs.Job;

public class UpdateBeansightUserToFacebookUserRelationshipJob extends Job {

	public Long currentBeansightUserId = null;
	public boolean forceIsHidden = false;
	
    public UpdateBeansightUserToFacebookUserRelationshipJob(
			Long currentBeansightUserId, boolean forceIsHidden) {
		super();
		this.currentBeansightUserId = currentBeansightUserId;
		this.forceIsHidden = forceIsHidden;
	}

	/**
     * This Job updates links between the current connected user and his Facebook friends
     * @param currentBeansightUser : the user to update its social graph
     * @param forceIsHidden : set to true if you want to force isHidden to false 
     * (note : isHidden won't be forced if the facebookUser was already followed)
     */
	@Override
	public void doJob() throws Exception {
		if (currentBeansightUserId == null) {
			return;
		}
		
		User currentBeansightUser = User.findById(currentBeansightUserId);
		
    	FacebookUser facebookUser = FacebookUser.findByFacebookId(currentBeansightUser.facebookUserId);
    	if (currentBeansightUser.relatedFacebookUser == null) {
    		currentBeansightUser.relatedFacebookUser = facebookUser;
    		currentBeansightUser.save();
    	}
    	
    	// this query find all the FacebookUser objects that exist as friend in the facebook friends but that are not yet
    	// recorded as facebook friends in beansight model (to know if a FacebookUser is recorded as a friend in beansight
    	// then there should exist a FacebookFriend entity to connect the User (beansight) and the FacebookUser (facebook) 
    	List<FacebookUser> fbUsers = FacebookUser.find("select friend from FacebookUser fbu " +
    			"join fbu.friends as friend " +
    			"where fbu.facebookId = :facebookId " +
    			"and friend.facebookId not in (select fbf.facebookUser.facebookId from FacebookFriend fbf where fbf.user = :user)")
    			.bind("facebookId", currentBeansightUser.facebookUserId)
    			.bind("user", currentBeansightUser)
    			.fetch();
    	
    	// Save the new Facebook users (if any)
    	for (FacebookUser fbUser : fbUsers) {
    		User aBeansightUserFriend = User.findByFacebookUserId(fbUser.facebookId);
    		// create a relationship from currentBeansightUser to (fbUser & aBeansightUserFriend)
    		FacebookFriend fbFriend = new FacebookFriend(fbUser, aBeansightUserFriend, currentBeansightUser);
    		fbFriend.save();
    		
    		// we also need to create the inverse of the relationship
    		if(aBeansightUserFriend != null) {
    			FacebookFriend inverseFbFriend = FacebookFriend.findRelationshipBetweenUserIdAndFacebookId(aBeansightUserFriend.id, currentBeansightUser.facebookUserId);
    			if (inverseFbFriend != null) {
    				inverseFbFriend.isBeansightUser = true;
    				inverseFbFriend.beansightUserFriend = currentBeansightUser;
    			} else {
    				inverseFbFriend = new FacebookFriend(currentBeansightUser.relatedFacebookUser, currentBeansightUser, aBeansightUserFriend);
    			}
	    		inverseFbFriend.save();
    		}
    	}
    	
    	// update the information for the link (FacebookFriend) between the beansight user (User entity) and the facebook user (FacebookUser entity) 
    	// marked as a facebook friend of the beansight user
		if (facebookUser.friends != null && !facebookUser.friends.isEmpty()) {
			List<FacebookFriend> newFriendsToAdd = currentBeansightUser.findMyFacebookFriendWithABeansightAccountButNotAlreadyMyFriendsInBeansight();
			
			for (FacebookFriend aFacebookFriend : newFriendsToAdd) {
				aFacebookFriend.isBeansightUser = true;
				aFacebookFriend.beansightUserFriend = User.findByFacebookUserId(aFacebookFriend.facebookUser.facebookId);
				// check if this facebook friend is already in the followedUsers list ?
				Long count = User.find("select count(followedUser) from User u join u.followedUsers followedUser where u.id = :currentUserId and followedUser.facebookUserId = :friendFacebookId")
						.bind("currentUserId", currentBeansightUser.id)
						.bind("friendFacebookId", aFacebookFriend.facebookUser.facebookId)
						.first();
				if (count > 0) {
					aFacebookFriend.isAdded = true;
				} else {
					if (forceIsHidden) {
						aFacebookFriend.isHidden = true;
					}
				}
				aFacebookFriend.save();
			}
		}
		
	}
	
}
