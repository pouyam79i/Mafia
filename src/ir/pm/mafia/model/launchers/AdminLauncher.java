package ir.pm.mafia.model.launchers;

import ir.pm.mafia.controller.client.Client;
import ir.pm.mafia.controller.server.Server;
import ir.pm.mafia.model.loops.godloop.GodLoop;
import ir.pm.mafia.model.player.Player;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.view.console.Color;
import ir.pm.mafia.view.console.Console;
import ir.pm.mafia.view.ui.interfaces.ChatRoomUI;

import java.util.Locale;

/**
 * This Launcher is used to build a host for game,
 * and sets your account as admin of host,
 * then run the game for you!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class AdminLauncher implements Launcher, Color {

    /**
     * it launches a host and set you as admin player!
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

        // Building server!
        Server server = null;
        console.println(YELLOW + "Building host ...\n");
        console.println(YELLOW + "Please enter a port number:");
        console.println(PURPLE + "  -Like: 8000");
        console.println(PURPLE + "  -If you want to leave enter '" + RED + "exit" + PURPLE + "'");
        while (true){
            try {
                input = console.readConsole();
                if(input.toLowerCase(Locale.ROOT).equals("exit")){
                    console.println("Closing Launcher...");
                    return;
                }
                server = new Server(Integer.parseInt(input));
                break;
            }catch (Exception e){
                Logger.error("Failed to build server!" + e.getMessage(),
                        LogLevel.ServerFailed, "AdminLauncher");
                console.println(RED + "Invalid port!");
            }
        }
        // setting player as server admin!
        server.setAdmin(player);
        // Running server!
        server.start();

        // Building client
        Client client = null;
        for(int i = 0; i < 10; i++){
            try {
                Thread.sleep(100);
                client = new Client("127.0.0.1",
                        server.getPort(),
                        player.getSendBox(),
                        player.getReceiveBox(),
                        player.getToken());
                break;
            } catch (Exception e) {
                if(i == 9){
                    Logger.error("Launcher failed, cannot join server",
                            LogLevel.ClientFailed,
                            "AdminLauncher");
                    console.println(RED + "Launcher failed!");
                    server.shutdown();
                    return;
                }
            }
        }
        // Running client
        client.start();
        console.println(GREEN + "You joined the host as admin :)");

        // Running god loop
        GodLoop godLoop;
        try {
            godLoop = new GodLoop(server.getConnectionBox(), player.getToken());
            godLoop.start();
        } catch (Exception e) {
            console.println(RED + "Launcher failed!");
            return;
        }

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
        godLoop.shutdown();
        server.shutdown();
        client.shutdown();

    }

}
