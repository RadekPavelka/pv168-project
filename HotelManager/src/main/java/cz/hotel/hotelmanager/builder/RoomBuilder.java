/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.builder;


import cz.hotel.hotelmanager.entity.Room;
import java.math.BigDecimal;

/**
 * Builder for Room entity.
 */
public class RoomBuilder {
   private Long id;
   private int roomNumber;
   private int capacity;
   private String roomType;
   private BigDecimal price;
   
   public RoomBuilder id(Long id){
       this.id = id;
       return this;
   } 
   
   public RoomBuilder roomNumber(int num) {
       this.roomNumber = num;
       return this;
   } 
   
   public RoomBuilder capacity(int cap) {
       this.capacity = cap;
       return this;
   } 
   
   public RoomBuilder roomType(String type) {
       this.roomType = type;
       return this;
   } 
   
   public RoomBuilder price(BigDecimal price) {
       this.price = price;
       return this;
   } 
   
   public Room build() {
       Room room = new Room();
       room.setId(id);
       room.setRoomNumber(roomNumber);
       room.setCapacity(capacity);
       room.setRoomType(roomType);
       room.setPrice(price);
       return room;       
   }
   
   /**
    * Default build settings for quick Room construction.
    * ID = null
    * Room Number = 404
    * capacity = 2
    * Room Type = WildJungle
    * price = 1 000 000
    * @return Room with default settings.
    */
   public static Room defBuild() {
       return defBuild(0);
   }
       public static Room defBuild(int n) {
        switch(n){
            case(0):
                return new RoomBuilder()
               .roomNumber(404)
               .capacity(2)
               .roomType("WildJungle")
               .price(new BigDecimal(100))
               .build();
            case(1):
                return new RoomBuilder()
               .roomNumber(202)
               .capacity(3)
               .roomType("Triple")
               .price(new BigDecimal(300))
               .build();
            default:
                return new RoomBuilder()
               .roomNumber(303)
               .capacity(4)
               .roomType("Double bed")
               .price(new BigDecimal(400))
               .build();
        }
        
    }
}
