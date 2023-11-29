package models.booking;

import exceptions.OverlapException;
import exceptions.ValidationException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import records.BookingDTO;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BookingRepositoryDBChecksPremium implements BookingRepository {
  public static TimeThreshold timeThreshold = TimeThreshold.byWeek;

  private final Jdbi jdbi;
  private final Duration usualLimit;
  private final Duration premiumLimit;
  private final int streakRequirement;
  private final Duration premiumThreshold;

  public BookingRepositoryDBChecksPremium(Jdbi jdbi, Duration usualLimit, Duration premiumLimit, int streakRequirement, Duration premiumThreshold, Duration inTheFutureLimit) {
    this.jdbi = jdbi;
    this.usualLimit = usualLimit;
    this.premiumLimit = premiumLimit;
    this.streakRequirement = streakRequirement;
    this.premiumThreshold = premiumThreshold;
  }

  @Override
  public Optional<Booking> getBooking(Long id) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM booking WHERE booking_id = :id")
              .bind("id", id)
              .mapToMap()
              .stream().findFirst();
      if (result.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(new Booking(
          (long) result.get().get("booking_id"),
          Timestamp.valueOf((String) result.get().get("time_from")).toLocalDateTime(),
          Timestamp.valueOf((String) result.get().get("time_to")).toLocalDateTime(),
          (long) result.get().get("account_id"),
          (long) result.get().get("room_id")));
    });
  }

  @Override
  public List<Booking> getBookingsByUser(Long id, LocalDateTime from, LocalDateTime to) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM booking WHERE account_id = :id " +
                  "AND (:from is null OR time_from >= :from)" +
                  "AND (:to is null OR time_to <= :to);")
              .bind("id", id)
              .bind("from", from)
              .bind("to", to)
              .mapToMap()
              .stream();
      return result.map(map -> new Booking(
          (long) map.get("booking_id"),
          Timestamp.valueOf((String) map.get("time_from")).toLocalDateTime(),
          Timestamp.valueOf((String) map.get("time_to")).toLocalDateTime(),
          (long) map.get("account_id"),
          (long) map.get("room_id")
      )).toList();
    });
  }

  @Override
  public List<Booking> getBookingsByRoom(Long id, LocalDateTime from, LocalDateTime to) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM booking WHERE room_id = :id " +
                  "AND (:from is null OR time_from >= :from)" +
                  "AND (:to is null OR time_to <= :to);")
              .bind("id", id)
              .bind("from", from)
              .bind("to", to)
              .mapToMap()
              .stream();
      return result.map(map -> new Booking(
          (long) map.get("booking_id"),
          Timestamp.valueOf((String) map.get("time_from")).toLocalDateTime(),
          Timestamp.valueOf((String) map.get("time_to")).toLocalDateTime(),
          (long) map.get("account_id"),
          (long) map.get("room_id")
      )).toList();
    });
  }

  @Override
  public Booking addBooking(BookingDTO bookingDTO) throws ValidationException, OverlapException {
    return jdbi.inTransaction((Handle handle) -> {
      // add checks
      var result = handle.createUpdate("IF (\n" +
              "\t\tselect count(*) \n" +
              "\t\tfrom booking \n" +
              "\t\twhere (room_id = :room_id or account_id = :account_id) and \n" +
              "\t\t(time_from between :time_from and :time_to) or \n" +
              "\t\t(time_to between :time_from and :time_to) or\n" +
              "\t\t(time_from <= :time_from and time_to >= :time_to))\n" +
              "\t) = 0 THEN\n" +
              "    \tINSERT INTO booking(account_id, room_id, time_from, time_to)\n" +
              "    \tVALUES (:account_id, :room_id, :time_from, :time_to);\n" +
              "\tEND IF;")
          .bind("account_id", bookingDTO.userID())
          .bind("room_id", bookingDTO.roomID())
          .bind("time_from", Timestamp.valueOf(bookingDTO.from()))
          .bind("time_to", Timestamp.valueOf(bookingDTO.to()))
          .executeAndReturnGeneratedKeys("booking_id").mapToMap().findFirst();
      if (result.isEmpty()) {
        throw new OverlapException("Booking time overlaps", "Time from or time to", bookingDTO.from().toString() + " -> " + bookingDTO.to().toString());
      }
      long generatedID = (long) result.get().get("booking_id");
      return new Booking(generatedID, bookingDTO.from(), bookingDTO.to(), bookingDTO.userID(), bookingDTO.roomID());
    });
  }

  @Override
  public void deleteBooking(Booking toDelete) {
    jdbi.useTransaction((Handle handle) -> {
      handle.createUpdate("DELETE FROM booking WHERE id = :id").bind("id", toDelete.id).execute();
    });
  }
}
