package com.tedu.element;

import com.tedu.manager.GameRuntime;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class PlatformObj extends ElementObj {
    private static final Color PLATFORM_FILL = new Color(112, 86, 52, 220);
    private static final Color PLATFORM_EDGE = new Color(228, 194, 120, 230);
    private static final Color PLATFORM_SHADOW = new Color(46, 27, 16, 180);
    private static final int POST_HEIGHT = 22;

    @Override
    public void showElement(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int x = this.getX();
        int y = this.getY();
        int w = this.getW();
        int h = this.getH();

        g2.setColor(PLATFORM_SHADOW);
        g2.fillRoundRect(x + 2, y + 2, w, h, 10, 10);
        g2.setColor(PLATFORM_FILL);
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(PLATFORM_EDGE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 10, 10);
        g2.drawLine(x + 8, y + 4, x + w - 8, y + 4);

        int leftPostX = x + Math.max(16, w / 5);
        int rightPostX = x + w - Math.max(16, w / 5);
        g2.setColor(new Color(94, 72, 42, 210));
        g2.fillRoundRect(leftPostX, y + h - 2, 6, POST_HEIGHT, 4, 4);
        g2.fillRoundRect(rightPostX, y + h - 2, 6, POST_HEIGHT, 4, 4);
        g2.dispose();
    }

    @Override
    protected void move() {
        int shift = GameRuntime.worldScrollX;
        if (shift > 0) {
            this.setX(this.getX() - shift);
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.setW(Integer.parseInt(split[2]));
        this.setH(split.length > 3 ? Integer.parseInt(split[3]) : 14);
        return this;
    }

    public int getTopSurfaceY() {
        return this.getY();
    }
}
