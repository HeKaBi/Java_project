package com.tedu.element;

import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import javax.swing.ImageIcon;

public class MapObj extends ElementObj {
    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(),
                    this.getX(), this.getY(),
                    this.getX() + this.getW(), this.getY() + this.getH(),
                    null);
        }
    }

    @Override
    protected void move() {
        this.setX(this.getX() - 1);
        if (this.getX() <= -this.getW()) {
            this.setX(this.getW());
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        ImageIcon icon = com.tedu.manager.GameLoad.imgMap.get(split[2]);
        this.setIcon(icon);
        int mapWidth = GameJFrame.GameX;
        if (icon != null && icon.getIconWidth() > 0) {
            mapWidth = icon.getIconWidth();
        }
        this.setW(mapWidth);
        this.setH(GameJFrame.GameY);
        return this;
    }
}
