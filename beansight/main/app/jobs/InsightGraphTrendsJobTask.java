package jobs;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import models.Insight;
import models.Trend;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

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
//			i.buildTrends(new DateTime(2011, 3, 8, 0,0,0,0), null, 4);
		}
        
		Logger.info("< InsightGraphTrendsJobTask end from " + from + " to " + to);
    }
    
    

    
}
