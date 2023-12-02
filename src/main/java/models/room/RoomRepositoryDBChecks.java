package models.room;

import exceptions.OverlapException;
import models.booking.Booking;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import records.RoomDTO;

import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RoomRepositoryDBChecks implements RoomRepository {
  private final Jdbi jdbi;

  public RoomRepositoryDBChecks(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Optional<Room> getRoom(String name) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM room WHERE name = :name")
              .bind("name", name)
              .mapToMap()
              .stream().findFirst();
      if (result.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(new Room(
          (long) result.get().get("room_id"),
          (String) result.get().get("name"),
          (boolean) result.get().get("restricts"),
          (boolean) result.get().get("restricts") ? Time.valueOf((String) result.get().get("time_from")).toLocalTime() : null,
          (boolean) result.get().get("restricts") ? Time.valueOf((String) result.get().get("time_to")).toLocalTime() : null));
    });
  }

  @Override
  public Optional<Room> getRoom(Long id) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM room WHERE room_id = :id")
              .bind("id", id)
              .mapToMap()
              .stream().findFirst();
      if (result.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(new Room(
          (long) result.get().get("room_id"),
          (String) result.get().get("name"),
          (boolean) result.get().get("restricts"),
          (boolean) result.get().get("restricts") ? Time.valueOf((String) result.get().get("time_from")).toLocalTime() : null,
          (boolean) result.get().get("restricts") ? Time.valueOf((String) result.get().get("time_to")).toLocalTime() : null));
    });
  }

  @Override
  public Room addRoom(RoomDTO roomDTO) throws OverlapException {
    return jdbi.inTransaction((Handle handle) -> {
      var result = handle.createUpdate("INSERT INTO room (name, restricts, time_from, time_to) VALUES (:name, :restricts, :time_from, :time_to);")
          .bind("name", roomDTO.name())
          .bind("restricts", !roomDTO.noCheck())
          .bind("time_from", roomDTO.from())
          .bind("time_to", roomDTO.to())
          .executeAndReturnGeneratedKeys("room_id").mapToMap().findFirst();
      if (result.isEmpty()) {
        throw new OverlapException("Room name overlaps", "name", roomDTO.name());
      }
      long generatedID = (long) result.get().get("room_id");
      return new Room(generatedID, roomDTO.name(), !roomDTO.noCheck(), roomDTO.from(), roomDTO.to());
    });
  }

  @Override
  public Room editRoom(Room from, RoomDTO to) {
    jdbi.useTransaction((Handle handle) -> {
      handle.createUpdate("UPDATE room SET " +
              "name = :name, " +
              "restricts = :restricts, " +
              "time_from = :time_from " +
              "time_to = :time_to " +
              "WHERE room_id = :id")
          .bind("name", to.name())
          .bind("restricts", !to.noCheck())
          .bind("time_from", to.from())
          .bind("time_to", to.to())
          .execute();
    });
    return getRoom(from.id).get();
  }

  @Override
  public List<Room> getAllRooms() {
    return jdbi.inTransaction((Handle handle) -> {
      var result = handle.createQuery("SELECT * FROM room")
          .mapToMap().list();
      return result.stream().map(map -> new Room(
          (long) map.get("room_id"),
          (String) map.get("name"),
          (boolean) map.get("restricts"),
          (boolean) map.get("restricts") ? Time.valueOf((String) map.get("time_from")).toLocalTime() : null,
          (boolean) map.get("restricts") ? Time.valueOf((String) map.get("time_to")).toLocalTime() : null)
      ).toList();
    });
  }
}
