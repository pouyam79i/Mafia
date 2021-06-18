package ir.pm.mafia.model.game.logic;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataType;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.controller.server.ClientState;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.handlers.PartHandler;
import ir.pm.mafia.model.game.handlers.ReceiverHandler;
import ir.pm.mafia.model.game.handlers.SenderHandler;
import ir.pm.mafia.model.game.logic.commands.PlayerCommand;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.model.game.state.StateUpdater;
import ir.pm.mafia.view.console.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * It handles Voting process of game.
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.0
 */
public class Vote extends PartHandler {

    /**
     * Contains list of alive player voted players
     * and the number of vote that!
     * this.PlayerToken ---> the player that this.Player voted for
     */
    private final HashMap<String , String> votesOfPlayers;
    /**
     * number of gathered vote for a player
     */
    private final HashMap<String, Integer> numberOfGatheredVotes;
    /**
     * Mayer token is used to confirm dismiss!
     */
    private final String mayerToken;
    /**
     * Used to tell it to skip this part!
     * after voting time is done!
     */
    private final StateUpdater stateUpdater;
    /**
     * If mayer is alive!
     */
    private boolean mayerAlive;
    /**
     * If result is dismissed!
     */
    private boolean dismissed;

    /**
     * Constructor of Vote
     * @param stateUpdater updates states
     * @param mayerToken token of mayer
     * @throws Exception if null state updater!
     */
    public Vote(StateUpdater stateUpdater, String mayerToken) throws Exception {
        if(stateUpdater == null)
            throw new Exception("Null state updater!");
        this.stateUpdater = stateUpdater;
        this.mayerToken = mayerToken;
        votesOfPlayers = new HashMap<String, String>();
        numberOfGatheredVotes = new HashMap<String, Integer>();
        mayerAlive = false;
        dismissed = false;
    }

    /**
     * After setting client handler call the initial!
     * do it before start!
     */
    public void initial(){
        ArrayList<String> listOfPlayers = new ArrayList<String>();
        for(ClientHandler ch : clientHandlers){
            if(ch.getClientState() == ClientState.ALIVE ||
                    ch.getClientState() == ClientState.GHOST){
                votesOfPlayers.put(ch.getToken(), null);
                numberOfGatheredVotes.put(ch.getToken(), 0);
                listOfPlayers.add(ch.getNickname());
            }
        }
        gameState = new GameState(State.Vote, listOfPlayers);
        if(mayerToken == null)
            return;
        ClientHandler mayerCH = getClientHandler(mayerToken);
        if(mayerCH == null)
            return;
        if(mayerCH.getCharacter().getCharacterName() == CharacterName.Mayer)
            if(mayerCH.getClientState() == ClientState.ALIVE ||
                    mayerCH.getClientState() == ClientState.GHOST)
            mayerAlive = true;
    }

    /**
     * Call this command after setting client handlers
     * resets ghost player to alive player
     */
    public void resetGhostClients(){
        for (ClientHandler ch : clientHandlers)
            if(ch.getClientState() == ClientState.GHOST)
                ch.updateClientState(ClientState.ALIVE);
    }

    /**
     * Shut downs the vote handler!
     * this also applies the voting process result
     */
    @Override
    public void shutdown(){
        finished = true;
        try {
            Thread.sleep(500);
        }catch (InterruptedException ignored){}
        String result = checkResult();
        mayerCheck(result);
        applyResult(result);
        stateUpdater.advance();
        try {
            Thread.sleep(500);
        }catch (InterruptedException ignored){}
        for(ReceiverHandler rh : receiverHandlers){
            rh.shutdown();
        }
        for(SenderHandler sh : senderHandlers){
            sh.shutdown();
        }
        this.close();
    }

    /**
     * Applying logic in vote system
     * You client cannot send normal messages
     * only can use command:
     * - @vote
     */
    @Override
    protected void applyLogic() {
        if(lastRead >= inputDataBase.getSize())
            return;
        Data data = (Data) inputDataBase.readData(lastRead);
        lastRead++;
        if(data == null)
            return;
        if(data.getSenderToken() == null)
            return;
        String userToken = data.getSenderToken();
        // If player is not alive, (s)he cannot interact in game!
        if(getClientHandler(userToken).getClientState() == ClientState.DISCONNECTED ||
                getClientHandler(userToken).getClientState() == ClientState.KILLED)
            return;
        // Analyzing player vote
        if (data.getDataType() == DataType.Vote){
            ir.pm.mafia.controller.data.boxes.Vote vote = (ir.pm.mafia.controller.data.boxes.Vote) data;
            if(vote.getTarget() == null){
                if(votesOfPlayers.get(userToken) == null)
                    sendToUser(Color.YELLOW_BOLD + "Your vote is already cleared!", userToken);
                else{
                    setVote(userToken, null);
                    sendToUser(Color.GREEN_BOLD + "Your vote is set to empty!", userToken);
                }
            }
            else {
                ClientHandler ch = null;
                ch = getClientHandlerByName(vote.getTarget());
                if(ch == null){
                    sendToUser(Color.RED_BOLD + "This player does not exist!", userToken);
                    return;
                }
                if(ch.getClientState() == ClientState.ALIVE || ch.getClientState() == ClientState.GHOST){
                    setVote(userToken, ch.getToken());
                }
                else {
                    sendToUser(Color.YELLOW_BOLD + ch.getNickname() +
                            " is out of game!", userToken);
                }
            }
        }
    }

