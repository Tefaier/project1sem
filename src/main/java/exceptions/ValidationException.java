package exceptions;

public class ValidationException extends RuntimeException {
  public final String reasonDescription;

  public ValidationException(String message, Throwable cause, String reasonDescription) {
    super(message, cause);
    this.reasonDescription = reasonDescription;
  }
}
