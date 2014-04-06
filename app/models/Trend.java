package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * Represents the state of a given insight at a given date, used for Graph plotting.
 */
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
        this.relativeIndex = insight.getTrendCount();
        this.agreeCount = agreeCount;
        this.disagreeCount = disagreeCount;
        long total = agreeCount + disagreeCount;
        if (total > 0 && agreeCount > 0) {
        	this.agreeRatio =  100 * (agreeCount)/ ((double)agreeCount + disagreeCount);
        } else if (agreeCount == 0) {
        	this.agreeRatio = 0;
        } else if (disagreeCount == 0) {
        	this.agreeRatio = 100;
        } else {
        	this.agreeRatio = 50;
        }
    }
 
}
