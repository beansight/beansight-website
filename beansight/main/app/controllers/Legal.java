package controllers;

import helpers.ImageHelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import models.Category;
import models.Comment;
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

public class Legal extends Controller {
	
	public static void privacyPolicy() {
		renderTemplate("Legal/privacyPolicy.html");
	}
	
	public static void termsOfUse() {
		renderTemplate("Legal/termsOfUse.html");
	}
}