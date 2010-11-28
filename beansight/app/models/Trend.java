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
    public double   agreeRatio;

    public Trend(Date date, Insight insight,  long agreeCount, long disagreeCount) {
        this.trendDate = date;
        this.insight = insight;
        this.relativeIndex = getTrendCountForInsight(insight.id);
        this.agreeCount = agreeCount;
        this.disagreeCount = disagreeCount;
        this.agreeRatio = 100 * (agreeCount)/ ((double)agreeCount + disagreeCount);
    }

    public static long getTrendCountForInsight(long insightId) {
         return find("select count(t) from Trend t join t.insight i where i.id = :insightId").bind("insightId", insightId).first();
    }
    
 
}
