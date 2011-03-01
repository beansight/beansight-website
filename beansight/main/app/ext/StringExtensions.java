package ext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
	
	/**
	 * Example :
	 * 
	 * 	List<String> langs = new ArrayList<String>();
	 *	langs.add("en");
	 *  langs.add("fr");
	 *  
	 *  And then in the template :
	 *  ${langs.asJavascriptArray()}
	 *  result : ['en', 'fr']
	 *  
	 * @param list
	 * @return
	 */
	public static String asJavascriptArray(List<?> list)
	{
	    StringBuffer sb = new StringBuffer(15 * list.size());
	    sb.append('[');

	    Iterator ite = list.iterator();
	    int i = 0;
	    while (ite.hasNext())
	    {
	        if (i++ > 0)
	        {
	            sb.append(',');
	        }
	        sb.append("'").append(ite.next().toString().replace("'", "\'")).append("'");
	    }

	    sb.append(']');
	    return sb.toString();
	}
	

}
