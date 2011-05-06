package helpers;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

public class TimeHelper {

	/**
	 * @param hourNumber : the hour number (see DateTime)
	 * @param dayNumber : can be null
	 * @return true if the current time is during this hour and day
	 */
	public static boolean hourAndDayCheck(int hourNumber, Integer dayNumber) {
		DateTime time = new DateTime();
		if(time.getHourOfDay() == hourNumber && (dayNumber == null || time.getDayOfWeek() == dayNumber)) {
			return true;
		}
		return false;
	}
}