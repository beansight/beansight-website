package models.analytics;

import play.Logger;
import play.mvc.Http.Request;

/**
 * This class aims to be used to have information about the user's client, 
 * like its ip, user agent, ...
 * 
 * But it's not a JPA entity and is'nt stored directly in DB
 * 
 * @author jb
 *
 */
public class UserClientInfo {
	
	/** ip of the user */
	public String ip;
	
	/** user-agent of the visiting user */
	public String userAgent;
	
	/** the id of the application used (example: web-desktop if used from beansight.com) */
	public String application;
	
	public UserClientInfo(String ip, String userAgent, String application) {
		super();
		this.ip = ip;
		this.userAgent = userAgent;
		this.application = application;
	}
	
	public UserClientInfo(Request request, String application) {
		try {
			ip = request.remoteAddress;
		} catch(Exception e) {
			ip = "";
			Logger.warn("Cannot get user ip : " + e.getMessage());
		}
		try {
			userAgent = request.headers.get("user-agent").toString();
		} catch(Exception e) {
			userAgent = "";
			Logger.warn("Cannot get user ip : " + e.getMessage());
		}
		this.application = application;
	}
}
