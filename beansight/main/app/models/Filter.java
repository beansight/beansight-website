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
	
	/**
	 * Generate a part of the where clause of a JPA query
	 * the insight must be called "i"
	 * example : "and c.id in ( '1','2' ) and i.lang.id in ('en')
	 * 
	 * I'm sorry, this is ugly but blame SQL.
	 */
	public String generateJPAQueryWhereClause() {
        String categoryIds = Category.listToIdString(this.categories);
        String languageIds = Language.listToIdString(this.languages);
		String whereQuery= "";
        if (!this.categories.isEmpty() || !this.languages.isEmpty()) {
			if (!this.categories.isEmpty()) {
				whereQuery += " and i.category.id in (" + categoryIds + ") ";
			}
			if (!this.languages.isEmpty()) {
				whereQuery += " and i.lang.id in (" + languageIds + ") ";
			}
		}
		return whereQuery;
	}
}
