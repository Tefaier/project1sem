package models.user;

import exceptions.OverlapException;
import org.jdbi.v3.core.Jdbi;
import records.UserDTO;

import java.sql.Connection;
import java.util.Optional;
import java.util.Set;

// user is named account in db
public class UserRepositoryDBChecks implements UserRepository {
  private final Jdbi jdbi;

  public UserRepositoryDBChecks(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Optional<User> getUser(String name) {
    return null;
  }

  @Override
  public Optional<User> getUser(Long id) {
    return null;
  }

  @Override
  public User addUser(UserDTO userDTO) throws OverlapException {
    return null;
  }

  @Override
  public User editUser(User from, UserDTO to) {
    return null;
  }

  @Override
  public Set<User> getAllUsers() {
    return null;
  }
}
