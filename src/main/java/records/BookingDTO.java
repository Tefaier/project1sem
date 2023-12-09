package records;

import java.time.LocalDateTime;
import java.util.Objects;

public record BookingDTO(LocalDateTime from, LocalDateTime to, long userID, long roomID) {
  public BookingDTO {
    Objects.requireNonNull(from);
    Objects.requireNonNull(to);
    if (to.isBefore(from)) {
      throw new IllegalArgumentException("Time to has to be after time from");
    }
  }
}
