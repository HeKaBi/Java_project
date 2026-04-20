package com.tedu.element;

import java.awt.Graphics2D;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;

public class PlayerBullet extends ElementObj {
    private static List<ImageIcon> bulletFrames;
    private final ElementManager elementManager = ElementManager.getManager();
    private int direction = 1;
    private int frameIndex = 0;
    private long spawnTick = 0L;
    private long currentGameTime = 0L;

    public PlayerBullet() {
    }

    public PlayerBullet(double x, double y, int direction) {
        ensureFrames();
        setX(x);
        setY(y);
        this.direction = direction;
        setIcon(bulletFrames.getFirst());
        setW((int) (getIcon().getIconWidth() * 1.1));
        setH((int) (getIcon().getIconHeight() * 1.1));
    }

    @Override
    public void showElement(Graphics2D g2) {
        ImageIcon current = getIcon();
        if (current == null) {
            return;
        }
        int screenX = (int) Math.round(getX() - GameRuntime.getInstance().getCameraX() + getRenderOffsetX());
        if (direction < 0) {
            g2.drawImage(current.getImage(), screenX + getW(), (int) getY(), screenX, (int) getY() + getH(), 0, 0,
                    current.getIconWidth(), current.getIconHeight(), null);
            return;
        }
        g2.drawImage(current.getImage(), screenX, (int) getY(), getW(), getH(), null);
    }

    @Override
    protected void move() {
        setX(getX() + direction * 12);
        if (getX() < -100 || getX() > GameRuntime.getInstance().getStageWidth() + 100) {
            setLive(false);
            return;
        }
        for (ElementObj blockObj : elementManager.getElementsByKey(GameElement.SCENE)) {
            if (pk(blockObj)) {
                setLive(false);
                break;
            }
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        ensureFrames();
        currentGameTime = gameTime;
        if (spawnTick == 0L) {
            spawnTick = gameTime;
        }
        frameIndex = (int) ((gameTime - spawnTick) % bulletFrames.size());
        setIcon(bulletFrames.get(frameIndex));
    }

    @Override
    public void die() {
    }

    @Override
    public void onCameraShift(double deltaX) {
        super.onCameraShift(deltaX);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] data = str.split(",");
        ensureFrames();
        setX(Integer.parseInt(data[0].trim()));
        setY(Integer.parseInt(data[1].trim()));
        direction = Integer.parseInt(data[2].trim());
        setIcon(bulletFrames.getFirst());
        setW((int) (getIcon().getIconWidth() * 1.1));
        setH((int) (getIcon().getIconHeight() * 1.1));
        return this;
    }

    private void ensureFrames() {
        if (bulletFrames != null) {
            return;
        }
        bulletFrames = GameLoad.getAnimation("bullet");
    }

    @Override
    public GameElement getGameElement() {
        return GameElement.PLAY_BULLET;
    }
}
