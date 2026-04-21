package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class Bullet extends ElementObj {
    private static final int HORIZONTAL_LANE_PADDING_Y = 10;

    private int vx = 10;
    private int vy = 0;
    private int damage = 1;

    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            if (isVerticalShot()) {
                Graphics2D g2 = (Graphics2D) g.create();
                int drawW = this.getIcon().getIconWidth();
                int drawH = this.getIcon().getIconHeight();
                int drawX = this.getX() - (drawW - this.getW()) / 2;
                int drawY = this.getY() + (this.getH() - drawH) / 2;
                double centerX = this.getX() + this.getW() / 2.0;
                double centerY = this.getY() + this.getH() / 2.0;
                g2.rotate(-Math.PI / 2, centerX, centerY);
                g2.drawImage(this.getIcon().getImage(),
                        drawX, drawY,
                        drawW, drawH,
                        null);
                g2.dispose();
                return;
            }
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
        if (this.getX() > GameJFrame.GameX + 60 || this.getX() + this.getW() < -60) {
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
        if (split.length > 3) {
            this.vx = Integer.parseInt(split[3]);
        }
        if (split.length > 4) {
            this.vy = Integer.parseInt(split[4]);
        }
        if (split.length > 5) {
            this.damage = Integer.parseInt(split[5]);
        }
        int width = icon == null ? 24 : icon.getIconWidth();
        int height = icon == null ? 24 : icon.getIconHeight();
        if (isVerticalShot()) {
            this.setW(height);
            this.setH(width);
        } else {
            this.setW(width);
            this.setH(height);
        }
        return this;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public Rectangle getRectangle() {
        if (vy == 0 && vx != 0) {
            // Horizontal player bullets should only affect the current combat row,
            // but allow a small tolerance so same-row enemies don't feel missable.
            return new Rectangle(
                    this.getX(),
                    this.getY() - HORIZONTAL_LANE_PADDING_Y,
                    this.getW(),
                    this.getH() + HORIZONTAL_LANE_PADDING_Y * 2);
        }
        return super.getRectangle();
    }

    private boolean isVerticalShot() {
        return vx == 0 && vy != 0;
    }
}
