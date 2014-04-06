package tags;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import models.Insight;
import models.Vote;

import org.apache.commons.codec.binary.Base64;

import play.Play;
import play.libs.IO;
import play.templates.FastTags;
import play.templates.JavaExtensions;
import play.templates.GroovyTemplate.ExecutableTemplate;
import controllers.CurrentUser;
import controllers.Security;

@FastTags.Namespace("beansight")
public class Tags extends FastTags {
	
	private static ConcurrentHashMap<String, String> resourceMap = new ConcurrentHashMap<String, String>();
	
    @SuppressWarnings("unchecked")
    public static void _resource(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
    	String resourceName = args.get("name").toString();
    	
    	if ( !resourceMap.containsKey(resourceName) ) {
	    	byte[] content = IO.readContent(Play.getFile(resourceName));
	        resourceMap.putIfAbsent(resourceName, resourceName + "?" + hash(content));
    	} 
    	out.print(resourceMap.get(resourceName));
    }
    

    public static String hash(byte[] input) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] out = m.digest(input);
            return new String(Base64.encodeBase64(out));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
	
    public static void _hasVotedFor(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		if (hasVotedFor((Insight)args.get("insight"), (String)args.get("insightId"))) {
			out.print(JavaExtensions.toString(body));
		}
    }
    
    public static void _hasNotVotedFor(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		if (!hasVotedFor((Insight)args.get("insight"), (String)args.get("insightId"))) {
			out.print(JavaExtensions.toString(body));
		}
    }
    
    private static boolean hasVotedFor(Insight insight, String idStr) {
    	if (insight == null) {
    		if (idStr != null && !idStr.trim().equals("")) {
    			insight = Insight.findById(new Long(idStr));
    		} else {
    			return false;
    		}
    	}
    	
		Vote vote = Vote.findLastVoteByUserAndInsight(CurrentUser.getCurrentUser().id, insight.uniqueId);
		if (vote != null) {
			return true;
		}
		
		return false;
    }
}
