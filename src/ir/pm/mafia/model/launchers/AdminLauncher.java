package ir.pm.mafia.model.launchers;

import ir.pm.mafia.controller.client.Client;
import ir.pm.mafia.controller.server.Server;
import ir.pm.mafia.model.loops.gameloop.GameLoop;
import ir.pm.mafia.model.loops.godloop.GodLoop;
import ir.pm.mafia.model.player.Player;
import ir.pm.mafia.model.utils.logger.LogLevel;
import ir.pm.mafia.model.utils.logger.Logger;
import ir.pm.mafia.view.console.Color;
import ir.pm.mafia.view.console.Console;

import java.util.Locale;

/**
 * This Launcher is used to build a host for game,
 * and sets your account as admin of host,
 * then run the game for you!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0.2
 */
public class AdminLauncher implements Launcher, Color {

    /**
     * It launches a server and set you as admin player!
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
        try {
            server.setAdmin(player);
        }catch (Exception e){
            console.println(RED + "Failed to set admin!");
            console.println(RED + "Launcher failed!");
            server.shutdown();
            return;
        }

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
                        player.getToken(),
                        player.getNickname());
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
        if(client == null){
            console.println(RED + "Null client!");
            console.println(RED + "Launcher failed!");
            return;
        }
        client.start();
        console.println(GREEN + "You joined the host as admin :)");

        // Running god loop
        GodLoop godLoop;
        try {
            godLoop = new GodLoop(server, player.getToken());
            godLoop.start();
        } catch (Exception e) {
            console.println(RED + "God loop failed!");
            console.println(RED + "Launcher failed!");
            return;
        }

        // Building  game loop
        GameLoop gameLoop = null;
        try {
            gameLoop = new GameLoop(player);
            gameLoop.start();
        } catch (Exception e) {
            Logger.error("Player launcher failed!" + e.getMessage(),
                    LogLevel.GameInterrupted, "GameLoop");
            console.println(RED + "Launcher failed!");
            client.shutdown();
            return;
        }

        // On holed
        while (!godLoop.isFinished()) Thread.onSpinWait();
        while (!server.isFinished())
        // close area - shutting down all threads!
        godLoop.shutdown();
        server.shutdown();
        client.shutdown();

    }

}
