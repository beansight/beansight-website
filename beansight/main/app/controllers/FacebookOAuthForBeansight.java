package controllers;

import gson.FacebookUserGson;
import helpers.FileHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import models.FacebookFriend;
import models.FacebookUser;
import models.User;
import models.UserActivity;

import org.apache.commons.lang.RandomStringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import play.Logger;
import play.libs.Crypto;
import play.libs.WS;

/**
 * 
 * @author jb
 *
 */
public class FacebookOAuthForBeansight extends FacebookOAuth.FacebookOAuthDelegate {
    
	public static final String FACEBOOK_SYNC_COOKIE = "FACEBOOK_SYNC_COOKIE";
	public static final String LINK_FACEBOOK_TO_BEANSIGHT_COOKIE = "LINK_FACEBOOK_TO_BEANSIGHT_COOKIE";
	public static final String OVERRIDE_FACEBOOK_LINKING = "OVERRIDE_FACEBOOK_LINKING";
	
	public static final String FACEBOOK_REGISTER_VIA_LOGIN = "FACEBOOK_REGISTER_VIA_LOGIN";
	public static final String FACEBOOK_REGISTER_VIA_SIGNUP = "FACEBOOK_REGISTER_VIA_SIGNUP";
                        
    static void onFacebookAuthentication(String accessToken, FacebookUserGson fbUser) {
    	if (session.get(FACEBOOK_SYNC_COOKIE) != null && session.get(FACEBOOK_SYNC_COOKIE).equals("true")) {
    		session.remove(FACEBOOK_SYNC_COOKIE);
    		onFacebookSynchronization(accessToken, fbUser);
    		return;
    	}
    	
    	if (session.get(LINK_FACEBOOK_TO_BEANSIGHT_COOKIE) != null && session.get(LINK_FACEBOOK_TO_BEANSIGHT_COOKIE).equals("true")) {
    		session.remove(LINK_FACEBOOK_TO_BEANSIGHT_COOKIE);
    		onLinkFacebookToBeansight(accessToken, fbUser);
    		return;
    	}
    	
        Long facebookUserId = fbUser.getId();

        User beansightFbUser = null;
        
        // we try to find him with his facebook id
        beansightFbUser = User.findByFacebookUserId(fbUser.getId());
        
        // if no user with the facebookId we use the facebook email to find a user already existing in beansight
        if (beansightFbUser == null && fbUser.getEmail() != null && !fbUser.getEmail().trim().equals("")) {
        	if (!User.isEmailAvailable(fbUser.getEmail())) {
        		beansightFbUser = User.findByEmail(fbUser.getEmail());
        	}
        }
        
        // Finally no user found this is the first time this user connects to beansight
        // then create a beansight account linked to his facebook account
        if (beansightFbUser == null) {
        	String facebookScreenName = User.createNewAvailableUserName(fbUser.getFirst_name());
        	
        	// note : we have to generate a random password because if we use "" as a password facebook account could be easily hacked
            beansightFbUser = new User(fbUser.getEmail(), facebookScreenName, RandomStringUtils.randomAlphabetic(15));
            beansightFbUser.facebookUserId = facebookUserId;
            beansightFbUser.emailConfirmed = true;
            beansightFbUser.isPromocodeValidated = true;
        	FacebookUser facebookUser = FacebookUser.findByFacebookId(facebookUserId);
        	beansightFbUser.relatedFacebookUser = facebookUser;
            beansightFbUser.save();
        } 
        // if the user already has a beansight account and he is trying 
        // to connect with his facebook account then "merge" the facebook 
        // account with the beansight account
        else if (beansightFbUser != null && beansightFbUser.facebookUserId == null) {
        	beansightFbUser.facebookUserId = facebookUserId;
        	FacebookUser facebookUser = FacebookUser.findByFacebookId(facebookUserId);
        	beansightFbUser.relatedFacebookUser = facebookUser;
        	beansightFbUser.save();
        } 
        
        // add these information in cookie to know the user has used Facebook to login
        session.put("userId", beansightFbUser.getId());
        session.put("isFacebookUser", Boolean.TRUE);
        session.put("facebookUserId", facebookUserId);
        session.put("username", beansightFbUser.email);
        // Remember
        response.setCookie("rememberme", Crypto.sign(beansightFbUser.email) + "-" + beansightFbUser.email, "30d");
        
        updateBeansightUserLinkToFacebookUser(beansightFbUser, false);
        
        refreshBeansightAvatarWithFacebookImage();
        
     	// redirect to the previous url or index if nothing was set in session
		if (session.contains("url")) {
			String url = session.get("url");
			session.remove("url");
			redirect(url);
		} else {
			Application.index();
		}
    }
    
    static String getExtendedPermissions() {
    	return "email";
    }
    
