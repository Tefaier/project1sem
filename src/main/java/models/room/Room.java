package models.room;

import lombok.EqualsAndHashCode;
import models.booking.BookingRepository;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@EqualsAndHashCode
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
    this.availableFrom = availableFrom == null ? null : availableFrom.withNano(0);
    this.availableTo = availableTo == null ? null : availableTo.withNano(0);
  }

  public boolean isFreeAtPeriod(LocalDateTime periodStart, LocalDateTime periodFinish, BookingRepository bookingRepository) {
    return bookingRepository.getBookingsByRoom(id, periodStart, periodFinish).isEmpty();
  }

  public static Room parseMap(Map<String, Object> map) {
    return new Room(
        (long) map.get("room_id"),
        (String) map.get("name"),
        !((boolean) map.get("restricts")),
        (boolean) map.get("restricts") ? ((Time) map.get("time_from")).toLocalTime() : null,
        (boolean) map.get("restricts") ? ((Time) map.get("time_to")).toLocalTime() : null);
  }

  public boolean isOpenAtPeriod(LocalDateTime periodStart, LocalDateTime periodFinish) {
    if (noCheck || periodStart == null || periodFinish == null) {
      return true;
    }
    LocalTime inDayStartTime = periodStart.toLocalTime();
    if (inDayStartTime.isBefore(availableFrom) || inDayStartTime.isAfter(availableTo)) {
      return false;
    }
    Duration leftTime = Duration.between(inDayStartTime, availableTo);
    Duration neededTime = Duration.between(periodStart, periodFinish);
    return leftTime.compareTo(neededTime) >= 0;
  }
}
