/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.manager;

import cz.hotel.hotelmanager.entity.Guest;
import cz.hotel.hotelmanager.entity.Room;
import java.util.List;


public interface HotelManager {
    public Room findRoomWithGuest(Guest guest);
    public List<Guest> findGuestsInRoom(Room room);
    public void checkInGuest(Guest guest, Room room);
    public void checkOutGuest(Guest guest);
    public void addGuest(Guest guest);
    public void addRoom(Room room);
}
