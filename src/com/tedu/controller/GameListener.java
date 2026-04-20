package com.tedu.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameRuntime;

public class GameListener implements KeyListener {
    private final ElementManager elementManager = ElementManager.getManager();
    private final GameRuntime runtime = GameRuntime.getInstance();
    private final Set<Integer> pressedKeys = new HashSet<>();

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (pressedKeys.contains(keyCode)) {
            return;
        }
        pressedKeys.add(keyCode);

        if (keyCode == KeyEvent.VK_R && (runtime.isGameOver() || runtime.isStageClear())) {
            runtime.requestRestart();
        }

        List<ElementObj> players = elementManager.getElementsByKey(GameElement.PLAY);
        for (ElementObj obj : players) {
            obj.keyClick(true, keyCode);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (!pressedKeys.contains(keyCode)) {
            return;
        }
        pressedKeys.remove(keyCode);

        List<ElementObj> players = elementManager.getElementsByKey(GameElement.PLAY);
        for (ElementObj obj : players) {
            obj.keyClick(false, keyCode);
        }
    }
}
