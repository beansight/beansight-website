import models.User;


public class TestHelper {

	
	public static String TEST_MAIL = "foo@test.com";
	public static String TEST_USER_NAME = "foo";
	public static String TEST_PASSWORD = "bar";
	
	public static User createTestUser() {
		User user = new User(TEST_MAIL, TEST_USER_NAME, TEST_PASSWORD);
        user.save();
        return user;
	}
	
	public static User getTestUser() {
		return User.findByUserName(TEST_USER_NAME);
	}
	
}
