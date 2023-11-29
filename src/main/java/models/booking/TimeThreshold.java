package models.booking;

import java.time.Duration;

public enum TimeThreshold {
  byWeek(Duration.ofDays(7));

  public final Duration timePeriod;

  TimeThreshold(Duration duration) {
    this.timePeriod = duration;
  }
}
