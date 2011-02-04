package ext;

import org.apache.commons.lang.StringUtils;

import play.templates.JavaExtensions;

public class StringExtensions extends JavaExtensions {

	public static String abbreviate(String str, int size) {
		if (str.length()<=size) {
			return str;
		}
		return StringUtils.abbreviate(str, size);
	}
	
}
