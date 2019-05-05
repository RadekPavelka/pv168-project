/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.hotel.hotelmanager.exceptions;


public class ServiceFailureException extends RuntimeException {

    /**
     * Creates a new instance of <code>ServiceFailureException</code> without
     * detail message.
     */
    public ServiceFailureException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>ServiceFailureException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ServiceFailureException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>ServiceFailureException</code> with the
     * specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause cause of exception.
     */
    public ServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
