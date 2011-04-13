package controllers;

import helpers.RenderSitemap;

import java.util.List;

import models.Filter;
import models.Insight;
import models.Language;
import models.User;
import models.Filter.FilterType;
import play.mvc.Controller;

public class Sitemap extends Controller {
	
	private static final int INSIGHT_NUMBER = 200;
	
    public static void siteMap() {
    	
		Filter filter = new Filter();
		filter.filterType = FilterType.UPDATED;
		filter.languages.add(Language.findByLabelOrCreate("en"));
		filter.languages.add(Language.findByLabelOrCreate("fr"));
    	
        List<User> users = User.findAll();
        List<Insight> insights = Insight.findLatest(0, INSIGHT_NUMBER, filter).results;
        throw new RenderSitemap(users, insights);
    }
	
}