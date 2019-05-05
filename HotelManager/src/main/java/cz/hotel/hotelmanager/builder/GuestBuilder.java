/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.builder;

import cz.hotel.hotelmanager.entity.Guest;

/**
 * Builder for Guest entity.
 */
public class GuestBuilder {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private Long roomId;
    
    public GuestBuilder id(Long id) {
        this.id = id;
        return this;
    }
    
    public GuestBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public GuestBuilder address(String address) {
        this.address = address;
        return this;
    }
    
    public GuestBuilder phoneNumber(String phone) {
        this.phoneNumber = phone;
        return this;
    }
    
    public GuestBuilder roomId(Long id) {
        this.roomId = id;
        return this;
    }
    
    public Guest build() {
        Guest guest = new Guest();
        guest.setId(id);
        guest.setName(name);
        guest.setAddress(address);
        guest.setPhoneNumber(phoneNumber);
        guest.setRoomId(roomId);
        return guest;
    }
    
    /**
     * Helper function for defBuild.
     * @return Guest with default settings.
     */
    public static Guest defBuild(){
        return defBuild(0);
    }
    
    /**
     * Default build settings for quick Guest construction. Contains 3 possible
     * default builds.
     * @param n number of build.
     * @return Guest with default settings.
     */
    public static Guest defBuild(int n) {
        switch(n){
            case(0):
                return new GuestBuilder()
                .address("FakeStreet 69/72, 07 007 Brno")
                .name("Pedro Ramirez")
                .phoneNumber("+420988347758")
                .build();
            case(1):
                return new GuestBuilder()
                .address("OrangePeel 14/88, 07 007 Brno")
                .name("Gerbert Hoffman")
                .phoneNumber("+420845832675")
                .build();
            default:
                return new GuestBuilder()
                .address("NichTell 91/27, 07 007 Brno")
                .name("Igor Raznew")
                .phoneNumber("+420975842358")
                .build();
        }
        
    }
}
