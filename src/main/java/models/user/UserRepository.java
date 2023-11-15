package models.user;

import models.user.User;
import records.UserDTO;

public interface UserRepository {
  public User getUser(String name);
  public User getUser(Long id);
  public User addUser(UserDTO userDTO);
  public User editUser(User from, User to);
}
