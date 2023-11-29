package models.room;

import exceptions.OverlapException;
import records.RoomDTO;

import java.util.Optional;
import java.util.Set;

public interface RoomRepository {
  public Optional<Room> getRoom(String name);
  public Optional<Room> getRoom(Long id);
  public Room addRoom(RoomDTO roomDTO) throws OverlapException;
  public Room editRoom(Room from, RoomDTO to);
  public Set<Room> getAllRooms();
}
