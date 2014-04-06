package unit;
import java.util.Calendar;
import java.util.Date;

import models.User;


public class TestHelper {

	
	public static String TEST_MAIL = "foo@test.com";
	public static String TEST_USER_NAME = "Bob";
	public static String TEST_PASSWORD = "secret";
	
	public static User createTestUser() {
		User user = new User(TEST_MAIL, TEST_USER_NAME, TEST_PASSWORD);
        user.save();
        return user;
	}
	
	public static User getTestUser() {
		return User.findByUserName(TEST_USER_NAME);
	}
	
	public static Date getDateWithXMonthFromNow(int months) {
		Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();        
	}
	
}
