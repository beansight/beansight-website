package controllers;

import models.Filter;
import models.Insight;
import models.Filter.FilterType;
import models.Insight.InsightResult;
import play.mvc.*;

public class Mobile extends Controller {

    public static void insights() {
        render();
    }

    public static void create() {
    	render();
    }
    
}
