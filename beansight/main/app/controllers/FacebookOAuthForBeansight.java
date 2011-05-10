package controllers;

import gson.FacebookUserGson;
import helpers.FileHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import models.FacebookFriend;
import models.FacebookUser;
import models.User;

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
        
        // facebook email could be null/void
        if (fbUser.getEmail() != null && !fbUser.getEmail().trim().equals("")) {
        	if (!User.isEmailAvailable(fbUser.getEmail())) {
        		beansightFbUser = User.findByEmail(fbUser.getEmail());
        	}
        }
        
        // facebookUser is still null after an email lookup we try to find him with his facebook id
        if (beansightFbUser == null) {
        	beansightFbUser = User.findByFacebookUserId(fbUser.getId());
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
        session.put("isFacebookUser", Boolean.TRUE);
        session.put("facebookUserId", facebookUserId);
        session.put("username", beansightFbUser.email);
        // Remember
        response.setCookie("rememberme", Crypto.sign(beansightFbUser.email) + "-" + beansightFbUser.email, "30d");
        
        updateBeansightUserLinkToFacebookUser(beansightFbUser);
        
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
     * This method update links between the current connecting user and his Facebook friends
     * @param currentBeansightUser
     */
    static void updateBeansightUserLinkToFacebookUser(User currentBeansightUser) {
    	FacebookUser facebookUser = FacebookUser.findByFacebookId(currentBeansightUser.facebookUserId);
    	currentBeansightUser.relatedFacebookUser = facebookUser;
    	currentBeansightUser.save();
    	
    	// get all the facebook user's friends who are using beansight but that are not already known as friends in beansight
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
    		FacebookFriend fbFriend = new FacebookFriend(fbUser, aBeansightUserFriend, currentBeansightUser);
    		fbFriend.save();
    	}
    	
    	// update the information for the link (FacebookFriend) between the beansight user and the facebook user 
    	// marked as a facebook friend of the beansight user
		if (facebookUser.friends != null && !facebookUser.friends.isEmpty()) {
			List<FacebookFriend> newFriendsToAdd = currentBeansightUser.findMyFriendsInFacebookNotYetMyFriendsInBeansight();
			
			for (FacebookFriend aFacebookFriend : newFriendsToAdd) {
				aFacebookFriend.isBeansightUser = true;
				// check if this facebook friend is already in the followedUsers list ?
				Long count = User.find("select count(followedUser) from User u join u.followedUsers followedUser where u.id = :currentUserId and followedUser.facebookUserId = :friendFacebookId")
						.bind("currentUserId", currentBeansightUser.id)
						.bind("friendFacebookId", aFacebookFriend.facebookUser.facebookId)
						.first();
				if (count > 0) {
					aFacebookFriend.isAdded = true;
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
        updateBeansightUserLinkToFacebookUser(currentUser);
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
    	if (currentUser != null && currentUser.facebookUserId == null) {
    		currentUser.facebookUserId = fbUser.getId();
        	FacebookUser facebookUser = FacebookUser.findByFacebookId(fbUser.getId());
        	currentUser.relatedFacebookUser = facebookUser;
        	currentUser.save();
        	currentUser = currentUser.refresh();
        	
        	updateBeansightUserLinkToFacebookUser(currentUser);
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
	//			String response = WS.url("http://graph.facebook.com/%s/picture", currentUser.facebookUserId.toString()).get().getString();
				InputStream profileImageInputStream = WS.url("http://graph.facebook.com/%s/picture", currentUser.facebookUserId.toString()).get().getStream();
	//			source.setEncoding("UTF-8");
	
			
//				Document doc = DocumentBuilderFactory.newInstance()
//						.newDocumentBuilder().parse(source);
//				String profileImageUrl = doc.getDocumentElement()
//						.getElementsByTagName("profile_image_url").item(0)
//						.getTextContent();
//				InputStream profileImageInputStream = WS.url(profileImageUrl).get()
//						.getStream();
				// save so that we get an id for the new user
//				currentUser.save();
//				currentUser = currentUser.refresh();
				// and now we can update avatar with the twitter profil image
				currentUser.updateAvatar(
						FileHelper.getTmpFile(profileImageInputStream), true);
			} catch (Exception e) {
				Logger.error("cannot get user's Facebook image : %s", e.getMessage());
			}
    	}
    }
}
