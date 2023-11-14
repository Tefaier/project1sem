package models.room;

import records.RoomDTO;

public interface RoomRepository {
  public Room getRoom(String name);
  public Room getRoom(Long id);
  public Room addRoom(RoomDTO roomDTO);
  public Room editRoom(Room from, Room to);
}
