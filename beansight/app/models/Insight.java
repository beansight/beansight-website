package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.jpa.Model;


@Entity
public class Insight extends Model {

	@ManyToOne
	public User owner;
	
	public String content;
	
	public int agreeCount;
	@ManyToMany(mappedBy="agreededInsights", cascade = CascadeType.ALL)
	public List<User> whoAgreeds;
	
	public int neutralCount;
//	public List<User> whoNeutrals; 
	
	public int disagreeCount;
	@ManyToMany(mappedBy="disagreededInsights")
	public List<User> whoDisagreeds;
	
	
	public void addSomeoneWhoAgreed(User user) {
		agreeCount++;
		if(whoAgreeds==null)
			whoAgreeds = new ArrayList<User>();
		whoAgreeds.add(user);
		if (user.agreededInsights==null)
			user.agreededInsights = new ArrayList<Insight>();
		user.agreededInsights.add(this);
	}
	
	public void addSomeoneWhoDisagreed(User user) {
		disagreeCount++;
		if(whoDisagreeds==null)
			whoDisagreeds = new ArrayList<User>();
		whoDisagreeds.add(user);
	}
}
