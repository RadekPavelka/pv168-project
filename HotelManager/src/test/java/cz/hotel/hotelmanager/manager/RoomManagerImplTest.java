/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.manager;

import cz.hotel.hotelmanager.entity.Room;
import cz.hotel.hotelmanager.builder.RoomBuilder;
import cz.hotel.hotelmanager.exceptions.IllegalEntityException;
import cz.hotel.hotelmanager.exceptions.ValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.apache.derby.jdbc.EmbeddedDataSource;

import static org.assertj.core.api.Assertions.*;

public class RoomManagerImplTest {

    private RoomManagerImpl roomManager;
    private DataSource ds;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:room-mgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Before
    public void setUp() throws SQLException, IOException {
        ds = prepareDataSource();
        executeSqlScript(ds, RoomManager.class.getResourceAsStream("createTables.sql"));
        roomManager = new RoomManagerImpl(ds);
    }

    @After
    public void tearDown() throws SQLException, IOException {
        executeSqlScript(ds,RoomManager.class.getResourceAsStream("dropTables.sql"));
    }

   

    private RoomBuilder sampleDoubleRoomBuilder() {
        return new RoomBuilder()
                .capacity(2)
                .id(null)
                .price(new BigDecimal(1000))
                .roomNumber(1)
                .roomType("Double");
    }

    private RoomBuilder sampleTripleRoomBuilder() {
        return new RoomBuilder()
                .capacity(3)
                .id(null)
                .price(new BigDecimal(2000))
                .roomNumber(2)
                .roomType("Triple");
    }

