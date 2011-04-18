package notifiers;

import play.*;
import play.mvc.*;
import play.i18n.*;
import play.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ext.StringExtensions;

import models.CommentMentionMailTask;
import models.ContactMailTask;
import models.FollowNotificationTask;
import models.Insight;
import models.InsightShareMailTask;
import models.InvitationMailTask;
import models.InvitedSubscribedNotification;
import models.MailTask;
import models.MessageMailTask;
import models.NewCommentNotificationMailTask;
import models.User;
import models.WeeklyMailingTask;

public class Mails extends Mailer {

	public static final String FROM_NOTIFICATION = "notification@beansight.com";
	
	public static void confirmation(User user) {
		Logger.info("Confirmation email: sending to " + user.email);
		Lang.set(user.uiLanguage.label);
		setSubject(Messages.get("emailconfirmationsubject"));
		addRecipient(user.email);
		setFrom(FROM_NOTIFICATION);

		send(user);
	}

	public static boolean followNotification(FollowNotificationTask task) {
		Lang.set(task.language);
		return sendMailTask(task, Messages.get("emailfollownotificationsubject", task.follower.userName), "Mails/followNotification");
	}

	public static boolean invitation(InvitationMailTask task) {
		Lang.set(task.language);
		return sendMailTask(task, Messages.get("emailinvitationsubject", task.invitation.invitor.userName), "Mails/invitation");
	}
	
	public static boolean message(MessageMailTask task) {
		Lang.set(task.language);
		return sendMailTask(task, Messages.get("emailmessagesubject", task.message.fromUser.userName), "Mails/message");
	}
	
	public static boolean contact(ContactMailTask task) {
		Lang.set(task.language);
		return sendMailTask(task, task.subject, "Mails/contact");
	}

	public static boolean newCommentNotification(NewCommentNotificationMailTask task) {
		Lang.set(task.language);
		return sendMailTask(task, Messages.get("email.newCommentNotification.subject", StringExtensions.abbreviate( task.notification.comment.insight.content, 40) ), "Mails/newCommentNotification");
	}
	
	public static boolean commentMention(CommentMentionMailTask task) {
		Lang.set(task.language);	
		return sendMailTask(task, Messages.get("email.commentMention.subject", task.commentNotificationMsg.fromUser.userName), "Mails/commentMention");
	}

	public static boolean insightShare(InsightShareMailTask task) {
		Lang.set(task.language);	
		return sendMailTask(task, Messages.get("email.shareInsight.subject", task.share.fromUser.userName), "Mails/insightShare");
	}
	
	public static boolean weeklyMailing(WeeklyMailingTask task) {
		Lang.set(task.language);	
		return sendMailTask(task, Messages.get("email.beansightWeeklyUpdate.subject"), "Mails/beansightWeeklyUpdate");
	}
	
	private static boolean sendMailTask(MailTask task, String subject, String templateName) {
		Lang.set(task.language);
		
		task.attempt++;
		task.save();
		Logger.info("MailTask " + task.getClass().getSimpleName() + " to: " + task.sendTo);

		setSubject(subject);
		addRecipient(task.sendTo);
		setFrom(FROM_NOTIFICATION);
		
		Lang.set(task.language);	

		try {
			return send(templateName, task).get(20, TimeUnit.SECONDS);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} 
	}
	
	
	public static void forgotPassword(String forgotPasswordId, String email, String templateName, String language) {
		Lang.set(language);	
		setSubject(Messages.get("forgotPassword.subject"));
		addRecipient(email);
		setFrom(FROM_NOTIFICATION);

		send(templateName, forgotPasswordId);
	}

	public static boolean invitedSubscribedNotification(InvitedSubscribedNotification notification) {
		Lang.set(notification.notifiedUser.uiLanguage.label);
		setSubject(Messages.get("email.invitedSubscribed.subject", notification.subscribedUser.userName));
		addRecipient(notification.notifiedUser.email);
		setFrom(FROM_NOTIFICATION);
		try {
			return send(notification).get(20, TimeUnit.SECONDS);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} 
	}

}
