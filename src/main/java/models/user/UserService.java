package models.user;

import exceptions.OverlapException;
import exceptions.ValidationException;
import models.booking.Booking;
import models.room.Room;
import records.UserDTO;

public interface UserService {
  public User tryAdd (UserDTO userDTO) throws OverlapException, ValidationException;
  public boolean hasUser (UserDTO userDTO);
  public User getUser (long id);
  public User addBooking (long id, Booking booking);
  public User removeBooking (long id, Booking booking);
}
