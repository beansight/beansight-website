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
	 * Find a language from its label, if nothing is found creates it.
	 */
	public static Language findByLabelOrCreate(String label) {
		Language language = Language.find("byLabel", label).first();
		if (language == null && !label.equals("none")) {
			language = new Language(label);
			language.save();
		}
		return language;
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
