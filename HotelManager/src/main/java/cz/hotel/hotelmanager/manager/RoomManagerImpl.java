/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.manager;

import cz.hotel.hotelmanager.entity.Room;
import cz.hotel.hotelmanager.exceptions.IllegalEntityException;
import cz.hotel.hotelmanager.exceptions.ServiceFailureException;
import cz.hotel.hotelmanager.exceptions.ValidationException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RoomManagerImpl implements RoomManager {

    private DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(RoomManagerImpl.class);

    @SuppressWarnings("WeakerAccess")
    public RoomManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Room> findAllRooms() {
        logger.debug("finding all rooms");
        try (Connection conn = dataSource.getConnection();
                PreparedStatement st = conn.prepareStatement("SELECT id, number, capacity, type, price FROM Room")) {
            return executeQueryForMultipleRooms(st);
        } catch (SQLException ex) {
            logger.error("Error when getting all rooms from DB: " + ex.getMessage());
            throw new ServiceFailureException("Error when getting all rooms from DB", ex);
        }
    }

//    @Override
//    public void createRoom(Room room) {
//        validate(room);
//        if (room.getId() != null) {
//            throw new IllegalEntityException("room id is already set");
//        }
//        try (Connection conn = dataSource.getConnection();
//                PreparedStatement st = conn.prepareStatement("INSERT INTO Room (number, capacity, type, price) VALUES (?,?,?,?)",
//                        Statement.RETURN_GENERATED_KEYS)) {
//            st.setInt(1, room.getRoomNumber());
//            st.setInt(2, room.getCapacity());
//            st.setString(3, room.getRoomType());
//            st.setBigDecimal(4, room.getPrice());
//            st.executeUpdate();
//            room.setId(getId(st.getGeneratedKeys()));
//        } catch (SQLException ex) {
//            throw new ServiceFailureException("Error when inserting room into db", ex);
//        }
//    }
    @Override
    public void createRoom(Room room) {
        validate(room);
        logger.debug("creating room number " + room.getRoomNumber());
        String sqlCode = "INSERT INTO Room (number,capacity,type,price) VALUES (?,?,?,?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement statement = conn.prepareStatement(sqlCode,
                        Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, room.getRoomNumber());
            statement.setInt(2, room.getCapacity());
            statement.setString(3, room.getRoomType());
            statement.setBigDecimal(4, room.getPrice());
            statement.executeUpdate();

            ResultSet set = statement.getGeneratedKeys();
            if (!set.next()) {
                logger.error("Room was not created");
                throw new ServiceFailureException("Room was not created");
            }
            room.setId(set.getLong(1));
            if (set.next()) {
                logger.error("More than one row were created");
                throw new ServiceFailureException("More than one row were created");
            }
            logger.info("Room number: " + room.getRoomNumber() + " created");
        } catch (SQLException ex) {
            logger.error("Warning createRoom ended with SQLException: " + ex.getMessage());
            //System.err.println("Warning createRoom ended with SQLException\n");
        }

    }

    @Override
    public void updateRoom(Room room) {
        validate(room);
        logger.debug("updating room " + room.getRoomNumber());
        if (room.getId() == null) {
            logger.error("room id is null");
            throw new IllegalEntityException("room id is null");
        }
        try (Connection conn = dataSource.getConnection();
                PreparedStatement st = conn.prepareStatement("UPDATE Room SET number = ?, capacity = ?, type = ?, price = ? WHERE id = ?")) {
            st.setInt(1, room.getRoomNumber());
            st.setInt(2, room.getCapacity());
            st.setString(3, room.getRoomType());
            st.setBigDecimal(4, room.getPrice());
            st.setLong(5, room.getId());
            int count = st.executeUpdate();
            if (count != 1) {
                logger.error("updated " + count + " instead of 1 room");
                throw new IllegalEntityException("updated " + count + " instead of 1 room");
            }
        } catch (SQLException ex) {
            logger.error("Error when updating room in the db: " + ex.getMessage());
            throw new ServiceFailureException("Error when updating room in the db", ex);
        }
    }

    @Override
    public Room findRoomById(Long id) {
        if (id == null) {
            logger.error("id is null");
            throw new IllegalArgumentException("id is null");
        }
        try (Connection conn = dataSource.getConnection();
                PreparedStatement st = conn.prepareStatement("SELECT id, number, capacity, type, price FROM Room WHERE id = ?")) {
            st.setLong(1, id);
            logger.info("room with id: " + id + " found");
            return executeQueryForSingleRoom(st);
            
        } catch (SQLException ex) {
            logger.error("Error when getting room with id = " + id + " from DB: " + ex.getMessage());
            throw new ServiceFailureException("Error when getting room with id = " + id + " from DB", ex);
        }
    }

    static Room executeQueryForSingleRoom(PreparedStatement st) throws SQLException {
        try (ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                return rowToRoom(rs);
            } else {
                return null;
            }
        }
    }

    static List<Room> executeQueryForMultipleRooms(PreparedStatement st) throws SQLException {
        try (ResultSet rs = st.executeQuery()) {
            List<Room> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowToRoom(rs));
            }
            return result;
        }
    }

    private static Room rowToRoom(ResultSet rs) throws SQLException {
        Room result = new Room();
        result.setId(rs.getLong("id"));
        result.setRoomNumber(rs.getInt("number"));
        result.setCapacity(rs.getInt("capacity"));
        result.setRoomType(rs.getString("type"));
        result.setPrice(rs.getBigDecimal("price"));
        return result;
    }

    private static void validate(Room room) {
        if (room == null) {
            logger.error("room is null");
            throw new IllegalArgumentException("room is null");
        }
        if (room.getCapacity() <= 0) {
            logger.error("capacity is not positive number");
            throw new ValidationException("capacity is not positive number");
        }
        if (room.getRoomNumber() <= 0) {
            logger.error("room number is not positive number");
            throw new ValidationException("room number is not positive number");
        }
        if (room.getRoomType() == null) {
            logger.error("room type is null");
            throw new ValidationException("room type is null");
        }
        if (room.getPrice().compareTo(BigDecimal.ZERO) < 1) {
            logger.error("price is not positive number");
            throw new ValidationException("price is not positive number");
        }
    }

    /**
     * DB tool for extracting key from given ResultSet.
     *
     * @param key resultSet with key
     * @return key from given result set
     * @throws SQLException when operation fails
     */
    private static Long getId(ResultSet key) throws SQLException {
        if (key.getMetaData().getColumnCount() != 1) {
            throw new IllegalArgumentException("Given ResultSet contains more columns");
        }
        if (key.next()) {
            Long result = key.getLong(1);
            if (key.next()) {
                throw new IllegalArgumentException("Given ResultSet contains more rows");
            }
            return result;
        } else {
            throw new IllegalArgumentException("Given ResultSet contain no rows");
        }
    }

    @Override
    public void deleteRoom(Long id) throws Exception {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement st = con.prepareStatement("delete from books where id=?")) {
                st.setLong(1, id);
                int n = st.executeUpdate();
                if (n == 0) {;
                    throw new Exception("not deleted room with id " + id, null);
                }
                logger.info("deleted room {}",id);
            }
        } catch (SQLException e) {
            logger.error("cannot delete room ", e);
            throw new Exception("database delete failed", e);
        }

    }

    @Override
    public void deleteRoom(Room room) throws ServiceFailureException {
        if (room == null) {
            logger.error("room is null");
            throw new IllegalArgumentException("room is null");
        }
        if (room.getId() == null) {
            logger.error("room id is null");
            throw new IllegalEntityException("room id is null");
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement st = conn.prepareStatement("DELETE FROM Room WHERE id = ?")) {
            st.setLong(1, room.getId());
            int count = st.executeUpdate();
            if (count != 1) {
                logger.error("deleted " + count + " instead of 1 room");
                throw new IllegalEntityException("deleted " + count + " instead of 1 room");
            }
            logger.info("Room number " + room.getRoomNumber() + " successfully deleted");
        } catch (SQLException ex) {
            logger.error("Error when deleting room from the db ", ex);
            throw new ServiceFailureException("Error when deleting room from the db", ex);
        }
    }

}
