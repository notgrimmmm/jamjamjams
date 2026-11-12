/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.rblgame.Chapters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import io.game.Behaviors.Strategy_LCDCamera;
import io.rblgame.GameContext;
import io.rblgame.TextAdventureEngine;
import io.rebelengine.Components.CameraComponent;
import io.rebelengine.Components.StrategyLogic;
import io.rebelengine.Components.TransformComponent;
import io.rebelengine.EngineContext;
import io.rebelengine.Helpers.InputFocus;
import io.rebelengine.Instances.SFX;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class Chp_Intro {
    
    public static void startChapter(){
        loadScene(new Scene_Corridor());
    }
    
    public static void loadScene(TextAdventureEngine.CommandListener listener){
        GameContext.TA_ENGINE.setCommandListener(listener);
    }
    
    private static boolean hasAny(Set<String> words, Set<String> options) {
        return !Collections.disjoint(words, options);
    }
    
    private static class Scene_Corridor implements TextAdventureEngine.CommandListener{

        public Scene_Corridor(){
            GameContext.TA_ENGINE.clearHistory();
            GameContext.TA_ENGINE.print("How long has it been since I was last here? \n"
                + "The entire place looks like it's being devoured. It's crumbling \n"
                + "down, caving in on itself. Water is starting to seep through, I might \n"
                + "be the very last person to come here.");
            GameContext.TA_ENGINE.print("The corridor has fractured walls. Flickering lights reaveal parts of the facility. \n"
                    + "The floor is a bit uneven, but still walkable thankfully.");
        }
        
        @Override
        public int onCommand(String command) {
            Set<String> words = new HashSet<>(Arrays.asList(command.toLowerCase().split("\\s+")));
            
            if (hasAny(words, NAV) && hasAny(words, DIR_F)) {
                GameContext.TA_ENGINE.print("I make my way into the familiar yet distant hallway.");
                GameContext.TA_ENGINE.pauseFor(2f);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        loadScene(new Scene_Entrance());
                    }
                }, 2f);
                return 0;
            }
            if (hasAny(words, VIEW)){
                GameContext.TA_ENGINE.print("The dark water is pooling below my feet. I can feel it.");
                return 0;
            }
            
            return 1;
        }
        
    }
    
    private static class Scene_Entrance implements TextAdventureEngine.CommandListener{
        private final String KEY = "4208";
        private boolean keyWasHinted = false;
        private boolean doorOpen = false;
        //private SFX sfx_flicker = new SFX("flicker");
        
        public Scene_Entrance(){
            GameContext.TA_ENGINE.clearHistory();
            GameContext.TA_ENGINE.print("I can hear the barely working light above me as I stop infront of the door.\n"
                + "It's the entrance. \n"
                + "Surprisingly it's locked with the keypad. It's been so long. But I think I remember the numbers. \n"
                + "It was four digits...");
            
            //sfx_flicker.play();;
        }
        
        @Override
        public int onCommand(String command) {
            Set<String> words = new HashSet<>(Arrays.asList(command.toLowerCase().split("\\s+")));
            
            if (hasAny(words, VIEW)){
                GameContext.TA_ENGINE.print("The keypad is worn, almost destroyed. I wouldn't be surprised if it didn't work.\n"
                        + "Four digits are faded: 8, 4, 2, 0");
                keyWasHinted = true;
                return 0;
            }
            
            if (hasAny(words, USE) && words.contains(KEY) && !doorOpen){
                GameContext.TA_ENGINE.print("The mechanism churns. It struggles to open. \n"
                        + "My presence echos through the entire facility.\n"
                        + "I'm here once again.");
                
                new SFX("sfx_heavydooropen").play();
                StrategyLogic strat = InputFocus.get().getComponent(CameraComponent.class).strategyLogic;
                if (strat instanceof Strategy_LCDCamera stratL){
                    stratL.shake(1f, 5f);
                }
                
                GameContext.MOUSELOCK = false; // enable looking
                doorOpen = true;
                return 0;
            }
            
            if (hasAny(words, NAV) && hasAny(words, DIR_F) && doorOpen){
                GameContext.TA_ENGINE.print("I walk inside.");
                GameContext.TA_ENGINE.pauseFor(2f);
                GameContext.TA_ENGINE.print("hello ?");

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        loadScene(new Scene_Inside());
                    }
                }, 2f);
                
                return 0;
            }
            
            if (hasAny(words, NAV) && hasAny(words, DIR_B)){
                GameContext.TA_ENGINE.print("I walk back into the dark corridor behind me.");
                GameContext.TA_ENGINE.pauseFor(2f);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        loadScene(new Scene_Corridor());
                    }
                }, 2f);
                return 0;
            }
            
            if (keyWasHinted && !doorOpen){
                if (words.contains(KEY)){
                    GameContext.TA_ENGINE.print("Worth a try.");
                }else{
                    GameContext.TA_ENGINE.print("I dont think that was it.");
                }
                return 0;
            }
            
            return 1;
        }
    }
    
    private static class Scene_Inside implements TextAdventureEngine.CommandListener{
        public Scene_Inside(){
            GameContext.TA_ENGINE.clearHistory();
            GameContext.TA_ENGINE.print("I make my way into the facility I once remembered. It all seems fuzzy now.\n"
                    + "A metal terminal sits in front of me, its display dead, its surface covered in a thin layer of dust.\n"
                    + "This is not what I came here for.");
        }
        @Override
        public int onCommand(String command) {
            Set<String> words = new HashSet<>(Arrays.asList(command.toLowerCase().split("\\s+")));
            
            
            if (hasAny(words, VIEW)){
                GameContext.TA_ENGINE.print("Two corridors extend from the room, one to the left and one to the right.\n "
                        + "I need to find the [#f70000]CORE[]. It should be somewhere nearby.");
                return 0;
            }
            
            if (words.contains("terminal") || words.contains("computer")){
                GameContext.TA_ENGINE.print("It's dead.");
                return 0;
            }
            
            if (hasAny(words, NAV) && hasAny(words, DIR_L)){
                GameContext.TA_ENGINE.pauseFor(1f);
                
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        loadScene(new Scene_InsideStorage());
                    }
                }, 1f);
                
                return 0;
            }
            
            if (hasAny(words, NAV) && hasAny(words, DIR_R)){
                GameContext.TA_ENGINE.pauseFor(1f);
                
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        loadScene(new Scene_InsideMaintenance());
                    }
                }, 1f);
                
                return 0;
            }
            
            return 1;
        }
    }
    
    private static class Scene_InsideStorage implements TextAdventureEngine.CommandListener{
        
        public Scene_InsideStorage(){
            GameContext.TA_ENGINE.clearHistory();
            GameContext.TA_ENGINE.print("What used to be a storage room lined with metal shelves.\n"
                    + "Empty instrument cases sit open on the floor.\n"
                    + "A few surgical tools hang from a rack, swaying slightly, though the air is still.");
        }
        
        @Override
        public int onCommand(String command) {
            Set<String> words = new HashSet<>(Arrays.asList(command.toLowerCase().split("\\s+")));
            
            if (hasAny(words, VIEW)){
                GameContext.TA_ENGINE.print("A small metal box, engraved with symbols is in the corner.\n"
                        + "I found it, it's the [#f70000]CORE[], with this I can-");
                GameContext.TA_ENGINE.pauseFor(1.5f);
                GameContext.TA_ENGINE.print("ITS HERE");
                GameContext.TA_ENGINE.print("ITS HERE");
                GameContext.TA_ENGINE.print("ITS HERE");
                GameContext.TA_ENGINE.print("ITS HERE");
                GameContext.TA_ENGINE.print("ITS HERE");
                GameContext.TA_ENGINE.print("ITS HERE");
                EngineContext.get().getMultiplexer().removeProcessor(GameContext.TA_ENGINE);
                
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        if (GameContext.basePlayerEntity != null){
                            InputFocus.set(GameContext.basePlayerEntity);
                            EngineContext.get().set_activeCamera(InputFocus.get().getComponent(CameraComponent.class).camera);
                            InputFocus.get().getComponent(TransformComponent.class).currentYaw += 180f;// hard code rotation to make player face monitor
                            
                            GameContext.gameEnded = true;
                            Gdx.audio.newMusic(Gdx.files.internal("sounds/music_final.wav")).play();
                        }
                    }
                }, 2.5f);
                
                
                return 0;
            }
            
            return 1;
        }
        
    }
    
    private static class Scene_InsideMaintenance implements TextAdventureEngine.CommandListener{
        
        public Scene_InsideMaintenance(){
            GameContext.TA_ENGINE.clearHistory();
            GameContext.TA_ENGINE.print("The room is narrow, crowded with dismantled machinery and carts of unfamiliar equipment.\n"
                    + " A ventilation fan turns slowly overhead, clicking with every rotation.");
        }

        @Override
        public int onCommand(String command) {
            Set<String> words = new HashSet<>(Arrays.asList(command.toLowerCase().split("\\s+")));
            
            if (hasAny(words, VIEW)){
                GameContext.TA_ENGINE.print("Theres nothing here");
                GameContext.TA_ENGINE.print("...");
                GameContext.TA_ENGINE.pauseFor(1.5f);
                GameContext.TA_ENGINE.print("Something is in this facility");
                return 0;
            }
            
            if (hasAny(words, NAV) && hasAny(words, DIR_B)){
                loadScene(new Scene_Inside());
                return 0;
            }
            
            return 1;
        }
        
    }
        
}
