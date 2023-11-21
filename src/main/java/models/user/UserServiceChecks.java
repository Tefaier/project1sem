package models.user;

import exceptions.OverlapException;
import models.booking.Booking;
import records.UserDTO;

public class UserServiceChecks implements UserService {
  private final UserRepository userRepository;

  UserServiceChecks(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public User tryAdd(UserDTO userDTO) {
    if (!hasUser(userDTO)) {
      return userRepository.addUser(userDTO);
    } else {
      throw new OverlapException("User with such name already exists", "name", userDTO.name());
    }
  }

  @Override
  public boolean hasUser(UserDTO userDTO) {
    return userRepository.getUser(userDTO.name()) == null;
  }

  @Override
  public User getUser(long id) {
    return userRepository.getUser(id);
  }
}
