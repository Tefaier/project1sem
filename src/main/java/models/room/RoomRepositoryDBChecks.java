package models.room;

import exceptions.OverlapException;
import records.RoomDTO;

import java.util.Set;

public class RoomRepositoryDBChecks implements RoomRepository {
  @Override
  public Room getRoom(String name) {
    return null;
  }

  @Override
  public Room getRoom(Long id) {
    return null;
  }

  @Override
  public Room addRoom(RoomDTO roomDTO) throws OverlapException {
    return null;
  }

  @Override
  public Room editRoom(Room from, Room to) {
    return null;
  }

  @Override
  public Set<Room> getAllRooms() {
    return null;
  }
}
