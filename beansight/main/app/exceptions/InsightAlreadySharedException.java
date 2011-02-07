package exceptions;

public class InsightAlreadySharedException extends Exception {

	public InsightAlreadySharedException() {
		super("Insight already shared"); // TODO : I18N ?
	}

	public InsightAlreadySharedException(String message) {
		super(message);
	}

	public InsightAlreadySharedException(Throwable cause) {
		super(cause);
	}

	public InsightAlreadySharedException(String message, Throwable cause) {
		super(message, cause);
	}

}
