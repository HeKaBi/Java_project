package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import javax.swing.ImageIcon;

public class EnemyBullet extends ElementObj {
    private static final int TERRAIN_CONTACT_MARGIN = 1;

    private final ElementManager em = ElementManager.getManager();
    private double px = 0.0;
    private double py = 0.0;
    private double vx = -7.0;
    private double vy = 0.0;
    private double gravity = 0.0;
    private int damage = 1;
    private boolean stopOnGround = false;
    private int impactEffectSize = 0;
    private boolean impactTriggered = false;

    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            if (vx > 0.0) {
                g.drawImage(this.getIcon().getImage(),
                        this.getX() + this.getW(), this.getY(),
                        -this.getW(), this.getH(),
                        null);
            } else {
                g.drawImage(this.getIcon().getImage(),
                        this.getX(), this.getY(),
                        this.getW(), this.getH(),
                        null);
            }
        }
    }

    @Override
    protected void move() {
        double targetX = px + vx - GameRuntime.worldScrollX;
        double targetY = py + vy;
        vy += gravity;
        if (moveIntoTerrain(targetX, targetY)) {
            return;
        }
        px = targetX;
        py = targetY;
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
        if (split.length > 8) {
            this.impactEffectSize = Integer.parseInt(split[8]);
        }
        this.impactTriggered = false;
        return this;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public void die() {
        if (!impactTriggered || impactEffectSize <= 0) {
            return;
        }
        int effectX = this.getCenterX() - impactEffectSize / 2;
        int effectY = this.getCenterY() - impactEffectSize / 2;
        ElementObj effect = new Effect().createElement(effectX + "," + effectY + ",effect,"
                + impactEffectSize + "," + impactEffectSize);
        em.addElement(effect, GameElement.DIE);
    }

    public void triggerImpact() {
        if (impactTriggered) {
            return;
        }
        impactTriggered = true;
        this.setLive(false);
    }

    private boolean moveIntoTerrain(double targetX, double targetY) {
        double dx = targetX - px;
        double dy = targetY - py;
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy))));
        for (int step = 1; step <= steps; step++) {
            double ratio = step / (double) steps;
            int candidateX = (int) Math.round(px + dx * ratio);
            int candidateY = (int) Math.round(py + dy * ratio);
            if (!collidesWithTerrain(candidateX, candidateY, dx)) {
                continue;
            }
            this.setX(candidateX);
            this.setY(candidateY);
            px = candidateX;
            py = candidateY;
            if (stopOnGround) {
                int groundBottom = GameRuntime.getBattlefieldMaxBottomAt(this.getCenterX());
                this.setY(Math.min(this.getY(), groundBottom - this.getH()));
                py = this.getY();
            }
            triggerImpact();
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
