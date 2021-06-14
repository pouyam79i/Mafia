package ir.pm.mafia.controller.data;

/**
 * This class contain data type!
 * Used to determine data types!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0.2
 */
public enum DataType {

    Message,        // Used for chat rooms to transform text messages and commands.
    Vote,           // Used to vote.
    Action,         // Used for action.
    Undefined,      // This type is ignored!

}
