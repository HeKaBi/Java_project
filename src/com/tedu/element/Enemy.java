package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import java.awt.Graphics;
import java.util.Random;
import javax.swing.ImageIcon;

public class Enemy extends ElementObj {
    private int speed = 2;
    private long fireTime = 0;
    private int fireGap = 90;
    private final Random random = new Random();
    private final ElementManager em = ElementManager.getManager();

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
    protected void add(long gameTime) {
        if (gameTime - fireTime < fireGap) {
            return;
        }
        fireTime = gameTime;
        fireGap = 70 + random.nextInt(60);
        ElementObj bulletTemplate = GameLoad.getObj("ebullet");
        if (bulletTemplate == null) {
            return;
        }
        int bulletX = this.getX() - 6;
        int bulletY = this.getY() + this.getH() / 2 - 6;
        ElementObj bullet = bulletTemplate.createElement(bulletX + "," + bulletY + ",ebullet,7");
        em.addElement(bullet, GameElement.ENEMYFILE);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        ImageIcon icon = GameLoad.imgMap.get(split[2]);
        this.setIcon(icon);
        this.setW(icon == null ? 48 : icon.getIconWidth());
        this.setH(icon == null ? 48 : icon.getIconHeight());
        if (split.length > 3) {
            this.speed = Integer.parseInt(split[3]);
        }
        return this;
    }
}
