package models.booking;

import java.time.Duration;
import java.time.LocalDateTime;

public class Booking {
  public final LocalDateTime timeFrom;
  public final LocalDateTime timeTo;
  public final long userId;
  public final long roomId;

  public Booking(LocalDateTime timeFrom, LocalDateTime timeTo, long userId, long roomId) {
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
}
