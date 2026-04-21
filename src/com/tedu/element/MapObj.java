package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import javax.swing.ImageIcon;

public class MapObj extends ElementObj {
    private double scrollRatio = 1.0;
    private int minX = 0;

    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(),
                    this.getX(), this.getY(),
                    this.getW(), this.getH(),
                    null);
        }
    }

    @Override
    protected void move() {
        int shift = (int) Math.round(GameRuntime.worldScrollX * scrollRatio);
        if (shift <= 0) {
            return;
        }
        int nextX = this.getX() - shift;
        if (nextX < minX) {
            nextX = minX;
        }
        this.setX(nextX);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        ImageIcon icon = GameLoad.getImage(split[2]);
        this.setIcon(icon);
        int mapWidth = GameJFrame.GameX;
        if (icon != null && icon.getIconWidth() > 0) {
            mapWidth = icon.getIconWidth();
        }
        this.setW(mapWidth);
        this.setH(GameJFrame.GameY);
        this.minX = this.getX() - Math.max(0, mapWidth - GameJFrame.GameX);
        if (split.length > 3) {
            this.scrollRatio = Double.parseDouble(split[3]);
        }
        return this;
    }
}
