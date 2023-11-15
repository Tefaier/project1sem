package models.booking;

import models.booking.Booking;
import records.BookingDTO;

public interface BookingRepository {
  public Booking getBooking(Long id);
  public Booking addBooking(BookingDTO bookingDTO);
  public Booking deleteBooking(Booking toDelete);
}
