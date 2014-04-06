package controllers;

import java.util.Map;

import models.Filter;
import models.Insight;
import models.Filter.SortBy;
import models.Insight.InsightResult;
import play.mvc.*;

public class BeansightPlay extends Controller {

	/**
	 * The user needs to be connected to access BeansightPlay
	 */
	@Before(unless={"login"})
	public static void forceConnect() {
		if (!Security.isConnected()) {
			login();
		}
	}

	public static void beansightPlay() {
		render();
	}
	
	public static void login() {
		render();
	}

}
