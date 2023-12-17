package controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.OverlapException;
import exceptions.ValidationException;
import models.booking.Booking;
import models.user.User;
import models.user.UserRepository;
import models.room.Room;
import models.room.RoomRepository;
import models.booking.BookingRepository;
import records.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Service;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static models.booking.Booking.parseMap;

public class BookingController implements Controller {
  private static final Logger LOG = LoggerFactory.getLogger(BookingController.class);
  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final BookingRepository bookingRepository;
  private final Service service;
  private final ObjectMapper objectMapper;
  private final FreeMarkerEngine freeMarkerEngine;

  public BookingController(Service service,
                           UserRepository userRepository,
                           RoomRepository roomRepository,
                           BookingRepository bookingRepository,
                           ObjectMapper objectMapper,
                           FreeMarkerEngine freeMarkerEngine) {
    this.service = service;
    this.userRepository = userRepository;
    this.roomRepository = roomRepository;
    this.bookingRepository = bookingRepository;
    this.objectMapper = objectMapper;
    this.freeMarkerEngine = freeMarkerEngine;
    createUser();
    createRoom();
    updateRoom();
    book();
    unbook();
    getUserBookingList();
  }

  @Override
  public void init() {
    service.init();
    service.awaitInitialization();
    LOG.debug("Booking controller started");
  }

  private void createUser() {
    service.post(
        "user/create",
        (Request request, Response response) -> {
          LOG.debug("Trying to create the user");
          response.type("application/json");
          String body = request.body();
          UserDTO userDTO = objectMapper.readValue(body, UserDTO.class);
          try {
            User user = userRepository.addUser(userDTO);
            response.status(201);
            return objectMapper.writeValueAsString(user);
          } catch (OverlapException e) {
            LOG.warn("User creation failed", e);
            response.status(400);
            return e.getMessage() + " " + e.fieldName + " " + e.overlappingValue;
          }
        }
    );
  }

  private void createRoom() {
    service.post(
        "room/create",
        (Request request, Response response) -> {
          LOG.debug("Trying to create the room");
          response.type("application/json");
          String body = request.body();
          RoomDTO roomDTO = objectMapper.readValue(body, RoomDTO.class);
          try {
            Room room = roomRepository.addRoom(roomDTO);
            response.status(201);
            return objectMapper.writeValueAsString(room);
          } catch (OverlapException e) {
            LOG.warn("Room creation failed", e);
            response.status(400);
            return e.getMessage() + " " + e.fieldName + " " + e.overlappingValue;
          }
        }
    );
  }

  private void updateRoom() {
    service.put(
        "room/:roomId/update",
        (Request request, Response response) -> {
          LOG.debug("Trying to update the room");

          long roomId = Long.parseLong(request.params("roomId"));
          response.type("application/json");
          String body = request.body();
          RoomDTO roomDTO = objectMapper.readValue(body, RoomDTO.class);
          try {
            Optional<Room> room = roomRepository.getRoom(roomId);
            if (room.isEmpty()) {
              LOG.debug("Cannot find a room with ID " + roomId);
              response.status(400);
              return "Cannot find a room with ID " + roomId;
            }
            Room roomFrom = room.get();
            Room roomUpd = roomRepository.editRoom(roomFrom, roomDTO);
            response.status(200);
            return objectMapper.writeValueAsString(roomUpd);
          } catch (OverlapException e) {
            LOG.warn("Room update failed", e);
            response.status(400);
            return e.getMessage() + " " + e.fieldName + " " + e.overlappingValue;
          }
        }
    );
  }

  private void book() {
    service.post(
        "room/:roomId/book",
        (Request request, Response response) -> {
          LOG.debug("Trying to book the room");

          response.type("application/json");
          String body = request.body();
          BookingDTO bookingDTO = objectMapper.readValue(body, BookingDTO.class);
          try {
            Booking booking = bookingRepository.addBooking(bookingDTO);
            response.status(201);
            return objectMapper.writeValueAsString(booking);
          } catch (ValidationException e) {
            LOG.warn("Booking attempt failed: cannot validate the request", e);
            response.status(400);
            return e.getMessage() + " " + e.reasonDescription;
          } catch (OverlapException e) {
            LOG.warn("Booking attempt failed: overlapping time", e);
            response.status(400);
            return e.getMessage() + " " + e.fieldName + " " + e.overlappingValue;
          }
        }
    );
  }

  private void unbook() {
    service.delete(
        "room/unbook/:bookingId",
        (Request request, Response response) -> {
          LOG.debug("Trying to unbook the room");

          long bookingId = Long.parseLong(request.params("bookingId"));
          response.type("application/json");
          Optional<Booking> booking = bookingRepository.getBooking(bookingId);
          if (booking.isEmpty()) {
            LOG.debug("Cannot find the booking with ID " + bookingId);
            response.status(400);
            return "Cannot find the booking with ID " + bookingId;
          }
          Booking bookingDeleted = booking.get();
          bookingRepository.deleteBooking(bookingDeleted);
          response.status(200);
          return objectMapper.writeValueAsString(bookingDeleted);
        }
    );
  }

  private void getUserBookingList() {
    service.get(
        "user/:userId",
        (Request request, Response response) -> {
          LOG.debug("Trying to get the booking list");

          long userId = Long.parseLong(request.params("userId"));
          response.type("text/html; charset=utf-8");
          Optional<User> user = userRepository.getUser(userId);
          if (user.isEmpty()) {
            LOG.debug("Cannot find a user with ID " + userId);
            response.status(400);
            return "Cannot find a user with ID " + userId;
          }
          List<Booking> bookings = bookingRepository.getBookingsByUser(userId, null, null);
          DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT);
          List<Map<String, String>> bookingMapList =
              bookings.stream()
                  .map(booking -> Map.of("id", "" + booking.id,
                      "timeFrom", booking.timeFrom.format(dateTimeFormatter),
                      "timeTo", booking.timeTo.format(dateTimeFormatter),
                      "roomName", "" + roomRepository.getRoom(booking.roomId).get().name))
                  .toList();
          Map<String, Object> model = new HashMap<>();
          model.put("bookings", bookingMapList);
          response.status(200);
          return freeMarkerEngine.render(new ModelAndView(model, "index.ftl"));
        }
    );
  }
}
