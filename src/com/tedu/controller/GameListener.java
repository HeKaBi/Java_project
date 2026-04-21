package com.tedu.controller;

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameRuntime;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameListener implements KeyListener {
    private final ElementManager em = ElementManager.getManager();
    private final Set<Integer> pressedKeys = new HashSet<>();

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        handleKeyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        handleKeyReleased(e.getKeyCode());
    }

    public void handleKeyPressed(int key) {
        if (GameRuntime.waitingStart) {
            if (!GameRuntime.loadingStart && key == KeyEvent.VK_ENTER) {
                GameRuntime.startRequested = true;
            }
            return;
        }
        if (key == KeyEvent.VK_R && GameRuntime.waitingRestart) {
            GameRuntime.restartRequested = true;
            return;
        }
        if (GameRuntime.waitingRestart || pressedKeys.contains(key)) {
            return;
        }
        pressedKeys.add(key);
        List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        for (ElementObj obj : plays) {
            obj.keyClick(true, key);
        }
    }

    public void handleKeyReleased(int key) {
        if (GameRuntime.waitingStart) {
            return;
        }
        if (!pressedKeys.remove(key)) {
            return;
        }
        List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        for (ElementObj obj : plays) {
            obj.keyClick(false, key);
        }
    }
}
