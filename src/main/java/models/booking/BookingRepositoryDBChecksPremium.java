package models.booking;

import exceptions.OverlapException;
import exceptions.ValidationException;
import models.room.Room;
import models.room.RoomRepository;
import models.user.User;
import models.user.UserRepository;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import records.BookingDTO;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

public class BookingRepositoryDBChecksPremium implements BookingRepository {
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;
  private final Jdbi jdbi;
  private final Duration minimumTime;
  private final Duration usualLimit;
  private final Duration premiumLimit;
  private final int streakRequirement;
  private final Duration premiumThreshold;
  private final TimeThreshold timeThresholdMethod;
  private final Duration inFutureAvailability;

  public BookingRepositoryDBChecksPremium(RoomRepository roomRepository,
                                          UserRepository userRepository,
                                          Jdbi jdbi,
                                          Duration minimumTime,
                                          Duration usualLimit,
                                          Duration premiumLimit,
                                          int streakRequirement,
                                          Duration premiumThreshold,
                                          TimeThreshold timeThresholdMethod,
                                          Duration inFutureAvailability) {
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
    this.jdbi = jdbi;
    this.minimumTime = minimumTime;
    this.usualLimit = usualLimit;
    this.premiumLimit = premiumLimit;
    this.streakRequirement = streakRequirement;
    this.premiumThreshold = premiumThreshold;
    this.timeThresholdMethod = timeThresholdMethod;
    this.inFutureAvailability = inFutureAvailability;
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
      return Optional.of(Booking.parseMap(result.get()));
    });
  }

  @Override
  public List<Booking> getBookingsByUser(Long id, LocalDateTime from, LocalDateTime to) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM booking WHERE account_id = :id " +
                  "AND (:from::timestamptz is null OR time_from >= :from::timestamptz OR time_to >= :from::timestamptz) " +
                  "AND (:to::timestamptz is null OR time_from <= :to::timestamptz OR time_to <= :to::timestamptz);")
              .bind("id", id)
              .bind("from", from == null ? null : Timestamp.valueOf(from))
              .bind("to", to == null ? null : Timestamp.valueOf(to))
              .mapToMap()
              .stream();
      return result.map(Booking::parseMap).toList();
    });
  }

  @Override
  public List<Booking> getBookingsByRoom(Long id, LocalDateTime from, LocalDateTime to) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM booking WHERE room_id = :id " +
                  "AND (:from::timestamptz is null OR time_from >= :from::timestamptz OR time_to >= :from::timestamptz)" +
                  "AND (:to::timestamptz is null OR time_from <= :to::timestamptz OR time_to <= :to::timestamptz);")
              .bind("id", id)
              .bind("from", from == null ? null : Timestamp.valueOf(from))
              .bind("to", to == null ? null : Timestamp.valueOf(to))
              .mapToMap()
              .stream();
      return result.map(Booking::parseMap).toList();
    });
  }

  @Override
  public Booking addBooking(BookingDTO bookingDTO) throws ValidationException, OverlapException {
    return jdbi.inTransaction((Handle handle) -> {
      if (userRepository.getUser(bookingDTO.userId()).isEmpty() ||
      roomRepository.getRoom(bookingDTO.roomId()).isEmpty()) {
        throw new ValidationException("Room or user problem", "Room with id: " + bookingDTO.roomId() + " or user with id: " + bookingDTO.userId() + "doesn't exist");
      }
      validateTime(bookingDTO);
      long overlaps = (long) handle.createQuery("select count(*) as counter " +
          "from booking " +
          "where (room_id = :room_id or account_id = :account_id) and " +
          "((time_from between :time_from::timestamptz and :time_to::timestamptz) or " +
          "(time_to between :time_from::timestamptz and :time_to::timestamptz) or " +
          "(time_from <= :time_from::timestamptz and time_to >= :time_to::timestamptz))")
          .bind("account_id", bookingDTO.userId())
          .bind("room_id", bookingDTO.roomId())
          .bind("time_from", Timestamp.valueOf(bookingDTO.from()))
          .bind("time_to", Timestamp.valueOf(bookingDTO.to()))
          .mapToMap()
          .first()
          .get("counter");
      if (overlaps != 0) {
        throw new OverlapException("Booking time overlaps", "Time from or time to", bookingDTO.from() + " -> " + bookingDTO.to());
      }
      var result = handle.createUpdate("INSERT INTO booking(account_id, room_id, time_from, time_to) " +
              "VALUES (:account_id, :room_id, :time_from::timestamptz, :time_to::timestamptz);")
          .bind("account_id", bookingDTO.userId())
          .bind("room_id", bookingDTO.roomId())
          .bind("time_from", Timestamp.valueOf(bookingDTO.from()))
          .bind("time_to", Timestamp.valueOf(bookingDTO.to()))
          .executeAndReturnGeneratedKeys("booking_id").mapToMap().findFirst();
      long generatedID = (long) result.get().get("booking_id");
      return new Booking(generatedID, bookingDTO.from(), bookingDTO.to(), bookingDTO.userId(), bookingDTO.roomId());
    });
  }

  @Override
  public void deleteBooking(Booking toDelete) {
    jdbi.useTransaction((Handle handle) -> {
      handle.createUpdate("DELETE FROM booking WHERE booking_id = :id").bind("id", toDelete.id).execute();
    });
  }

  private boolean passedPremiumCheck (BookingDTO bookingDTO, User user) {
    LocalDateTime periodStart = timeThresholdMethod.periodStartFunc.apply(bookingDTO.from());
    Boolean passedPremiumCheck = true;
    for (int i = 0; i < streakRequirement; i++) {
      if (user.getTotalBookTime(
          periodStart.minus(timeThresholdMethod.timePeriod.multipliedBy(i + 1)),
          periodStart.minus(timeThresholdMethod.timePeriod.multipliedBy(i)),
          this
      ).compareTo(premiumThreshold) < 0) {
        passedPremiumCheck = false;
        break;
      }
    }
    return passedPremiumCheck;
  }

  private void validateTime(BookingDTO bookingDTO) throws ValidationException {
    validateMinimumDuration(bookingDTO);
    validateInThePast(bookingDTO);
    validateFarInFuture(bookingDTO);
    validateRoomLimit(bookingDTO);
    validateTimeLimit(bookingDTO);
  }

  private void validateMinimumDuration (BookingDTO bookingDTO) throws ValidationException {
    if (bookingDTO.to().minus(minimumTime).isBefore(bookingDTO.from())) {
      throw new ValidationException("Booking time is less than minimum", "Minimum time of booking is: " + minimumTime);
    }
  }

  private void validateInThePast (BookingDTO bookingDTO) throws ValidationException {
    if (LocalDateTime.now().isAfter(bookingDTO.from())) {
      throw new ValidationException(
          "Booking can't be in the past",
          "Booking start time is: " +
              bookingDTO.from() +
              " while current time is: " +
              LocalDateTime.now());
    }
  }

  private void validateFarInFuture (BookingDTO bookingDTO) throws ValidationException {
    if (bookingDTO.to().minus(inFutureAvailability).isAfter(LocalDateTime.now())) {
      throw new ValidationException(
          "Booking is too far in the future",
          "Booking end time is: " +
              bookingDTO.to() +
              " while current time is: " +
              LocalDateTime.now() +
              " with difference of: " +
              Period.between(LocalDateTime.now().toLocalDate(), bookingDTO.to().toLocalDate()) +
              " while maximum time in the future is: " +
              inFutureAvailability);
    }
  }

  private void validateRoomLimit (BookingDTO bookingDTO) throws ValidationException {
    Room room = roomRepository.getRoom(bookingDTO.roomId()).get();
    if (!room.isOpenAtPeriod(bookingDTO.from(), bookingDTO.to())) {
      throw new ValidationException("Room limit doesn't allow this booking", "Room " + room.name + " is open from: " + room.availableFrom + " up to: " + room.availableTo);
    }
  }

  private void validateTimeLimit (BookingDTO bookingDTO) throws ValidationException {
    LocalDateTime periodStart = timeThresholdMethod.periodStartFunc.apply(bookingDTO.from());
    User user = userRepository.getUser(bookingDTO.userId()).get();
    boolean passedPremiumCheck = passedPremiumCheck(bookingDTO, user);
    Duration currentBookedDuration = user.getTotalBookTime(
        periodStart,
        periodStart.plus(timeThresholdMethod.timePeriod),
        this
    );
    Duration newBookingDuration;
    Duration addedDuration;
    try {
      addedDuration = Duration.between(bookingDTO.from(), bookingDTO.to());
      newBookingDuration = currentBookedDuration.plus(addedDuration);
    } catch (ArithmeticException e) {
      throw new ValidationException("Booking duration is too big", "From: " + bookingDTO.from() + " to: " + bookingDTO.to());
    }
    if (newBookingDuration.compareTo(passedPremiumCheck ? premiumLimit : usualLimit) > 0) {
      throw new ValidationException(
          "Tried to book too much",
          "From: " +
              periodStart +
              " up to: " +
              periodStart.plus(timeThresholdMethod.timePeriod) +
              " already booked: " +
              currentBookedDuration +
              " and tried to add: " +
              addedDuration +
              " while limit is: " +
              (passedPremiumCheck ? premiumLimit : usualLimit));
    }
  }
}
