package helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeSeriePointHelper {

	/** Smooth the given list and return */
	public static List<TimeSeriePoint> smooth(List<TimeSeriePoint> serie,
			int days) {
		List<TimeSeriePoint> result = new ArrayList<TimeSeriePoint>();
		for (int i = days; i < serie.size(); i++) {
			double sum = 0;
			for(int j=0; j < days; j++) {
				sum += serie.get(i - j).value;
			}
			result.add( new TimeSeriePoint(serie.get(i).date, sum / days) );
		}
		return result;
	}

}
