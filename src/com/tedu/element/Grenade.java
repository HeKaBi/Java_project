package com.tedu.element;

import com.tedu.manager.AudioPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;

public class Grenade extends ElementObj {
    private static final ImageIcon GRENADE_ICON = GameLoad.loadImage("image/images/\u5b50\u5f39/image583.png");
    private static final int HITBOX = 18;
    private static final int TERRAIN_CONTACT_MARGIN = 1;

    private final ElementManager em = ElementManager.getManager();
    private double vx = 8.0;
    private double vy = -10.5;
    private double gravity = 0.55;
    private int damage = 2;
    private int blastRadius = 110;
    private boolean shouldExplode = false;

    @Override
    public void showElement(Graphics g) {
        if (GRENADE_ICON == null) {
            return;
        }
        int drawX = this.getX() + (this.getW() - GRENADE_ICON.getIconWidth()) / 2;
        int drawY = this.getY() + (this.getH() - GRENADE_ICON.getIconHeight()) / 2;
        g.drawImage(GRENADE_ICON.getImage(),
                drawX, drawY,
                GRENADE_ICON.getIconWidth(), GRENADE_ICON.getIconHeight(),
                null);
    }

    @Override
    protected void move() {
        int startX = this.getX();
        int startY = this.getY();
        double targetX = startX + vx - GameRuntime.worldScrollX;
        double targetY = startY + vy;
        vy += gravity;
        if (moveIntoTerrain(startX, startY, targetX, targetY)) {
            explode();
            return;
        }
        this.setX((int) Math.round(targetX));
        this.setY((int) Math.round(targetY));
        if (this.getX() + this.getW() < -120 || this.getX() > GameJFrame.GameX + 120 || this.getY() > GameJFrame.GameY + 120) {
            this.setLive(false);
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.setW(HITBOX);
        this.setH(HITBOX);
        if (split.length > 3) {
            this.vx = Double.parseDouble(split[3]);
        }
        if (split.length > 4) {
            this.vy = Double.parseDouble(split[4]);
        }
        return this;
    }

    @Override
    public void die() {
        if (!shouldExplode) {
            return;
        }
        AudioPlayer.playOnce("music/die.wav");
        ElementObj effect = new Effect().createElement((this.getX() - 46) + "," + (this.getY() - 46) + ",effect,120,120");
        em.addElement(effect, GameElement.DIE);
        List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
        int centerX = this.getCenterX();
        int centerY = this.getCenterY();
        for (ElementObj enemyObj : enemys) {
            int dx = enemyObj.getCenterX() - centerX;
            int dy = enemyObj.getCenterY() - centerY;
            if (dx * dx + dy * dy > blastRadius * blastRadius) {
                continue;
            }
            if (enemyObj instanceof Enemy) {
                ((Enemy) enemyObj).hurt(damage);
            } else if (enemyObj instanceof ScoutEnemy) {
                ((ScoutEnemy) enemyObj).hurt(damage);
            } else {
                enemyObj.setLive(false);
            }
        }
        List<ElementObj> bosses = em.getElementsByKey(GameElement.BOSS);
        for (ElementObj bossObj : bosses) {
            int dx = bossObj.getCenterX() - centerX;
            int dy = bossObj.getCenterY() - centerY;
            if (dx * dx + dy * dy > blastRadius * blastRadius) {
                continue;
            }
            if (bossObj instanceof Boss) {
                ((Boss) bossObj).hurt(damage);
            } else {
                bossObj.setLive(false);
            }
        }
    }

    public void explode() {
        if (shouldExplode) {
            return;
        }
        shouldExplode = true;
        this.setLive(false);
    }

    private boolean moveIntoTerrain(int startX, int startY, double targetX, double targetY) {
        double dx = targetX - startX;
        double dy = targetY - startY;
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy))));
        for (int step = 1; step <= steps; step++) {
            double ratio = step / (double) steps;
            int candidateX = (int) Math.round(startX + dx * ratio);
            int candidateY = (int) Math.round(startY + dy * ratio);
            if (!collidesWithTerrain(candidateX, candidateY, dx)) {
                continue;
            }
            this.setX(candidateX);
            int groundBottom = GameRuntime.getBattlefieldMaxBottomAt(candidateX + this.getW() / 2);
            this.setY(Math.min(candidateY, groundBottom - this.getH()));
            return true;
        }
        return false;
    }

    private boolean collidesWithTerrain(int candidateX, int candidateY, double dx) {
        int bottomY = candidateY + this.getH() - TERRAIN_CONTACT_MARGIN;
        int centerX = candidateX + this.getW() / 2;
        if (bottomY >= GameRuntime.getBattlefieldMaxBottomAt(centerX)) {
            return true;
        }
        if (dx == 0.0) {
            return false;
        }
        int frontX = candidateX + (dx > 0 ? this.getW() - 1 : 0);
        return bottomY >= GameRuntime.getBattlefieldMaxBottomAt(frontX);
    }
}
