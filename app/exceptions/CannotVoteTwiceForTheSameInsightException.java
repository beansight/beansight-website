package exceptions;

public class CannotVoteTwiceForTheSameInsightException extends Exception {

	public CannotVoteTwiceForTheSameInsightException() {
		super("You cannot vote twice for the same insight"); // TODO : I18N ?
	}

	public CannotVoteTwiceForTheSameInsightException(String message) {
		super(message);
	}

	public CannotVoteTwiceForTheSameInsightException(Throwable cause) {
		super(cause);
	}

	public CannotVoteTwiceForTheSameInsightException(String message, Throwable cause) {
		super(message, cause);
	}

}
