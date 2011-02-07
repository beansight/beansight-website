package unit;
import java.util.Date;
import java.util.List;

import models.Category;
import models.Insight;
import models.Language;
import models.User;
import models.Vote;
import models.Vote.State;
import models.Vote.Status;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.InsightWithSameUniqueIdAndEndDateAlreadyExistsException;
import exceptions.UserIsAlreadyFollowingInsightException;

public class InsightTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteAll();
		Fixtures.load("initial-data.yml");
		User user = TestHelper.createTestUser();
	}
    
    @Test
    public void createAnInsight() throws InsightWithSameUniqueIdAndEndDateAlreadyExistsException {
    	Category categoryWeb = Category.find("byLabel", "Web").first();
        assertNotNull(categoryWeb);
        Insight insight = TestHelper.getTestUser().createInsight("I know the future, don't you ?", new LocalDate(2010, 9, 1).toDateMidnight().toDate(), "test", categoryWeb.id, "en");

        assertEquals(insight, Insight.findById(insight.id));
        
        assertEquals(insight, TestHelper.getTestUser().createdInsights.get(0));
    }
    
    
    @Test
    public void startStopFollowingAnInsight() throws InsightWithSameUniqueIdAndEndDateAlreadyExistsException {
   		User user = new User("john.do@usa.com", "john", "24hours");
        user.save();
        
        Category categoryWeb = Category.findByLabel("Web");

        Insight insight = user.createInsight("I know the future, don't you ?", new LocalDate(2010, 9, 1).toDateMidnight().toDate(), "test", categoryWeb.id, "en");
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
    public void votingTwiceSameSideForAnInsightIsNotPossible() throws InsightWithSameUniqueIdAndEndDateAlreadyExistsException {
    	// create a user and make him post a new insight
    	User user = new User("john.doe@usa.com", "john", "thepassword");
        user.save();
        Category categoryWeb = Category.findByLabel("Web");
        
        Insight insight = user.createInsight("I m always right", TestHelper.getDateWithXMonthFromNow(2), "", categoryWeb.id, "en");
        
        // use the user test to vote for the created insight
        User userTest = TestHelper.getTestUser();
		assertFalse(Vote.hasUserVotedForInsight(userTest.id, insight.uniqueId));
		try {
			userTest.voteToInsight(insight.uniqueId, State.AGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			fail("CannotVoteTwiceForTheSameInsightException should not happen here");
		}
        assertTrue(Vote.hasUserVotedForInsight(userTest.id, insight.uniqueId));
        
        // userTest changes his mind and vote for the other side, this is possible
        try {
			userTest.voteToInsight(insight.uniqueId, State.DISAGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			fail("CannotVoteTwiceForTheSameInsightException should not happen here");
		}
        assertTrue(Vote.hasUserVotedForInsight(userTest.id, insight.uniqueId));
        
        // we test that there is 2 votes for the insight by the same user
        List<Vote> historicalVotes = Vote.findVotesByUserAndInsight(userTest.id, insight.uniqueId);
        assertNotNull(historicalVotes);
        assertTrue(historicalVotes.size() == 2);
        assertTrue(historicalVotes.get(0).status.equals(Status.ACTIVE));
        assertTrue(historicalVotes.get(1).status.equals(Status.HISTORIZED));
        
        // now userTest vote a second time but on the same side
        boolean exceptionRaised = false;
        try {
        	userTest.voteToInsight(insight.uniqueId, State.DISAGREE);
		} catch (CannotVoteTwiceForTheSameInsightException e) {
			// this should happen
			exceptionRaised = true;
		}
		assertTrue(exceptionRaised);
    }
    
    
    @Test
    public void testFindLastVote() throws CannotVoteTwiceForTheSameInsightException, InsightWithSameUniqueIdAndEndDateAlreadyExistsException {
    	User user = new User("john.doe@usa.com", "john", "thepassword");
        user.save();
        Category categoryWeb = Category.findByLabel("Web");
        
        Insight insight = user.createInsight("I m always right", TestHelper.getDateWithXMonthFromNow(2), "brag", categoryWeb.id, "en");
        
        User userTest = TestHelper.getTestUser();
        
        // no last vote yet return should be null
        Vote lastVote = Vote.findLastVoteByUserAndInsight(userTest.id, insight.uniqueId);
        assertNull(lastVote);
        
        // This time we vote so we'll have a "last vote"
		userTest.voteToInsight(insight.uniqueId, State.AGREE);
		lastVote = Vote.findLastVoteByUserAndInsight(userTest.id, insight.uniqueId);
		assertNotNull(lastVote);	
		assertTrue(lastVote.state.equals(State.AGREE));
		
		// let's vote another time
		userTest.voteToInsight(insight.uniqueId, State.DISAGREE);
		lastVote = Vote.findLastVoteByUserAndInsight(userTest.id, insight.uniqueId);
		assertNotNull(lastVote);	
		assertTrue(lastVote.state.equals(State.DISAGREE));
		
    }
    
    @Test
    public void testDuplicatedUniqueId() throws CannotVoteTwiceForTheSameInsightException, InsightWithSameUniqueIdAndEndDateAlreadyExistsException {
    	// We test that when the same Insight uniqueId is used more than once 
    	// then another unique should be automatically searched again :
    	Logger.debug("testing InsightTest.testDuplicatedUniqueId");
    	Date date = TestHelper.getDateWithXMonthFromNow(2);
    	Insight i = new Insight(TestHelper.getTestUser(), "Insight Test", date, Category.findByLabel("Web"), Language.findByLabelOrCreate("fr"));
    	i.save();
    	try {
			i = new Insight(TestHelper.getTestUser(), "Insight Test", date,
					Category.findByLabel("Web"),
					Language.findByLabelOrCreate("fr"));
		} catch (InsightWithSameUniqueIdAndEndDateAlreadyExistsException e) {
			// this is the exception we were waiting for
		}
    }
 
}
