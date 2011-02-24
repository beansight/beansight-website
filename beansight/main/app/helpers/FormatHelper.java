package helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jregex.Pattern;
import jregex.Replacer;

import play.Logger;

public class FormatHelper {

	static SimpleDateFormat dateFormater_MMddyyyy = new SimpleDateFormat("MM-dd-yyyy");
	static SimpleDateFormat dateFormater_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * format to : "MM-dd-yyyy"
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		return  dateFormater_MMddyyyy.format(date);
	}
	
	/**
	 * format to : "yyyy-MM-dd"
	 * @param date
	 * @return
	 */
	public static Date parseDate(int year, int month, int day) {
		String dateStr = year + "-" + month + "-" + day;
		try {
			return dateFormater_yyyyMMdd.parse(year + "-" + month + "-" + day);
		} catch (ParseException e) {
			Logger.error("error parsing : %s", dateStr);
		}
		return null;
	}
	
	public static String htmlLinkifyExpert(String text, String options) {
		Replacer r = new Replacer(new Pattern("(\\W*@([\\w]+))"), "<a href='/expert/$2'" + options + ">$1</a>");
		return r.replace(text);
	}
	
	public static String htmlLinkifyUrl(String text, String options) {
		 //URLs starting with http://, https://, or ftp://
		Replacer r = new Replacer(new Pattern("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?)"), "<a href='$1' target='_blank' " + options + ">$1</a>");
		text = r.replace(text);
		
		//URLs starting with www. (without // before it, or it'd re-link the ones done above)
		r = new Replacer(new Pattern("(^|[^\\/])(www\\.[\\S]+(\\b|$))"), "$1<a href='http://$2' target='_blank' " + options + ">$2</a>");
		text = r.replace(text);
		
		//Change email addresses to mailto:: links
		r = new Replacer(new Pattern("(\\w+@[a-zA-Z_]+?\\.[a-zA-Z]{2,6})"), "<a href='mailto:$1' " + options + ">$1</a>");
		text = r.replace(text);
		
		return text;
	}
	
	public static String ln2br(String text) {
		Replacer r = new Replacer(new Pattern("\\n"), "<br/>");
		return r.replace(text);
	}
	
	public static String htmlLinkifyAll(String text, String options) {
		return htmlLinkifyUrl(htmlLinkifyExpert(text, options), options);
	}
}
