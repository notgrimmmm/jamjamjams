package io.rblgame;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.ChromaticAberrationEffect;
import com.crashinvaders.vfx.effects.FxaaEffect;
import com.crashinvaders.vfx.effects.VignettingEffect;
import com.crashinvaders.vfx.effects.BloomEffect;
import com.crashinvaders.vfx.effects.OldTvEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import games.rednblack.miniaudio.MiniAudio;
import io.rblgame.Chapters.Chp_Intro;
import io.rebelengine.EngineContext;
import io.rebelengine.Helpers.EntityFlags;
import io.rebelengine.RebelEngineBase;

/** First screen of the application. Displayed after the application is created. */
public class Scene_Intro implements Screen {
    private static final String MAP_PATH     = "main/wwD.bsp"; // change bsp
    private static final String TEXTURE_ROOT = "main/baseq3";

    private final RebelEngineBase ENGINE;
    private final MiniAudio AUDIO;
    
    private FrameBuffer sceneFbo;
    private TextureRegion sceneTextureRegion;
    private SpriteBatch spriteBatch = new SpriteBatch();
    
    
    /**
     * MONITOR TEXT ADVENTURE TESTING
     */
    
    private VfxManager VFX_MAIN;
    private FxaaEffect fxaa;
    private VignettingEffect vfxVignette;
    private ChromaticAberrationEffect vfxChromaticAberration;
    
    private VfxManager VFX_LCD;
    private VfxFrameBuffer lcdFbo;
    
    private SpriteBatch monitorSpriteBatch;
    
    private TextAdventureEngine TAEngine;
    private final int lcdfbosize = 700;

    private void initMonitorScreen() {
        lcdFbo = new VfxFrameBuffer(Pixmap.Format.RGBA8888);
        lcdFbo.initialize(lcdfbosize, lcdfbosize);

        monitorSpriteBatch = new SpriteBatch();
        
        VFX_LCD = new VfxManager(Pixmap.Format.RGBA8888);
        VFX_LCD.resize(lcdfbosize, lcdfbosize);
        
        BloomEffect lcdBloom = new BloomEffect();
        lcdBloom.setBloomIntensity(2f);
        OldTvEffect oldTV = new OldTvEffect();
        
        VFX_LCD.addEffect(lcdBloom);
        VFX_LCD.addEffect(oldTV);
        
        TAEngine = new TextAdventureEngine(17, 30, 0.01f);
    }
    
    public void renderMonitorContent(float delta) {
        TAEngine.update(delta);
        
        VFX_LCD.cleanUpBuffers();
        VFX_LCD.beginInputCapture();
        
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
        
        monitorSpriteBatch.getProjectionMatrix().setToOrtho2D(lcdfbosize, lcdfbosize, -lcdfbosize, -lcdfbosize);
        monitorSpriteBatch.begin();
        
        float offsetX = 24; // Shift right to get away from the clipped left edge
        float startY = 650;  // Adjust vertical starting position
        
        TAEngine.render(monitorSpriteBatch, offsetX, startY);
        
        monitorSpriteBatch.end();
        
        VFX_LCD.endInputCapture();
        VFX_LCD.applyEffects();
        
        VFX_LCD.renderToFbo(lcdFbo);
    }
    
    /**
     *  ------------------------------------
     */
    
    public Scene_Intro(Game game){
        AUDIO = new MiniAudio();
        ENGINE = new RebelEngineBase(AUDIO);
        
        initMonitorScreen();
        
        GameContext.TA_ENGINE = TAEngine;
        EngineContext.get().add_groupComponent("LGS", lcdFbo.getFbo()); // add for lookup
    }

    @Override
    public void show() {
        // --- Load map ---
        ENGINE.loadMap(MAP_PATH, TEXTURE_ROOT);
        Gdx.input.setInputProcessor(ENGINE.getInputProcessor());
        Gdx.input.setCursorCatched(true);
        createPostProcessing();
        
        // start game ta
        Chp_Intro.startChapter();
        for (Entity ent : ENGINE.ASHLEY.getEntities()){
            if ((ent.flags & EntityFlags.PLAYER) != 0){
                GameContext.basePlayerEntity = ent;
            }
        }
    }
    
    private void createPostProcessing(){
        VFX_MAIN = new VfxManager(Pixmap.Format.RGBA8888);

        vfxVignette = new VignettingEffect(false);
        vfxChromaticAberration = new ChromaticAberrationEffect(30);
        
        ensureSceneFbo(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
    }
    
    public void ensureSceneFbo(int w, int h) {
        GLFrameBuffer.FrameBufferBuilder frameBufferBuilder = new GLFrameBuffer.FrameBufferBuilder(w,h);
        frameBufferBuilder.addBasicColorTextureAttachment(Pixmap.Format.RGBA8888);
        frameBufferBuilder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24); // depth precision
        
        sceneFbo = frameBufferBuilder.build();// build custom fbo with better depth precision
        
        Texture tex = sceneFbo.getColorBufferTexture();
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sceneTextureRegion = new TextureRegion(tex);
        sceneTextureRegion.flip(false, true); // FBOs are upside-down
    }

    @Override
    public void render(float delta) {
        if (GameContext.requestExitGame){
            vfxChromaticAberration.dispose();
            vfxVignette.dispose();
            Gdx.app.exit(); // Safe exit 
        }
        
        sceneFbo.begin();
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL30.GL_DEPTH_TEST); // Ensure depth testing is active for 3D

        ENGINE.render(delta);
        sceneFbo.end();
        
        renderMonitorContent(delta);
        
        VFX_MAIN.cleanUpBuffers();
        VFX_MAIN.beginInputCapture();
        
        spriteBatch.begin();
        spriteBatch.draw(sceneTextureRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.end();

        VFX_MAIN.endInputCapture();
        VFX_MAIN.applyEffects();
        VFX_MAIN.renderToScreen();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        ENGINE.resize(width, height);
        VFX_MAIN.resize(width, height);
        VFX_LCD.resize(lcdfbosize, lcdfbosize);
        ensureSceneFbo(width, height);
    }

    @Override
    public void dispose() {
        System.out.println("[ SCREEN DISPOSE ]");
        if (AUDIO != null) AUDIO.dispose();
        if (ENGINE != null) ENGINE.dispose();
        if (VFX_MAIN != null) VFX_MAIN.dispose();
    }

    @Override
    public void pause() {}
    @Override
    public void hide() {}

    @Override
    public void resume() {
        ensureSceneFbo(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
    }

}