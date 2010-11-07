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
import exceptions.UserIsAlreadyFollowingInsightException;

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
    	boolean connected = User.authenticate(TestHelper.TEST_MAIL, TestHelper.TEST_PASSWORD);
    	
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
    public void votingTwiceSameSideForAnInsightIsNotPossible() {
    	// create a user and make him post a new insight
    	User user = new User("john.doe@usa.com", "john", "thepassword");
        user.save();
        Category categoryWeb = Category.findByLabel("Web");
        
        Insight insight = user.createInsight("I m always right", TestHelper.getDateWithXMonthFromNow(2), "", categoryWeb.id);
        
        // use the user test to vote for the created insight
        User userTest = TestHelper.getTestUser();
		assertFalse(Vote.hasUserVotedForInsight(userTest.id, insight.id));
		try {
			userTest.voteToInsight(insight.id, State.AGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			fail("CannotVoteTwiceForTheSameInsightException should not happen here");
		}
        assertTrue(Vote.hasUserVotedForInsight(userTest.id, insight.id));
        
        // userTest changes his mind and vote for the other side, this is possible
        try {
			userTest.voteToInsight(insight.id, State.DISAGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			fail("CannotVoteTwiceForTheSameInsightException should not happen here");
		}
        assertTrue(Vote.hasUserVotedForInsight(userTest.id, insight.id));
        
        // we test that there is 2 votes for the insight by the same user
        List<Vote> historicalVotes = Vote.findVotesByUserAndInsight(userTest.id, insight.id);
        assertNotNull(historicalVotes);
        assertTrue(historicalVotes.size() == 2);
        assertTrue(historicalVotes.get(0).status.equals(Status.ACTIVE));
        assertTrue(historicalVotes.get(1).status.equals(Status.HISTORIZED));
        
        // now userTest vote a second time but on the same side
        try {
        	userTest.voteToInsight(insight.id, State.DISAGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			// this should happen, so it's ok
		}
    }
    
    
    @Test
    public void testFindLastVote() throws CannotVoteTwiceForTheSameInsightException {
    	User user = new User("john.doe@usa.com", "john", "thepassword");
        user.save();
        Category categoryWeb = Category.findByLabel("Web");
        
        Insight insight = user.createInsight("I m always right", TestHelper.getDateWithXMonthFromNow(2), "brag", categoryWeb.id);
        
        User userTest = TestHelper.getTestUser();
        
        // no last vote yet return should be null
        Vote lastVote = Vote.findLastVoteByUserAndInsight(userTest.id, insight.id);
        assertNull(lastVote);
        
        // This time we vote so we'll have a "last vote"
		userTest.voteToInsight(insight.id, State.AGREE);
		lastVote = Vote.findLastVoteByUserAndInsight(userTest.id, insight.id);
		assertNotNull(lastVote);	
		assertTrue(lastVote.state.equals(State.AGREE));
		
		// let's vote another time
		userTest.voteToInsight(insight.id, State.DISAGREE);
		lastVote = Vote.findLastVoteByUserAndInsight(userTest.id, insight.id);
		assertNotNull(lastVote);	
		assertTrue(lastVote.state.equals(State.DISAGREE));
		
    }
}
