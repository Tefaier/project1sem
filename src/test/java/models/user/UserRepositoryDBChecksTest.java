package models.user;

import exceptions.OverlapException;
import models.room.RoomRepositoryDBChecks;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import records.UserDTO;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserRepositoryDBChecksTest {
  @Container
  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14");

  private static Jdbi jdbi;
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
  }

  @BeforeEach
  void beforeEach() {
    jdbi.useTransaction(handle -> handle.createUpdate("DELETE FROM account; DELETE FROM room; DELETE FROM booking;").execute());
  }

  @Test
  void getUser() {
    User user = userRepository.addUser(new UserDTO("meow"));
    Assertions.assertEquals(user, userRepository.getUser(user.id).get());
    Assertions.assertEquals(user, userRepository.getUser(user.name).get());
    Assertions.assertTrue(userRepository.getUser(user.id + 10).isEmpty());
  }

  @Test
  void addUser() {
    User user = userRepository.addUser(new UserDTO("meow"));
    Assertions.assertEquals(user, userRepository.getUser(user.id).get());
    Assertions.assertThrows(OverlapException.class, () -> userRepository.addUser(new UserDTO("meow")));
    Assertions.assertDoesNotThrow(() -> userRepository.addUser(new UserDTO("hawk")));
  }

  @Test
  void editUser() {
    User user = userRepository.addUser(new UserDTO("meow"));
    User user2 = userRepository.addUser(new UserDTO("hawk"));
    String newName = "woem";
    userRepository.editUser(user, new UserDTO(newName));
    Assertions.assertEquals(newName, userRepository.getUser(user.id).get().name);
    Assertions.assertThrows(OverlapException.class, () -> userRepository.editUser(user2, new UserDTO(newName)));
  }

  @Test
  void getAllUsers() {
    User user = userRepository.addUser(new UserDTO("meow"));
    User user2 = userRepository.addUser(new UserDTO("hawk"));
    Assertions.assertEquals(2, userRepository.getAllUsers().size());
  }
}