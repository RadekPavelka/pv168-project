/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.manager;

import cz.hotel.hotelmanager.entity.Guest;
import java.util.List;


public interface GuestManager {
    public void createGuest(Guest guest);
    public void updateGuest(Guest guest);
    public void deleteGuest(Guest guest);
    public Guest findGuestById(Long id);
    public List<Guest> findAllGuests();
}
