package exceptions;

public class InvitationException extends Exception {


	public InvitationException(String message) {
		super(message);
	}

	public InvitationException(Throwable cause) {
		super(cause);
	}

	public InvitationException(String message, Throwable cause) {
		super(message, cause);
	}

}
