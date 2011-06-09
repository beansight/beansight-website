package controllers;

import gson.FacebookUserGson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jobs.facebook.RefreshBeansightAvatarWithFacebookImageJob;
import jobs.facebook.UpdateBeansightUserToFacebookUserRelationshipJob;
import models.FacebookFriend;
import models.FacebookUser;
import models.User;

import org.apache.commons.lang.RandomStringUtils;

import play.Logger;
import play.cache.Cache;
import play.libs.Crypto;

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
            updateBeansightUserLinkToFacebookUser(beansightFbUser, false, false);
        } 
        // if the user already has a beansight account and he is trying 
        // to connect with his facebook account then "merge" the facebook 
        // account with the beansight account
        else if (beansightFbUser != null && beansightFbUser.facebookUserId == null) {
        	beansightFbUser.facebookUserId = facebookUserId;
        	FacebookUser facebookUser = FacebookUser.findByFacebookId(facebookUserId);
        	beansightFbUser.relatedFacebookUser = facebookUser;
        	beansightFbUser.save();
        	updateBeansightUserLinkToFacebookUser(beansightFbUser, false, false);
        } else {
        	updateBeansightUserLinkToFacebookUser(beansightFbUser, false, true);
        }
        
        // add these information in cookie to know the user has used Facebook to login
        session.put("userId", beansightFbUser.getId());
        session.put("isFacebookUser", Boolean.TRUE);
        session.put("facebookUserId", facebookUserId);
        session.put("username", beansightFbUser.email);
        // Remember
        response.setCookie("rememberme", Crypto.sign(beansightFbUser.email) + "-" + beansightFbUser.email, "30d");
        
        refreshBeansightAvatarWithFacebookImage();
        
        // is it an authentication to use the API ?
        String apiUrlCallback = session.get(APIController.API_URL_CALLBACK);
        if (session.get(APIController.API_URL_CALLBACK) != null) {
        	UUID uuid = UUID.randomUUID();
        	Cache.add(uuid.toString(), beansightFbUser.email);
        	// (apiToken is equal to "?" or "#")
        	String apiTokenResult = session.get(APIController.API_TOKEN_RESULT_KEY);
        	
        	// clean the session
        	session.remove(APIController.API_URL_CALLBACK);
        	session.remove(APIController.API_TOKEN_RESULT_KEY);
        	
        	redirect(String.format("%s%Saccess_token=%s", apiUrlCallback, apiTokenResult, uuid.toString()));
        	return;
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
    
    static String getExtendedPermissions() {
    	return "email";
    }
    
    /**
     * This method update links between the current connected user and his Facebook friends
     * @param currentBeansightUser : the user to update its social graph
     * @param forceIsHidden : set to true if you want to force isHidden to false 
     * (note : isHidden won't be forced if the facebookUser was already followed)
     * @param async : set it to true to update beansight user info asynchronously
     */
    static void updateBeansightUserLinkToFacebookUser(User currentBeansightUser, boolean forceIsHidden, boolean async) {
    	if (async == true) {
    		new UpdateBeansightUserToFacebookUserRelationshipJob(currentBeansightUser.id, forceIsHidden).now();
    	} else {
    		try {
				new UpdateBeansightUserToFacebookUserRelationshipJob(currentBeansightUser.id, forceIsHidden).doJob();
			} catch (Exception e) {
				Logger.error(e, "UpdateBeansightUserToFacebookUserRelationshipJob has thrown an error");
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
        updateBeansightUserLinkToFacebookUser(currentUser, false, false);
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
        	
        	updateBeansightUserLinkToFacebookUser(currentUser, true, false);
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
    	
    	new RefreshBeansightAvatarWithFacebookImageJob(currentUser.id).now();
    }
    
}
