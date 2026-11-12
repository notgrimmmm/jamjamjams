/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.rblgame.Chapters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Timer;
import io.rblgame.GameContext;
import io.rblgame.TextAdventureEngine;
import io.rebelengine.Instances.SFX;

/**
 *
 */
public class Chp_MainMenu {
    public static void startChapter(){
        loadScene(new MainMenu());
    }
    
    public static void loadScene(TextAdventureEngine.CommandListener listener){
        GameContext.TA_ENGINE.setCommandListener(listener);
    }
    
    public static class MainMenu implements TextAdventureEngine.CommandListener {
        Music mmm;
        public MainMenu(){
            GameContext.TA_ENGINE.clearHistory();
            GameContext.TA_ENGINE.print("""
                                                                                                        +@@           --
                                                                                                            .=+#@@@@@@@@@@%@@@#=+#@@*=.
                                                                                                                .-:+#@@@@%@@@%-
                                                                                                                        :*@@@@*-
                                                                                                                    +*%@%#%-
                                                                        -=% W I S H ING  *+##+#%+#*-=-                        =+*++@%-
                                                                         #%%-#*%*%%*   WELL+**+==                         +@#   -=.
                                              :+#%@@@@@@@+                                                          +%.
                                               .    -@%@@*.:=                                                   ##
                                                     -%@@@@@=:
                                                     -#%@@@@@%:
                                                           .:*@@@:
                                        
                                        """);
            
            mmm = Gdx.audio.newMusic(Gdx.files.internal("sounds/music_mainmenu.mp3"));
            mmm.play();
        }

        @Override
        public int onCommand(String command) {
            if (command.equalsIgnoreCase(".")){
                GameContext.TA_ENGINE.pauseFor(0.4f);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        loadScene(new MainMenu());
                    }
                }, 0.4f);
                return 0;
            }
            
            if (command.equalsIgnoreCase("start")){
                GameContext.TA_ENGINE.pauseFor(2f);
                mmm.stop();
                new SFX("sfx_terminalswitch_a").play();
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        Chp_Intro.startChapter();
                    }
                }, 2f);
                return 0;
            }
            if (command.equalsIgnoreCase("help")){
                GameContext.TA_ENGINE.print("Helpful Commands: \n"
                        + " [ ENTER ] :    Skip Line Animation  \n");
                return 0;
            }
            return 1;
        }
        
    }
}
