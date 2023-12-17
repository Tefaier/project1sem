package records;

import java.util.Objects;

public record UserDTO(String name) {
  public UserDTO {
    Objects.requireNonNull(name);
  }
}
