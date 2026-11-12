/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.rblgame;

import com.badlogic.ashley.core.Entity;

/**
 *
 */
public class GameContext {
    public static TextAdventureEngine TA_ENGINE;
    public static boolean MOUSELOCK = false;
    public static Entity basePlayerEntity;
    
    public static boolean gameEnded = false;
    public static boolean requestExitGame = false;
}

