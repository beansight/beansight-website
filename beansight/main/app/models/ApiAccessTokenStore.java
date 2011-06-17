package models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Index;
import org.joda.time.DateMidnight;

import play.cache.Cache;
import play.db.jpa.Model;

@Entity
public class ApiAccessTokenStore extends Model {

	/** the token string */
	@Index(name = "API_USER_TOKEN_IDX")
	public String accessToken;
	
	/** when this token was created */
	public Date crdate;
	
	/** this token is given to a user */
	@OneToOne
	public User user;
	

	
	public ApiAccessTokenStore(String accessToken, User user) {
		super();
		this.accessToken = accessToken;
		this.user = user;
		this.crdate = new Date();
	}

	/**
	 * Lookup in database the user's access token.
	 * If no existing access token then creates a new one and saves it in database.
	 * 
	 * @param email
	 * @return
	 */
	public static String getAccessTokenForUser(String email) {
		User user = User.findByEmail(email);
		// does the use has already an access token in database
		ApiAccessTokenStore apiAccessTokenStore = find("user.id = ? and accessToken is not null", user.id).first();
		if (apiAccessTokenStore != null) {
			Cache.add(apiAccessTokenStore.accessToken, email);
			return apiAccessTokenStore.accessToken;
		} else {
			// sinon 
			UUID uuid = UUID.randomUUID();
			ApiAccessTokenStore aNewApiAccessTokenStore = new ApiAccessTokenStore(uuid.toString(), user);
			aNewApiAccessTokenStore.save();
	    	Cache.add(uuid.toString(), user.email);
	    	return uuid.toString();
		}
	}
	
	public static ApiAccessTokenStore findByAccessToken(String accessToken) {
		return find("accessToken = ?", accessToken).first();
	}

	
	public static String getEmailByAccessToken(String accessToken) {
		String email = (String)Cache.get(accessToken);
		
		if (email == null) {
			ApiAccessTokenStore apiAccessTokenStore = findByAccessToken(accessToken);
			
			if (apiAccessTokenStore != null) {
				if (apiAccessTokenStore.isObsolete() ) {
					Cache.safeDelete(accessToken);
					apiAccessTokenStore.delete();
					return null;
				} else {
					email = apiAccessTokenStore.user.email;
					Cache.set(accessToken, email);
				}
			} else {
				return null;
			}
			
		}
		
		return email;
	}

	/**
	 * token created for more than 60 days is obsolete 
	 * @return
	 */
	public boolean isObsolete() {
		if (new DateMidnight(crdate).plusDays(60).isAfter(new DateMidnight())) {
			return true;
		} 
		return false;
	}
}
