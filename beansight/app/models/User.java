package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.jpa.Model;
import play.libs.Crypto;

@Entity
public class User extends Model {

	public String userName;
	public String firstName;
	public String lastName;
	public String password;
	public String email;
	
	/** list of insights created by this user */
	@OneToMany(mappedBy="creator", cascade=CascadeType.ALL)
	public List<Insight> createdInsights;
	
	/** every votes of the current user */
	@OneToMany(mappedBy="user", cascade = CascadeType.ALL)
	public List<Vote> votes;

	/** the insights followed by this user */
	@ManyToMany(cascade = CascadeType.ALL)
	public List<Insight> followedInsights;
	
	
    public User(String email, String password, String userName) {
        this.email = email;
        this.password = Crypto.passwordHash(password);
        this.userName = userName;
        this.votes = new ArrayList<Vote>();
        this.createdInsights = new ArrayList<Insight>();
    }
    
    public static boolean connect(String username, String password) {
    	User user = find("userName=? and password=?", username, Crypto.passwordHash(password)).first();
    	if (user!=null)
    		return true;

    	return false;
    }
	
    public static User findByUserName(String userName) {
    	return find("userName = ?", userName).first();
    }

    // TODO : get the end date.
    public Insight createInsight(String insightContent) {
    	Date endDate = new Date();
    	Insight i = new Insight(this, insightContent, endDate);
    	this.createdInsights.add(i);
    	i.save();
    	
    	return i;
    }

    /**
     * add a vote to the user's votes
     * @param vote
     */
//    public void addVote(Vote vote) {
//    	if (this.votes == null) {
//    		this.votes = new ArrayList<Vote>();
//    	}
//    	this.votes.add(vote);
//    }
//    
//    public int getAgreeNumber(Insight insight) {
//    	
//    	
//    	
//    	return 0;
//    }
    

    
}
