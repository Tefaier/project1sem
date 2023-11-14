package models.user;

import models.booking.Booking;
import models.booking.BookingService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

public class User {
  public final long id;
  public final String name;
  public final Set<Long> bookingIds;

  public User(long id, String name, Set<Long> bookingIds) {
    this.id = id;
    this.name = name;
    this.bookingIds = bookingIds;
  }

  public Duration getTotalBookTime(LocalDateTime from, LocalDateTime to, BookingService bookingService){
    Duration totalDuration = Duration.ZERO;
    for (Booking booking : bookingService.getBookings(bookingIds, from, to)) {
      if (booking.overlapsWithPeriod(from, to)) {
        totalDuration = totalDuration.plus(booking.getDuration());
      }
    }
    return totalDuration;
  }
}
