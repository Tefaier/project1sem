package models.booking;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class Booking {
  public final long id;
  public final LocalDateTime timeFrom;
  public final LocalDateTime timeTo;
  public final long userId;
  public final long roomId;

  public Booking(long id, LocalDateTime timeFrom, LocalDateTime timeTo, long userId, long roomId) {
    this.id = id;
    this.timeFrom = timeFrom;
    this.timeTo = timeTo;
    this.userId = userId;
    this.roomId = roomId;
  }

  public Duration getDuration() {
    return Duration.between(timeFrom, timeTo);
  }

  public boolean overlapsWithPeriod(LocalDateTime periodStart, LocalDateTime periodFinish) {
    return timeFrom.isAfter(periodStart) && timeFrom.isBefore(periodFinish) ||
        timeTo.isAfter(periodStart) && timeTo.isBefore(periodFinish);
  }

  public static Booking parseMap(Map<String, Object> map) {
    return new Booking(
        (long) map.get("booking_id"),
        Timestamp.valueOf((String) map.get("time_from")).toLocalDateTime(),
        Timestamp.valueOf((String) map.get("time_to")).toLocalDateTime(),
        (long) map.get("account_id"),
        (long) map.get("room_id"));
  }
}
