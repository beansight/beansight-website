package models;

import java.util.HashSet;
import java.util.Set;

/**
 * A filter is used to filter results
 */
public class UserInsightsFilter  {

	public User				user;
	public Set<Category> 	categories;
	public String 			filterVote;
	
	public UserInsightsFilter() {
		user = null; 
		categories 	= new HashSet<Category>();
		filterVote 	= "voted";
	}
	
	/**
	 * Generate a part of the where clause of a JPA query
	 * the insight must be called "i"
	 * example : "and c.id in ( '1','2' ) and i.lang.id in ('en')
	 */
	public String generateJPAQueryWhereClause() {
        String categoryIds = Category.listToIdString(this.categories);
		String whereQuery= "";
		
		if (!this.categories.isEmpty()) {
			whereQuery += " and i.category.id in (" + categoryIds + ") ";
		}
		
		if (filterVote.equals("voted")) {
			whereQuery += " and u.id=" + user.id;
		} else if (filterVote.equals("created")) {
			whereQuery += " and i.creator.id=" + user.id;
		}
		

		return whereQuery;
	}
}
