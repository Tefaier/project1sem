package models.room;

import models.booking.BookingRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Room {
  public final long id;
  public final String name;
  public final boolean noCheck;
  public final LocalTime availableFrom;
  public final LocalTime availableTo;

  public Room(long id, String name, boolean noCheck, LocalTime availableFrom, LocalTime availableTo) {
    this.id = id;
    this.name = name;
    this.noCheck = noCheck;
    this.availableFrom = availableFrom;
    this.availableTo = availableTo;
  }

  public boolean isFreeAtPeriod(LocalDateTime periodStart, LocalDateTime periodFinish, BookingRepository bookingRepository) {
    return bookingRepository.getBookingsByRoom(id, periodStart, periodFinish).isEmpty();
  }
}
