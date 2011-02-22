package controllers;

import org.apache.commons.lang.RandomStringUtils;

import models.User;
import gson.FacebookModelObject;
import play.Play;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.results.Redirect;

import com.google.gson.Gson;

import controllers.FacebookOAuth.FacebookOAuthDelegate;

/**
 * 
 * @author jb
 *
 */
public class FacebookOAuthForBeansight extends FacebookOAuth.FacebookOAuthDelegate {
    
	public static final String FACEBOOK_REGISTER_VIA_LOGIN = "FACEBOOK_REGISTER_VIA_LOGIN";
	public static final String FACEBOOK_REGISTER_VIA_SIGNUP = "FACEBOOK_REGISTER_VIA_SIGNUP";
                        
    public static void onFacebookAuthentication(FacebookModelObject facebookModelObject) {
        Long facebookUserId = facebookModelObject.getId();

        User facebookUser = null;
        
        // facebook email could be null/void
        if (facebookModelObject.getEmail() != null && !facebookModelObject.getEmail().trim().equals("")) {
        	if (!User.isEmailAvailable(facebookModelObject.getEmail())) {
        		facebookUser = User.findByEmail(facebookModelObject.getEmail());
        	}
        }
        
        // facebookUser is still null after an email lookup we try to find him with his facebook id
        if (facebookUser == null) {
        	facebookUser = User.findByFacebookUserId(facebookModelObject.getId());
        }
        
        // Finally no user found this is the first time this user connects to beansight
        // then create a beansight account linked to his facebook account
        if (facebookUser == null) {
        	String facebookScreenName = createNewAvailableUserName(facebookModelObject.getFirst_name());
        	
            facebookUser = new User(facebookModelObject.getEmail(), facebookScreenName, "");
            facebookUser.facebookUserId = facebookUserId;
            facebookUser.save();
        } 
        // if the user already has a beansight account and he is trying 
        // to connect with his facebook account then "merge" the facebook 
        // account with the beansight account
        else if (facebookUser != null && facebookUser.facebookUserId == null) {
        	facebookUser.facebookUserId = facebookUserId;
        	facebookUser.save();
        }
        
        session.put("isFacebookUser", Boolean.TRUE);
        session.put("facebookUserId", facebookUserId);
        session.put("username", facebookUser.userName);
        
        Application.index();
    }
    
    public static String createNewAvailableUserName(String firstName) {
    	int firstNameMaxSize = 14;
    	
    	String userName = firstName.replace(" ", "").replace("-", "");
    	
    	if (userName.length() < firstNameMaxSize) {
    		firstNameMaxSize = userName.length();
    	}
    	
    	userName = userName.substring(0, firstNameMaxSize);
    	
    	for (int i=1; i<100; i++) {
	    	if (User.isUsernameAvailable(userName)) {
	    		return userName;
	    	} else {
	    		userName = userName + i;
	    	}
    	}
    	
    	// This should never happen but like that wee still return a string
    	return RandomStringUtils.randomAlphabetic(10);
    }
    
    public static String getExtendedPermissions() {
    	return "email";
    }
    
}
