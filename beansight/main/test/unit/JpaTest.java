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

public class JpaTest extends UnitTest {

    
    @Test
    public void createAnInsight() throws InsightWithSameUniqueIdAndEndDateAlreadyExistsException {
    	Category categoryWeb = Category.find("byLabel", "Web").first();
        assertNotNull(categoryWeb);
        Insight insight = TestHelper.getTestUser().createInsight("I know the future, don't you ?", new LocalDate(2010, 9, 1).toDateMidnight().toDate(), "test", categoryWeb.id, "en");

        assertEquals(insight, Insight.findById(insight.id));
        
        assertEquals(insight, TestHelper.getTestUser().createdInsights.get(0));
    }
  
 
}
