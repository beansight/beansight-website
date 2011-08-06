package ext;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import helpers.FormatHelper;

import org.apache.commons.lang.StringUtils;

import play.i18n.Messages;
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
	
    public static String in(Date date, Boolean stopAtMonth) {
    	if (stopAtMonth == null) {
    		stopAtMonth = Boolean.FALSE;
    	}
//    	if (showToday == null) {
//    		showToday = Boolean.FALSE;
//    	}
        Date now = new Date();
        if (now.after(date)) {
            return Messages.get("in.format.on", asdate(date.getTime(), Messages.get("in.format")));
        }
        long delta = (date.getTime() - now.getTime()) / 1000;
        if (delta < 24 * 60 * 60) {
        	return Messages.get("in.today");
        } 
        if (delta > 24 * 60 * 60 && delta < 2 * 24 * 60 * 60) {
        	return Messages.get("in.tomorrow");
        }
//        } else {
//	        if (delta < 60) {
//	            return Messages.get("in.seconds", delta, pluralize(delta));
//	        }
//	        if (delta < 60 * 60) {
//	            long minutes = delta / 60;
//	            return Messages.get("in.minutes", minutes, pluralize(minutes));
//	        }
//	        if (delta < 24 * 60 * 60) {
//	            long hours = delta / (60 * 60);
//	            return Messages.get("in.hours", hours, pluralize(hours));
//	        }
//        }
        if (delta < 30 * 24 * 60 * 60) {
            long days = delta / (24 * 60 * 60);
            return Messages.get("in.days", days, pluralize(days));
        }
        if (stopAtMonth) {
            return Messages.get("in.format.on", asdate(date.getTime(), Messages.get("in.format")));
        }
        if (delta < 365 * 24 * 60 * 60) {
            long months = delta / (30 * 24 * 60 * 60);
            return Messages.get("in.months", months, pluralize(months));
        }
        long years = delta / (365 * 24 * 60 * 60);
        return Messages.get("in.years", years, pluralize(years));
    }

    public static String pluralize2(Number n) {
        long l = n.longValue();
        if (l != 1 && l != 0) {
            return "s";
        }
        return "";
    }

    public static String lowerFirst(Object o) {
        String string = o.toString();
        if (string.length() == 0) {
            return string;
        }
        return ("" + string.charAt(0)).toLowerCase() + string.substring(1);
    }
    
    public static String still(Date lastDate, int maxMinutes) {
    	int minutesLeftResult = 0;
    	int secondesLeftResult = 0;
    	long maxTime = lastDate.getTime() + maxMinutes * 60000;
    	long delta = (maxTime - System.currentTimeMillis()) / 1000;
    	if (delta > 0) {
    		minutesLeftResult = Math.round( delta / 60 );
    		secondesLeftResult = Math.round( 60 * ((delta / 60f) - minutesLeftResult) );
    	}
    	return Messages.get("still.left", minutesLeftResult, secondesLeftResult); 
    }
}
