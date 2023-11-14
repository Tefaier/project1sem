package models.room;

import models.booking.Booking;

import java.util.Set;

public class Room {
  public final long id;
  public final String name;
  public final Set<Long> bookingIds;

  public Room(long id, String name, Set<Long> bookingIds) {
    this.id = id;
    this.name = name;
    this.bookingIds = bookingIds;
  }
}
