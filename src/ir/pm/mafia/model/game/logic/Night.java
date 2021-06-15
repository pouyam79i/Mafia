package ir.pm.mafia.model.game.logic;

import ir.pm.mafia.controller.data.Data;
import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.DataType;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.controller.server.ClientState;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;
import ir.pm.mafia.model.game.character.action.Action;
import ir.pm.mafia.model.game.character.action.ActionNotes;
import ir.pm.mafia.model.game.character.characters.DoctorLecter;
import ir.pm.mafia.model.game.character.characters.Impregnable;
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

public class Night extends PartHandler {

    /**
     * Alive players
     */
    private final ArrayList<ClientHandler> alivePlayer;
    /**
     * Dead players
     */
    private final ArrayList<ClientHandler> deadPlayer;
    /**
     * Citizen Team
     */
    private ArrayList<ClientHandler> citizenTeam;
    /**
     * Mafia Team
     */
    private final ArrayList<ClientHandler> mafiaTeam;
    /**
     * Action ---> target
     * note that some action has no target!
     */
    private final HashMap<Action, ClientHandler> setActionList;
    /**
     * State updater of game
     */
    private StateUpdater stateUpdater;

    /**
     *
     * @param citizenTeam
     * @param mafiaTeam
     * @param stateUpdater
     * @throws Exception
     */
    public Night(ArrayList<ClientHandler> citizenTeam,
                 ArrayList<ClientHandler> mafiaTeam,
                 StateUpdater stateUpdater) throws Exception {
        if(citizenTeam == null || mafiaTeam == null || stateUpdater == null)
            throw new Exception("Null input");
        this.citizenTeam = citizenTeam;
        this.mafiaTeam = mafiaTeam;
        this.stateUpdater = stateUpdater;
        deadPlayer = new ArrayList<ClientHandler>();
        alivePlayer = new ArrayList<ClientHandler>();
        setActionList = new HashMap<Action, ClientHandler>();
    }

    /**
     * Sets alive and dead players
     * And checks head of mafia
     */
    public void initial(){
        ArrayList<String> nameOfAlivePlayers = new ArrayList<String>();
        for(ClientHandler ch : clientHandlers){
            if(ch.getClientState() == ClientState.KILLED || ch.getClientState() == ClientState.DISCONNECTED){
                deadPlayer.add(ch);
            }
            else{
                alivePlayer.add(ch);
                nameOfAlivePlayers.add(ch.getNickname());
            }
        }
        gameState = new GameState(State.Night, nameOfAlivePlayers);
    }

    @Override
    public void shutdown(){
        finished = true;
        applyActions();
        if(checkGameEnd() != Group.NULL){
            stateUpdater.setGameFinished(true);
            sendFinishedState();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {}
        }
        stateUpdater.advance();
        for(ReceiverHandler rh : receiverHandlers){
            rh.shutdown();
        }
        for(SenderHandler sh : senderHandlers){
            sh.shutdown();
        }
        this.close();
    }

    /**
     * Applies logic for night!
     * Gets and set actions of players!
     */
    @Override
    protected void applyLogic() {

        if(lastRead >= inputDataBase.getSize())
            return;
        Data data = (Data) inputDataBase.readData(lastRead);
        lastRead++;
        if(data == null)
            return;
        if(!(data.getDataType() == DataType.Message))
            return;
        Message message = (Message) data;
        ClientHandler player = getClientHandler(data.getSenderToken());
        if(player == null)
            return;
        if(!alivePlayer.contains(player))
            return;
        String command = message.getMessageText();
        command = command.toUpperCase(Locale.ROOT);

        if(command.startsWith("@" + PlayerCommand.ACTION.toString())){
            ClientHandler target = null;
            try {
                target = getClientHandlerByName(message.getMessageText().split(" ")[1]);
            }catch (Exception ignored){}
            if(setAction(player, target)){
                sendToUser(Color.GREEN_BOLD + "You set your action", data.getSenderToken());
            }else {
                sendToUser(Color.YELLOW_BOLD + "Your action is not set", data.getSenderToken());
            }
        }
        else if(command.startsWith("@" + PlayerCommand.HELP.toString())){
            sendToUser(getActionNote(player.getCharacter().getCharacterName()), data.getSenderToken());
        }
    }

