package models;

import gson.FriendGson;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;


@Entity
public class FacebookUser extends Model {

	@Column(unique=true, nullable=false)
	public Long facebookId;
	
	public String name;
	public String firstName;
	public String lastName;
	public String link;
	public String gender;
	public String timezone;
	public String locale;
	public boolean verified;
	public String updateTime;
	
	@OneToMany(cascade=CascadeType.ALL)
	public List<FacebookUser> friends;
	
	
	public FacebookUser(Long facebookId) {
		this.facebookId = facebookId;
		this.friends = new ArrayList<FacebookUser>();
	}
	
	public static FacebookUser findByFacebookId(Long facebookId) {
		return FacebookUser.find("select f from FacebookUser f where facebookId = ?", facebookId).first();
	}
	
	public boolean isThisFacebookUserAlreadyMyFriend(Long friendFacebookId) {
		Long count = FacebookUser.find("select count(f) from FacebookUser f " +
				"join f.friends as fs " +
				"where f.facebookId = ? " +
				"and fs.facebookId = ?", this.facebookId,  friendFacebookId)
				.first();
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}
	
}
