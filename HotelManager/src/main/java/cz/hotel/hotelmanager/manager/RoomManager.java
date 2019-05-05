/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.manager;

import cz.hotel.hotelmanager.entity.Room;
import cz.hotel.hotelmanager.exceptions.ServiceFailureException;
import java.util.List;


public interface RoomManager {
    void updateRoom(Room room);
    Room findRoomById(Long id);
    List<Room> findAllRooms();
    void createRoom(Room room);

    public void deleteRoom(Long id) throws Exception;
    public void deleteRoom(Room room) throws ServiceFailureException;
}
