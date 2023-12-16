package models.booking;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Function;

public enum TimeThreshold {
  byWeek(Duration.ofDays(7), (localDateTime ->
      localDateTime
          .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
          .withHour(0)
          .withSecond(0))
  );

  public final Duration timePeriod;
  public final Function<LocalDateTime, LocalDateTime> periodStartFunc;

  TimeThreshold(Duration duration, Function<LocalDateTime, LocalDateTime> periodStartFunc) {
    this.timePeriod = duration;
    this.periodStartFunc = periodStartFunc;
  }
}
