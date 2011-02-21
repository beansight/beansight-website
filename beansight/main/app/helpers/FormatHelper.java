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
	
	public static String htmlLinkifyExpert(String text) {
		Replacer r = new Replacer(new Pattern("(\\W*@([\\w]+))"), "<a href='/expert/$2'>$1</a>");
		return r.replace(text);
	}
	
	public static String ln2br(String text) {
		Replacer r = new Replacer(new Pattern("\\n"), "<br/>");
		return r.replace(text);
	}
	
	public static String htmlLinkifyAll(String text) {
		return ln2br(htmlLinkifyExpert(text));
	}
}
