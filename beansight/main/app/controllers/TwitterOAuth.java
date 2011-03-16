package controllers;

import helpers.FileHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import models.Credentials;
import models.Promocode;
import models.User;
import models.analytics.UserClientInfo;
import models.analytics.UserPromocodeCampaign;
import notifiers.Mails;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.commons.lang.RandomStringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import play.Logger;
import play.Play;
import play.db.jpa.Blob;
import play.libs.WS;
import play.modules.oauthclient.ICredentials;
import play.modules.oauthclient.OAuthClient;
import play.mvc.Controller;
import play.mvc.Router;

public class TwitterOAuth extends Controller {

	private static final OAuthClient client = new OAuthClient(
			"http://twitter.com/oauth/request_token",
			"https://twitter.com/oauth/access_token",
			"http://twitter.com/oauth/authenticate", 
			Play.configuration.getProperty("twitter.consumerKey"),
			Play.configuration.getProperty("twitter.consumerSecret"));
	
	public static void authenticate() throws Exception {
		ICredentials creds = new Credentials();

		String callbackURL = Router
				.getFullUrl(request.controller + ".callback");

		// Uncomment this to enable twitter login
		client.authenticate(creds, callbackURL);
	}

	public static void callback(String oauth_token, String oauth_verifier)
			throws Exception {
		// get the access token
		Credentials creds = new Credentials();
		Logger.info("Callback begin");
		
		client.retrieveAccessToken(creds, oauth_verifier);
		String twitterUserId = client.getProvider().getResponseParameters()
				.get("user_id");
		String twitterScreenName = client.getProvider().getResponseParameters()
				.get("screen_name");

		User twitterUser = User.findByTwitterUserId(twitterUserId);
		
		// If this is the first time this user uses his twitter account to
		// connect to beansight
		// then create a beansight account linked to his twitter account
		if (null == twitterUser) {
			String userName = User.createNewAvailableUserName(twitterScreenName);
			// note : we have to generate a random password because if we use "" as a password twitter account could be easily hacked
			twitterUser = new User("", userName, RandomStringUtils.randomAlphabetic(15));
			twitterUser.twitterScreenName = twitterScreenName;
			twitterUser.twitterUserId = twitterUserId;
			updateUserWithTwitterInformations(twitterUser, creds);
			twitterUser.save();
		} 

		session.put("isTwitterUser", Boolean.TRUE);
		session.put("twitterUserId", twitterUserId);
		session.put("username", twitterUser.userName);

		Logger.info("Callback end");
		Application.index();
	}

	
	private static void updateUserWithTwitterInformations(User twitterUser, Credentials creds) throws SAXException, IOException, ParserConfigurationException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
		// Appel de l'api twitter
		// Récupérer l'image du profile twitter
		String url = "http://api.twitter.com/1/users/show.xml?user_id="
				+ URLEncoder.encode(twitterUser.twitterUserId, "utf-8");
		String response = client.getConsumer(creds).sign(WS.url(url), "GET")
				.get().getString();
		InputSource source = new InputSource(new ByteArrayInputStream(
				response.getBytes()));
		source.setEncoding("UTF-8");

		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(source);
		String profileImageUrl = doc.getDocumentElement()
				.getElementsByTagName("profile_image_url").item(0)
				.getTextContent();

		InputStream profileImageInputStream = WS.url(profileImageUrl).get()
				.getStream();

//		if (twitterUser.avatarSmall == null) {
//			twitterUser.avatarSmall = new Blob();
//		}
		// save so that we get an id for the new user
		twitterUser.save();
		twitterUser = twitterUser.refresh();
		// and now we can update avatar with the twitter profil image
		twitterUser.updateAvatar(FileHelper.getTmpFile(profileImageInputStream), true);
	}
	
	// private static void getUserInfo() throws Exception {
	//
	// String url = "http://twitter.com/statuses/update.json?status=" +
	// URLEncoder.encode(status, "utf-8");
	// String response = getConnector().sign(getUser().twitterCreds,
	// WS.url(url), "POST").post().getString();
	// request.current().contentType = "application/json";
	// renderText(response);
	// }

}
