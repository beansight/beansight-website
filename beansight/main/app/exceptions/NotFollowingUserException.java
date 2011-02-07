package exceptions;

public class NotFollowingUserException extends Exception {

	public NotFollowingUserException() {
		super("You must follow this user"); // TODO : I18N ?
	}

	public NotFollowingUserException(String message) {
		super(message);
	}

	public NotFollowingUserException(Throwable cause) {
		super(cause);
	}

	public NotFollowingUserException(String message, Throwable cause) {
		super(message, cause);
	}

}
