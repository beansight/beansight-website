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
	public static final String PAGE_TO_PROCESS_KEY = "pageToProcess_page";
	
    @Override
    public void doJob() throws Exception {
    	// we get from the cache the page that the job have to process
    	// if PAGE_TO_PROCESS_KEY is not available in cache it means that the job is just starting
    	Long pageToProcess = (Long)Cache.get(PAGE_TO_PROCESS_KEY);
    	if (pageToProcess == null || pageToProcess == 0) {
    		pageToProcess = 1l;
    		Cache.add(PAGE_TO_PROCESS_KEY, pageToProcess);
    	}
    	Logger.info("InsightTrendsCalculateJob begin. Page:%s", pageToProcess);
    	
    	List<Insight> insights = null;
		// get insights having their target date not after the current date
		insights = Insight.findEndDateNotOver(pageToProcess.intValue(), INSIGHT_NUMBER_TO_PROCESS);
    	
    	Logger.info("InsightTrendsCalculateJob : insights.size()=%s", insights.size());
    	
    	processInsights(insights);
    	
    	if (Insight.count() > (pageToProcess * INSIGHT_NUMBER_TO_PROCESS)) {
    		Logger.info("scheduling another job in 5 seconds for page : " + (pageToProcess + 1));
    		// set in the cache the next page that InsightTrendsCalculateJob will have to process
    		// just in case we set an expiration of this cache in 5 minutes
    		Cache.incr(PAGE_TO_PROCESS_KEY);
    		new InsightTrendsCalculateJob().in(5);
    	} else {
    		// no more insinghts to process
    		// so we remove the PAGE_TO_PROCESS_KEY from the cache
    		Cache.delete(PAGE_TO_PROCESS_KEY);
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
