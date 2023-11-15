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
    return null;
  }

  @Override
  public boolean hasRoom(RoomDTO roomDTO) {
    return false;
  }

  @Override
  public Room getRoom(long id) {
    return null;
  }

  @Override
  public Room addBooking(long id, Booking booking) {
    return null;
  }

  @Override
  public Room removeBooking(long id, Booking booking) {
    return null;
  }
}
