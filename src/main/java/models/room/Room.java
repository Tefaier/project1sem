package models.room;

import models.booking.Booking;
import models.booking.BookingRepository;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

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

  public static Room parseMap(Map<String, Object> map) {
    return new Room(
        (long) map.get("room_id"),
        (String) map.get("name"),
        (boolean) map.get("restricts"),
        (boolean) map.get("restricts") ? Time.valueOf((String) map.get("time_from")).toLocalTime() : null,
        (boolean) map.get("restricts") ? Time.valueOf((String) map.get("time_to")).toLocalTime() : null);
  }
}
