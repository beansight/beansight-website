package controllers;

import java.util.Map;

import models.Filter;
import models.Insight;
import models.Filter.SortBy;
import models.Insight.InsightResult;
import play.mvc.*;

public class Mobile extends Controller {

	/**
	 * The user needs to be connected to access the mobile version
	 */
	@Before(unless={"login"})
	public static void forceConnect() {
		if (!Security.isConnected()) {
			login();
		}
	}

	public static void insights() {
		render();
	}
	
	public static void insight(String insightUniqueId) {
		render();
	}

	public static void login() {
		render();
	}

	// public static void create() {
	// render();
	// }

}
