package records;

import java.time.LocalDateTime;
import java.util.Objects;

public record BookingDTO(LocalDateTime from, LocalDateTime to, Long userID, Long roomID) {
  public BookingDTO {
    Objects.requireNonNull(from);
    Objects.requireNonNull(to);
    Objects.requireNonNull(roomID);
    Objects.requireNonNull(userID);
    if (to().isBefore(from)) {
      throw new IllegalArgumentException("Time to has to be after time from");
    }
  }
}
