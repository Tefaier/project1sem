package models.booking;

import exceptions.OverlapException;
import exceptions.ValidationException;
import models.room.RoomRepository;
import models.room.RoomService;
import models.user.UserService;
import records.BookingDTO;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class BookingServiceChecks implements BookingService{
  private final BookingRepository bookingRepository;

  BookingServiceChecks(BookingRepository bookingRepository) {
    this.bookingRepository = bookingRepository;
  }

  @Override
  public Booking tryAdd(BookingDTO bookingDTO) throws ValidationException {
    if (!hasBooking(bookingDTO)) {
      return bookingRepository.addBooking(bookingDTO);
    } else {
      throw new ValidationException("Booking couldn't be added", "");
    }
  }

  @Override
  public boolean hasBooking(BookingDTO bookingDTO) {
    return getBookingsByUser(bookingDTO.userID(), bookingDTO.from(), bookingDTO.to()).isEmpty() &&
        getBookingsByRoom(bookingDTO.roomID(), bookingDTO.from(), bookingDTO.to()).isEmpty();
  }

  @Override
  public Booking getBooking(long id) {
    return bookingRepository.getBooking(id);
  }

  @Override
  public Set<Booking> getBookingsByUser(Long userID, LocalDateTime from, LocalDateTime to) {
    return bookingRepository.getBookingsByUser(userID)
        .stream().filter(booking -> !booking.overlapsWithPeriod(from, to))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Booking> getBookingsByRoom(Long roomID, LocalDateTime from, LocalDateTime to) {
    return bookingRepository.getBookingsByRoom(roomID)
        .stream().filter(booking -> !booking.overlapsWithPeriod(from, to))
        .collect(Collectors.toSet());
  }

  @Override
  public void deleteBooking(Booking booking) {
    bookingRepository.deleteBooking(booking);
  }
}
