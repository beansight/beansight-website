package controllers;

import helpers.InSitemap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.beanutils.BeanUtils;

import models.Category;
import models.Comment;
import models.ContactMailTask;
import models.Filter;
import models.FollowNotificationTask;
import models.Insight;
import models.Insight.InsightResult;
import models.Language;
import models.Tag;
import models.User;
import models.Vote;
import models.Vote.State;
import models.WaitingEmail;
import play.Logger;
import play.Play;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.Blob;
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.Images;
import play.modules.search.Query;
import play.modules.search.Search;
import play.mvc.Before;
import play.mvc.Controller;
import exceptions.CannotVoteTwiceForTheSameInsightException;
import exceptions.InsightAlreadySharedException;
import exceptions.NotFollowingUserException;
import exceptions.UserIsAlreadyFollowingInsightException;

public class Contact extends Controller {
	
	@InSitemap(changefreq="monthly", priority=0.3)
	public static void contact() {
		render();
	}
	
	public static void sendToContact(@Required String name, @Required @Email String from, @Required String subject, @Required String message) {
		ContactMailTaskForm result = new ContactMailTaskForm(false);
    	if(validation.hasErrors()) {
    		result.hasError = true;
    		for (String property : validation.errorsMap().keySet()) {
    			try {
					BeanUtils.setProperty(result, property, validation.errors(property));
				} catch (Exception e) {
					Logger.error(e, "Erreur while parsing validation error on the contact form");
					result.otherMessage = "Sorry we are encountering some problems. Please try later.";
				} 
    		}
    		
    		renderJSON(result);
	   	}
    	ContactMailTask contactMailTask = new ContactMailTask(name, from, "contact@beansight.com", subject, message);
    	contactMailTask.save();
    	result.otherMessage = "Thanks for your message we'll read it as soon as possible.";
    	renderJSON(result);
	}
	
	/**
	 * Object for json communication
	 * @author jb
	 *
	 */
	public static class ContactMailTaskForm {
		public boolean hasError;
		public String name;
		public String from;
		public String subject;
		public String message;
		public String otherMessage;
		
		public ContactMailTaskForm(boolean hasError) {
			this.hasError = hasError;
		}
	}
}