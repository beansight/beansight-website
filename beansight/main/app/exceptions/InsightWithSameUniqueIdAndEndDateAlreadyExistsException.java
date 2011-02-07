package exceptions;

public class InsightWithSameUniqueIdAndEndDateAlreadyExistsException extends Exception {

	public InsightWithSameUniqueIdAndEndDateAlreadyExistsException() {
		super("You cannot create an insight having the same content and end date than an existing one"); // TODO : I18N ?
	}

	public InsightWithSameUniqueIdAndEndDateAlreadyExistsException(String message) {
		super(message);
	}

	public InsightWithSameUniqueIdAndEndDateAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public InsightWithSameUniqueIdAndEndDateAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

}
