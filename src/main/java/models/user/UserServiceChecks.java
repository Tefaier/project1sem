package models.user;

import models.booking.Booking;
import records.UserDTO;

public class UserServiceChecks implements UserService {
  private final UserRepository userRepository;

  UserServiceChecks(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public User tryAdd(UserDTO userDTO) {
    return null;
  }

  @Override
  public boolean hasUser(UserDTO userDTO) {
    return false;
  }

  @Override
  public User getUser(long id) {
    return null;
  }

  @Override
  public User addBooking(long id, Booking booking) {
    return null;
  }

  @Override
  public User removeBooking(long id, Booking booking) {
    return null;
  }
}
