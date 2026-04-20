package com.tedu.element;

import java.awt.Graphics2D;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;

public class ExplosionEffect extends ElementObj {
    private static List<ImageIcon> frames;
    private int frameIndex = 0;
    private long lastTick = -1;

    public ExplosionEffect() {
    }

    public ExplosionEffect(double x, double y) {
        ensureFrames();
        setX(x);
        setY(y);
        setIcon(frames.getFirst());
        setW((int) (getIcon().getIconWidth() * 1.3));
        setH((int) (getIcon().getIconHeight() * 1.3));
    }

    @Override
    public void showElement(Graphics2D g2) {
        ImageIcon current = getIcon();
        if (current == null) {
            return;
        }
        int screenX = (int) Math.round(getX() - GameRuntime.getInstance().getCameraX());
        g2.drawImage(current.getImage(), screenX, (int) getY(), getW(), getH(), null);
    }

    @Override
    protected void updateImage(long gameTime) {
        ensureFrames();
        if (lastTick == -1) {
            lastTick = gameTime;
        }
        if (gameTime - lastTick >= 3) {
            frameIndex++;
            lastTick = gameTime;
            if (frameIndex >= frames.size()) {
                setLive(false);
                return;
            }
            setIcon(frames.get(frameIndex));
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] data = str.split(",");
        ensureFrames();
        setX(Integer.parseInt(data[0].trim()));
        setY(Integer.parseInt(data[1].trim()));
        setIcon(frames.getFirst());
        setW((int) (getIcon().getIconWidth() * 1.3));
        setH((int) (getIcon().getIconHeight() * 1.3));
        return this;
    }

    private void ensureFrames() {
        if (frames != null) {
            return;
        }
        frames = GameLoad.getAnimation("effect_boom");
    }

    @Override
    public GameElement getGameElement() {
        return GameElement.EFFECT;
    }
}
