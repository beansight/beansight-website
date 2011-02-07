package helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatHelper {

	static SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
	
	
	/**
	 * format to : "MM-dd-yyyy"
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		return  formatter.format(date);
	}
	
}
