/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.manager;

import cz.hotel.hotelmanager.entity.Guest;
import cz.hotel.hotelmanager.entity.Room;
import cz.hotel.hotelmanager.builder.*;
import cz.hotel.hotelmanager.exceptions.IllegalEntityException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.assertj.core.api.Assertions.*;


public class HotelManagerImplTest {

    private HotelManagerImpl hotelManager;
    private RoomManagerImpl roomManager;
    private GuestManagerImpl guestManager;
    private DataSource ds;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:hotel-mgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Before
    public void setUp() throws SQLException, IOException {
        ds = prepareDataSource();
        executeSqlScript(ds, RoomManager.class.getResourceAsStream("createTables.sql"));
        hotelManager = new HotelManagerImpl(ds);
        guestManager = new GuestManagerImpl(ds);
        roomManager = new RoomManagerImpl(ds);
        prepareTestData();
    }
    
   @After
    public void tearDown() throws SQLException, IOException {
        executeSqlScript(ds, RoomManager.class.getResourceAsStream("dropTables.sql"));
    }
 
    private Room r1, r2, r3, roomWithNullId, roomNotInDB;
    private Guest g1, g2, g3, g4, g5, guestWithNullId, guestNotInDB;

    private void prepareTestData() {

        r1 = new RoomBuilder().capacity(1).roomType("Single").price(BigDecimal.ONE).roomNumber(1).build();
        r2 = new RoomBuilder().capacity(2).roomType("Double").price(BigDecimal.valueOf(5)).roomNumber(3).build();
        r3 = new RoomBuilder().capacity(3).roomType("Triple").price(BigDecimal.TEN).roomNumber(5).build();

        g1 = new GuestBuilder().name("John Lennon").address("Address 1").phoneNumber("111").build();
        g2 = new GuestBuilder().name("Paul McCartney").address("Address 2").phoneNumber("222").build();
        g3 = new GuestBuilder().name("Ringo Starr").address("Address 3").phoneNumber("333").build();
        g4 = new GuestBuilder().name("George Harrison").address("Address 4").phoneNumber("444").build();
        g5 = new GuestBuilder().name("John Doe").address("Address 5").phoneNumber("555").build();

        roomManager.createRoom(r1);
        roomManager.createRoom(r2);
        roomManager.createRoom(r3);

        guestManager.createGuest(g1);
        guestManager.createGuest(g2);
        guestManager.createGuest(g3);
        guestManager.createGuest(g4);
        guestManager.createGuest(g5);

        roomWithNullId = new RoomBuilder().id(null).build();
        guestWithNullId = new GuestBuilder().id(null).build();
        
        roomNotInDB = new RoomBuilder().id(r3.getId() + 100).build();
        assertThat(roomManager.findRoomById(roomNotInDB.getId())).isNull();
        guestNotInDB = new GuestBuilder().name("Guest not in DB").id(g5.getId() + 100).build();
        assertThat(guestManager.findGuestById(guestNotInDB.getId())).isNull();
        
    }

