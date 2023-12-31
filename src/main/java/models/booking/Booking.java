package models.booking;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@EqualsAndHashCode
public class Booking {
  public final long id;
  public final LocalDateTime timeFrom;
  public final LocalDateTime timeTo;
  public final long userId;
  public final long roomId;

  @JsonCreator
  public Booking(
      @JsonProperty("id") long id,
      @JsonProperty("timeFrom") LocalDateTime timeFrom,
      @JsonProperty("timeTo") LocalDateTime timeTo,
      @JsonProperty("userId") long userId,
      @JsonProperty("roomId") long roomId) {
    this.id = id;
    this.timeFrom = timeFrom.withNano(0);
    this.timeTo = timeTo.withNano(0);
    this.userId = userId;
    this.roomId = roomId;
  }

  @JsonIgnore
  public Duration getDuration() {
    return Duration.between(timeFrom, timeTo);
  }

  public boolean overlapsWithPeriod(LocalDateTime periodStart, LocalDateTime periodFinish) {
    return timeFrom.isAfter(periodStart) && timeFrom.isBefore(periodFinish) ||
        timeTo.isAfter(periodStart) && timeTo.isBefore(periodFinish) ||
        timeFrom.isBefore(periodStart) && timeTo.isAfter(periodFinish) ||
        timeFrom.isAfter(periodStart) && timeTo.isBefore(periodFinish);
  }

  public static Booking parseMap(Map<String, Object> map) {
    return new Booking(
        (long) map.get("booking_id"),
        ((Timestamp) map.get("time_from")).toLocalDateTime(),
        ((Timestamp) map.get("time_to")).toLocalDateTime(),
        (long) map.get("account_id"),
        (long) map.get("room_id"));
  }
}
