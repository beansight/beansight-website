package controllers;

import models.User;
import gson.FacebookModelObject;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.results.Redirect;

import com.google.gson.Gson;

import controllers.FacebookOAuth.FacebookOAuthDelegate;

/**
 * 
 * @author jb
 *
 */
public class FacebookOAuthForBeansight extends FacebookOAuth.FacebookOAuthDelegate {
    
                        
    public static void onFacebookAuthentication(FacebookModelObject facebookModelObject) {
        Long facebookUserId = facebookModelObject.getId();
        String facebookScreenName = facebookModelObject.getName();

        User facebookUser = User.findByFacebookUserId(facebookUserId);
        
        // If this is the first time this user uses his facebook account to
        // connect to beansight
        // then create a beansight account linked to his facebook account
        if (null == facebookUser) {
            // TODO : we should check that the username is not already in use, and if so add something like _twitterat the end of the username.            
            facebookUser = new User("", facebookScreenName, "");
            facebookUser.facebookScreenName = facebookScreenName;
            facebookUser.facebookUserId = facebookUserId;
            facebookUser.save();

        } else {
            // update the facebook screen name
            facebookUser.facebookScreenName = facebookScreenName;
            facebookUser.save();
        }
        
        session.put("isFacebookUser", Boolean.TRUE);
        session.put("facebookUserId", facebookUserId);
        session.put("username", facebookScreenName);
        
        Application.index();
    }
    
//
//    /**
//     * client_id
//     */
//    private static final String FB_CLIENT_ID = "137918342928326";
//    
//    /**
//     * client_secret
//     */
//    private static final String FB_APPLICATION_SECRET = "ac6ff2bdcd16a896106a66302d13b94a";
//    
//    /**
//     * API key
//     */
//    private static final String FB_API_KEY = "414df934367721acd7b72ab9fde90812";
//    
//    /*
//     * Example FB Cookie after FB authentication :
//     * fbs_137918342928326="access_token=137918342928326%7C2.x9vtoVicqHnW2iuT3M_kVA__.3600.1291939200-672593156%7CxKlyadvkZnuIqoWbFn5NTcU0J9I&base_domain=beansight.com&expires=1291939200&secret=2C8zGIZYFmVcbpZe65TLaw__&session_key=2.x9vtoVicqHnW2iuT3M_kVA__.3600.1291939200-672593156&sig=b15498e303d5d83a7eb7b23417a91c29&uid=672593156"
//     *  
//     */
//   
//// PHP example code to parse the FB cookie :
////    
////    function get_facebook_cookie($app_id, $application_secret) {
////        $args = array();
////        parse_str(trim($_COOKIE['fbs_' . $app_id], '\\"'), $args);
////        ksort($args);
////        $payload = '';
////        foreach ($args as $key => $value) {
////          if ($key != 'sig') {
////            $payload .= $key . '=' . $value;
////          }
////        }
////        if (md5($payload . $application_secret) != $args['sig']) {
////          return null;
////        }
////        return $args;
////      }
//    
//    
//    
//    public static void authenticate() {
//        throw new Redirect("https://graph.facebook.com/oauth/authorize?client_id=" + FB_CLIENT_ID + "&redirect_uri=" + Router.getFullUrl(request.controller + ".callback"));
//    }
//    
//    /**
//     * called by facebook after being authenticated 
//     */
//    public static void callback(String code) {
//        System.out.println("cool ça marche !");
//        System.out.println("code=" + code);
//        
//        StringBuilder fbAccessTokenUrl = new StringBuilder();
//        fbAccessTokenUrl.append("https://graph.facebook.com/oauth/access_token?client_id=").append(FB_CLIENT_ID)
//                                    .append("&redirect_uri=").append(Router.getFullUrl(request.controller + ".callback"))
//                                    .append("&client_secret=").append(FB_APPLICATION_SECRET)
//                                    .append("&code=").append(WS.encode(code));
//        
//        String response = WS.url(fbAccessTokenUrl.toString()).get().getString();
//        
//        System.out.println(response);
//        String accessToken = response.split("=")[1];
//        session.put("fb", accessToken);
//        
//        String facebookUserJson = WS.url("https://graph.facebook.com/me?access_token=" + accessToken).get().getString();
//        System.out.println(facebookUserJson);
//        
//        Gson gson = new Gson();
//        FacebookModelObject o = gson.fromJson(facebookUserJson, FacebookModelObject.class);
//        System.out.println("==>" + o.getId() ); 
//        //{"id":"672593156","name":"Jean-Baptiste Claramonte","first_name":"Jean-Baptiste","last_name":"Claramonte",
//        //"link":"http:\/\/www.facebook.com\/profile.php?id=672593156","gender":"male","timezone":1,"locale":"fr_FR",
//        //"verified":true,"updated_time":"2010-09-19T22:32:26+0000"}
//        
//        Application.index();
//        
////        throw new Redirect(fbAccessTokenUrl.toString());
//        // https://graph.facebook.com/oauth/access_token?client_id=137918342928326&redirect_uri=http://test.beansight.com:9000/facebookoauth/acccesstokencallback&client_secret=ac6ff2bdcd16a896106a66302d13b94a&code=2.hekNkBg8ITQkWA7O2oILkQ__.3600.1292158800-672593156|2WIbeXFry5IFEQuqWxr7yJDggZQ
//        
////        Cookie fbCookie = request.cookies.get("fbs_" + FB_CLIENT_ID);
////        System.out.println(fbCookie.value);
//        
//        // To validate the FB Cookie :
//        // 1. parse the string to get all the key/value in a map
//        // 2. order the key by alphabetic order
//        // 3. concatenate key=value (without the "sig" key associated value 
//        // which contains the md5 hash from facebook) and also add at the end "application secret id"  
//        // 4. create a md5 hash with the string created in 3. and compare with the value given in the "sig" key
//        // 5. if 4 return true then the cookie is coming from Facebook.
//        
//    }
//    
//    
//    public static void acccessTokenCallback(String accessToken) {
//        System.out.println(accessToken);
//        throw new Redirect(Router.getFullUrl("Application.index"));
//    }
}
