package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import javax.swing.ImageIcon;

public class EnemyBullet extends ElementObj {
    private double px = 0.0;
    private double py = 0.0;
    private double vx = -7.0;
    private double vy = 0.0;
    private double gravity = 0.0;
    private int damage = 1;
    private boolean stopOnGround = false;

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
        px += vx - GameRuntime.worldScrollX;
        py += vy;
        vy += gravity;
        this.setX((int) Math.round(px));
        this.setY((int) Math.round(py));
        if (this.getX() + this.getW() < -60 || this.getX() > GameJFrame.GameX + 60) {
            this.setLive(false);
            return;
        }
        if (this.getY() > GameJFrame.GameY + 60 || this.getY() + this.getH() < -60) {
            this.setLive(false);
            return;
        }
        if (stopOnGround) {
            int groundBottom = GameRuntime.getBattlefieldMaxBottomAt(this.getCenterX());
            if (this.getY() + this.getH() >= groundBottom) {
                this.setY(groundBottom - this.getH());
                py = this.getY();
                this.setLive(false);
            }
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.px = this.getX();
        this.py = this.getY();
        ImageIcon icon = GameLoad.getImage(split[2]);
        this.setIcon(icon);
        this.setW(icon == null ? 20 : icon.getIconWidth());
        this.setH(icon == null ? 20 : icon.getIconHeight());
        if (split.length > 3) {
            this.vx = Double.parseDouble(split[3]);
        }
        if (split.length > 4) {
            this.vy = Double.parseDouble(split[4]);
        }
        if (split.length > 5) {
            this.damage = Integer.parseInt(split[5]);
        }
        if (split.length > 6) {
            this.gravity = Double.parseDouble(split[6]);
        }
        if (split.length > 7) {
            this.stopOnGround = "1".equals(split[7]) || Boolean.parseBoolean(split[7]);
        }
        return this;
    }

    @Override
    public int getDamage() {
        return damage;
    }

}
