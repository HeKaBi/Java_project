package com.tedu.element;

import com.tedu.manager.GameLoad;
import java.awt.Graphics;
import javax.swing.ImageIcon;

public class EnemyBullet extends ElementObj {
    private int speed = 7;

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
        this.setX(this.getX() - speed);
        if (this.getX() + this.getW() < 0) {
            this.setLive(false);
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        ImageIcon icon = GameLoad.imgMap.get(split[2]);
        this.setIcon(icon);
        this.setW(icon == null ? 20 : icon.getIconWidth());
        this.setH(icon == null ? 20 : icon.getIconHeight());
        if (split.length > 3) {
            this.speed = Integer.parseInt(split[3]);
        }
        return this;
    }
}
