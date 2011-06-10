package controllers;

import java.util.UUID;

import models.User;
import play.cache.Cache;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

import com.google.gson.Gson;

public class APIController extends Controller {

	public static final String API_URL_CALLBACK = "api_url_callback";
	public static final String API_JSON_CALLBACK = "callback";
	public static final String API_ACCESS_TOKEN = "access_token";
	public static final String API_TOKEN_RESULT_KEY = "token_result";
	
	/**
	 * Check before every API call that the accessToken is valid
	 */
	@Before(unless={"authenticate", "authenticateSuccess", "register"})
	public static void checkAccessToken() {
		String accessToken = params.get(API_ACCESS_TOKEN);
		if(accessToken == null) {
			badRequest(); // error
		}
		String email = (String)Cache.get(accessToken);
		if (email == null) {
			forbidden("The provided access_token " +  accessToken + " is not valid."); // error
		}
	}
	
	/**
	 * @return the accessToken associated with this user
	 */
	protected static User getUserFromAccessToken() {
		String accessToken = params.get(API_ACCESS_TOKEN);
		String email = (String)Cache.get(accessToken);
		User user = User.findByEmail(email);
		return user;
	}
	
	/**
	 * prepend the "callback" parameter to the JSON serialization of the object
	 * @param o : object to serialize
	 * @param callback : callback to prepend
	 */
	protected static void renderJSONP(Object o, String callback) {
		renderText( callback + "(" + new Gson().toJson(o) + ")" );
	}
	
	/**
	 * render the object either in JSON or JSONP, depending on the presence of the "callback" parameter
	 * @param o : JSON Object to render
	 */
	protected static void renderAPI(Object o) {
		String callback = params.get(API_JSON_CALLBACK);
		if(callback != null) {
			renderJSONP(o, callback);
		} else {
			renderJSON(o);
		}
	}
	
	/**
	 * action to authenticate a user for a tier application.
	 * urlCallback is used to tell where to redirect after the user has been authenticated successfully
	 * tokenResult is used to have the access_token return on the urlCallBack either with a # or with ?
	 * tokenResult values can be "param" (if tokenResult is not set tokenResult=param will be used by default) or
	 * "fragment".
	 * if tokenResultType=param then access_token will be available on the urlCallback as : http://myurlcallback/example?access_token=76063297-f5ab-4b82-a0bd-571d27b01074 
	 * if tokenResultType=fragment then access_token will be available on the urlCallback as : http://myurlcallback/example#access_token=76063297-f5ab-4b82-a0bd-571d27b01074
	 * @param urlCallback 
	 * @param tokenResultType
	 */
	public static void authenticate(String urlCallback, String tokenResultType) {
		if (urlCallback == null) {
			urlCallback = String.format(Router.getFullUrl(request.controller + ".authenticateSuccess"));
		}
		session.put(API_URL_CALLBACK, urlCallback);

		String apiTokenResult = "";
		if (tokenResultType != null && tokenResultType.trim().equals("fragment")) {
			apiTokenResult =  "#";
		} else {
			if (urlCallback.contains("?")) {
				apiTokenResult = "&";
			} else {
				apiTokenResult = "?";
			}
		}
		session.put(API_TOKEN_RESULT_KEY, apiTokenResult);
		
		// if user is already authenticated on beansight redirect to the urlCallback this the access_token
		if(Security.isConnected()) {
			User currentUser = CurrentUser.getCurrentUser();

        	UUID uuid = UUID.randomUUID();
        	Cache.add(uuid.toString(), currentUser.email); 
        	
        	// clean the session
        	session.remove(APIController.API_URL_CALLBACK);
        	session.remove(APIController.API_TOKEN_RESULT_KEY);
        	
        	redirect(String.format("%s%saccess_token=%s", urlCallback, apiTokenResult, uuid.toString()));
        	return;
		}
		
		render();
	}
	
	/**
	 * generic callback url if no specific url provided in authenticate(String url) : the access token will be available in the url.
	 * For example : www.beansight.com/openapi/authenticateSuccess#access_token=a52795fc-8374-4c2b-8f46-7c8684687536
	 */
	public static void authenticateSuccess() {
		render();
	}
	
}
