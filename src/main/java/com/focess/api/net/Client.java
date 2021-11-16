package com.focess.api.net;

/**
 * Represents a Client connected to a server.
 */
public interface Client {


    /**
     * Get the client name
     *
     * @return the client name
     */
    String getName();

    /**
     * Get the client id
     *
     * @return the client id
     */
    int getId();
}
