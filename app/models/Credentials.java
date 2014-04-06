package models;

import play.*;
import play.db.jpa.*;
import play.modules.oauthclient.ICredentials;

import javax.persistence.*;
import java.util.*;

public class Credentials implements ICredentials {

	private String token;

	private String secret;

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getSecret() {
		return secret;
	}

}
