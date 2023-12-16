package models.booking;

import exceptions.OverlapException;
import exceptions.ValidationException;
import records.BookingDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {
  public Optional<Booking> getBooking(Long id);
  public List<Booking> getBookingsByUser(Long id, LocalDateTime from, LocalDateTime to);
  public List<Booking> getBookingsByRoom(Long id, LocalDateTime from, LocalDateTime to);
  public Booking addBooking(BookingDTO bookingDTO) throws ValidationException, OverlapException;
  public void deleteBooking(Booking toDelete);
}
