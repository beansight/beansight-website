package controllers;

import models.Filter;
import models.Insight;
import models.Filter.FilterType;
import models.Insight.InsightResult;
import play.mvc.*;

public class Mobile extends Controller {

    public static void insights() {
    	InsightResult result;
    	Filter filter = new Filter();
    	filter.filterType = FilterType.UPDATED;
    	result = Insight.findLatest(0, 20, filter);
        render(result);
    }

    public static void insight(String insightUniqueId) {
    	Insight insight = Insight.findByUniqueId(insightUniqueId);
    	render(insight);
    }
    
    public static void create() {
    	render();
    }
    
}
