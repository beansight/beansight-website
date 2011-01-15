package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Language extends Model {
    
	public String label;
	
	public Language(String label) {
		this.label = label;
	}
	
	/**
	 * Find a language from its label, if nothing is found, return English
	 */
	public static Language findByLabel(String label) {
		Language language = Language.find("byLabel", label).first();
		if (language == null) {
			language = Language.find("byLabel", "en").first();
		}
		return language;
	}
	
}
