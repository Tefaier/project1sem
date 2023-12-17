package models.booking;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import records.BookingDTO;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

  @Test
  void getDuration() {
    Booking booking = new Booking(0, LocalDateTime.now(), LocalDateTime.now().plusHours(1), 0, 0);
    Assertions.assertEquals(Duration.ofHours(1), booking.getDuration());
  }

  @Test
  void overlapsWithPeriod() {
    LocalDateTime now = LocalDateTime.now();
    Booking booking = new Booking(0, now, now.plusHours(1), 0, 0);
    Assertions.assertTrue(booking.overlapsWithPeriod(now.minusMinutes(10), now.plusMinutes(20)));
    Assertions.assertTrue(booking.overlapsWithPeriod(now.plusMinutes(10), now.plusMinutes(20)));
    Assertions.assertTrue(booking.overlapsWithPeriod(now.minusMinutes(10), now.plusHours(2)));
    Assertions.assertFalse(booking.overlapsWithPeriod(now.minusMinutes(20), now.minusMinutes(10)));
    Assertions.assertFalse(booking.overlapsWithPeriod(now.plusHours(2), now.plusHours(3)));
  }
}