    @Test
    public void findRoomWithGuest() {

        assertThat(hotelManager.findRoomWithGuest(g1)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g2)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g3)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g4)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g5)).isNull();

        hotelManager.checkInGuest(g1, r3);

        assertThat(hotelManager.findRoomWithGuest(g1))
                .isEqualToComparingFieldByField(r3);
        assertThat(hotelManager.findRoomWithGuest(g2)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g3)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g4)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g5)).isNull();
    }

    @Test(expected = IllegalEntityException.class)
    public void findRoomWithNullGuest() {
        hotelManager.findRoomWithGuest(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void findRoomWithGuestHavingNullId() {
        hotelManager.findRoomWithGuest(guestWithNullId);
    }

    @Test
    public void findGuestsInRoom() {
        assertThat(hotelManager.findGuestsInRoom(r1)).isEmpty();
        assertThat(hotelManager.findGuestsInRoom(r2)).isEmpty();
        assertThat(hotelManager.findGuestsInRoom(r3)).isEmpty();

        hotelManager.checkInGuest(g2, r3);
        hotelManager.checkInGuest(g3, r2);
        hotelManager.checkInGuest(g4, r3);
        hotelManager.checkInGuest(g5, r2);

        assertThat(hotelManager.findGuestsInRoom(r1)).isEmpty();
        List<Guest> tmp = hotelManager.findGuestsInRoom(r2);
        assertThat(hotelManager.findGuestsInRoom(r2))
                .usingFieldByFieldElementComparator()
                .containsOnly(g3, g5);
        assertThat(hotelManager.findGuestsInRoom(r3))
                .usingFieldByFieldElementComparator()
                .containsOnly(g2, g4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findGuestsInNullRoom() {
        hotelManager.findGuestsInRoom(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void findGuestsInRoomHavingNullId() {
        hotelManager.findGuestsInRoom(roomWithNullId);
    }

    @Test
    public void checkInGuest() {

        assertThat(hotelManager.findRoomWithGuest(g1)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g2)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g3)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g4)).isNull();
        assertThat(hotelManager.findRoomWithGuest(g5)).isNull();

        hotelManager.checkInGuest(g1, r3);
        hotelManager.checkInGuest(g5, r1);
        hotelManager.checkInGuest(g3, r3);

        assertThat(hotelManager.findGuestsInRoom(r1))
                .usingFieldByFieldElementComparator()
                .containsOnly(g5);

        assertThat(hotelManager.findGuestsInRoom(r2))
                .isEmpty();

        assertThat(hotelManager.findGuestsInRoom(r3))
                .usingFieldByFieldElementComparator()
                .containsOnly(g1, g3);

        assertThat(hotelManager.findRoomWithGuest(g1))
                .isEqualToComparingFieldByField(r3);
        assertThat(hotelManager.findRoomWithGuest(g2))
                .isNull();
        assertThat(hotelManager.findRoomWithGuest(g3))
                .isEqualToComparingFieldByField(r3);
        assertThat(hotelManager.findRoomWithGuest(g4))
                .isNull();
        assertThat(hotelManager.findRoomWithGuest(g5))
                .isEqualToComparingFieldByField(r1);
    }

    
    /*@Test
    public void checkInGuestIntoFullRoom() {
        hotelManager.checkInGuest(g1, r3);
        hotelManager.checkInGuest(g5, r1);
        hotelManager.checkInGuest(g3, r3);

        assertThatThrownBy(() -> hotelManager.checkInGuest(g2, r1))
                .isInstanceOf(IllegalEntityException.class);

        assertThat(hotelManager.findGuestsInRoom(r1))
                .usingFieldByFieldElementComparator()
                .containsOnly(g5);
        assertThat(hotelManager.findGuestsInRoom(r2))
                .isEmpty();
        assertThat(hotelManager.findGuestsInRoom(r3))
                .usingFieldByFieldElementComparator()
                .containsOnly(g1, g3);
    }*/

    @Test(expected = IllegalEntityException.class)
    public void checkInNullGuest() {
        hotelManager.checkInGuest(null, r2);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkInGuestWithNullId() {
        hotelManager.checkInGuest(guestWithNullId, r2);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkInGuestNotInDB() {
        hotelManager.checkInGuest(guestNotInDB, r2);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkInGuestIntoNullRoom() {
        hotelManager.checkInGuest(g2, null);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkInGuestIntoRoomWithNullId() {
        hotelManager.checkInGuest(g2, roomWithNullId);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkInGuestIntoRoomNotInDB() {
        hotelManager.checkInGuest(g2, roomNotInDB);
    }

    @Test
    public void checkOutGuest() {

        hotelManager.checkInGuest(g1, r3);
        hotelManager.checkInGuest(g3, r3);
        hotelManager.checkInGuest(g4, r3);
        hotelManager.checkInGuest(g5, r1);

        assertThat(hotelManager.findRoomWithGuest(g1))
                .isEqualToComparingFieldByField(r3);
        assertThat(hotelManager.findRoomWithGuest(g2))
                .isNull();
        assertThat(hotelManager.findRoomWithGuest(g3))
                .isEqualToComparingFieldByField(r3);
        assertThat(hotelManager.findRoomWithGuest(g4))
                .isEqualToComparingFieldByField(r3);
        assertThat(hotelManager.findRoomWithGuest(g5))
                .isEqualToComparingFieldByField(r1);

        hotelManager.checkOutGuest(g3);

        assertThat(hotelManager.findGuestsInRoom(r1))
                .usingFieldByFieldElementComparator()
                .containsOnly(g5);
        assertThat(hotelManager.findGuestsInRoom(r2))
                .isEmpty();
        assertThat(hotelManager.findGuestsInRoom(r3))
                .usingFieldByFieldElementComparator()
                .containsOnly(g1, g4);

        assertThat(hotelManager.findRoomWithGuest(g1))
                .isEqualToComparingFieldByField(r3);
        assertThat(hotelManager.findRoomWithGuest(g2))
                .isNull();
        assertThat(hotelManager.findRoomWithGuest(g3))
                .isNull();
        assertThat(hotelManager.findRoomWithGuest(g4))
                .isEqualToComparingFieldByField(r3);
        assertThat(hotelManager.findRoomWithGuest(g5))
                .isEqualToComparingFieldByField(r1);
    }

    @Test
    public void checkOutGuestFromRoomWhereHeIsNotAccommodated() {

        hotelManager.checkInGuest(g1, r3);
        hotelManager.checkInGuest(g4, r3);
        hotelManager.checkInGuest(g5, r1);

        hotelManager.checkOutGuest(g1);

        assertThat(hotelManager.findGuestsInRoom(r1))
                .usingFieldByFieldElementComparator()
                .containsOnly(g5);
        assertThat(hotelManager.findGuestsInRoom(r2))
                .isEmpty();
        
        hotelManager.checkInGuest(g1, r3);
        
        assertThat(hotelManager.findGuestsInRoom(r3))
                .usingFieldByFieldElementComparator()
                .containsOnly(g1, g4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkOutNullGuest() {
        hotelManager.checkOutGuest(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkOutGuestWithNullId() {
        hotelManager.checkOutGuest(guestWithNullId);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkOutGuestNotInDB() {
        hotelManager.checkOutGuest(guestNotInDB);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkOutGuestFromNullRoom() {
        hotelManager.checkInGuest(g2, null);
        hotelManager.checkOutGuest(g2);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkOutGuestFromRoomWithNullId() {
        hotelManager.checkInGuest(g2, roomWithNullId);
        hotelManager.checkOutGuest(g2);
    }

    @Test(expected = IllegalEntityException.class)
    public void checkOutGuestFromRoomNotInDB() {
        hotelManager.checkInGuest(g2, roomNotInDB);
        hotelManager.checkOutGuest(g2);
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
