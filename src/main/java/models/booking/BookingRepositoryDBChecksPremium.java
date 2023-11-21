package models.booking;

import exceptions.OverlapException;
import exceptions.ValidationException;
import records.BookingDTO;

import java.time.Duration;
import java.util.Set;

public class BookingRepositoryDBChecksPremium implements BookingRepository {
  private final Duration usualLimit;
  private final Duration premiumLimit;
  private final Duration checkPeriod;
  private final Duration premiumThreshold;

  public BookingRepositoryDBChecksPremium(Duration usualLimit, Duration premiumLimit, Duration checkPeriod, Duration premiumThreshold) {
    this.usualLimit = usualLimit;
    this.premiumLimit = premiumLimit;
    this.checkPeriod = checkPeriod;
    this.premiumThreshold = premiumThreshold;
  }

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
  public Booking addBooking(BookingDTO bookingDTO) throws ValidationException, OverlapException {
    return null;
  }

  @Override
  public Booking deleteBooking(Booking toDelete) {
    return null;
  }
}
