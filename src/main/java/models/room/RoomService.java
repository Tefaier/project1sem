package models.room;

import exceptions.OverlapException;
import exceptions.ValidationException;
import models.booking.Booking;
import models.user.User;
import records.RoomDTO;
import records.UserDTO;

public interface RoomService {
  public Room tryAdd (RoomDTO roomDTO) throws OverlapException, ValidationException;
  public boolean hasRoom (RoomDTO roomDTO);
  public Room getRoom (long id);
  public Room addBooking (long id, Booking booking);
  public Room removeBooking (long id, Booking booking);
}
