package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.booking.Booking;
import models.booking.BookingRepository;
import models.booking.BookingRepositoryDBChecksPremium;
import models.booking.TimeThreshold;
import models.room.Room;
import models.room.RoomRepository;
import models.room.RoomRepositoryDBChecks;
import models.user.User;
import models.user.UserRepository;
import models.user.UserRepositoryDBChecks;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import records.BookingDTO;
import records.RoomDTO;
import records.UserDTO;
import spark.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Testcontainers
class BookingControllerTest {
  @Container
  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:14");

  private static Service service;
  private static BookingController controller;
  private static Jdbi jdbi;
  private static ObjectMapper objectMapper;

  @BeforeAll
  static void setController() {
    String postgresJdbcUrl = POSTGRES.getJdbcUrl();
    Flyway flyway =
        Flyway.configure()
            .outOfOrder(true)
            .locations("classpath:db/migrations")
            .dataSource(postgresJdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword())
            .load();
    flyway.migrate();
    jdbi = Jdbi.create(postgresJdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword());
    UserRepository userRepository = new UserRepositoryDBChecks(jdbi);
    RoomRepository roomRepository = new RoomRepositoryDBChecks(jdbi);
    BookingRepository bookingRepository = new BookingRepositoryDBChecksPremium(
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

    service = Service.ignite();
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    Controller controller = new BookingController(
        service,
        userRepository,
        roomRepository,
        bookingRepository,
        objectMapper,
        TemplateFactory.freeMarkerEngine()
    );
    controller.init();
  }

  @BeforeEach
  void setUp() {
    jdbi.useTransaction(handle -> handle.createUpdate("DELETE FROM booking; DELETE FROM room; DELETE FROM account;").execute());
  }

  private HttpResponse<String> createUser(UserDTO userDTO) throws Exception {
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(userDTO)))
            .uri(URI.create("http://localhost:%d/user/create".formatted(service.port())))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> createRoom(RoomDTO roomDTO) throws Exception {
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roomDTO)))
            .uri(URI.create("http://localhost:%d/room/create".formatted(service.port())))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> createBooking(BookingDTO bookingDTO) throws Exception {
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(bookingDTO)))
            .uri(URI.create("http://localhost:%d/room/book".formatted(service.port())))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> updateRoom(long id, RoomDTO roomDTO) throws Exception {
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roomDTO)))
            .uri(URI.create("http://localhost:%d/room/%d/update".formatted(service.port(), id)))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> deleteBooking(long id) throws Exception {
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .DELETE()
            .uri(URI.create("http://localhost:%d/room/unbook/%d".formatted(service.port(), id)))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> getUserBookings(long id) throws Exception {
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .GET()
            .uri(URI.create("http://localhost:%d/user/%d/list".formatted(service.port(), id)))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  @Test
  void creationTest () throws Exception {
    LocalDateTime now = LocalDateTime.now().plusDays(1);
    String debugName = "meow";
    String debugRoomName = "halt";

    // user
    var response = createUser(new UserDTO(debugName));
    Assertions.assertEquals(201, response.statusCode());
    User user = objectMapper.readValue(response.body(), User.class);
    Assertions.assertEquals(debugName, user.name);

    var responseFail = createUser(new UserDTO(debugName));
    Assertions.assertEquals(400, responseFail.statusCode());

    // room
    response = createRoom(new RoomDTO(null, null, true, debugRoomName));
    Assertions.assertEquals(201, response.statusCode());
    Room room = objectMapper.readValue(response.body(), Room.class);
    Assertions.assertEquals(debugRoomName, room.name);
    Assertions.assertTrue(room.noCheck);
    Assertions.assertNull(room.availableFrom);
    Assertions.assertNull(room.availableTo);

    responseFail = createRoom(new RoomDTO(LocalTime.of(5, 0), LocalTime.of(15, 0), false, debugRoomName));
    Assertions.assertEquals(400, responseFail.statusCode());

    // booking
    response = createBooking(new BookingDTO(now, now.plusHours(1), user.id, room.id));
    Assertions.assertEquals(201, response.statusCode());
    Booking booking = objectMapper.readValue(response.body(), Booking.class);
    Assertions.assertTrue(Duration.between(booking.timeFrom, now).abs().compareTo(Duration.ofMinutes(5)) < 0);
    Assertions.assertTrue(Duration.between(booking.timeTo, now.plusHours(1)).abs().compareTo(Duration.ofMinutes(5)) < 0);
    Assertions.assertEquals(user.id, booking.userId);
    Assertions.assertEquals(room.id, booking.roomId);

    responseFail = createBooking(new BookingDTO(now, now.plusHours(10), user.id, room.id));
    Assertions.assertEquals(400, responseFail.statusCode());
    responseFail = createBooking(new BookingDTO(now.minusDays(5), now.minusDays(5).plusHours(1), user.id, room.id));
    Assertions.assertEquals(400, responseFail.statusCode());
    responseFail = createBooking(new BookingDTO(now.plusMinutes(5), now.plusHours(1), user.id, room.id));
    Assertions.assertEquals(400, responseFail.statusCode());
  }

  @Test
  void updateTest () throws Exception {
    LocalDateTime now = LocalDateTime.now().plusDays(1);
    String debugName = "meow";
    String debugRoomName = "halt";
    String newName = "purr";
    User user = objectMapper.readValue(createUser(new UserDTO(debugName)).body(), User.class);
    var response = createRoom(new RoomDTO(null, null, true, debugRoomName));
    Room room = objectMapper.readValue(response.body(), Room.class);

    // test booking delete
    Booking booking = objectMapper.readValue(createBooking(new BookingDTO(now, now.plusHours(1), user.id, room.id)).body(), Booking.class);
    deleteBooking(booking.id);
    response = createBooking(new BookingDTO(now, now.plusHours(1), user.id, room.id));
    Assertions.assertEquals(201, response.statusCode());

    response = updateRoom(room.id, new RoomDTO(LocalTime.of(5, 0), LocalTime.of(15, 0), false, newName));
    Room newRoom = objectMapper.readValue(response.body(), Room.class);

    Assertions.assertEquals(newName, newRoom.name);
    Assertions.assertFalse(newRoom.noCheck);
    Assertions.assertNotNull(newRoom.availableFrom);
    Assertions.assertNotNull(newRoom.availableTo);
  }

  @Test
  void getListTest () throws Exception {
    LocalDateTime now = LocalDateTime.now().plusDays(1);
    String debugName1 = "meow";
    String debugName2 = "lurse";
    String debugRoomName1 = "halt";
    String debugRoomName2 = "delta";

    User user1 = objectMapper.readValue(createUser(new UserDTO(debugName1)).body(), User.class);
    User user2 = objectMapper.readValue(createUser(new UserDTO(debugName2)).body(), User.class);
    Room room1 = objectMapper.readValue(createRoom(new RoomDTO(null, null, true, debugRoomName1)).body(), Room.class);
    Room room2 = objectMapper.readValue(createRoom(new RoomDTO(null, null, true, debugRoomName2)).body(), Room.class);
    createBooking(new BookingDTO(now, now.plusMinutes(10), user1.id, room1.id));
    createBooking(new BookingDTO(now, now.plusMinutes(10), user2.id, room2.id));
    createBooking(new BookingDTO(now.plusMinutes(20), now.plusMinutes(30), user1.id, room1.id));
    createBooking(new BookingDTO(now.plusMinutes(40), now.plusMinutes(50), user1.id, room2.id));

    var html = getUserBookings(user1.id);
    Assertions.assertEquals(12, StringUtils.countMatches(html.body(), "<td>"));
  }
}