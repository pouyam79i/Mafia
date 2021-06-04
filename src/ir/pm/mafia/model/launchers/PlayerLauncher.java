package ir.pm.mafia.model.launchers;

import ir.pm.mafia.controller.client.Client;
import ir.pm.mafia.model.player.Player;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.view.console.Color;
import ir.pm.mafia.view.console.Console;
import ir.pm.mafia.view.ui.interfaces.ChatRoomUI;

import java.util.Locale;

/**
 * This launcher help you to build a connection to server,
 * then runs the game
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class PlayerLauncher implements Launcher, Color {

    /**
     * Lunches client-side. runs the game for player!
     */
    @Override
    public void launch() {

        // Allocation memory
        Console console = Console.getConsole();
        String input = null;
        console.println(GREEN + "Lunching Game ...");

        // Building a player
        Player player = new Player();

        // Setting player name
        player.setNickname(null);

        // Building Client, connection to server
        boolean breakLoop = false;
        Client client = null;
        console.println(YELLOW + "Inter IP:Port of server:");
        console.println(PURPLE + "  -Like: 127.0.0.1:8000");
        console.println(PURPLE + "  -If you want to leave enter '" + RED + "exit" + PURPLE + "'");
        while (!breakLoop){
            input = console.readConsole();
            // check if exit is called!
            if(input.toLowerCase(Locale.ROOT).equals("exit")){
                console.println(YELLOW + "Closing launcher");
                return;
            }
            try {
                String ip = input.split(":")[0];
                int port = Integer.parseInt(input.split(":")[1]);
                client = new Client(ip, port, player.getSendBox(), player.getReceiveBox(), null);
                console.println(GREEN + "Found Server");
                console.println(YELLOW + "Running connection...");
                client.start();
                breakLoop = true;
            }catch (Exception e){
                Logger.error("Failed to build client" + e.getMessage(),
                        LogLevel.ClientFailed,
                        "Launcher");
                console.println(RED + "Invalid IP:Port!");
            }
        }
        player.setToken(client.getMyToken());
        console.println(GREEN + "You joined server successfully!");

        //  Test area *******************************************************
        try {
            ChatRoomUI chatRoomUI = new ChatRoomUI(player.getSendBox(), player.getReceiveBox(),
                    player.getToken(), player.getNickname(), BLUE + "Lobby" + RESET);
            chatRoomUI.start();
        } catch (Exception ignored) {}

        // Wait 3 min
        try {
            Thread.sleep(180000);
        } catch (InterruptedException ignored) {}
        //  End of test area *******************************************************


        // close area
        client.shutdown();

    }

}
