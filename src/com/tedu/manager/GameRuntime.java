package com.tedu.manager;

import java.util.List;

import com.tedu.element.ElementObj;
import com.tedu.element.MsPlayer;

public class GameRuntime {
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    private static final GameRuntime INSTANCE = new GameRuntime();

    private double cameraX;
    private int stageWidth;
    private int stageHeight;
    private int score;
    private boolean stageClear;
    private boolean gameOver;
    private boolean restartRequested;
    private boolean debugCollision;

    private GameRuntime() {
        reset();
    }

    public static GameRuntime getInstance() {
        return INSTANCE;
    }

    public void reset() {
        this.cameraX = 0;
        this.stageWidth = 3600;
        this.stageHeight = SCREEN_HEIGHT;
        this.score = 0;
        this.stageClear = false;
        this.gameOver = false;
        this.restartRequested = false;
        this.debugCollision = false;
    }

    public MsPlayer findPlayer() {
        List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
        if (players.isEmpty()) {
            return null;
        }
        ElementObj player = players.getFirst();
        if (player instanceof MsPlayer msPlayer) {
            return msPlayer;
        }
        return null;
    }

    public double getCameraX() {
        return cameraX;
    }

    public void setCameraX(double cameraX) {
        this.cameraX = cameraX;
    }

    public int getStageWidth() {
        return stageWidth;
    }

    public void setStageWidth(int stageWidth) {
        this.stageWidth = stageWidth;
    }

    public int getStageHeight() {
        return stageHeight;
    }

    public void setStageHeight(int stageHeight) {
        this.stageHeight = stageHeight;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int delta) {
        this.score += delta;
    }

    public boolean isStageClear() {
        return stageClear;
    }

    public void markStageClear() {
        this.stageClear = true;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void markGameOver() {
        this.gameOver = true;
    }

    public void requestRestart() {
        this.restartRequested = true;
    }

    public boolean consumeRestartRequest() {
        if (!restartRequested) {
            return false;
        }
        restartRequested = false;
        return true;
    }

    public boolean isDebugCollision() {
        return debugCollision;
    }
}
