package jobs;

import java.util.Date;
import java.util.List;

import models.Insight;
import models.Trend;
import play.Logger;
import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

/**
 * This job calculate all the InsightTrend
 */
@Every("1h")
public class InsightTrendsCalculateJob extends Job {

	public static final int INSIGHT_NUMBER_TO_PROCESS = 100;
	private int page = 1;
	/** set all to true if you want to recalculate for all insights */
	private boolean all = false; 
	
	public InsightTrendsCalculateJob() {
	}
	
	public InsightTrendsCalculateJob(int page, boolean all) {
		this.page = page;
		this.all = all;
	}
	
    @Override
    public void doJob() throws Exception {
    	Logger.info("InsightTrendsCalculateJob begin : page=%s all=%s", page, all);
    	
    	List<Insight> insights = null;
    	if (all) {
    		insights = Insight.all().fetch(page, INSIGHT_NUMBER_TO_PROCESS);
    	} else {
    		// get insights having their target date not after the current date
    		insights = Insight.findEndDateNotOver(page, INSIGHT_NUMBER_TO_PROCESS);
    	}
    	
    	Logger.info("InsightTrendsCalculateJob : insights.size()=%s", insights.size());
    	
    	processInsights(insights);
    	
    	if (Insight.count() > (page * INSIGHT_NUMBER_TO_PROCESS)) {
    		Logger.info("scheduling another job in 5 seconds for page : " + (page + 1));
    		new InsightTrendsCalculateJob(++page, all).in(5);
    	} else {
        	// delete the trends list from the cache so that ui get new calculated trends value
    		Logger.info("InsightTrendsCalculateJob : deleting cache agreeInsightTrendsCache");
        	Cache.delete("agreeInsightTrendsCache");
    	}
    	
        Logger.info("InsightTrendsCalculateJob end");
    }
    
    
    private void processInsights(List<Insight> insights) {
        for (Insight insight : insights) {
            insight.buildInsightTrends();
        }
    }
    
}
