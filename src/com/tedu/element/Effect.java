package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;

public class Effect extends ElementObj {
    private static final String FX_BASE = "image/images/\u7206\u70b8/";
    private static final List<ImageIcon> EXPLOSION = GameLoad.loadFrames(
            FX_BASE + "bomb_bang0.png",
            FX_BASE + "bomb_bang1.png",
            FX_BASE + "bomb_bang2.png",
            FX_BASE + "bomb_bang3.png",
            FX_BASE + "bomb_bang4.png",
            FX_BASE + "bomb_bang5.png",
            FX_BASE + "bomb_bang6.png",
            FX_BASE + "bomb_bang7.png",
            FX_BASE + "bomb_bang8.png",
            FX_BASE + "bomb_bang9.png",
            FX_BASE + "bomb_bang10.png",
            FX_BASE + "bomb_bang11.png",
            FX_BASE + "bomb_bang12.png",
            FX_BASE + "bomb_bang13.png",
            FX_BASE + "bomb_bang14.png",
            FX_BASE + "bomb_bang15.png",
            FX_BASE + "bomb_bang16.png");

    private int frameIndex = 0;
    private long lastFrameTime = -1;

    @Override
    public void showElement(Graphics g) {
        if (EXPLOSION.isEmpty()) {
            return;
        }
        ImageIcon frame = EXPLOSION.get(Math.min(frameIndex, EXPLOSION.size() - 1));
        int drawX = this.getX() + (this.getW() - frame.getIconWidth()) / 2;
        int drawY = this.getY() + (this.getH() - frame.getIconHeight()) / 2;
        g.drawImage(frame.getImage(),
                drawX, drawY,
                frame.getIconWidth(), frame.getIconHeight(),
                null);
    }

    @Override
    protected void move() {
        this.setX(this.getX() - GameRuntime.worldScrollX);
    }

    @Override
    protected void updateImage(long time) {
        if (EXPLOSION.isEmpty()) {
            this.setLive(false);
            return;
        }
        if (lastFrameTime < 0) {
            lastFrameTime = time;
            return;
        }
        if (time - lastFrameTime < 2) {
            return;
        }
        lastFrameTime = time;
        frameIndex++;
        if (frameIndex >= EXPLOSION.size()) {
            this.setLive(false);
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.setW(split.length > 3 ? Integer.parseInt(split[3]) : 96);
        this.setH(split.length > 4 ? Integer.parseInt(split[4]) : 96);
        return this;
    }
}
