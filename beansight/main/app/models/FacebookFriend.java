package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class FacebookFriend extends Model {

	/** user didn't want to add this person to its beansight favorites */
	public boolean isHidden;
	
	/** user has added this person to its beansight favorites */
	public boolean isAdded;
	
	/** is this facebook user also a beansight user ? */
	public boolean isBeansightUser;
	
	/** a friend */
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public FacebookUser facebookUser;
	
	/** shortcut to the beansight user friend (beansight user of the facebookUser property) */
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public User beansightUserFriend;
	
	/** "owner" of the friendship */
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public User user;

	public FacebookFriend(FacebookUser facebookUser, User aBeansightUserFriend, User user) {
		super();
		this.isHidden = false;
		this.isAdded = false;
		this.isBeansightUser = false;
		this.facebookUser = facebookUser;
		this.beansightUserFriend = aBeansightUserFriend;
		this.user = user;
		user.facebookFriends.add(this);
	}
	
	/**
	 * Use this method to retrieve the FacebookFriend relation entity between
	 * a user "owning" the relationship and another user
	 * @param userIdOwningTheFriendship
	 * @param userIdOfTheOtherSideOfTheFriendship
	 * @return
	 */
	public static FacebookFriend findBetweenUserIds(Long userIdOwningTheFriendship, Long userIdOfTheOtherSideOfTheFriendship) {
		return FacebookFriend.find("user.id = ? and beansightUserFriend.id = ?", userIdOwningTheFriendship, userIdOfTheOtherSideOfTheFriendship).first();
	}
	
	//public static List<Facebookfriend> findBy
	
	public String toString() {
		StringBuilder sb = new StringBuilder()
		.append(user.userName)
		.append("(id:")
		.append(user.id)
		.append(", fbId:")
		.append(user.facebookUserId)
		.append(")")
		.append(" has a facebook friend :");
		if (beansightUserFriend != null) {
			sb.append(beansightUserFriend.userName)
			.append("(id:")
			.append(beansightUserFriend.id)
			.append(", fbId:")
			.append(facebookUser.facebookId)
			.append(")");
		} else {
			sb.append(" fbId:")
			.append(facebookUser.facebookId);
		}
		sb.append(", in beansight this relationship is : isHidden:")
		.append(isHidden)
		.append(", isAdded:")
		.append(isAdded)
		.append(", isBeansightUser:")
		.append(isBeansightUser);
		
		return sb.toString();
	}
}
