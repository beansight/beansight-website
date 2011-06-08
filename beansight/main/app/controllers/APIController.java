package controllers;

import models.User;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

import com.google.gson.Gson;

public class APIController extends Controller {

	public static final String API_URL_CALLBACK = "api_url_callback";
	public static final String API_JSON_CALLBACK = "callback";
	public static final String API_ACCESS_TOKEN = "access_token";
	
	/**
	 * Check before every API call that the accessToken is valid
	 */
	@Before(unless={"authenticate", "authenticateSuccess"})
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
	
	public static void authenticate(String urlCallback) {
		if (urlCallback == null) {
			urlCallback = String.format(Router.getFullUrl(request.controller + ".authenticateSuccess"));
		}
		session.put(API_URL_CALLBACK, urlCallback);
		renderTemplate("Secure/login.html");
	}
	
	/**
	 * generic callback url if no specific url provided in authenticate(String url) : the access token will be available in the url.
	 * For example : www.beansight.com/openapi/authenticateSuccess#access_token=a52795fc-8374-4c2b-8f46-7c8684687536
	 */
	public static void authenticateSuccess() {
		render();
	}
	
}