    /**
     * Sets a proper action for player
     * @param player who called action
     * @param target target of his action
     * @return true if setting action
     */
    private boolean setAction(ClientHandler player, ClientHandler target){
        if(player == null)
            return false;
        Character playerCharacter = player.getCharacter();
        // Setting mafia action
        if(playerCharacter.getGroup() == Group.Mafia){
            // Godfather
            if(playerCharacter.getCharacterName() == CharacterName.God_Father){
                if((!mafiaTeam.contains(target)) && (!deadPlayer.contains(target))){
                    setActionList.put(Action.SHOOT_MAFIA, target);
                    return true;
                }
            }
            // Doctor lecter
            else if(playerCharacter.getCharacterName() == CharacterName.Doctor_Lecter){
                if(mafiaTeam.contains(target) && target != player){
                    setActionList.put(Action.REVIVE_MAFIA, target);
                    return true;
                }
                else if(((DoctorLecter) playerCharacter).isHeadOfMafia()){
                    if((!mafiaTeam.contains(target)) && (!deadPlayer.contains(target))){
                        setActionList.put(Action.SHOOT_MAFIA, target);
                        return true;
                    }
                }
            }
            // Normal mafia
            else {
                if(((DoctorLecter) playerCharacter).isHeadOfMafia()){
                    if((!mafiaTeam.contains(target)) && (!deadPlayer.contains(target))){
                        setActionList.put(Action.SHOOT_MAFIA, target);
                        return true;
                    }
                }
            }
        }
        // Setting citizen action
        else if(playerCharacter.getGroup() == Group.Citizen){
            // Sniper
            if(playerCharacter.getCharacterName() == CharacterName.Sniper){
                if(target != player && (!deadPlayer.contains(target))){
                    setActionList.put(Action.SHOOT_SNIPER, target);
                    return true;

                }
            }
            // Detective
            else if(playerCharacter.getCharacterName() == CharacterName.Detective){
                if(target != player){
                    if(alivePlayer.contains(target)){
                        setActionList.put(Action.ENQUIRY, target);
                        return true;
                    }
                }
            }
            // Impregnable
            else if(playerCharacter.getCharacterName() == CharacterName.Impregnable){
                setActionList.put(Action.ENQUIRY_DEAD, player);
                return true;
            }
            // Psychologist
            else if(playerCharacter.getCharacterName() == CharacterName.Psychologist){
                if(target != player){
                    setActionList.put(Action.SILENCE, target);
                    return true;
                }
            }
            // Psychologist
            else if(playerCharacter.getCharacterName() == CharacterName.Doctor_Citizen){
                if(target != player){
                    setActionList.put(Action.REVIVE, target);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Find related note for players action
     * @param characterName name of character
     * @return note of action
     */
    private String getActionNote(CharacterName characterName){
        if(characterName == CharacterName.God_Father)
            return ActionNotes.SHOOT;
        else if(characterName == CharacterName.Doctor_Lecter)
            return ActionNotes.REVIVE;
        else if(characterName == CharacterName.Normal_Mafia)
            return ActionNotes.NORMAL_MAFIA;
        else if(characterName == CharacterName.Mayer)
            return ActionNotes.DISMISS;
        else if(characterName == CharacterName.Doctor_Citizen)
            return ActionNotes.REVIVE;
        else if(characterName == CharacterName.Sniper)
            return ActionNotes.SHOOT;
        else if(characterName == CharacterName.Detective)
            return ActionNotes.ENQUIRY;
        else if(characterName == CharacterName.Impregnable)
            return ActionNotes.ENQUIRY_DEAD;
        else if(characterName == CharacterName.Psychologist)
            return ActionNotes.SILENCE;
        else
            return ActionNotes.EMPTY;
    }

    // Applies night actions
    private void applyActions(){

        // Checking mafia shoot
        if(setActionList.containsKey(Action.SHOOT_MAFIA)){
            ClientHandler target = setActionList.get(Action.SHOOT_MAFIA);
            if(target != null){
                target.getCharacter().gottenShot();
            }
        }

        // Checking sniper shoot
        if(setActionList.containsKey(Action.SHOOT_SNIPER)){
            ClientHandler target = setActionList.get(Action.SHOOT_SNIPER);
            if(target != null){
                target.getCharacter().gottenShot();
                if(citizenTeam.contains(target)){
                    for(ClientHandler sniper : citizenTeam){
                        if(sniper.getCharacter().getCharacterName() == CharacterName.Sniper){
                            sendToUser(Color.RED_BOLD + "You shot a citizen", sniper.getToken());
                            sniper.getCharacter().gottenExecuted();
                        }
                    }
                }
            }
        }

        // Checking doctor lecter revive
        if(setActionList.containsKey(Action.REVIVE_MAFIA)){
            ClientHandler target = setActionList.get(Action.REVIVE_MAFIA);
            if(target != null){
                target.getCharacter().revive();
            }
        }

        // Checking doctor citizen revive
        if(setActionList.containsKey(Action.REVIVE)){
            ClientHandler target = setActionList.get(Action.REVIVE);
            if(target != null){
                target.getCharacter().revive();
            }
        }

        // Checking psychologist silence order
        if(setActionList.containsKey(Action.SILENCE)){
            ClientHandler target = setActionList.get(Action.SILENCE);
            if(target != null){
                target.updateClientState(ClientState.GHOST);
            }
        }

        // Checking impregnable dead enquiry
        if(setActionList.containsKey(Action.ENQUIRY_DEAD)){
            ClientHandler target = setActionList.get(Action.ENQUIRY_DEAD);
            if(target != null){
                for(ClientHandler impregnable : citizenTeam){
                    if(impregnable.getCharacter().getCharacterName() == CharacterName.Impregnable){
                        if(((Impregnable) impregnable.getCharacter()).useDeadEnquiry()){
                            StringBuilder list = new StringBuilder(Color.YELLOW_BOLD + "Impregnable dead enquiry is called!");
                            for (ClientHandler dead : deadPlayer){
                                list.append("\n" + Color.PURPLE_BOLD).append(dead.getCharacter().toString());
                            }
                            if(deadPlayer.size() > 0)
                                sendToAll(list.toString());
                        }
                    }
                }
            }
        }

        // Checking ENQUIRY for detective
        if(setActionList.containsKey(Action.ENQUIRY)){
            ClientHandler target = setActionList.get(Action.ENQUIRY);
            String serverRespond = null;
            if(target != null){
                if(target.getCharacter().getCharacterName() == CharacterName.God_Father || citizenTeam.contains(target)){
                    serverRespond = Color.PURPLE_BOLD + "Sorry cannot talk tell you the character!";
                }
                else {
                    serverRespond = Color.YELLOW_BOLD + target.getNickname() + " is " + target.getCharacter().toString();
                }
                for(ClientHandler detective : citizenTeam){
                    if(detective.getCharacter().getCharacterName() == CharacterName.Detective){
                        sendToUser(serverRespond, detective.getToken());
                        break;
                    }
                }
            }
        }

        // Checking result
        for(ClientHandler player : alivePlayer){
            if(player.getCharacter().checkPassedAway()){
                sendToUser(Color.RED_BOLD + "You are killed!", player.getToken());
                player.updateClientState(ClientState.KILLED);
            }
        }
    }

    /**
     * Checks if we have a winner group
     * @return group of winners!
     */
    private Group checkGameEnd(){
        int aliveMafia = 0;
        int aliveCitizen = 0;
        for(ClientHandler mafia : mafiaTeam){
            if(mafia.getClientState() == ClientState.ALIVE || mafia.getClientState() == ClientState.GHOST)
                aliveMafia++;
        }
        for(ClientHandler citizen : citizenTeam){
            if(citizen.getClientState() == ClientState.ALIVE || citizen.getClientState() == ClientState.GHOST)
                aliveCitizen++;
        }
        Group winner = Group.NULL;
        if(aliveMafia == 0)
            winner = Group.Citizen;
        else if(aliveMafia >= aliveCitizen)
            winner = Group.Mafia;

        if(winner != Group.NULL){
            sendToAll(Color.YELLOW_BOLD + "Group of " + winner.toString() + " is winner!");
            try {
                Thread.sleep(2000);
            }catch (InterruptedException ignored){}
        }
        return winner;
    }

    /**
     * Sends finished state to all
     */
    private void sendFinishedState(){
        for (ClientHandler ch : clientHandlers){
            ch.send(new DataBox(new GameState(State.FINISHED, null), null));
        }
    }

}
