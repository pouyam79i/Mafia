package ir.pm.mafia.controller.data.boxes;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataType;

/**
 * This is the data box of action.
 * Contains the structure of action.
 * Used to transfer data of player's action between clients and server!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class Action extends Data {

    /**
     * Action of player!
     */
    private final String action;
    /**
     * Target of action!
     * if the action of player doesnt require target
     * it will be EMPTY
     */
    private final String target;

    /**
     * Constructor of Action
     * Sets important fields
     * @param senderToken is the sender token!     ?? Still dont know how to deploy this!
     * @param senderName  is the sender nickname or username!
     * @param action is the call for action of player!
     * @param target is the token of target of action
     */
    public Action(String senderToken, String senderName, String action, String target) {
        super(senderToken, senderName, DataType.Action);
        if(action == null){
            action = "EMPTY";
            target = "EMPTY";
        }
        if(target == null){
            target = "EMPTY";
        }
        this.action = action;
        this.target = target;
    }

    // Getters
    public String getAction() {
        return action;
    }
    public String getTarget() {
        return target;
    }

}
