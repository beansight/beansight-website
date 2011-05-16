package controllers;

import controllers.Secure.Security;
import models.Event;
import play.mvc.Controller;

public class EventMiniSite extends Controller {

	public static void showEvent(String eventUniqueId) {
		Event event = Event.find("byUniqueId", eventUniqueId).first();
		notFoundIfNull(event);
		
		render(event);
	}
	
	public static void logout(String eventUniqueId) {
        session.clear();
        response.removeCookie("rememberme");
        flash.success("secure.logout");
        showEvent(eventUniqueId);
	}
	
}
