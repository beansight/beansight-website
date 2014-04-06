package functional;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import models.User;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class Signup extends FunctionalTest {

	
	@Before
	public void setup() {
		Fixtures.deleteAll();
		Fixtures.load("test-data.yml");
	}
	
	@Test
	public void testRegisterUserNameWithSpace() {
		Map<String, String> args = new HashMap<String, String>();
		args.put("email", "jb@claramonte.com");
		args.put("username", "ab cd");
		args.put("password", "aPassword");
		args.put("passwordconfirm", "aPassword");
		args.put("promocode", "ilovetagada");
		
		Response registerResponse = POST("/Register/registerNew", args, new HashMap<String,File>());
		// we shouldn't have an empty string because the user name has a space and this is not allowed
		assertNotSame(registerResponse.cookies.get("PLAY_ERRORS").value, "");
	}
	
	@Test
	public void testRegisterUserNameWithoutSpace() {
		Map<String, String> args = new HashMap<String, String>();
		args.put("email", "jb@claramonte.com");
		args.put("username", "abcd");
		args.put("password", "aPassword");
		args.put("passwordconfirm", "aPassword");
		args.put("promocode", "ilovetagada");
		
		Response registerResponse = POST("/Register/registerNew", args, new HashMap<String,File>());
		assertEquals(registerResponse.cookies.get("PLAY_ERRORS").value, "");
	}
}
