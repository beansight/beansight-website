package jobs;

import java.util.Date;
import java.util.List;

import models.Insight;
import models.Trend;
import play.jobs.Every;
import play.jobs.Job;

@Every("24h")
public class InsightTrendJob extends Job {

    @Override
    public void doJob() throws Exception {
        System.out.println("InsightTrendJob begin");
        
        int pageSize = 100;
        long insightCount = Insight.count();
        int pageCount = (int)(insightCount / pageSize);
        
        for (int i = 1; i <= pageCount; i++) {
            List<Insight> insights = Insight.all().fetch((i-1)*pageCount+1, i*pageCount);
            processInsights(insights);
        }
        
        List<Insight> insights = Insight.all().fetch(pageCount*pageSize+1, (int)insightCount);
        processInsights(insights);
        
        System.out.println("InsightTrendJob end");
    }
    
    
    private void processInsights(List<Insight> insights) {
        for (Insight insight : insights) {
            System.out.println("processing insight : " + insight.content);
            insight.addTrend(new Trend(new Date(), insight, insight.agreeCount, insight.disagreeCount));
            insight.save();
        }
    }
    
}
