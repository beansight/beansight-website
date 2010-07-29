package models;

import java.util.ArrayList;
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
	
	@OneToMany(mappedBy="owner", cascade=CascadeType.ALL)
	public List<Insight> ownedInsights;
	
	@ManyToMany(cascade = CascadeType.ALL)
	public List<Insight> agreededInsights;
	
	@ManyToMany(cascade = CascadeType.ALL)
	public List<Insight> disagreededInsights;
	
    public User(String email, String password, String userName) {
        this.email = email;
        this.password = Crypto.passwordHash(password);
        this.userName = userName;
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
    
    public Insight createAnInsight(String insightContent) {
    	Insight i = new Insight();
    	i.content = insightContent;
    	i.owner = this;
    	if (this.ownedInsights == null)
    		this.ownedInsights = new ArrayList<Insight>();
    	this.ownedInsights.add(i);
    	i.save();
    	
    	return i;
    }
    
    /**
     * Retourne vraie si l'id de l'insight passé en paramètre
     * appartient au user
     * @param insightId
     * @return
     */
    public boolean ownThisInsight(Long insightId) {
		   Insight insight = Insight.findById(insightId);
		   if (insight.owner.id.equals(this.id))
			   return true;
		   else
			   return false;
    }

    public boolean hasAlreadyVotedForThisInsight(Long insightId) {
    	if (!this.agreededInsights.isEmpty())
    		System.out.println("agreededInsights non vide !");
    	return false;
    }
    
//    public boolean hasAlreadyVotedForThisInsight(Long insightId) {
//    	Insight anInsight = find("select distinct i from models.Insight i join i.whoAgreeds as whoAgreed where i.id= ? and whoAgreed.id = ?", insightId, this.id).first();
//    	if (anInsight!=null)
//    		return true;
//    	else
//    		return false;
//    }
}
