

package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * Snapshot of the occurrence probability, agree and disagree numbers of a specific insight at a specific date
 */
@Entity
public class InsightTrend extends Model {

	/** Date at which the snapshot has been done */
    public Date    trendDate;
    @ManyToOne
    public Insight insight;
    
    /** probability between 0 and 1 the insight has to occur at this specific date */
    public double   occurenceProbability;
    /** number of agree votes at this specific date */
    public long		agreeCount;
    /** number of disagree votes at this specific date */
    public long		disagreeCount;

    public InsightTrend(Date date, Insight insight) {
        this.trendDate 	= date;
        this.insight 	= insight;
        this.agreeCount = 0;
        this.disagreeCount = 0;
        
        // score = ( sum position * DT ) / ( sum DT )
        // DT = (timestamp position) - (timestamp creation) 
        // position = 1 if agree, 0 if disagree
        
        double oneHour 	= 60*60*1000;
        
        // Two votes are added from the beginning to avoid a wrong probability with few voters
        double num 		= oneHour;
    	double denum 	= oneHour*2;
    	
        for(Vote vote : insight.getVotesBefore(date)) {
        	double dt = vote.creationDate.getTime() - insight.creationDate.getTime() + oneHour; // TODO GUILLAUME : why add one hour here ?
        	if(vote.state.equals(Vote.State.AGREE)) {
        		num += dt;
        		this.agreeCount++;
        	} else {
        		this.disagreeCount++;
        	}
        	denum += dt;
        }
        this.occurenceProbability = num / denum;    
    }
 
}

