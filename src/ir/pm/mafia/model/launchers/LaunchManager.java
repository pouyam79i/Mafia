package ir.pm.mafia.model.launchers;

import ir.pm.mafia.view.console.Color;

/**
 * This class contains static methods to launch the game or tools for player!
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class LaunchManager implements Color {

    /**
     * This method runs main menu of game
     */
    public static void launchMain(){
        Launcher main = new MainMenuLauncher();
        main.launch();
    }

    /**
     * Lunches client-side runs the game for player!
     */
    public static void launchOnJoinMode(){
        Launcher player = new PlayerLauncher();
        player.launch();
    }

    /**
     * Lunches server-side and client-side. runs the game for admin player!
     */
    public static void launchOnCreateMode(){
        Launcher adminPlayer = new AdminLauncher();
        adminPlayer.launch();
    }

}
