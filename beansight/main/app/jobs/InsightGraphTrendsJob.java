package jobs;

import java.util.Date;
import java.util.List;

import models.Insight;
import models.Trend;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

@Every("6h")
public class InsightGraphTrendsJob extends Job {

	public static final int INSIGHT_NUMBER_TO_PROCESS = 100;
	
    @Override
    public void doJob() throws Exception {
    	Logger.info("InsightGraphTrendsJob begin");
    	
    	int page = 1;
    	// get insights having their target date not after the current date
    	List<Insight> insights = Insight.findEndDateNotOver(page, INSIGHT_NUMBER_TO_PROCESS);
    	
    	while(insights.size() > 0) {
    		processInsights(insights);
            Logger.info("InsightGraphTrendsJob: page " + page);
    		page++;
    		insights = Insight.all().fetch(page, INSIGHT_NUMBER_TO_PROCESS);
    	}
    	
        Logger.info("InsightGraphTrendsJob end");
    }
    
    
    private void processInsights(List<Insight> insights) {
        for (Insight insight : insights) {
            insight.buildTrends(null, null, 4);
        }
    }
    
}
