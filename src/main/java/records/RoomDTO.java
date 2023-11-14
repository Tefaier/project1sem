package records;

import java.time.LocalTime;
import java.util.Objects;

public record RoomDTO(LocalTime from, LocalTime to, Boolean noCheck, String name) {
  public RoomDTO {
    if (!noCheck) {
      Objects.requireNonNull(from);
      Objects.requireNonNull(to);
    }
    Objects.requireNonNull(name);
  }
}
