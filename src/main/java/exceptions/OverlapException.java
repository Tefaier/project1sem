package exceptions;

public class OverlapException extends RuntimeException {
  public final String fieldName;
  public final String overlappingValue;

  public OverlapException(String message, Throwable cause, String fieldName, String overlappingValue) {
    super(message, cause);
    this.fieldName = fieldName;
    this.overlappingValue = overlappingValue;
  }
}
