package controllers;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import models.User;
import models.oauthclient.Credentials;
import play.Logger;
import play.libs.WS;
import play.modules.oauthclient.ICredentials;
import play.modules.oauthclient.OAuthClient;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.results.RenderTemplate;

public class TwitterOAuth extends Controller {

    
    
    private static OAuthClient client = new OAuthClient("http://twitter.com/oauth/request_token", "http://twitter.com/oauth/access_token",
                                              "http://twitter.com/oauth/authorize", "Aa1wAqJ1qNGwtQMnHPLhNg", "63Bfee3Ni9zQbWtel5nALxMzKBeBaRFfWUXsTLJDs");

    public static void loginWithTwitter() throws Exception {
        ICredentials creds = new Credentials();

        String callbackURL = Router.getFullUrl(request.controller + ".callback");

        client.authenticate(creds, callbackURL);
    }

    public static void callback(String oauth_token, String oauth_verifier) throws Exception {
        // get the access token
        Credentials creds = new Credentials();
        Logger.info("Callback begin");
        
        client.retrieveAccessToken(creds, oauth_verifier);
        String twitterUserId = client.getProvider().getResponseParameters().get("user_id");
        String twitterScreenName = client.getProvider().getResponseParameters().get("screen_name");

        User twitterUser = User.findByTwitterUserId(twitterUserId);

        // If this is the first time this user uses his twitter account to
        // connect to beansight
        // then create a beansight account linked to his twitter account
        if (null == twitterUser) {
            twitterUser = new User("", twitterScreenName, "");
            twitterUser.twitterScreenName = twitterScreenName;
            twitterUser.twitterUserId = twitterUserId;
            twitterUser.save();

            // Exemple pour appeler l'api twitter :
            // String url = "http://api.twitter.com/1/users/show.xml?user_id="+
            // URLEncoder.encode(twitterUserId, "utf-8");
            // String response = client.getConsumer(creds).sign(WS.url(url),
            // "GET").get().getString();
            // System.out.println(response);
        } else {
            // update the twitter screen name
            twitterUser.twitterScreenName = twitterScreenName;
            twitterUser.save();
        }

        session.put("isTwitterUser", Boolean.TRUE);
        session.put("twitterUserId", twitterUserId);
        session.put("username", twitterUser.userName);

        // redirect(callback);
        Logger.info("Callback end");
        Application.index();
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
