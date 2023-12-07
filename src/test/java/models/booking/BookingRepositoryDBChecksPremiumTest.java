package models.booking;

import exceptions.ValidationException;
import models.room.Room;
import models.room.RoomRepositoryDBChecks;
import models.user.User;
import models.user.UserRepositoryDBChecks;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import records.BookingDTO;
import records.RoomDTO;
import records.UserDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class BookingRepositoryDBChecksPremiumTest {
  @Container
  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14");

  private static Jdbi jdbi;
  private static BookingRepositoryDBChecksPremium bookingRepository;
  private static RoomRepositoryDBChecks roomRepository;
  private static UserRepositoryDBChecks userRepository;

  @BeforeAll
  static void beforeAll() {
    String postgresJdbcUrl = POSTGRES.getJdbcUrl();
    Flyway flyway =
        Flyway.configure()
            .outOfOrder(true)
            .locations("classpath:db/migrations")
            .dataSource(postgresJdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword())
            .load();
    flyway.migrate();
    jdbi = Jdbi.create(postgresJdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword());
    userRepository = new UserRepositoryDBChecks(jdbi);
    roomRepository = new RoomRepositoryDBChecks(jdbi);
    bookingRepository = new BookingRepositoryDBChecksPremium(
        roomRepository,
        userRepository,
        jdbi,
        Duration.ofMinutes(5),
        Duration.ofHours(3),
        Duration.ofHours(8),
        1,
        Duration.ofHours(2),
        TimeThreshold.byWeek,
        Duration.ofDays(14)
    );
  }

  @BeforeEach
  void beforeEach() {
    jdbi.useTransaction(handle -> handle.createUpdate("DELETE FROM account; DELETE FROM room; DELETE FROM booking;").execute());
  }

  @Test
  void getBooking() {
    LocalDateTime now = LocalDateTime.now();
    User user = userRepository.addUser(new UserDTO("meow"));
    Room room = roomRepository.addRoom(new RoomDTO(null, null, true, "parp"));
    Booking booking = bookingRepository.addBooking(new BookingDTO(now, now.plusHours(1), user.id, room.id));
    Assertions.assertEquals(booking, bookingRepository.getBooking(booking.id).get());
  }

  @Test
  void getBookingsByUser() {
    LocalDateTime now = LocalDateTime.now();
    User user1 = userRepository.addUser(new UserDTO("meow"));
    User user2 = userRepository.addUser(new UserDTO("rawr"));
    Room room = roomRepository.addRoom(new RoomDTO(null, null, true, "parp"));
    bookingRepository.addBooking(new BookingDTO(now, now.plusHours(1), user1.id, room.id));
    bookingRepository.addBooking(new BookingDTO(now.plusHours(2), now.plusHours(3), user1.id, room.id));
    bookingRepository.addBooking(new BookingDTO(now, now.plusHours(1), user2.id, room.id));

    Assertions.assertEquals(1, bookingRepository.getBookingsByUser(user1.id, now, now.plusHours(1)).size());
    Assertions.assertEquals(2, bookingRepository.getBookingsByUser(user1.id, null, null).size());
  }

  @Test
  void getBookingsByRoom() {
    LocalDateTime now = LocalDateTime.now();
    User user = userRepository.addUser(new UserDTO("meow"));
    Room room1 = roomRepository.addRoom(new RoomDTO(null, null, true, "parp"));
    Room room2 = roomRepository.addRoom(new RoomDTO(null, null, true, "hawk"));
    bookingRepository.addBooking(new BookingDTO(now, now.plusHours(1), user.id, room1.id));
    bookingRepository.addBooking(new BookingDTO(now.plusHours(2), now.plusHours(3), user.id, room1.id));
    bookingRepository.addBooking(new BookingDTO(now, now.plusHours(1), user.id, room2.id));

    Assertions.assertEquals(1, bookingRepository.getBookingsByRoom(room1.id, now, now.plusHours(1)).size());
    Assertions.assertEquals(2, bookingRepository.getBookingsByRoom(room1.id, null, null).size());
  }

  private void assertValidationThrow(BookingDTO bookingDTO) {
    Assertions.assertThrows(
        ValidationException.class,
        () -> bookingRepository.addBooking(bookingDTO)
    );
  }

  @Test
  void addBooking() {
    LocalDateTime now = LocalDateTime.now().plusDays(1);
    User user = userRepository.addUser(new UserDTO("meow"));
    Room roomNoLimit = roomRepository.addRoom(new RoomDTO(null, null, true, "parp"));
    Room roomLimit = roomRepository.addRoom(new RoomDTO(LocalTime.of(10, 0), LocalTime.of(20, 0), false, "hawk"));

    // fully out of time boundaries
    assertValidationThrow(new BookingDTO(now.withHour(5), now.withHour(6), user.id, roomLimit.id));
    // partly out of time boundaries
    assertValidationThrow(new BookingDTO(now.withHour(9), now.withHour(11), user.id, roomLimit.id));
    // too long time
    assertValidationThrow(new BookingDTO(now.withHour(11), now.withHour(19), user.id, roomLimit.id));
    // not existing user/room ids
    assertValidationThrow(new BookingDTO(now.withHour(12), now.withHour(13), user.id + 10, roomLimit.id));
    assertValidationThrow(new BookingDTO(now.withHour(12), now.withHour(13), user.id, roomLimit.id + 10));
    // too far in the future
    assertValidationThrow(new BookingDTO(now.plusDays(15), now.plusDays(15).plusHours(1), user.id, roomNoLimit.id));

    Assertions.assertDoesNotThrow(() -> bookingRepository.addBooking(new BookingDTO(now.withHour(11), now.withHour(12), user.id, roomLimit.id)));
  }

  @Test
  void deleteBooking() {
    LocalDateTime now = LocalDateTime.now();
    User user = userRepository.addUser(new UserDTO("meow"));
    Room room = roomRepository.addRoom(new RoomDTO(null, null, true, "parp"));
    Booking booking = bookingRepository.addBooking(new BookingDTO(now, now.plusHours(1), user.id, room.id));
    Assertions.assertEquals(booking, bookingRepository.getBooking(booking.id).get());

    bookingRepository.deleteBooking(booking);
    Assertions.assertTrue(bookingRepository.getBooking(booking.id).isEmpty());
  }
}