    /**
     * This method update links between the current connectied user and his Facebook friends
     * @param currentBeansightUser : the user to update its social graph
     * @param forceIsHidden : set to true if you want to force isHidden to false 
     * (note : isHidden won't be forced if the facebookUser was already followed)
     */
    static void updateBeansightUserLinkToFacebookUser(User currentBeansightUser, boolean forceIsHidden) {
    	FacebookUser facebookUser = FacebookUser.findByFacebookId(currentBeansightUser.facebookUserId);
    	currentBeansightUser.relatedFacebookUser = facebookUser;
    	currentBeansightUser.save();
    	
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
	    		FacebookFriend inverseFbFriend = new FacebookFriend(currentBeansightUser.relatedFacebookUser, currentBeansightUser, aBeansightUserFriend);
	    		inverseFbFriend.save();
    		}
    	}
    	
    	// update the information for the link (FacebookFriend) between the beansight user (User entity) and the facebook user (FacebookUser entity) 
    	// marked as a facebook friend of the beansight user
		if (facebookUser.friends != null && !facebookUser.friends.isEmpty()) {
			List<FacebookFriend> newFriendsToAdd = currentBeansightUser.findMyFacebookFriendWithABeansightAccountButNotAlreadyMyFriendsInBeansight();
			
			for (FacebookFriend aFacebookFriend : newFriendsToAdd) {
				aFacebookFriend.isBeansightUser = true;
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
    
    /**
     * this method will be called when a user having his facebook linked to beansight synchronize
     * its facebook infos
     * @param accessToken
     * @param fbUser
     */
    static void onFacebookSynchronization(String accessToken, FacebookUserGson fbUser) {
    	User currentUser = CurrentUser.getCurrentUser();
        updateBeansightUserLinkToFacebookUser(currentUser, false);
        refreshBeansightAvatarWithFacebookImage();
        
     	// redirect to the previous url or index if nothing was set in session
		if (session.contains("url")) {
			String url = session.get("url");
			session.remove("url");
			redirect(url);
		} else {
			Application.index();
		}
    }
    
    /**
     * 
     * @param accessToken
     * @param fbUser
     */
    static void onLinkFacebookToBeansight(String accessToken, FacebookUserGson fbUser) {
    	User currentUser = CurrentUser.getCurrentUser();
    	
    	// is there already a beansight account with the facebooId ?
    	User aUserWithTheSameFacebookId = User.findByFacebookUserId(fbUser.getId());
    	if (aUserWithTheSameFacebookId !=null) {
    		String val = session.get(OVERRIDE_FACEBOOK_LINKING);
    		session.remove(OVERRIDE_FACEBOOK_LINKING);
    		if (val != null && val.equalsIgnoreCase("true")) {
    			aUserWithTheSameFacebookId.facebookUserIdDisabled = fbUser.getId();
    			aUserWithTheSameFacebookId.facebookUserId = null;
    			aUserWithTheSameFacebookId.relatedFacebookUser = null;
    			aUserWithTheSameFacebookId.save();
    			
    			// all the FacebookFriend entities referencing aUserWithTheSameFacebookId
    			// should now reference currentUser
    			List<FacebookFriend> fbfToUpdate = FacebookFriend.find("select fbf from FacebookFriend fbf where fbf.beansightUserFriend = :oldUser")
    				.bind("oldUser", aUserWithTheSameFacebookId).fetch();
    			for (FacebookFriend fbf : fbfToUpdate) {
    				fbf.beansightUserFriend = currentUser;
    				if (fbf.user.isFollowingUser(currentUser)) {
    					fbf.isAdded = true;
    					fbf.isHidden = false;
    				}
    				fbf.save();
    			}
    			
    			// all those who followed aUserWithTheSameFacebookId should now follow currentUser
    			List<User> followers = new ArrayList<User>(aUserWithTheSameFacebookId.followers);
    			for( User follower : followers) {
     				follower.stopFollowingThisUser(aUserWithTheSameFacebookId);
     				follower.startFollowingThisUser(currentUser, false);
     			}
    			// all who were followed by aUserWithTheSameFacebookId are now followed by currentUser
    			List<User> followeds = new ArrayList<User>(aUserWithTheSameFacebookId.followedUsers);
    			for( User followed : followeds) {
    				aUserWithTheSameFacebookId.stopFollowingThisUser(followed);
    				currentUser.startFollowingThisUser(followed, false);
    			}
    			
    		} else {
	    		// we cannot continue because there can't be 2 users with the same facebookId
	    		// so here we redirect to a page where we ask the user to decide if he is ok to
	    		// set the current beansight account to be the only one to be linked to beansight
	    		// and if he answers yes then the facebookId of the other account will be set to null
	    		Application.facebookIdAlreadyInUseWarning(aUserWithTheSameFacebookId.id);
    		}
    	}
    	
    	if (currentUser != null && currentUser.facebookUserId == null) {
    		currentUser.facebookUserId = fbUser.getId();
        	FacebookUser facebookUser = FacebookUser.findByFacebookId(fbUser.getId());
        	currentUser.relatedFacebookUser = facebookUser;
        	currentUser.save();
        	currentUser = currentUser.refresh();
        	
        	updateBeansightUserLinkToFacebookUser(currentUser, true);
        	refreshBeansightAvatarWithFacebookImage();
        } 
    	
     	// redirect to the previous url or index if nothing was set in session
		if (session.contains("url")) {
			String url = session.get("url");
			session.remove("url");
			redirect(url);
		} else {
			Application.index();
		}
    }
    
    /**
     * if the user's avatar image is the default one then this method will download
     * the user's Facebook image and use it as default Beansight avatar
     */
    static void refreshBeansightAvatarWithFacebookImage() {
    	User currentUser = CurrentUser.getCurrentUser();
    	
    	if (!currentUser.avatarSmall.exists()) {
    		try {
				InputStream profileImageInputStream = WS.url("http://graph.facebook.com/%s/picture?type=large", currentUser.facebookUserId.toString()).get().getStream();
				// and now we can update avatar with the facebook profil image
				currentUser.updateAvatar(
						FileHelper.getTmpFile(profileImageInputStream), true);
			} catch (Exception e) {
				Logger.error("cannot get user's Facebook image : %s", e.getMessage());
			}
    	}
    }
    
}
