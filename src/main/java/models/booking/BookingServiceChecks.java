package models.booking;

import exceptions.OverlapException;
import exceptions.ValidationException;
import models.room.RoomRepository;
import models.room.RoomService;
import models.user.UserService;
import records.BookingDTO;

import java.time.LocalDateTime;
import java.util.Set;

public class BookingServiceChecks implements BookingService{
  private final BookingRepository bookingRepository;

  BookingServiceChecks(BookingRepository bookingRepository) {
    this.bookingRepository = bookingRepository;
  }

  @Override
  public Booking tryAdd(BookingDTO bookingDTO, RoomService roomService, UserService userService) throws ValidationException {
    return null;
  }

  @Override
  public boolean hasBooking(BookingDTO bookingDTO) {
    return false;
  }

  @Override
  public Booking getBooking(long id) {
    return null;
  }

  @Override
  public Set<Booking> getBookings(Set<Long> ids, LocalDateTime from, LocalDateTime to) {
    return null;
  }

  @Override
  public void deleteBooking(Booking booking, RoomService roomService, UserService userService) {

  }
}
