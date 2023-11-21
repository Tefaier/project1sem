package models.room;

import models.booking.BookingService;

import java.time.LocalDateTime;

public class Room {
  public final long id;
  public final String name;

  public Room(long id, String name) {
    this.id = id;
    this.name = name;
  }

  public boolean isFreeAtPeriod(LocalDateTime periodStart, LocalDateTime periodFinish, BookingService bookingService) {
    return bookingService.getBookingsByRoom(id, periodStart, periodFinish).isEmpty();
  }
}
