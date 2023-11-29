package models.user;

import exceptions.OverlapException;
import models.user.User;
import records.UserDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {
  public Optional<User> getUser(String name);
  public Optional<User> getUser(Long id);
  public User addUser(UserDTO userDTO) throws OverlapException;
  public User editUser(User from, UserDTO to);
  public List<User> getAllUsers();
}
