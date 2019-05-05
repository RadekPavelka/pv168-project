/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.manager;

import cz.hotel.hotelmanager.builder.GuestBuilder;
import cz.hotel.hotelmanager.entity.Guest;
import cz.hotel.hotelmanager.entity.Room;
import cz.hotel.hotelmanager.exceptions.IllegalEntityException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertTrue;





public class GuestManagerImplTest {
    
    private GuestManagerImpl manager;
    private Guest guest;
    private EmbeddedDataSource source;
    
    private void databaseSetUp() {
        source = new EmbeddedDataSource();
        source.setDatabaseName("memory:guest-mgr-test");
        source.setCreateDatabase("create");
    }
    
    private void loadSQLFile(String fileName) throws SQLException {
        InputStream is = GuestManager.class.getResourceAsStream(fileName);
        
        Scanner s = new Scanner(is).useDelimiter(";");
        
        try(Connection conn = source.getConnection()) {
            while(s.hasNext()) {
                String sql = s.next();
                sql = sql.trim();
                if (sql.isEmpty()) continue;
                try(PreparedStatement state = conn.prepareStatement(sql)) {
                    state.execute();
                }
            }
        }
    }
    
    @Before
    public void setUp() throws SQLException {
        databaseSetUp();
        loadSQLFile("createTables.sql");
        
        manager = new GuestManagerImpl(source);
        guest = GuestBuilder.defBuild();
    }
    
    @After
    public void tearDown() throws SQLException {
        loadSQLFile("dropTables.sql");
    }

    @Test
    public void testCreateGuest() {
        manager.createGuest(guest);
        
        assertThat(guest.getId()).isNotNull();
        
        assertThat(manager.findGuestById(guest.getId()))
                .isNotSameAs(guest)
                .isEqualToComparingFieldByField(guest);
    }
    
    @Test
    public void testCreateOnlyOneGuest() {
        manager.createGuest(guest);
        
        assertTrue(manager.findAllGuests().size() == 1);
    }
    
    @Test
    public void testCreateOnlyThreeGuests() {
        Guest guest1 = GuestBuilder.defBuild();
        Guest guest2 = GuestBuilder.defBuild();
        
        manager.createGuest(guest);
        manager.createGuest(guest1);
        manager.createGuest(guest2);
        
        assertTrue(manager.findAllGuests().size() == 3);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void createGuestWithNull() {
        manager.createGuest(null);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void createGuestWithExistingID() {
        guest.setId(1L);
        manager.createGuest(guest);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void createGuestWithNullName() {
        guest.setName(null);
        manager.createGuest(guest);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void createGuestWithNullAddress() {
        guest.setAddress(null);
        manager.createGuest(guest);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void createGuestWithNullPhoneNumber() {
        guest.setPhoneNumber(null);
        manager.createGuest(guest);
    }
    
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }
    
    private void testUpdateGuest(Operation<Guest> operation) {
        Guest updateGuest = guest;
        Guest checkGuest = GuestBuilder.defBuild(1);
        
        manager.createGuest(updateGuest);
        manager.createGuest(checkGuest);
        
        operation.callOn(updateGuest);
        
        manager.updateGuest(updateGuest);
        
        assertThat(manager.findGuestById(updateGuest.getId()))
                .isEqualToComparingFieldByField(updateGuest);
        assertThat(manager.findGuestById(checkGuest.getId()))
                .isEqualToComparingFieldByField(checkGuest);
    }
    
    @Test
    public void updateGuestName() {
        testUpdateGuest((g) -> g.setName("John Doe"));
    }
    
    @Test
    public void updateGuestsRoom() {
        RoomManagerImpl roomMgr = new RoomManagerImpl(source);
        Room room = new Room();
        room.setCapacity(5);
        room.setPrice(BigDecimal.valueOf(500));
        room.setRoomNumber(10);
        room.setRoomType("Extra");
        roomMgr.createRoom(room);
        testUpdateGuest((g) -> g.setRoomId(room.getId()));
    }
    
    @Test
    public void updateGuestAddress() {
        testUpdateGuest((g) -> g.setAddress("Maple street 35/84, 07 007 Brno"));
    }
    
    @Test
    public void updateGuestPhone() {
        testUpdateGuest((g) -> g.setPhoneNumber("+420685482159"));
    }
    
    
    
    @Test
    public void updateGuestsRoomWithNull() {
        testUpdateGuest((g) -> g.setRoomId(null));
    }
    
    @Test(expected = IllegalEntityException.class)
    public void updateWithNull() {
        manager.updateGuest(null);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void updateGuestWithNullId() {
        manager.updateGuest(guest);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void updateNonExistingGuest() {
        guest.setId(1L);
        manager.updateGuest(guest);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void updateGuestWithNullName() {
        manager.createGuest(guest);
        guest.setName(null);
        manager.updateGuest(guest);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void updateGuestWithNullAddress() {
        manager.createGuest(guest);
        guest.setAddress(null);
        manager.updateGuest(guest);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void updateGuestWithNullPhoneNumber() {
        manager.createGuest(guest);
        guest.setPhoneNumber(null);
        manager.updateGuest(guest);
    }

    @Test
    public void testDeleteGuest() {
        Guest guest0 = guest;
        Guest guest1 = GuestBuilder.defBuild(1);
        manager.createGuest(guest0);
        manager.createGuest(guest1);
        
        assertThat(manager.findGuestById(guest0.getId())).isNotNull();
        assertThat(manager.findGuestById(guest1.getId())).isNotNull();
        
        manager.deleteGuest(guest0);
        
        assertThat(manager.findGuestById(guest0.getId())).isNull();
        assertThat(manager.findGuestById(guest1.getId())).isNotNull();
    }
    
    @Test
    public void testDeleteOnlyOneGuest() {
        manager.createGuest(guest);
        manager.deleteGuest(guest);
        
        assertTrue(manager.findAllGuests().isEmpty());
    }
    
    @Test
    public void testDeleteOnlyOneOfThreeGuests() {
        Guest guest1 = GuestBuilder.defBuild();
        Guest guest2 = GuestBuilder.defBuild();
        
        manager.createGuest(guest);
        manager.createGuest(guest1);
        manager.createGuest(guest2);
        
        manager.deleteGuest(guest1);
        
        assertTrue(manager.findAllGuests().size() == 2);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void deleteWithNull() {
        manager.deleteGuest(null);
    }

    @Test(expected = IllegalEntityException.class)
    public void deleteGuestWithNullId() {
        manager.deleteGuest(guest);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void deleteNonExistingGuest() {
        guest.setId(1L);
        manager.deleteGuest(guest);
    }
    
    @Test
    public void testFindGuestById() {
        manager.createGuest(guest);
        
        assertThat(manager.findGuestById(guest.getId())).isNotNull();
        
        assertThat(manager.findGuestById(guest.getId()))
                .isNotSameAs(guest)
                .isEqualToComparingFieldByField(guest);
    }
    
    @Test(expected = IllegalEntityException.class)
    public void testFindGuestWithNull() {
        manager.findGuestById(null);
    }
    
    @Test
    public void testFindNonExistentGuest() {
        guest.setId(1L);
        assertThat(manager.findGuestById(guest.getId())).isNull();
    }

    @Test
    public void testFindAllGuests() {
        assertThat(manager.findAllGuests()).isEmpty();
        
        Guest guest0 = guest;
        Guest guest1 = GuestBuilder.defBuild(1);
        
        manager.createGuest(guest0);
        manager.createGuest(guest1);
        
        assertThat(manager.findAllGuests())
                .usingFieldByFieldElementComparator()
                .containsOnly(guest0,guest1);
        
    }
}
