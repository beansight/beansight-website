package models;

import java.util.*;

public class Filter  {
	
	public List<Category> 	categories;
	public List<Tag>		tags;
	public List<Language> 	languages;
	
	public boolean favorites;
	
	public Filter() {
		categories = new ArrayList<Category>();
		tags = new ArrayList<Tag>();
		languages = new ArrayList<Language>();
		favorites = false;
	}
}
