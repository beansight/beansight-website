package controllers;

import models.User;
import play.libs.Crypto;
import play.mvc.Controller;

public class Register extends Controller {

	public static void register() {
		render("Secure/register.html");
	}
	
	public static void registerNew(String email, String username, String password) {
		System.out.println("register:" + email + "/" + username + "/" + Crypto.passwordHash(password));
		User user = new User(email, username, password);
		user.save();
		
		Application.index();
	}
	
}
