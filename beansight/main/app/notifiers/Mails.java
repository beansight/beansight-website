package notifiers;

import play.*;
import play.mvc.*;
import play.i18n.*;
import play.Logger;

import java.util.*;

import models.ContactMailTask;
import models.FollowNotificationTask;
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
		Logger.info("MailTask " + task.getClass().getSimpleName() + " to: " + task.sendTo);

		setSubject(subject);
		addRecipient(task.sendTo);
		setFrom("notification@beansight.com");
		
		Lang.set(task.language);	

		send(templateName, task);
	}
	
}
