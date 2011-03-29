package jobs;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import models.Insight;
import models.Trend;
import play.Logger;
import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

@Deprecated 
/**
 * @Deprecated : uses the old trends ratio
 */
public class InsightGraphTrendsJobTask extends Job {

	private Long from;
	private Long to;
	
	public InsightGraphTrendsJobTask(Long from, Long to) {
		this.from = from;
		this.to = to;
	}
	
    @Override
    public void doJob() throws Exception {
    	Logger.info("> InsightGraphTrendsJobTask begin from " + from + " to " + to);
    	
    	List<Insight> list = Insight.find("id between :from and :to").bind("from", from).bind("to", to).fetch();
		for (Insight i : list) {
			i.buildTrends(new DateTime(i.creationDate), null, 4);
		}
		
    	// delete the trends list from the cache so that ui get new calculated trends value
    	Cache.delete("agreeRatioTrendsCache");
        
		Logger.info("< InsightGraphTrendsJobTask end from " + from + " to " + to);
    }
    
    

    
}
