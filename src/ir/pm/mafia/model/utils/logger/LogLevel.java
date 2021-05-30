package ir.pm.mafia.model.utils.logger;

/**
 * This class contains log leve of this program 'Mafia'
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 */
public enum LogLevel {
    GameInterrupted,        // When game is interrupted
    ServerFailed,           // When server is failed
    ClientFailed,           // When client is failed
    ConsoleInputWarning,    // When console is failed to read the proper input
    IOException,            // When we I/O process is failed
    ThreadWarning,          // When threads of program face problem
    ThreadInterrupted       // When (a) thread(s) of program is(are) interrupted.
}
