package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Trend extends Model {

    public Date    trendDate;
    @ManyToOne
    public Insight insight;
    public long    relativeIndex;
    public long    agreeCount;
    public long    disagreeCount;

    public Trend(Date date, Insight insight,  long agreeCount, long disagreeCount) {
        this.trendDate = date;
        this.insight = insight;
        this.relativeIndex = getTrendCountForInsight(insight.id);
        this.agreeCount = agreeCount;
        this.disagreeCount = disagreeCount;
    }

    public static long getTrendCountForInsight(long insightId) {
         return find("select count(t) from Trend t join t.insight i where i.id = :insightId").bind("insightId", insightId).first();
    }
    
    /**
     * 
     * 
     * @param insightId
     * @return
     */
    public static List<List<Long>> getNormalizedAgreeDisagreeTrendForInsight(Long insightId) {
        List<List<Long>> result = new ArrayList<List<Long>>(2);

        // Number of horizontal value that will be used to create the charts
        long horizontalDefinition = 10;

        long trendsCount = getTrendCountForInsight(insightId);
        
        Long agreeCountMaxValue = find("select max(t.agreeCount) from Trend t join t.insight i where i.id = :insightId").bind("insightId", insightId).first();
        List<Long> agreeTrends;
        if (trendsCount <= horizontalDefinition) {
            agreeTrends = find("select t.agreeCount from Trend t join t.insight i where i.id = :insightId order by t.trendDate").bind("insightId", insightId)
                    .fetch();
        } else {
            long incrementSize = (getTrendCountForInsight(insightId) - 2) / horizontalDefinition;
            List<Long> indexList = new ArrayList<Long>((int)horizontalDefinition);
            for (int i = 1 ; i<horizontalDefinition ; i++) {
                indexList.add(i * incrementSize + 1);
            }
//            List<Long> indexList = Arrays.asList(0l, incrementSize + 1, incrementSize * 2 + 1, trendsCount-1 );
            
            agreeTrends = find(
                    "select t.agreeCount from Trend t join t.insight i where i.id = :insightId and t.relativeIndex in (:indexList) order by t.trendDate")
                    .bind("insightId", insightId).bind("indexList", indexList).fetch();
        }

        List<Long> normalizedAggreeTrends = new ArrayList<Long>(agreeTrends.size());
        for (Long agreeTrend : agreeTrends) {
            normalizedAggreeTrends.add((agreeTrend / agreeCountMaxValue * 100));
        }

        // TODO : do the same for the disagreeCount
        List<Long> disagreeTrends = new ArrayList<Long>();
//        Object disagreeCountMaxValue = find("select max(t.disagreeCount) from Trend t join t.insight i where i.id = :insightId").bind("insightId", insightId)
//                .first();
//        List<Long> disagreeTrends = find("select t.disagreeCount from Trend t join t.insight i where i.id = :insightId order by t.trendDate").bind("insightId",
//                insightId).fetch();

        result.add(normalizedAggreeTrends);
        result.add(disagreeTrends);

        return result;
    }
}
