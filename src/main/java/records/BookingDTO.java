package records;

import java.time.LocalDateTime;
import java.util.Objects;

public record BookingDTO(LocalDateTime from, LocalDateTime to) {
  public BookingDTO {
    Objects.requireNonNull(from);
    Objects.requireNonNull(to);
  }
}
