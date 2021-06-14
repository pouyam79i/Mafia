package ir.pm.mafia.controller.server;

/**
 * States of client handler
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public enum ClientState {

    ALIVE,          // Completely connected.
    DISCONNECTED,   // Completely disconnected.
    GHOST,          // Only can receive data and observe the game (just for one round).
    KILLED,         // Completely GHOST, For rest of rounds cannot send anything!

}
