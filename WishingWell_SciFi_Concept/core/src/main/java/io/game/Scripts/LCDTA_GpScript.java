/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.game.Scripts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.game.Interactables.ScriptedB;
import io.rblgame.GameContext;
import io.game.Behaviors.Strategy_LCDCamera;
import io.rebelengine.Components.CameraComponent;
import io.rebelengine.Components.EntityScript;
import io.rebelengine.Components.InputControlComponent;
import io.rebelengine.Components.MouseLookComponent;
import io.rebelengine.EngineContext;
import io.rebelengine.Helpers.Info_Entity;
import io.rebelengine.Helpers.InputFocus;
import io.rebelengine.Instances.UI_TextInteraction;

/**
 *
 */
public class LCDTA_GpScript implements EntityScript{
    private ScriptedB scriptable_screen;
    private FrameBuffer lcdFBO = null;
    private String meshkey = "";
    private Vector3 targetCamPos;
   
    private boolean INIT_SET_OF_ENT = false;
    private UI_TextInteraction endGameText = new UI_TextInteraction();
    
    @Override
    public void set_components(Array<Object> array) {
        for (Object obj : array){
            if (obj instanceof ScriptedB brushScripted){
                scriptable_screen = brushScripted;
                meshkey = scriptable_screen.instanceComponent.instance.meshKey;
            }
            if (obj instanceof Info_Entity.Location loc){
                targetCamPos = loc.vec;
            }
            if (obj instanceof UI_TextInteraction tui){
                endGameText = tui;
            }
        }
        
    }

    @Override
    public void update_fixed(float f) {
        if (GameContext.gameEnded){
            if (Gdx.input.isKeyPressed(Input.Keys.F)){
                GameContext.requestExitGame = true;
            }
        }
    }

    @Override
    public void execute(Object o) {
        endGameText.visible = true;
        for (Object obj : EngineContext.get().get_groupComponentsArray("LGS")){
            if (obj instanceof FrameBuffer fbo){
                lcdFBO = fbo;
            }
        }
        
        if (meshkey.isEmpty() || lcdFBO == null) return;
        
        EngineContext.get().getDYNRenderer().overrideTextureForMesh(meshkey, lcdFBO.getColorBufferTexture(), false);
        
        if (!INIT_SET_OF_ENT){           
            setFirstRealEntityControl();
            INIT_SET_OF_ENT = true;
        }
    }
    
    private void setFirstRealEntityControl(){
        MouseLookComponent mouseLook = new MouseLookComponent();
        InputControlComponent inputControl = new InputControlComponent();
        
        PerspectiveCamera camera = new PerspectiveCamera(90, 1200, 1200);
        camera.position.set(targetCamPos.sub(0, 0, 10));
        camera.near = 0.001f;
        camera.far = 200f;

        CameraComponent cameraComponent = new CameraComponent();
        cameraComponent.camera = camera;
        
        cameraComponent.strategyLogic = new Strategy_LCDCamera(scriptable_screen.transform, cameraComponent, mouseLook);
        cameraComponent.active = true;
        
        scriptable_screen.add(mouseLook);
        scriptable_screen.add(inputControl);
        scriptable_screen.add(cameraComponent);
        
        EngineContext.get().set_activeCamera(camera);
        InputFocus.set(scriptable_screen);
        camera.update();
        
        InputFocus.setLocked(true);
        EngineContext.get().getMultiplexer().addProcessor(GameContext.TA_ENGINE);
        GameContext.MOUSELOCK = true;
    }

    @Override
    public void execute_off() {
        endGameText.visible = false;
    }
    
}
