package models.booking;

import exceptions.OverlapException;
import exceptions.ValidationException;
import models.booking.Booking;
import records.BookingDTO;

import java.util.Set;

public interface BookingRepository {
  public Booking getBooking(Long id);
  public Set<Booking> getBookingsByUser(Long id);
  public Set<Booking> getBookingsByRoom(Long id);
  public Booking addBooking(BookingDTO bookingDTO) throws ValidationException, OverlapException;
  public Booking deleteBooking(Booking toDelete);
}
