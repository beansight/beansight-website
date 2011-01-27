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
	
	/**
	 * Transforms a list of language string to a set of Language
	 * @param langStrings : the list of language string
	 */
	public static Set<Language> toLanguageSet( Set<String> langStrings ) {
		Set<Language> languages = new HashSet<Language>();
		for(String lang : langStrings) {
			languages.add(Language.findByLabel(lang));	
		}
		return languages;
	}
	
	public static String listToIdString(Set<Language> languages) {
        StringBuffer buffer = new StringBuffer();
        Iterator<Language> iter = languages.iterator();
        while (iter.hasNext()) {
        	buffer.append("'");
            buffer.append(iter.next().id);
            buffer.append("'");
            if (iter.hasNext()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
	}
	
	public String toString() {
		return label;
	}
}
