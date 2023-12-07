package models.room;

import exceptions.OverlapException;
import models.booking.BookingRepositoryDBChecksPremium;
import models.booking.TimeThreshold;
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
import records.RoomDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class RoomRepositoryDBChecksTest {
  @Container
  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14");

  private static Jdbi jdbi;
  private static RoomRepositoryDBChecks roomRepository;

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
    roomRepository = new RoomRepositoryDBChecks(jdbi);
  }

  @BeforeEach
  void beforeEach() {
    jdbi.useTransaction(handle -> handle.createUpdate("DELETE FROM account; DELETE FROM room; DELETE FROM booking;").execute());
  }

  @Test
  void getRoom() {
    Room room = roomRepository.addRoom(new RoomDTO(null, null, true, "parp"));
    Assertions.assertEquals(room, roomRepository.getRoom(room.id).get());
    Assertions.assertEquals(room, roomRepository.getRoom(room.name).get());
    Assertions.assertTrue(roomRepository.getRoom(room.id + 10).isEmpty());
  }

  @Test
  void addRoom() {
    Room room = roomRepository.addRoom(new RoomDTO(null, null, true, "parp"));
    Assertions.assertEquals(room, roomRepository.getRoom(room.id).get());
    Assertions.assertThrows(OverlapException.class, () -> roomRepository.addRoom(new RoomDTO(null, null, true, "parp")));
    Assertions.assertThrows(OverlapException.class, () -> roomRepository.addRoom(new RoomDTO(LocalTime.of(10, 0), LocalTime.of(20, 0), false, "parp")));
    Assertions.assertDoesNotThrow(() -> roomRepository.addRoom(new RoomDTO(null, null, true, "hawk")));
  }

  @Test
  void editRoom() {
    Room room = roomRepository.addRoom(new RoomDTO(null, null, true, "parp"));
    Room room2 = roomRepository.addRoom(new RoomDTO(null, null, true, "hawk"));
    roomRepository.editRoom(room, new RoomDTO(LocalTime.of(10, 0), LocalTime.of(20, 0), false, "parp"));
    Assertions.assertFalse(roomRepository.getRoom(room.id).get().noCheck);
    Assertions.assertThrows(OverlapException.class, () -> roomRepository.editRoom(room2, new RoomDTO(LocalTime.of(10, 0), LocalTime.of(20, 0), false, "parp")));
  }

  @Test
  void getAllRooms() {
    Room room = roomRepository.addRoom(new RoomDTO(null, null, true, "parp"));
    Room room2 = roomRepository.addRoom(new RoomDTO(null, null, true, "hawk"));
    Assertions.assertEquals(2, roomRepository.getAllRooms().size());
  }
}