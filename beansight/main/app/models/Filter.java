package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Filter extends Model {
	
	@OneToMany
	public List<Category> 	categories;
	@OneToMany
	public List<Tag>		tags;
	@OneToMany
	public List<Language> 	languages;
	
	public boolean favorites;
}
