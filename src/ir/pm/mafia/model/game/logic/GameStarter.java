package ir.pm.mafia.model.game.logic;

import ir.pm.mafia.controller.server.ClientHandler;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

public class GameStarter {

    private ArrayList<ClientHandler> clientHandlers;
    private HashMap<Character, ClientHandler> gameCharacters;
    private SecureRandom random;

    public GameStarter(ArrayList<ClientHandler> clientHandlers) throws Exception {
        if(clientHandlers == null)
            throw new Exception("Null input");
        if(clientHandlers.size() < 6)
            throw new Exception("Not enough players!");
        this.clientHandlers = clientHandlers;
        gameCharacters = new HashMap<Character, ClientHandler>();
        random = new SecureRandom();
    }


    public void setCharacter(){
        if(gameCharacters.size() > 0)
            return;
        HashMap<Character, ClientHandler> gameCharacters = new HashMap<Character, ClientHandler>();
        ArrayList<ClientHandler> existingClients = new ArrayList<ClientHandler>(this.clientHandlers);
        int remainingPlayers = existingClients.size();
        int mafia = (int) Math.floor(remainingPlayers/3);
        int citizen = remainingPlayers - mafia;
        int indexOfCurrent;
        while (remainingPlayers > 0){
            indexOfCurrent = random.nextInt(remainingPlayers);
            ClientHandler clientHandler = existingClients.get(indexOfCurrent);
            // building characters


            existingClients.remove(indexOfCurrent);
            remainingPlayers = existingClients.size();
        }
        this.gameCharacters = gameCharacters;
    }

    public HashMap<Character, ClientHandler> getGameCharacters() {
        return gameCharacters;
    }

}
