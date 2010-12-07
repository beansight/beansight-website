package notifiers;

import play.*;
import play.mvc.*;
import play.i18n.*;
import java.util.*;

import models.FollowNotificationTask;
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
		Logger.info("Follow Notification email: sending to " + task.to.email);

		setSubject(Messages.get("emailfollownotificationsubject",
				task.follower.userName));
		addRecipient(task.to.email);
		setFrom("notification@beansight.com");

		send(task);
	}

}
