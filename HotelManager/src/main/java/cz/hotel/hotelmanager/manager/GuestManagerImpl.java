/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.manager;

import cz.hotel.hotelmanager.entity.Guest;
import cz.hotel.hotelmanager.exceptions.IllegalEntityException;
import cz.hotel.hotelmanager.exceptions.ServiceFailureException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GuestManagerImpl implements GuestManager{
    private DataSource source;
    private static final Logger logger = LoggerFactory.getLogger(GuestManagerImpl.class);
    
    private Boolean isGuestValid(Guest guest) {
        return guest != null &&
                guest.getAddress() != null &&
                guest.getName() != null &&
                guest.getPhoneNumber() != null;
    }
    
    public GuestManagerImpl(DataSource source) {
        this.source = source;
    }
    
    @Override
    public void createGuest(Guest guest) {
        if (!isGuestValid(guest) || guest.getId() != null) {
            logger.error("Guest's attributes are invalid or Guest is null");
            throw new IllegalEntityException("Guest's attributes are invalid or Guest is null");
        }
        String sqlCode = "INSERT INTO Guest (name,phonenumber,address) VALUES (?,?,?)";
        
        try(Connection conn = source.getConnection();
            PreparedStatement statement = conn.prepareStatement(sqlCode,
                    Statement.RETURN_GENERATED_KEYS)) {
        
            statement.setString(1, guest.getName());
            statement.setString(2, guest.getPhoneNumber());
            statement.setString(3, guest.getAddress());
            
            statement.executeUpdate();
            
            ResultSet set = statement.getGeneratedKeys();
            if (!set.next()) {
                throw new ServiceFailureException("Guest was not created");
            }
            guest.setId(set.getLong(1));
            if (set.next()) {
                throw new ServiceFailureException("More than one row were created");
            }
            logger.info("Guest created: " + guest.getName());
        } catch (SQLException ex) {
            logger.error("Warning createGuest ended with SQLException: " + ex.getMessage());
            //System.err.println("Warning createGuest ended with SQLException\n");
        }
        
    }

    @Override
    public void updateGuest(Guest guest) {
        if (!isGuestValid(guest) || guest.getId() == null) {
            logger.error("Guest's attributes are invalid or Guest is null");
            throw new IllegalEntityException("Guest's attributes are invalid or Guest is null");
        }
        String sqlCode = "UPDATE Guest SET roomid = ?, name = ?, phonenumber = ?, address = ? WHERE id = ?";
        
        try(Connection conn = source.getConnection();
            PreparedStatement statement = conn.prepareStatement(sqlCode)) {
            
            if (guest.getRoomId() == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, guest.getRoomId());
            }
            
            statement.setString(2, guest.getName());
            statement.setString(3, guest.getPhoneNumber());
            statement.setString(4, guest.getAddress());
            statement.setLong(5, guest.getId());
            
            //########################################### FIX THIS?!
            //How to know how many rows were retrieved?
            
            if (statement.executeUpdate() == 0) {
                throw new IllegalEntityException("Nothing was updated");
            }
            /*
            if (statement.executeUpdate() > 1) {
                throw new ServiceFailureException("More than one row were updated");
            }*/
            logger.info("Guest updated: " + guest.getName());
        } catch (SQLException ex) {
            logger.error("Warning updateGuest ended with SQLException: " + ex.getMessage());
            //System.err.println("Warning updateGuest ended with SQLException\n");
        }
    }

    @Override
    public void deleteGuest(Guest guest) {
        if (!isGuestValid(guest) || guest.getId() == null) {
            logger.error("Guest's attributes are invalid or Guest is null");
            throw new IllegalEntityException("Guest's attributes are invalid or Guest is null");
        }
        logger.debug("deleting guest: " + guest.getName());
        String sqlCode = "DELETE FROM Guest WHERE id = ?";
        
        try(Connection conn = source.getConnection();
            PreparedStatement statement = conn.prepareStatement(sqlCode)) {
            
            statement.setLong(1, guest.getId());
            
            //########################################### FIX THIS?!
            //How to know how many rows were retrieved?
            
            if (statement.executeUpdate() == 0) {
                throw new IllegalEntityException("Nothing was deleted");
            }
            logger.info("Guest deleted: " + guest.getName());
        } catch (SQLException ex) {
            logger.error("Warning deleteGuest ended with SQLException: " + ex.getMessage());
            //System.err.println("Warning deleteGuest ended with SQLException\n");
        }
    }

    @Override
    public Guest findGuestById(Long id) {
        logger.debug("finding guest by ID: " + id);
        if (id == null) {
            logger.error("Guest ID is NULL");
            throw new IllegalEntityException("ID is NULL");
        }
        String sqlCode = "SELECT * FROM Guest WHERE id = ?";
        Guest guest = new Guest();
        try(Connection conn = source.getConnection();
            PreparedStatement statement = conn.prepareStatement(sqlCode)) {
        
            statement.setLong(1, id);
            
            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                return null;
            }
            
            guest.setId(set.getLong(1));
            guest.setRoomId(set.getLong(2) == 0L ? null : set.getLong(2));
            guest.setName(set.getString(3));
            guest.setPhoneNumber(set.getString(4));
            guest.setAddress(set.getString(5));
            
            logger.info("Guest found: " + guest.getName());
            return guest;
        } catch (SQLException ex) {
            logger.error("Warning findGuest ended with SQLException: " + ex.getMessage());
            //System.err.println("Warning findGuest ended with SQLException\n");
        }
        return null;
    }

    @Override
    public List<Guest> findAllGuests() {
        logger.debug("findind all guests");
        List<Guest> result = new ArrayList<>();
        String sqlQuery = "SELECT * FROM Guest";
        
        try(Connection conn = source.getConnection();
            PreparedStatement statement = conn.prepareStatement(sqlQuery)) {
            
            try(ResultSet set = statement.executeQuery()) {
                while(set.next()) {
                    Guest guest = new Guest();
                    guest.setId(set.getLong("id"));
                    guest.setRoomId(set.getLong(2) == 0L ? null : set.getLong(2));
                    guest.setName(set.getString(3));
                    guest.setPhoneNumber(set.getString(4));
                    guest.setAddress(set.getString(5));
                    result.add(guest);
                }
            }
            logger.info("all guests found: " + result);
            return result;
        } catch (SQLException ex) {
            logger.error("Warning findAllGuests ended with SQLException: " + ex.getMessage());
            //System.err.println("Warning findAllGuests ended with SQLException\n");
        }
        return result;
    }
    
}
