package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;

public class AdvancePromptEffect extends ElementObj {
    private static final String IMAGE_PATH = "image/前进特效.png";
    private static final ImageIcon PROMPT_ICON = GameLoad.getImage(IMAGE_PATH);
    private static final int SOURCE_X = 118;
    private static final int SOURCE_Y = 52;
    private static final int SOURCE_W = 974;
    private static final int SOURCE_H = 968;
    private static final int DEFAULT_W = 96;
    private static final int DEFAULT_H = 96;
    private static final int DEFAULT_LIFETIME_TICKS = 180;
    private static final int BLINK_START_TICKS = 100;
    private static final int BLINK_INTERVAL_TICKS = 6;

    private long bornTick = -1L;
    private int lifetimeTicks = DEFAULT_LIFETIME_TICKS;
    private boolean visible = true;

    @Override
    public void showElement(Graphics g) {
        if (!visible || PROMPT_ICON == null) {
            return;
        }
        Image image = PROMPT_ICON.getImage();
        int sourceRight = Math.min(PROMPT_ICON.getIconWidth(), SOURCE_X + SOURCE_W);
        int sourceBottom = Math.min(PROMPT_ICON.getIconHeight(), SOURCE_Y + SOURCE_H);
        g.drawImage(image,
                this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(),
                SOURCE_X, SOURCE_Y, sourceRight, sourceBottom,
                null);
    }

    @Override
    protected void move() {
    }

    @Override
    protected void updateImage(long gameTime) {
        if (bornTick < 0L) {
            bornTick = gameTime;
        }
        long age = Math.max(0L, gameTime - bornTick);
        if (age >= lifetimeTicks) {
            this.setLive(false);
            return;
        }
        long remain = lifetimeTicks - age;
        if (remain <= BLINK_START_TICKS) {
            visible = ((remain / BLINK_INTERVAL_TICKS) & 1L) == 0L;
        } else {
            visible = true;
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.setW(split.length > 2 ? Integer.parseInt(split[2]) : DEFAULT_W);
        this.setH(split.length > 3 ? Integer.parseInt(split[3]) : DEFAULT_H);
        this.lifetimeTicks = split.length > 4 ? Integer.parseInt(split[4]) : DEFAULT_LIFETIME_TICKS;
        this.bornTick = -1L;
        this.visible = true;
        return this;
    }
}
