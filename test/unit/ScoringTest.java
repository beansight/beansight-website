package unit;

import java.util.Date;
import java.util.List;

import models.Category;
import models.Insight;
import models.PeriodEnum;
import models.User;
import models.UserCategoryScore;
import models.UserInsightScore;
import models.Vote;
import models.Vote.State;
import models.Vote.Status;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.UserIsAlreadyFollowingInsightException;

public class ScoringTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteAll();
		Fixtures.load("scoring-data.yml");
	}

	@Test
    public void insightValidation() {
    	Insight i = Insight.findByUniqueId("beansight-will-be-in-private-beta");
    	i.validate();
    	Logger.info("validation score: "+ i.validationScore);

    	assertTrue("The insight should be validated", 					i.validated				);
    	assertTrue("Greater than 0.5 because most people voted for", 	i.validationScore > 0.5	);
    	assertTrue("Not 1 because not everyone voted for", 				i.validationScore < 1	);
    }

	@Test
    public void insightValidationExact() {
    	Insight i = Insight.findByUniqueId("beansight-will-be-in-private-beta");
    	i.validate();
    	assertTrue("As of 01/2011, should be this exact result", i.validationScore == 0.7619047619047619);
    }
	
	@Test
	public void voterScores() {
		Insight i = Insight.findByUniqueId("beansight-will-be-in-private-beta");
    	i.validate();
    	i.computeVoterScores();
    	
    	// get the user's scores
    	User steren 	= User.findByUserName("Steren");
    	User cyril 		= User.findByUserName("Cyril");
    	User guillaume 	= User.findByUserName("Guillaume");
    	User jb 		= User.findByUserName("JB");
    	UserInsightScore sterenScore 	= steren.getInsightScore(i);
    	UserInsightScore cyrilScore 	= cyril.getInsightScore(i);
    	UserInsightScore guillaumeScore = guillaume.getInsightScore(i);
    	UserInsightScore jbScore 		= jb.getInsightScore(i);

    	
    	String voteright = "Users who voted right must have a positive score on this insight";
    	assertTrue(voteright, sterenScore.score 	> 0);
    	assertTrue(voteright, guillaumeScore.score 	> 0);
    	assertTrue(voteright, jbScore.score 		> 0);

    	assertTrue("users who voted wrong should have a negative score on this insight", cyrilScore.score < 0);
    	
    	String voteOrder = "Users who voted the first should get more points";
    	assertTrue(voteOrder, cyrilScore.score 		< jbScore.score);
    	assertTrue(voteOrder, jbScore.score 		< guillaumeScore.score);
    	assertTrue(voteOrder, guillaumeScore.score 	< sterenScore.score);
	}
	
	@Test
	public void voterScoresExact() {
		Insight i = Insight.findByUniqueId("beansight-will-be-in-private-beta");
    	i.validate();
    	i.computeVoterScores();
    	
    	// get the user's scores
    	User steren 	= User.findByUserName("Steren");
    	User cyril 		= User.findByUserName("Cyril");
    	User guillaume 	= User.findByUserName("Guillaume");
    	User jb 		= User.findByUserName("JB");
    	UserInsightScore sterenScore 	= steren.getInsightScore(i);
    	UserInsightScore cyrilScore 	= cyril.getInsightScore(i);
    	UserInsightScore guillaumeScore = guillaume.getInsightScore(i);
    	UserInsightScore jbScore 		= jb.getInsightScore(i);
    	
    	String result = "As of 01/2011, should be this exact result";
    	assertTrue(result, sterenScore.score 		== 223.9143997954407);
    	assertTrue(result, cyrilScore.score		 	== -379.5141716331307);
    	assertTrue(result, guillaumeScore.score 	== 123.3429712240121);
    	assertTrue(result, jbScore.score 			== 114.03080191184279);
	}
	
	@Test
	public void userCategoryScore() {
		Date date = new DateTime(2011, 2, 15, 0, 0, 0, 0).toDate();
		
		Insight i = Insight.findByUniqueId("beansight-will-be-in-private-beta");
    	i.validate();
    	i.computeVoterScores();
    	
    	User steren = User.findByUserName("Steren");
    	steren.computeCategoryScores(date, PeriodEnum.THREE_MONTHS);
    	List<UserCategoryScore> catScores = steren.getCategoryScores(date, PeriodEnum.THREE_MONTHS);
    	assertTrue("Tested user should have a score in the voted category", !catScores.isEmpty());

    	for(UserCategoryScore catScore : catScores) {
			if(catScore.category.equals(i.category)) {
				assertTrue("User should gain point in the category he answered well", catScore.score > 0);
			} else {
				assertTrue("Voting well in a category shouldn't give points in an other category", catScore.score == 0);
			}
		}
	}
	
	@Test
	public void userGlobalScore() {
		Date date = new DateTime(2011, 2, 15, 0, 0, 0, 0).toDate();
		
		Insight i = Insight.findByUniqueId("beansight-will-be-in-private-beta");
    	i.validate();
    	i.computeVoterScores();
    	
    	User steren = User.findByUserName("Steren");
    	steren.computeCategoryScores(date, PeriodEnum.THREE_MONTHS);
    	steren.computeUserScore(date, PeriodEnum.THREE_MONTHS);
    	
    	assertTrue("User voted well, we should have a positive score", steren.score > 0);
	}
}
