/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.exceptions;


public class IllegalEntityException extends RuntimeException {

    /**
     * Creates a new instance of <code>IllegalEntityException</code> without
     * detail message.
     */
    public IllegalEntityException() {
    }

    /**
     * Constructs an instance of <code>IllegalEntityException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public IllegalEntityException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>IllegalEntityException</code> with the
     * specified detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause
     */
    public IllegalEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
