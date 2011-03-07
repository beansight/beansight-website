package notifiers;

import play.*;
import play.mvc.*;
import play.i18n.*;
import play.Logger;

import java.util.*;

import models.CommentNotificationMailTask;
import models.ContactMailTask;
import models.FollowNotificationTask;
import models.Insight;
import models.InvitationMailTask;
import models.MailTask;
import models.MessageMailTask;
import models.User;

public class Mails extends Mailer {

	public static void confirmation(User user) {
		Logger.info("Confirmation email: sending to " + user.email);

		setSubject(Messages.get("emailconfirmationsubject"));
		addRecipient(user.email);
		setFrom("notification@beansight.com");

		send(user);
	}

	public static void followNotification(FollowNotificationTask task) {
		sendMailTask(task, Messages.get("emailfollownotificationsubject", task.follower.userName), "Mails/followNotification.html");
	}

	public static void invitation(InvitationMailTask task) {
		sendMailTask(task, Messages.get("emailinvitationsubject", task.invitation.invitor.userName), "Mails/invitation.html");
	}
	
	public static void message(MessageMailTask task) {
		sendMailTask(task, Messages.get("emailmessagesubject", task.message.fromUser.userName), "Mails/message.html");
	}
	
	// FIXME : changer le template pour contact
	public static void contact(ContactMailTask task) {
		sendMailTask(task, task.subject, "Mails/contact.html");
	}
	
	private static void sendMailTask(MailTask task, String subject, String templateName) {
		task.attempt++;
		task.save();
		Logger.info("MailTask " + task.getClass().getSimpleName() + " to: " + task.sendTo);

		setSubject(subject);
		addRecipient(task.sendTo);
		setFrom("notification@beansight.com");
		
		Lang.set(task.language);	

		send(templateName, task);
	}
	
	
	public static void forgotPassword(String forgotPasswordId, String email, String subject, String templateName, String language) {
		setSubject(subject);
		addRecipient(email);
		setFrom("notification@beansight.com");
		
		Lang.set(language);	

		send(templateName, forgotPasswordId);
	}
	
	public static void commentNotification(CommentNotificationMailTask task) {
		task.attempt++;
		task.save();
		Logger.info("MailTask " + task.getClass().getSimpleName() + " to: " + task.sendTo);

//		Insight insight = task.commentNotificationMsg.insight;
		User commentWriter = task.commentNotificationMsg.fromUser;
		User userToNotify = task.commentNotificationMsg.toUser;
//		String content = task.commentNotificationMsg.comment.content;
		
		sendMailTask(task, Messages.get("newCommentNotification.subject", commentWriter.userName), "Mails/commentNotification");
		
//		sendNewCommentNotification(
//				task.commentNotificationMsg.insight, 
//				task.commentNotificationMsg.fromUser, 
//				task.commentNotificationMsg.toUser, 
//				task.commentNotificationMsg.comment.content);
	}
	
//	private static void sendNewCommentNotification(Insight insight, User commentWriter, User userToNotify, String commentContent) {
//		setSubject(Messages.get("newCommentNotification.subject", commentWriter.userName, userToNotify.userName));
//		addRecipient(userToNotify.email);
//		setFrom("notification@beansight.com");
//		
//		Lang.set(userToNotify.writtingLanguage.label);	
//		
//		send("Mails/commentNotification", insight, commentWriter, userToNotify, commentContent);
//	}
	
}
