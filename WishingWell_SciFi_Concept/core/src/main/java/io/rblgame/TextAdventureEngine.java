/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.rblgame;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.rebelengine.Instances.SFX;
import java.util.Set;

public class TextAdventureEngine implements InputProcessor {
    
    private final Array<String> history;
    private final Array<String> pendingQueue; // Messages waiting to be typed out
    private final int maxLines;
    private final float lineSpacing;
    private final StringBuilder currentInput;
    
    // Typewriter state variables
    private String currentTypingLine = "";
    private int charIndex = 0;
    private float timeAccumulator = 0f;
    private float typingSpeed; // Seconds per character
    private float defaultTypeSpeed;
    private float pauseTimer = 0f;
    
    private SFX sfxline_print = new SFX("sfx_terminalline_init");
    private BitmapFont ta_font;
    
    private static class Pause {
        final int afterLine;     // start this pause once this many lines have been started
        final float seconds;
        Pause(int afterLine, float seconds) { this.afterLine = afterLine; this.seconds = seconds; }
    }

    private final Array<Pause> pauses = new Array<>();
    private int linesQueued = 0;    // total lines ever added
    private int linesStarted = 0;   // total lines pulled from the queue so far
   
    private CommandListener commandListener;
    public interface CommandListener {
        final Set<String> NAV = Set.of("go", "walk");
        final Set<String> DIR_F = Set.of("in", "up", "inside", "forward");
        final Set<String> DIR_B = Set.of("back", "down", "backward");
        final Set<String> DIR_L = Set.of("left");
        final Set<String> DIR_R = Set.of("right");
        final Set<String> VIEW = Set.of("look", "inspect");
        final Set<String> USE = Set.of("use", "try", "do");
        int onCommand(String command);
    }

    public TextAdventureEngine(int maxLines, float lineSpacing, float typingSpeed) {
        this.history = new Array<>();
        this.pendingQueue = new Array<>();
        this.maxLines = maxLines;
        this.lineSpacing = lineSpacing;
        this.typingSpeed = typingSpeed;
        this.defaultTypeSpeed = typingSpeed;
        this.currentInput = new StringBuilder();
        
        ta_font = new BitmapFont();
        ta_font.getData().markupEnabled = true;
    }

    public void setCommandListener(CommandListener listener) {
        this.commandListener = listener;
    }
    
    public void resetCommandListener(){
        this.commandListener = null;
    }

    /** Adds a message to the typewriter queue. Supports multi-line text. */
    public void print(String text) {
        sfxline_print.setVolume(3f);
        sfxline_print.setPitch(MathUtils.random(0.94f, 0.98f));
        sfxline_print.play();
        
        for (String line : text.split("\n")) {
            pendingQueue.add(line);
            linesQueued++;
        }
    }
    
    public void pauseFor(float seconds){
        pauses.add(new Pause(linesQueued, seconds));
    }

    /** Instantly finishes typing the current line or skips the queue entirely. */
    public void skipTyping() {
        if (!currentTypingLine.isEmpty()) {
            // Push currently animating line fully to history
            addLineToHistory(currentTypingLine);
            currentTypingLine = "";
            charIndex = 0;
        }
        // Flush remaining queue instantly
        while (pendingQueue.size > 0) {
            addLineToHistory(pendingQueue.removeIndex(0));
            linesStarted++;
        }
        pauses.clear();
        pauseTimer = 0f;
    }

    private void addLineToHistory(String line) {
        history.add(line);
        if (history.size > maxLines) {
            history.removeIndex(0); // Scroll oldest line out
        }
    }
    
    public void clearHistory() {
        history.clear();
        pendingQueue.clear();
        pauses.clear();
        currentTypingLine = "";
        charIndex = 0;
        timeAccumulator = 0f;
        pauseTimer = 0f;
        linesStarted = linesQueued;
    }

    /** Updates the typewriter animation. Call this inside your render loop. */
    public void update(float delta) {
        if (pauseTimer > 0) {
            pauseTimer -= delta;
            return;
        }

        if (currentTypingLine.isEmpty()) {
            // A pause is due once all the lines before it have been started and finished
            if (pauses.size > 0 && pauses.first().afterLine == linesStarted) {
                pauseTimer = pauses.removeIndex(0).seconds;
                return;
            }

            // Otherwise pull the next line
            if (pendingQueue.size > 0) {
                SFX sfx = new SFX("sfx_terminalline_0");
                sfx.setVolume(2.5f);
                sfx.setPitch(MathUtils.random(0.9f, 1f));
                sfx.play();

                currentTypingLine = pendingQueue.removeIndex(0);
                linesStarted++;
                charIndex = 0;
                timeAccumulator = 0f;
            }
        }

        // If we are actively typing a line, progress character by character
        if (!currentTypingLine.isEmpty()) {
            timeAccumulator += delta;
            if (timeAccumulator >= typingSpeed) {
                timeAccumulator = 0f;
                charIndex++;

                if (charIndex >= currentTypingLine.length()) {
                    addLineToHistory(currentTypingLine);
                    currentTypingLine = "";
                    charIndex = 0;
                }
            }
        }
    }

    /** Renders the history queue, currently typing line, and active prompt. */
    public void render(SpriteBatch batch, float startX, float startY) {
        float currentY = startY;

        // Render completed history lines
        for (int i = 0; i < history.size; i++) {
            ta_font.draw(batch, history.get(i), startX, currentY);
            currentY -= lineSpacing;
        }

        // Render the line currently being typed out letter-by-letter
        if (!currentTypingLine.isEmpty()) {
            String partialLine = currentTypingLine.substring(0, charIndex);
            ta_font.draw(batch, partialLine, startX, currentY);
            currentY -= lineSpacing;
        }

        // Render current player input line with a cursor
        String promptText = "[#0fe000] > [] " + currentInput.toString() + "_";
        ta_font.draw(batch, promptText, startX, currentY);
    }

    // --- InputProcessor Implementation ---

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER) {
            if (!currentTypingLine.isEmpty() || pendingQueue.size > 0 || pauseTimer > 0 || pauses.size > 0) {
                skipTyping();
                return true;
            }

            String command = currentInput.toString().trim();
            if (commandListener != null && !command.isEmpty() && commandListener.onCommand(command) == 0) {
                addLineToHistory("[#eb8c34] > " + command + "[]");
            }
            
            currentInput.setLength(0); // Reset input buffer
        } else if (keycode == Input.Keys.BACKSPACE && currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        if (pauseTimer > 0 || pauses.size > 0) return true; // no typing on pause
        // Accept printable ASCII characters
        if (character >= 32 && character < 127) {
            currentInput.append(character);
        }
        return true;
    }

    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
