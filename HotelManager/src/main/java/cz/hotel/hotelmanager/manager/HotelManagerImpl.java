/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.manager;

import cz.hotel.hotelmanager.entity.Guest;
import cz.hotel.hotelmanager.entity.Room;
import cz.hotel.hotelmanager.exceptions.IllegalEntityException;
import cz.hotel.hotelmanager.exceptions.ServiceFailureException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HotelManagerImpl implements HotelManager {

    private DataSource dataSource;
    private GuestManagerImpl guestManager;
    private RoomManagerImpl roomManager;
    private static final Logger logger = LoggerFactory.getLogger(HotelManagerImpl.class);

    @SuppressWarnings("WeakerAccess")
    public HotelManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.guestManager = new GuestManagerImpl(dataSource);
        this.roomManager = new RoomManagerImpl(dataSource);
        
    }
    
    @Override
    public Room findRoomWithGuest(Guest guest) {
        if (guest == null || guest.getId() == null) {
            logger.error("Guest's ID is null or Guest isn't accomodated");
            throw new IllegalEntityException("Guest's ID is null or Guest isn't accomodated");
        }
        if (guest.getRoomId() == null) {
            logger.info("Guest's roomID is null");
            return null;
        }
        
        Room foundRoom = roomManager.findRoomById(guest.getRoomId());
        logger.info(guest.getName() + " found in room number: " + foundRoom.getRoomNumber());
        return foundRoom;
    }

    @Override
    public void checkInGuest(Guest guest, Room room) {
        if (guest == null || room == null || guest.getId() == null || room.getId() == null) {
            logger.error("Room or Guest is null");
            throw new IllegalEntityException("Room or Guest is null");
        }
        
        if (roomManager.findRoomById(room.getId()) == null) {
            logger.error("Given room is not in Database");
            throw new IllegalEntityException("Given room is not in Database");
        }

        if (guest.getId() == null) {
            guestManager.createGuest(guest);
        }

        if (room.getId() == null) {
            roomManager.createRoom(room);
        }
        
        guest.setRoomId(room.getId());
        
        guestManager.updateGuest(guest);
        logger.info(guest.getName() + " was checked in to room number: " + room.getRoomNumber());
    }

    @Override
    public List<Guest> findGuestsInRoom(Room room) {
        if (room == null) {
            logger.error("room is null");
            throw new IllegalArgumentException("room is null");
        }
        if (room.getId() == null) {
            logger.error("room id is null");
            throw new IllegalEntityException("room id is null");
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement st = conn.prepareStatement(
                        "SELECT Guest.id, roomId, name, phonenumber, address "
                        + "FROM Guest JOIN Room ON Room.id = Guest.roomId "
                        + "WHERE Room.id = ?")) {
            st.setLong(1, room.getId());
            List<Guest> foundGuests = executeQueryForMultipleGuests(st);
            logger.info("Guests found in room number " + room.getRoomNumber() + ":" + foundGuests);
            return foundGuests;
        } catch (SQLException ex) {
            logger.error("Error when trying to find guests in room number " + room.getRoomNumber() + ": " + ex.getMessage());
            throw new ServiceFailureException("Error when trying to find guests in room " + room, ex);
        }
        
    }

    @Override
    public void checkOutGuest(Guest guest) {
        if (guest == null) {
            logger.error("guest is null");
            throw new IllegalArgumentException("guest is null");
        }
        if (guest.getId() == null) {
            logger.error("guest id is null");
            throw new IllegalEntityException("guest id is null");
        }
        try (Connection conn = dataSource.getConnection();
                PreparedStatement st = conn.prepareStatement(
                        "UPDATE Guest SET roomId = NULL WHERE id = ?")) {
            st.setLong(1, guest.getId());
            int count = st.executeUpdate();
            if (count != 1) {
                throw new IllegalEntityException("updated " + count + " instead of 1 guest");
            }
            guest.setRoomId(null);
            logger.info("Guest: " + guest.getName() + " sucessfully updated");
        } catch (SQLException ex) {
            logger.error("Error when checking out guest: " + ex.getMessage());
            throw new ServiceFailureException("Error when checking out guest", ex);
        }
    }
    
    @Override
    public void addGuest(Guest guest) {
        guestManager.createGuest(guest);
        logger.info(guest.getName() + " added");
    }
    
    @Override
    public void addRoom(Room room) {
        roomManager.createRoom(room);
        logger.info("room number " + room.getRoomNumber() + " added");
    }
    


    
     private void checkIfRoomHasSpace(Connection conn, Room room) throws SQLException {
        try (PreparedStatement checkSt = conn.prepareStatement(
                "SELECT capacity, COUNT(Guest.id) AS guestsCount " +
                        "FROM Room LEFT JOIN Guest ON Room.id = Guest.roomId " +
                        "WHERE Room.id = ? " +
                        "GROUP BY Room.id, capacity")) {
            checkSt.setLong(1, room.getId());
            try (ResultSet rs = checkSt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("capacity") <= rs.getInt("guestsCount")) {
                        logger.error("Room " + room + " is already full");
                        throw new IllegalEntityException("Room " + room + " is already full");
                    }
                } else {
                    logger.error("Room " + room + " does not exist in the database");
                    throw new IllegalEntityException("Room " + room + " does not exist in the database");
                }
            }
        }
    }
     
       
     
    private List<Guest> executeQueryForMultipleGuests(PreparedStatement st) throws SQLException {
        logger.debug("executing query for multiple guests");
        try (ResultSet rs = st.executeQuery()) {
            List<Guest> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowToGuest(rs));
            }
            return result;
        }
    }
    
    private Guest rowToGuest(ResultSet rs) throws SQLException {
        logger.debug("transforming row to Guest");
        Guest guest = new Guest();
        guest.setId(rs.getLong("id"));
        guest.setName(rs.getString("name"));
        guest.setPhoneNumber(rs.getString("phonenumber"));
        guest.setAddress(rs.getString("address"));
        guest.setRoomId(rs.getLong("roomid"));
        return guest;
    }
}