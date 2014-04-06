package exceptions;

public class CannotVoteForAnInsightYouOwnException extends Exception {

	public CannotVoteForAnInsightYouOwnException() {
		super("You cannot vote for your own insight"); // TODO : I18N ?
	}

	public CannotVoteForAnInsightYouOwnException(String message) {
		super(message);
	}

	public CannotVoteForAnInsightYouOwnException(Throwable cause) {
		super(cause);
	}

	public CannotVoteForAnInsightYouOwnException(String message, Throwable cause) {
		super(message, cause);
	}

}
