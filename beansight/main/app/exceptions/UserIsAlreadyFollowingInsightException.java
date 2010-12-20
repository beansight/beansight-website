package exceptions;

public class UserIsAlreadyFollowingInsightException extends Exception {

	public UserIsAlreadyFollowingInsightException() {
		super("You are already following this insight");
	}

	public UserIsAlreadyFollowingInsightException(String message) {
		super(message);
	}

	public UserIsAlreadyFollowingInsightException(Throwable cause) {
		super(cause);
	}

	public UserIsAlreadyFollowingInsightException(String message, Throwable cause) {
		super(message, cause);
	}

}
