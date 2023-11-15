package models.booking;

import exceptions.OverlapException;
import exceptions.ValidationException;
import models.room.Room;
import models.room.RoomService;
import models.user.UserService;
import records.BookingDTO;
import records.RoomDTO;

import java.time.LocalDateTime;
import java.util.Set;

public interface BookingService {
  public Booking tryAdd (BookingDTO bookingDTO, RoomService roomService, UserService userService) throws ValidationException;
  public boolean hasBooking (BookingDTO bookingDTO);
  public Booking getBooking (long id);
  public Set<Booking> getBookings (Set<Long> ids, LocalDateTime from, LocalDateTime to);
  public void deleteBooking (Booking booking, RoomService roomService, UserService userService);
}
