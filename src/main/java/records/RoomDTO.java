package records;

import java.time.LocalTime;
import java.util.Objects;

public record RoomDTO(LocalTime from, LocalTime to, boolean noCheck, String name) {
  public RoomDTO {
    if (!noCheck) {
      Objects.requireNonNull(from);
      Objects.requireNonNull(to);
      if (from.isAfter(to)) {
        throw new IllegalArgumentException("Time from can't be after time to");
      }
    }
    Objects.requireNonNull(name);
  }
}
