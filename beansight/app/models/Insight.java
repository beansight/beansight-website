package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.jpa.Model;


@Entity
public class Insight extends Model {

	@Id
	public Long id;
	
	public String content;
	
	
	
}