    @Test
    public void CreateRoom() {
        Room room = sampleDoubleRoomBuilder().build();
        roomManager.createRoom(room);
        Long roomId = room.getId();
        assertThat(roomId).isNotNull();

        assertThat(roomManager.findRoomById(roomId))
                .isNotSameAs(room)
                .isEqualToComparingFieldByField(room);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullRoom() {
        roomManager.createRoom(null);
    }

//    @Test
//    public void createRoomWithExistingID() {
//        Room room = sampleDoubleRoomBuilder().id(1L).build();
//        expectedException.expect(IllegalEntityException.class);
//        roomManager.createRoom(room);
//    }

    @Test (expected = ValidationException.class)
    public void createRoomWithNullRoomType() {
        Room room = sampleDoubleRoomBuilder().roomType(null).build();
        roomManager.createRoom(room);

        assertThat(roomManager.findRoomById(room.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(room);
    }

    @Test
    public void createRoomWithNegativeRoomNumber() {
        Room room = sampleDoubleRoomBuilder().roomNumber(-1).build();
        assertThatThrownBy(() -> roomManager.createRoom(room))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createRoomWithNegativeCapacity() {
        Room room = sampleDoubleRoomBuilder().capacity(-1).build();
        expectedException.expect(ValidationException.class);
        roomManager.createRoom(room);
    }

    @Test
    public void createRoomWithZeroCapacity() {
        Room room = sampleDoubleRoomBuilder().capacity(0).build();
        expectedException.expect(ValidationException.class);
        roomManager.createRoom(room);
    }

    @Test
    public void createRoomWithNegativePrice() {
        Room room = sampleDoubleRoomBuilder().price(BigDecimal.valueOf(-10.0)).build();
        expectedException.expect(ValidationException.class);
        roomManager.createRoom(room);
    }

    @Test
    public void createRoomWithZeroPrice() {
        Room room = sampleDoubleRoomBuilder().price(BigDecimal.ZERO).build();
        expectedException.expect(ValidationException.class);
        roomManager.createRoom(room);
    }

    @Test
    public void updateRoomNumber() {
        Room roomForUpdate = sampleDoubleRoomBuilder().build();
        Room anotherRoom = sampleTripleRoomBuilder().build();
        roomManager.createRoom(roomForUpdate);
        roomManager.createRoom(anotherRoom);

        roomForUpdate.setRoomNumber(1);

        roomManager.updateRoom(roomForUpdate);

        assertThat(roomManager.findRoomById(roomForUpdate.getId()))
                .isEqualToComparingFieldByField(roomForUpdate);

        assertThat(roomManager.findRoomById(anotherRoom.getId()))
                .isEqualToComparingFieldByField(anotherRoom);
    }

    @FunctionalInterface
    private static interface Operation<T> {

        void callOn(T subjectOfOperation);
    }

    private void testUpdateRoom(Operation<Room> updateOperation) {
        Room sourceRoom = sampleDoubleRoomBuilder().build();
        Room anotherRoom = sampleTripleRoomBuilder().build();
        roomManager.createRoom(sourceRoom);
        roomManager.createRoom(anotherRoom);

        updateOperation.callOn(sourceRoom);

        roomManager.updateRoom(sourceRoom);
        assertThat(roomManager.findRoomById(sourceRoom.getId()))
                .isEqualToComparingFieldByField(sourceRoom);

        assertThat(roomManager.findRoomById(anotherRoom.getId()))
                .isEqualToComparingFieldByField(anotherRoom);
    }

    @Test
    public void updateRoomCapacity() {
        testUpdateRoom((room) -> room.setCapacity(3));
    }

    @Test
    public void updateRoomType() {
        testUpdateRoom((room) -> room.setRoomType("Luxury"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullRoom() {
        roomManager.updateRoom(null);
    }

    @Test
    public void updateRoomWithNullId() {
        Room room = sampleDoubleRoomBuilder().id(null).build();
        expectedException.expect(IllegalEntityException.class);
        roomManager.updateRoom(room);
    }

    @Test
    public void updateRoomWithNonExistingId() {
        Room room = sampleDoubleRoomBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        roomManager.updateRoom(room);
    }

    @Test
    public void updateRoomWithNegativeCapacity() {
        Room room = sampleDoubleRoomBuilder().build();
        roomManager.createRoom(room);
        room.setCapacity(-1);
        expectedException.expect(ValidationException.class);
        roomManager.updateRoom(room);
    }

    @Test
    public void updateRoomWithZeroCapacity() {
        Room room = sampleDoubleRoomBuilder().build();
        roomManager.createRoom(room);
        room.setCapacity(0);
        expectedException.expect(ValidationException.class);
        roomManager.updateRoom(room);
    }

    @Test
    public void updateRoomWithNegativeRoomNumber() {
        Room room = sampleDoubleRoomBuilder().build();
        roomManager.createRoom(room);
        room.setRoomNumber(-1);
        expectedException.expect(ValidationException.class);
        roomManager.updateRoom(room);
    }

    @Test
    public void updateRoomWithNegativePrice() {
        Room room = sampleDoubleRoomBuilder().build();
        roomManager.createRoom(room);
        room.setPrice(new BigDecimal(-10));
        expectedException.expect(ValidationException.class);
        roomManager.updateRoom(room);
    }

    @Test
    public void updateRoomWithZeroPrice() {
        Room room = sampleDoubleRoomBuilder().build();
        roomManager.createRoom(room);
        room.setPrice(BigDecimal.ZERO);
        expectedException.expect(ValidationException.class);
        roomManager.updateRoom(room);
    }

    @Test
    public void findAllRooms() {
        assertThat(roomManager.findAllRooms()).isEmpty();

        Room r1 = sampleDoubleRoomBuilder().build();
        Room r2 = sampleTripleRoomBuilder().build();

        roomManager.createRoom(r1);
        roomManager.createRoom(r2);

        assertThat(roomManager.findAllRooms())
                .usingFieldByFieldElementComparator()
                .containsOnly(r1, r2);

    }

    @Test
    public void testFindRoomById() {
        Room room = sampleDoubleRoomBuilder().build();
        roomManager.createRoom(room);

        assertThat(roomManager.findRoomById(room.getId())).isNotNull();

        assertThat(roomManager.findRoomById(room.getId()))
                .isNotSameAs(room)
                .isEqualToComparingFieldByField(room);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindRoomWithNull() {
        roomManager.findRoomById(null);
    }


    private static void executeSqlScript(DataSource ds, InputStream is) throws SQLException, IOException {
        try (Connection c = ds.getConnection()) {
            Scanner s = new Scanner(is).useDelimiter(";");
            while (s.hasNext()) {
                String sql = s.next().trim();
                if (sql.isEmpty()) {
                    continue;
                }
                try (PreparedStatement st1 = c.prepareStatement(sql)) {
                    st1.execute();
                }
            }
        }
    }
}
