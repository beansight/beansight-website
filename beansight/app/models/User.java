package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.jpa.Model;
import play.libs.Crypto;

@Entity
public class User extends Model {

	@Id
	public Long id;
	
	public String userName;
	public String firstName;
	public String lastName;
	public String password;
	public String email;
	
	@OneToMany(mappedBy="owner")
	public List<Insight> ownedInsights;
	
	@ManyToMany
	public List<Insight> agreededInsights;
	
	
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
}
