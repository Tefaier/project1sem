package models.user;

import exceptions.OverlapException;
import models.user.User;
import records.UserDTO;

import java.util.Set;

public interface UserRepository {
  public User getUser(String name);
  public User getUser(Long id);
  public User addUser(UserDTO userDTO) throws OverlapException;
  public User editUser(User from, User to);
  public Set<User> getAllUsers();
}
