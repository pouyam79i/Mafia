package ir.pm.mafia.model.launchers;

import ir.pm.mafia.view.menu.menus.Main;

/**
 * This launcher lunches main menu of game
 * @author Pouya Mohammadi - CE@AUT - Uni ID:9829039
 * @version 1.0
 */
public class MainMenuLauncher implements Launcher{

    /**
     * This method runs main menu of game
     */
    @Override
    public void launch() {
        // Read player username and config file if needed
            // Complete this part .**************************************************************
        Main mainMenu = new Main(null);
        String input = null;
        while (true){
            mainMenu.display();
            input = mainMenu.listen();
            if(input.equals("1")){
                // Launching player in server-side. you will be admin in this mode
                LaunchManager.launchOnCreateMode();
            }
            else if(input.equals("2")){
                // Launching player in client-side only
                LaunchManager.launchOnJoinMode();
            }
            else if(input.equals("3")){
                // Launch setting menu
                    // Complete this part .**************************************************************
            }
            else if(input.equals("4")){
                break;
            }
        }
    }

}
