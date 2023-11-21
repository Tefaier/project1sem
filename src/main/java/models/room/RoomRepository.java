package models.room;

import exceptions.OverlapException;
import records.RoomDTO;

import java.util.Set;

public interface RoomRepository {
  public Room getRoom(String name);
  public Room getRoom(Long id);
  public Room addRoom(RoomDTO roomDTO) throws OverlapException;
  public Room editRoom(Room from, Room to);
  public Set<Room> getAllRooms();
}
