package models;

import java.util.*;

public class Filter  {
	
	public Set<Category> 	categories;
	public Set<Tag>		tags;
	public Set<Language> 	languages;
	
	public boolean favorites;
	
	public Filter() {
		categories = new HashSet<Category>();
		tags = new HashSet<Tag>();
		languages = new HashSet<Language>();
		favorites = false;
	}
}
