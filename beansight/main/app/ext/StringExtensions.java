package ext;

import helpers.FormatHelper;

import org.apache.commons.lang.StringUtils;

import play.templates.BaseTemplate.RawData;
import play.templates.JavaExtensions;

public class StringExtensions extends JavaExtensions {

	public static String abbreviate(String str, int size) {
		if (str.length()<=size) {
			return str;
		}
		return StringUtils.abbreviate(str, size);
	}
	
	public static RawData linkifyAll(Object data, String options) {
		return new RawData(FormatHelper.htmlLinkifyAll(data.toString(), options));
	}
	
}
