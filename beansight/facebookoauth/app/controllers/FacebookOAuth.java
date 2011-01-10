package controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import models.User;
import gson.FacebookModelObject;
import play.Logger;
import play.Play;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.results.Redirect;
import play.utils.Java;

import com.google.gson.Gson;

import controllers.Secure.Security;

/**
 * 
 * @author jb
 *
 */
public class FacebookOAuth extends Controller {

    /**
     * client_id
     */
    private static final String FB_CLIENT_ID = Play.configuration.getProperty("facebook.client_id", ""); 
    
    /**
     * client_secret
     */
    private static final String FB_APPLICATION_SECRET = Play.configuration.getProperty("facebook.client_secret");
    
    /**
     * API key
     */
    private static final String FB_API_KEY = Play.configuration.getProperty("facebook.api_key"); 
    
    
    public static void authenticate() {
        throw new Redirect("https://graph.facebook.com/oauth/authorize?client_id=" + FB_CLIENT_ID + "&redirect_uri=" + Router.getFullUrl(request.controller + ".callback"));
    }
    
    /**
     * called by facebook after being authenticated 
     * @throws Throwable 
     */
    public static void callback(String code) throws Throwable {
        StringBuilder fbAccessTokenUrl = new StringBuilder();
        fbAccessTokenUrl.append("https://graph.facebook.com/oauth/access_token?client_id=").append(FB_CLIENT_ID)
                                    .append("&redirect_uri=").append(Router.getFullUrl(request.controller + ".callback"))
                                    .append("&client_secret=").append(FB_APPLICATION_SECRET)
                                    .append("&code=").append(WS.encode(code));
        
        String response = WS.url(fbAccessTokenUrl.toString()).get().getString();
        
        String accessToken = response.split("=")[1];
        session.put("fb", accessToken);
        
        String facebookUserJson = WS.url("https://graph.facebook.com/me?access_token=" + accessToken).get().getString();
        
        Gson gson = new Gson();
        FacebookModelObject facebookModelObject = gson.fromJson(facebookUserJson, FacebookModelObject.class);

        FacebookOAuthDelegate.invoke("onFacebookAuthentication", facebookModelObject);
    }
    
    public static void acccessTokenCallback(String accessToken) {
        throw new Redirect(Router.getFullUrl("Application.index"));
    }

    
    /**
     * In the client application, extend FacebookOAuthDelegate to re define onFacebookAuthentication method
     * 
     * @author jb
     *
     */
    public static class FacebookOAuthDelegate extends Controller {
        
        static void onFacebookAuthentication(FacebookModelObject facebookModelObject)  {
            Application.index();
        }
        
        private static Object invoke(String m, Object... args) throws Throwable {
            Class facebookOAuthDelegate = null;
            List<Class> classes = Play.classloader.getAssignableClasses(FacebookOAuthDelegate.class);
            if(classes.size() == 0) {
                facebookOAuthDelegate = FacebookOAuthDelegate.class;
            } else {
                facebookOAuthDelegate = classes.get(0);
            }
            try {
                return Java.invokeStaticOrParent(facebookOAuthDelegate, m, args);
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
    
}
