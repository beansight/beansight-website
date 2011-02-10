package helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	
}
