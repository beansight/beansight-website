package unit;
import java.util.List;

import models.Category;
import models.Insight;
import models.User;
import models.Vote;
import models.Vote.State;
import models.Vote.Status;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.InvitationException;
import exceptions.UserIsAlreadyFollowingInsightException;

public class UserTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteAll();
		Fixtures.load("initial-data.yml");
		User user = TestHelper.createTestUser();
	}
	
    @Test
    public void createNewUser() {
   		User user = new User("john.doe@usa.com", "john", "thepassword");
        user.save();
        
        User userFound = User.findByUserName("john");
        assertEquals(user, userFound);
    }
    
    @Test
    public void connectUser() {
    	boolean connected = User.authenticate(TestHelper.TEST_MAIL, TestHelper.TEST_PASSWORD);
        assertTrue(connected);
    }
    
    @Test
    public void wrongPassword() {
    	boolean connected = User.authenticate(TestHelper.TEST_MAIL, "wrong");
        assertFalse(connected);
    }

    @Test
    public void wrongPasswordCase() {
    	boolean connected = User.authenticate(TestHelper.TEST_MAIL, TestHelper.TEST_PASSWORD.toUpperCase());
        assertFalse(connected);
    }

    @Test
    public void userNotAdmin() {
   		User user = TestHelper.getTestUser();
        assertFalse(user.isAdmin);
    }
    
    @Test
    public void inviteSomeone() {
   		User user = TestHelper.getTestUser();
   		
   		// insure no invitation left
		user.invitationsLeft = 0;
		user.save();
		
        try {
			user.invite("toto@test.com", "beansight is awesome !!!");
		} catch (InvitationException e) {
			// there have to be an exception since the user doesn't have any invitation left
		}
		
		// give an invitation to user
		user.invitationsLeft = 1;
		user.save();
		
		try {
			user.invite("toto@test.com", "beansight is awesome !!!");
		} catch (InvitationException e) {
			fail(e.getMessage()); 
		}
		
		
    }
}
