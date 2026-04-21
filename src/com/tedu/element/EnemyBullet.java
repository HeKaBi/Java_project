package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import javax.swing.ImageIcon;

public class EnemyBullet extends ElementObj {
    private int vx = -7;
    private int vy = 0;
    private int damage = 1;

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
        this.setX(this.getX() + vx - GameRuntime.worldScrollX);
        this.setY(this.getY() + vy);
        if (this.getX() + this.getW() < -60 || this.getX() > GameJFrame.GameX + 60) {
            this.setLive(false);
            return;
        }
        if (this.getY() > GameJFrame.GameY + 60 || this.getY() + this.getH() < -60) {
            this.setLive(false);
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        ImageIcon icon = GameLoad.getImage(split[2]);
        this.setIcon(icon);
        this.setW(icon == null ? 20 : icon.getIconWidth());
        this.setH(icon == null ? 20 : icon.getIconHeight());
        if (split.length > 3) {
            this.vx = Integer.parseInt(split[3]);
        }
        if (split.length > 4) {
            this.vy = Integer.parseInt(split[4]);
        }
        if (split.length > 5) {
            this.damage = Integer.parseInt(split[5]);
        }
        return this;
    }

    @Override
    public int getDamage() {
        return damage;
    }
}
