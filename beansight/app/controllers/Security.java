package controllers;

import models.User;
import play.mvc.Controller;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
        return User.authenticate(username, password);
    }

	static void onDisconnected() {
		Application.index();
    }
	
    static boolean check(String profile) {
        if("admin".equals(profile)) {
            return CurrentUser.isAdmin();
        }
        return false;
    }
	
}
