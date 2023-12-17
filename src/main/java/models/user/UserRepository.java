package models.user;

import exceptions.OverlapException;
import records.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
  public Optional<User> getUser(String name);
  public Optional<User> getUser(Long id);
  public User addUser(UserDTO userDTO) throws OverlapException;
  public User editUser(User from, UserDTO to) throws OverlapException;
  public List<User> getAllUsers();
}
