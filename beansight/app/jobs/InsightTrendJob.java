package jobs;

import java.util.Date;
import java.util.List;

import models.Insight;
import models.Trend;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("24h")
public class InsightTrendJob extends Job {

	public static final int INSIGHT_NUMBER_TO_PROCESS = 100;
	
    @Override
    public void doJob() throws Exception {
    	Logger.info("InsightTrendJob begin");
    	
    	int page = 1;
    	List<Insight> insights = Insight.all().fetch(page, INSIGHT_NUMBER_TO_PROCESS);
    	while(insights.size() > 0) {
    		processInsights(insights);
            Logger.info("InsightTrendJob: page " + page);
    		page++;
    		insights = Insight.all().fetch(page, INSIGHT_NUMBER_TO_PROCESS);
    	}
    	
        Logger.info("InsightTrendJob end");
    }
    
    
    private void processInsights(List<Insight> insights) {
        for (Insight insight : insights) {
            Logger.info("Trend Snapshot of Insight: " + insight.content);
            insight.createTrendSnapshot();
            insight.save();
        }
    }
    
}
