package ir.pm.mafia.controller.data.boxes;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataType;

/**
 * This is the data box of vote.
 * Contains the structure of vote.
 * Used to transfer data of voters between clients and server!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class Vote extends Data {

    /**
     * The target contains the token of person you voted for!
     * If null it means No target!
     */
    private final String target;

    /**
     * Constructor of Vote
     * Sets important fields
     * @param senderToken is the sender token!
     * @param senderName is the sender nickname or username!
     * @param target is the token of person you want to vote for.
     */
    public Vote(String senderToken, String senderName, String target) {
        super(senderToken, senderName, DataType.Vote);
        if(target == null)
            target = "EMPTY"; // When you vote for no one!
        this.target = target;
    }

    // Getter
    public String getTarget() {
        return target;
    }

}
