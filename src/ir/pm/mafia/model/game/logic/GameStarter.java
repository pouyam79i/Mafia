package ir.pm.mafia.model.game.logic;

import ir.pm.mafia.controller.data.DataBox;
import ir.pm.mafia.controller.data.boxes.GameState;
import ir.pm.mafia.controller.data.boxes.Message;
import ir.pm.mafia.controller.server.ClientHandler;
import ir.pm.mafia.model.game.character.Character;
import ir.pm.mafia.model.game.character.CharacterFactory;
import ir.pm.mafia.model.game.character.CharacterName;
import ir.pm.mafia.model.game.character.Group;
import ir.pm.mafia.model.game.state.State;
import ir.pm.mafia.view.console.Color;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Game handler the introduction night!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version v1.0
 */
public class GameStarter{

    /**
     * Game client handlers
     */
    private final ArrayList<ClientHandler> clientHandlers;
    /**
     * Mafia player
     */
    private final ArrayList<ClientHandler> mafiaTeam;
    /**
     * Citizen player
     */
    private final ArrayList<ClientHandler> citizenTeam;
    /**
     * Character in game
     */
    private HashMap<ClientHandler, Character> playerCharacters;
    /**
     * Secure random generate random number!
     */
    private final SecureRandom random;

    /**
     * Constructor of GameStarter.
     * It handles the introduction night.
     * @param clientHandlers client handlers
     * @throws Exception If params are null!
     */
    public GameStarter(ArrayList<ClientHandler> clientHandlers) throws Exception {
        if(clientHandlers == null)
            throw new Exception("Null input");
        if(clientHandlers.size() < 6)
            throw new Exception("Not enough players!");
        this.clientHandlers = clientHandlers;
        playerCharacters = null;
        mafiaTeam = new ArrayList<ClientHandler>();
        citizenTeam = new ArrayList<ClientHandler>();
        random = new SecureRandom();
    }

    /**
     * Build and set characters for players in a random way
     */
    public void setCharacter(){
        HashMap<ClientHandler, Character> playerCharacters = new HashMap<ClientHandler, Character>();
        ArrayList<ClientHandler> existingClients = new ArrayList<ClientHandler>(this.clientHandlers);
        CharacterFactory characterFactory = new CharacterFactory();
        int remainingPlayers = existingClients.size();
        int maxMafia = (int) Math.floor(remainingPlayers/3);
        int maxCitizen = remainingPlayers - maxMafia;
        int indexOfCurrent, builtMafia = 0, builtCitizen = 0;
        while (remainingPlayers > 0){
            indexOfCurrent = random.nextInt(remainingPlayers);
            ClientHandler clientHandler = existingClients.get(indexOfCurrent);
            existingClients.remove(indexOfCurrent);
            remainingPlayers = existingClients.size();
            // building characters for mafia
            if(builtMafia < maxMafia){
                Character character = characterFactory.buildCharacter(Group.Mafia);
                playerCharacters.put(clientHandler, character);
                clientHandler.setCharacter(character);
                mafiaTeam.add(clientHandler);
                builtMafia++;
            }
            // building characters for citizen
            else if(builtCitizen < maxCitizen){
                Character character = characterFactory.buildCharacter(Group.Citizen);
                playerCharacters.put(clientHandler, character);
                clientHandler.setCharacter(character);
                citizenTeam.add(clientHandler);
                builtCitizen++;
            }
            else {
                break;
            }
        }
        this.playerCharacters = playerCharacters;
    }

    /**
     * At this point mafia group must know each other!
     */
    public void applyLogic() {
        GameState cgs = new GameState(State.Lobby, null);
        Message message = null;
        String messageText = null;
        for(ClientHandler CH : clientHandlers){
            message = null;
            messageText = null;
            if(mafiaTeam.contains(CH)){
                int index = 0;
                messageText = Color.YELLOW_BOLD + "Your " + Color.RED_BOLD + "Mafia " +
                        Color.YELLOW_BOLD + "Teammates:\n";
                for(ClientHandler otherMafias : mafiaTeam){
                    if(otherMafias == CH)
                        continue;
                    messageText += Color.YELLOW_BOLD + index + " - " + otherMafias.getNickname() + " is " +
                            Color.RED_BOLD + playerCharacters.get(otherMafias).toString() + "\n";
                    index++;
                }
                if(index == 0)
                    continue;
                message = new Message(null, Color.BLUE_BOLD + "GOD",
                        messageText);
                CH.send(new DataBox(cgs, message));
            }else if(citizenTeam.contains(CH)){
                if(playerCharacters.get(CH).getCharacterName() == CharacterName.Mayer){
                    // Finding Citizen Doctor!
                    for(ClientHandler CD : citizenTeam){
                        if(CD.getCharacter().getCharacterName() == CharacterName.Doctor_Citizen){
                            message = new Message(null, Color.BLUE_BOLD + "GOD",
                                    Color.GREEN_BOLD + CD.getNickname() + " is Citizen Doctor!");
                            CH.send(new DataBox(cgs, message));
                            break;
                        }
                    }
                }
                else if(playerCharacters.get(CH).getCharacterName() == CharacterName.Doctor_Citizen){
                    // Finding mayer
                    for(ClientHandler MR : citizenTeam){
                        if(MR.getCharacter().getCharacterName() == CharacterName.Mayer){
                            message = new Message(null, Color.BLUE_BOLD + "GOD",
                                    Color.GREEN_BOLD + MR.getNickname() + " is Mayer!");
                            CH.send(new DataBox(cgs, message));
                            break;
                        }
                    }
                }
            }
        }
    }

    // Getters
    public HashMap<ClientHandler, Character> getGameCharacters() {
        return playerCharacters;
    }
    public ArrayList<ClientHandler> getMafiaTeam() {
        return mafiaTeam;
    }
    public ArrayList<ClientHandler> getCitizenTeam() {
        return citizenTeam;
    }

}
