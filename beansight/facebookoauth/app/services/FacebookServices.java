package services;

import gson.FacebookUserGson;
import gson.FriendGson;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import models.FacebookUser;
import play.libs.WS;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FacebookServices {
	
	private String accessToken;
	
	public FacebookServices(String accessToken) {
		this.accessToken = accessToken;
	}

	public FacebookUserGson getFacebookModelObject() {
        String facebookUserJson = WS.url("https://graph.facebook.com/me?access_token=%s", accessToken.replace("|","%7C")).get().getString();
        
        Gson gson = new Gson();
        FacebookUserGson facebookModelObject = gson.fromJson(facebookUserJson, FacebookUserGson.class);
        
        return facebookModelObject;
	}
	
	public LinkedList<FriendGson> getFriends(Long facebookId) {
//        String faceBookUserFriends = WS.url("https://api.facebook.com/method/fql.query?access_token=%s&query=SELECT%20uid1%20FROM%20friend%20WHERE%20uid2=%s&format=json", accessToken, facebookId.toString() ).get().getString();
		String faceBookUserFriends = WS.url("https://api.facebook.com/method/fql.query?access_token=%s&query=%s=%s&format=json", accessToken.replace("|","%7C"), "SELECT uid1 FROM friend WHERE uid2", facebookId.toString() ).get().getString();
        
		Gson gson = new Gson();
        
        // Type is needed to allow the use of typed list :
        Type listType = new TypeToken<List<FriendGson>>() {}.getType();
        LinkedList<FriendGson> friends = gson.fromJson(faceBookUserFriends, listType);
        
        return friends;
        
        /*
        FacebookUser fbUser = FacebookUser.findByFacebookId(facebookId);
        if (fbUser==null) {
        	fbUser = new FacebookUser(facebookId);
        	fbUser.save();
        } 
		for (FriendGson f : friends) {
			// if not already in the user's friends add it
			if (!fbUser.isThisFacebookUserAlreadyMyFriend(f.getUid1())) {
				// get the FacebookUser if it already exists :
				FacebookUser fbFriend = FacebookUser.findByFacebookId(f.getUid1());
				if (fbFriend == null) {
					fbFriend = new FacebookUser(f.getUid1());
				}
				fbUser.friends.add(fbFriend);
			}
		}
        fbUser.save();
        */
	}
	
	
//	public void getFacebookUserPhoto() {
//        String facebookUserJson = WS.url("https://graph.facebook.com/me?access_token=%s", accessToken.replace("|","%7C")).get().getString();
//        
//		InputStream profileImageInputStream = WS.url(profileImageUrl).get()
//		.getStream();
//	}
}
