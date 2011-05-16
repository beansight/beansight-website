package controllers;

import models.Event;
import play.mvc.Controller;

public class EventMiniSite extends Controller {

	public static void showEvent(String eventUniqueId) {
		Event event = Event.find("byUniqueId", eventUniqueId).first();
		notFoundIfNull(event);
		
		render(event);
	}
	
}