    /**
     * Sets the vote of user
     * @param userToken token of voter
     * @param targetToken token of target!
     */
    private void setVote(String userToken, String targetToken){
        votesOfPlayers.put(userToken, targetToken);
    }

    /**
     * Calculates the result of voting
     * @return string if one max is found! else return null
     */
    private String checkResult(){
        AtomicReference<String> tokenOfResult = new AtomicReference<String>();
        AtomicInteger max = new AtomicInteger();
        AtomicInteger numberOfCurrentVotes = new AtomicInteger();
        votesOfPlayers.forEach((playerToken, voteToken) -> {
            if(voteToken != null){
                numberOfCurrentVotes.set(numberOfGatheredVotes.get(voteToken));
                numberOfGatheredVotes.put(voteToken, numberOfCurrentVotes.get() + 1);
                if(numberOfGatheredVotes.get(voteToken) > max.get()){
                    max.set(numberOfGatheredVotes.get(voteToken));
                }
            }
        });
        if(max.get() == 0)
            return null;
        AtomicInteger repeatedMax = new AtomicInteger();
        numberOfGatheredVotes.forEach((playerToken, numberOfVotes) -> {
            if(numberOfVotes == max.get()){
                tokenOfResult.set(playerToken);
                repeatedMax.getAndIncrement();
            }
        });
        if(repeatedMax.get() > 1)
            return null;
        return tokenOfResult.get();
    }

    /**
     * mayer checks the voting!
     * @param resultToken if it is admin he cannot remove it!
     */
    private void mayerCheck(String resultToken){
        if(resultToken == null)
            return;
        if(!mayerAlive){
            dismissed = false;
            return;
        }
        if(resultToken.equals(mayerToken)){
            dismissed = false;
            return;
        }
        sendToUser(Color.YELLOW_BOLD + "Do you want to dismiss this voting?\n" +
                Color.PURPLE_BOLD + " - Tell me less than 15 seconds.", mayerToken);
        int timer = 0;
        while (timer < 15000){
            timer++;
            try {
                Thread.sleep(1);
            }catch (InterruptedException ignored){}
            if(lastRead >= inputDataBase.getSize())
                continue;
            Data data = (Data) inputDataBase.readData(lastRead);
            lastRead++;
            if(data == null)
                continue;
            if(data.getSenderToken() == null)
                continue;
            if(!data.getSenderToken().equals(mayerToken))
                continue;
            if(data.getDataType() == DataType.Message){
                String command = ((Message) data).getMessageText();
                if(command.toUpperCase(Locale.ROOT).startsWith("@" + PlayerCommand.SKIP.toString())){
                    dismissed = false;
                    return;
                }
                else if(command.toUpperCase(Locale.ROOT).startsWith("@" + PlayerCommand.DISMISS.toString())){
                    dismissed = true;
                    return;
                }
            }
        }
    }

    /**
     * Applies the voting result!
     * @param tokenOfExecuted this player will be executed if alive!
     */
    private void applyResult(String tokenOfExecuted){
        if(dismissed){
            Message exceptMayer = new Message(mayerToken, Color.BLUE_BOLD + "GOD",
                    Color.YELLOW_BOLD + "Mayer dismissed the voting!");
            send(exceptMayer);
            return;
        }
        if(tokenOfExecuted == null){
            sendToAll(Color.YELLOW_BOLD + "No one is killed");
            return;
        }
        ClientHandler ch = getClientHandler(tokenOfExecuted);
        if(ch == null){
            sendToAll(Color.RED_BOLD + "No such a player to execute!");
            return;
        }
        if(ch.getClientState() == ClientState.KILLED || ch.getClientState() == ClientState.DISCONNECTED){
            sendToAll(Color.YELLOW_BOLD + "This player is already killed!");
        }
        else{
            ch.updateClientState(ClientState.KILLED);
            Message message = new Message(null, Color.BLUE_BOLD + "GOD",
                    Color.RED_BOLD + "You are executed!");
            message.setReceiverToken(ch.getToken());
            send(message);
            Message toAllExceptExecuted = new Message(ch.getToken(), Color.BLUE_BOLD + "GOD",
                    Color.GREEN_BOLD + ch.getNickname() + " is executed!");
            send(toAllExceptExecuted);
        }
    }

}
