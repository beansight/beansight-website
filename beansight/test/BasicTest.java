import org.joda.time.LocalDate;
import org.junit.*;

import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.UserIsAlreadyFollowingInsightException;

import java.util.*;

import play.test.*;
import models.*;
import models.Vote.State;

public class BasicTest extends UnitTest {

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
    	boolean connected = User.connect(TestHelper.TEST_USER_NAME, TestHelper.TEST_PASSWORD);
    	
        assertTrue(connected);
    }
    
    
    @Test
    public void createAnInsight() {
    	Category categoryWeb = Category.find("byLabel", "Web").first();
        assertNotNull(categoryWeb);
        Insight insight = TestHelper.getTestUser().createInsight("I know the future, don't you ?", new LocalDate(2010, 9, 1).toDateMidnight().toDate(), "test", categoryWeb.id);

        assertEquals(insight, Insight.findById(insight.id));
        
        assertEquals(insight, TestHelper.getTestUser().createdInsights.get(0));
    }
    
    
    @Test
    public void startStopFollowingAnInsight() {
   		User user = new User("john.do@usa.com", "john", "24hours");
        user.save();
        
        Category categoryWeb = Category.findByLabel("Web");

        Insight insight = user.createInsight("I know the future, don't you ?", new LocalDate(2010, 9, 1).toDateMidnight().toDate(), "test", categoryWeb.id);
        assertNotNull(insight);
        assertNotNull(insight.id);
        
        try {
        	User u = TestHelper.getTestUser();
        	assertNotNull(u);
			u.startFollowingThisInsight(insight.id);
		} catch (UserIsAlreadyFollowingInsightException e) {
			fail("UserIsAlreadyFollowingInsightException has been thrown but shouldn't.");
		}
		assertTrue(TestHelper.getTestUser().isFollowingInsight(insight));
		assertTrue(TestHelper.getTestUser().followedInsights.size()==1);
    }
    
    @Test
    public void votingTwiceForAnInsightIsNotPossible() {
    	// create a user and make him post a new insight
    	User user = new User("john.doe@usa.com", "john", "thepassword");
        user.save();
        Category categoryWeb = Category.findByLabel("Web");
        
        Insight insight = user.createInsight("I m always right", TestHelper.getDateWithXMonthFromNow(2), "", categoryWeb.id);
        
        // use the user test and make him vote twice for the insight
        User userTest = TestHelper.getTestUser();
		assertFalse(Vote.hasUserVotedForInsight(userTest.id, insight.id));
		try {
			userTest.voteToInsight(insight.id, State.AGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			fail("CannotVoteTwiceForTheSameInsightException should not happen here");
		}
        assertTrue(Vote.hasUserVotedForInsight(userTest.id, insight.id));
        try {
        	userTest.voteToInsight(insight.id, State.AGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			// this should happen, so it's ok
		}
    }
}
