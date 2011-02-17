package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.sf.oval.constraint.MaxLength;

import models.Language;
import models.User;
import play.Logger;
import play.data.validation.Match;
import play.data.validation.Required;
import play.i18n.Messages;
import play.mvc.*;

public class Settings extends Controller {
	
	public static final int AVATAR_MAX_SIZE = 3000000;
	
	public static void updateUserRealName(@MaxLength(User.REALNAME_MAXLENGTH) String realName) {
		User user = CurrentUser.getCurrentUser();
		if(user == null) {
			Logger.error("CurrentUser is null. realName was" + realName);
		}
		
		if (validation.hasErrors()) {
			flash.error(Messages.get("updateUserRealName.validation"));
			render(user);
	    }

		user.realName = realName;
		user.save();
		render(user);
	}
	
	public static void updateUserDescription(@MaxLength(User.DESCRIPTION_MAXLENGTH) String description) {
		User user = CurrentUser.getCurrentUser();
		if(user == null) {
			Logger.error("CurrentUser is null. description was" + description);
		}
		
		if (validation.hasErrors()) {
			flash.error(Messages.get("updateUserDescription.validation"));
			render(user);
	    }

		user.description = description;
		user.save();
		render(user);
	}
	
	public static void updateUserAvatar(File originalImage) {
		if (validation.hasErrors()) {
			flash.error(Messages.get("updateUserAvatar.validation"));
			Application.profile();
	    }
		User user = CurrentUser.getCurrentUser();
		
		// check if it's a valid image
		if (originalImage != null) {
			try {
				
				if (ImageIO.read(originalImage) == null) {
					flash.error(Messages.get("settings.imageInvalidFormat")); // TODO: internationalize text
					originalImage.delete();
				} else {
					// check the image size 
					if (originalImage.length() > AVATAR_MAX_SIZE) {
						flash.error(Messages.get("settings.imageSizeTooBig"));
						originalImage.delete();
					}
				}
			} catch (IOException e1) {
				flash.error(Messages.get("settings.imageInvalidFormat")); // TODO: internationalize text
				originalImage.delete();
			}
		}
		
		// check if a new image has been uploaded
		if (originalImage != null) {
			try {
				// and save it if so
				user.updateAvatar(originalImage, true);
			} catch (FileNotFoundException e) {
				flash.error(Messages.get("saveSettingImageNotFoundException"));
			}
		}
		user.save();
		
		Application.profile();
	}
	
}
