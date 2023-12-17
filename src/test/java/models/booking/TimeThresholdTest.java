package models.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeThresholdTest {

  @Test
  void testOfByWeek() {
    TimeThreshold threshold = TimeThreshold.byWeek;
    Assertions.assertEquals(Duration.ofDays(7), threshold.timePeriod);
    LocalDateTime newTime = threshold.periodStartFunc.apply(LocalDateTime.now());
    Assertions.assertEquals(0, newTime.getHour());
  }
}