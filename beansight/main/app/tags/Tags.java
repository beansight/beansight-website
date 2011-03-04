package tags;

import groovy.lang.Closure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;

import play.Play;
import play.libs.Crypto;
import play.libs.IO;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;

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
	
}
