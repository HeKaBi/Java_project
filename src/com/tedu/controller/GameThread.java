package com.tedu.controller;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import com.tedu.element.ElementObj;
import com.tedu.element.EnemySoldier;
import com.tedu.element.MsPlayer;
import com.tedu.element.PlayerBullet;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;

public class GameThread extends Thread {
    private final ElementManager elementManager;
    private final GameRuntime runtime;
    private long gameTime = 0L;

    public GameThread() {
        this.elementManager = ElementManager.getManager();
        this.runtime = GameRuntime.getInstance();
        this.setName("game-main-thread");
    }

    @Override
    public void run() {
        gameLoad();
        while (true) {
            if (runtime.consumeRestartRequest()) {
                gameLoad();
            }
            gameTick();
            try {
                Thread.sleep(16L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void gameLoad() {
        runtime.reset();
        elementManager.reset();
        GameLoad.loadImg();
        GameLoad.loadObj();
        GameLoad.loadStage(1);
        gameTime = 0L;
        updateCamera();
    }

    private void gameTick() {
        if (!runtime.isGameOver() && !runtime.isStageClear()) {
            Map<GameElement, List<ElementObj>> all = elementManager.getGameElements();
            moveAndUpdate(all, gameTime++);
            handleCollisions();
            checkStageState();
        }
        updateCamera();
    }

    private void moveAndUpdate(Map<GameElement, List<ElementObj>> all, long time) {
        for (GameElement element : GameElement.values()) {
            List<ElementObj> list = all.get(element);
            for (int i = list.size() - 1; i >= 0; i--) {
                ElementObj obj = list.get(i);
                if (!obj.isLive()) {
                    obj.die();
                    list.remove(i);
                    continue;
                }
                obj.model(time);
            }
        }
    }

    private void handleCollisions() {
        MsPlayer player = runtime.findPlayer();
        List<ElementObj> playerBullets = elementManager.getElementsByKey(GameElement.PLAY_BULLET);
        List<ElementObj> enemies = elementManager.getElementsByKey(GameElement.ENEMY);

        for (ElementObj bulletObj : playerBullets) {
            if (!(bulletObj instanceof PlayerBullet bullet) || !bullet.isLive()) {
                continue;
            }
            for (ElementObj enemyObj : enemies) {
                if (!(enemyObj instanceof EnemySoldier enemy) || !enemy.isLive()) {
                    continue;
                }
                if (bullet.pk(enemy)) {
                    bullet.setLive(false);
                    enemy.takeDamage(1);
                    break;
                }
            }
        }

        if (player == null || !player.isLive()) {
            runtime.markGameOver();
            return;
        }

        Rectangle meleeRect = player.getMeleeRectangle();
        for (ElementObj enemyObj : enemies) {
            if (!(enemyObj instanceof EnemySoldier enemy) || !enemy.isLive()) {
                continue;
            }
            if (player.pk(enemy)) {
                player.takeDamage(1);
            }
            if (meleeRect != null && meleeRect.intersects(enemy.getRectangle())) {
                enemy.takeDamage(1);
            }
        }
    }

    private void checkStageState() {
        List<ElementObj> enemies = elementManager.getElementsByKey(GameElement.ENEMY);
        boolean hasLiveEnemy = false;
        for (ElementObj obj : enemies) {
            if (obj.isLive()) {
                hasLiveEnemy = true;
                break;
            }
        }
        if (!hasLiveEnemy) {
            runtime.markStageClear();
        }
    }

    private void updateCamera() {
        MsPlayer player = runtime.findPlayer();
        if (player == null) {
            runtime.setCameraX(0);
            return;
        }

        // Side-scrollers feel better with a dead-zone camera than with smoothing.
        // Once the player reaches the follow band, the camera advances 1:1 with
        // the player instead of "catching up", which keeps enemy screen motion stable.
        final double leftDeadZone = 180;
        final double rightDeadZone = 300;
        double maxCamera = Math.max(0, runtime.getStageWidth() - GameRuntime.SCREEN_WIDTH);
        double currentCamera = runtime.getCameraX();
        double playerScreenX = player.getX() - currentCamera;
        double targetCamera = currentCamera;

        if (playerScreenX > rightDeadZone) {
            targetCamera = player.getX() - rightDeadZone;
        } else if (playerScreenX < leftDeadZone) {
            targetCamera = player.getX() - leftDeadZone;
        }
        double nextCamera = Math.max(0, Math.min(targetCamera, maxCamera));
        double deltaCamera = nextCamera - currentCamera;
        runtime.setCameraX(nextCamera);
        if (Math.abs(deltaCamera) > 0.0001) {
            applyCameraShiftToDynamicElements(deltaCamera);
        }
    }

    private void applyCameraShiftToDynamicElements(double deltaCamera) {
        shiftGroup(GameElement.ENEMY, deltaCamera);
        shiftGroup(GameElement.PLAY_BULLET, deltaCamera);
        shiftGroup(GameElement.ENEMY_BULLET, deltaCamera);
        shiftGroup(GameElement.EFFECT, deltaCamera);
        shiftGroup(GameElement.ITEM, deltaCamera);
        shiftGroup(GameElement.NPC, deltaCamera);
        shiftGroup(GameElement.BOSS, deltaCamera);
    }

    private void shiftGroup(GameElement element, double deltaCamera) {
        List<ElementObj> list = elementManager.getElementsByKey(element);
        for (ElementObj obj : list) {
            obj.onCameraShift(deltaCamera);
        }
    }
}
