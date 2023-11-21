package models.room;

import exceptions.OverlapException;
import models.booking.Booking;
import records.RoomDTO;

public class RoomServiceChecks implements RoomService {
  private final RoomRepository roomRepository;

  RoomServiceChecks(RoomRepository roomRepository) {
    this.roomRepository = roomRepository;
  }

  @Override
  public Room tryAdd(RoomDTO roomDTO) throws OverlapException {
    if (!hasRoom(roomDTO)) {
      return roomRepository.addRoom(roomDTO);
    } else {
      throw new OverlapException("Room with such name already exists", "name", roomDTO.name());
    }
  }

  @Override
  public boolean hasRoom(RoomDTO roomDTO) {
    return roomRepository.getRoom(roomDTO.name()) == null;
  }

  @Override
  public Room getRoom(long id) {
    return roomRepository.getRoom(id);
  }
}
