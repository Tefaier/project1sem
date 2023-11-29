package models.user;

import models.booking.Booking;
import models.booking.BookingRepository;

import java.time.Duration;
import java.time.LocalDateTime;

public class User {
  public final long id;
  public final String name;

  public User(long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Duration getTotalBookTime(LocalDateTime from, LocalDateTime to, BookingRepository bookingRepository){
    Duration totalDuration = Duration.ZERO;
    for (Booking booking : bookingRepository.getBookingsByUser(id, from, to)) {
      if (booking.overlapsWithPeriod(from, to)) {
        totalDuration = totalDuration.plus(booking.getDuration());
      }
    }
    return totalDuration;
  }
}
