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

public class PlaneBomb extends ElementObj {
    private static final List<ImageIcon> FRAMES =
            GameLoad.loadFramesFromDirectory("image/images/\u5b50\u5f39/plane_bomb");
    private static final int FRAME_DURATION_TICKS = 4;
    private static final int TERRAIN_CONTACT_MARGIN = 1;

    private final ElementManager em = ElementManager.getManager();
    private double px = 0.0;
    private double py = 0.0;
    private double vx = -1.3;
    private double vy = 2.0;
    private double gravity = 0.22;
    private int damage = 1;
    private int impactEffectSize = 96;
    private long bornTick = -1L;
    private long currentTick = -1L;
    private boolean impactTriggered = false;

    @Override
    public void showElement(Graphics g) {
        if (FRAMES.isEmpty()) {
            return;
        }
        ImageIcon frame = FRAMES.get(resolveFrameIndex());
        int drawX = this.getX() + (this.getW() - frame.getIconWidth()) / 2;
        int drawY = this.getY() + (this.getH() - frame.getIconHeight()) / 2;
        g.drawImage(frame.getImage(), drawX, drawY, frame.getIconWidth(), frame.getIconHeight(), null);
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
        if (this.getX() + this.getW() < -80 || this.getX() > GameJFrame.GameX + 80) {
            this.setLive(false);
            return;
        }
        if (this.getY() > GameJFrame.GameY + 100 || this.getY() + this.getH() < -80) {
            this.setLive(false);
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        if (bornTick < 0L) {
            bornTick = gameTime;
        }
        currentTick = gameTime;
        if (!FRAMES.isEmpty()) {
            this.setIcon(FRAMES.get(resolveFrameIndex()));
            this.setW(FRAMES.get(0).getIconWidth());
            this.setH(FRAMES.get(0).getIconHeight());
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.px = this.getX();
        this.py = this.getY();
        if (!FRAMES.isEmpty()) {
            this.setIcon(FRAMES.get(0));
            this.setW(FRAMES.get(0).getIconWidth());
            this.setH(FRAMES.get(0).getIconHeight());
        } else {
            this.setW(28);
            this.setH(44);
        }
        if (split.length > 2) {
            this.vx = Double.parseDouble(split[2]);
        }
        if (split.length > 3) {
            this.vy = Double.parseDouble(split[3]);
        }
        if (split.length > 4) {
            this.damage = Integer.parseInt(split[4]);
        }
        if (split.length > 5) {
            this.gravity = Double.parseDouble(split[5]);
        }
        if (split.length > 6) {
            this.impactEffectSize = Integer.parseInt(split[6]);
        }
        this.bornTick = -1L;
        this.currentTick = -1L;
        this.impactTriggered = false;
        return this;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public void die() {
        if (!impactTriggered) {
            return;
        }
        AudioPlayer.playOnce("music/die.wav");
        int effectX = this.getCenterX() - impactEffectSize / 2;
        int effectY = this.getCenterY() - impactEffectSize / 2;
        ElementObj effect = new Effect().createElement(
                effectX + "," + effectY + ",effect," + impactEffectSize + "," + impactEffectSize);
        em.addElement(effect, GameElement.DIE);
    }

    public void triggerImpact() {
        if (impactTriggered) {
            return;
        }
        impactTriggered = true;
        this.setLive(false);
    }

    private int resolveFrameIndex() {
        if (FRAMES.isEmpty()) {
            return 0;
        }
        long elapsed = bornTick < 0L || currentTick < 0L ? 0L : Math.max(0L, currentTick - bornTick);
        int index = (int) ((elapsed / FRAME_DURATION_TICKS) % FRAMES.size());
        return Math.max(0, Math.min(FRAMES.size() - 1, index));
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
            int groundBottom = GameRuntime.getBattlefieldMaxBottomAt(this.getCenterX());
            this.setY(Math.min(this.getY(), groundBottom - this.getH()));
            py = this.getY();
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
