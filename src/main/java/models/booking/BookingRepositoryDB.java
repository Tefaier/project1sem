package models.booking;

import records.BookingDTO;

import java.util.Set;

public class BookingRepositoryDB implements BookingRepository {
  @Override
  public Booking getBooking(Long id) {
    return null;
  }

  @Override
  public Set<Booking> getBookingsByUser(Long id) {
    return null;
  }

  @Override
  public Set<Booking> getBookingsByRoom(Long id) {
    return null;
  }

  @Override
  public Booking addBooking(BookingDTO bookingDTO) {
    return null;
  }

  @Override
  public Booking deleteBooking(Booking toDelete) {
    return null;
  }
}
