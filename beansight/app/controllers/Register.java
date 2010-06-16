package controllers;

import com.sun.xml.internal.ws.developer.UsesJAXBContext;

import models.User;
import play.mvc.Controller;

public class Register extends Controller {

	public static void register() {
		render("Secure/register.html");
	}
	
	public static void registerNew(String email, String username, String password) {
		System.out.println("register:" + email + "/" + username + "/" + password);
		User user = new User(email, username, password);
		user.save();
		
		Application.index();
	}
	
}
