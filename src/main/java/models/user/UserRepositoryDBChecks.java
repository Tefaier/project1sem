package models.user;

import exceptions.OverlapException;
import records.UserDTO;

import java.util.Set;

public class UserRepositoryDBChecks implements UserRepository {
  @Override
  public User getUser(String name) {
    return null;
  }

  @Override
  public User getUser(Long id) {
    return null;
  }

  @Override
  public User addUser(UserDTO userDTO) throws OverlapException {
    return null;
  }

  @Override
  public User editUser(User from, User to) {
    return null;
  }

  @Override
  public Set<User> getAllUsers() {
    return null;
  }
}
