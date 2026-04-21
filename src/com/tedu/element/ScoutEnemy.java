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

public class ScoutEnemy extends ElementObj {
    private static final int HITBOX_W = 48;
    private static final int HITBOX_H = 72;
    private static final int MAX_STEP_UP = 12;
    private static final int GROUND_PROBE_INSET = 5;
    private static final List<ImageIcon> RUN_FRAMES =
            GameLoad.loadFramesFromDirectory("image/images/Enemy/R/sca");

    private final ElementManager em = ElementManager.getManager();

    private int hp = 1;
    private int speed = 3;
    private boolean faceRight = false;
    private boolean countedKill = false;
    private ImageIcon currentFrame = RUN_FRAMES.isEmpty() ? null : RUN_FRAMES.get(0);

    @Override
    public void showElement(Graphics g) {
        ImageIcon frame = currentFrame == null ? this.getIcon() : currentFrame;
        if (frame == null) {
            return;
        }
        int drawW = frame.getIconWidth();
        int drawH = frame.getIconHeight();
        int drawX = this.getX() + (this.getW() - drawW) / 2;
        int drawY = this.getY() + this.getH() - drawH;
        if (faceRight) {
            g.drawImage(frame.getImage(), drawX, drawY, drawW, drawH, null);
        } else {
            g.drawImage(frame.getImage(), drawX + drawW, drawY, -drawW, drawH, null);
        }
    }

    @Override
    protected void move() {
        int x = this.getX() - GameRuntime.worldScrollX;
        ElementObj player = getPlayer();
        if (player != null) {
            int dx = player.getCenterX() - this.getCenterX();
            faceRight = dx > 0;
            if (Math.abs(dx) > 18) {
                int desiredX = x + (dx > 0 ? speed : -speed);
                x = resolveGroundMove(x, desiredX);
            }
        }
        int footX = x + this.getW() / 2;
        this.setX(x);
        this.setY(GameRuntime.getBattlefieldMaxBottomAt(footX) - this.getH());
        if (this.getX() + this.getW() < -120 || this.getX() > GameJFrame.GameX + 160) {
            this.setLive(false);
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        if (!RUN_FRAMES.isEmpty()) {
            currentFrame = RUN_FRAMES.get((int) ((gameTime / 3) % RUN_FRAMES.size()));
            this.setIcon(currentFrame);
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setW(HITBOX_W);
        this.setH(HITBOX_H);
        int footX = this.getX() + HITBOX_W / 2;
        this.setY(GameRuntime.getBattlefieldMaxBottomAt(footX) - HITBOX_H);
        if (split.length > 2) {
            this.speed = Integer.parseInt(split[2]);
        }
        if (split.length > 3) {
            this.hp = Integer.parseInt(split[3]);
        }
        if (!RUN_FRAMES.isEmpty()) {
            currentFrame = RUN_FRAMES.get(0);
            this.setIcon(currentFrame);
        }
        return this;
    }

    @Override
    public void die() {
        AudioPlayer.playOnce("music/die.wav");
        ElementObj effect = new Effect().createElement((this.getX() - 16) + "," + (this.getY() - 8) + ",effect,96,96");
        em.addElement(effect, GameElement.DIE);
    }

    public boolean hurt(int damage) {
        if (!this.isLive()) {
            return false;
        }
        hp -= damage;
        if (hp > 0) {
            return false;
        }
        hp = 0;
        if (!countedKill) {
            countedKill = true;
            GameRuntime.killCount++;
        }
        this.setLive(false);
        return true;
    }

    private ElementObj getPlayer() {
        List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        return plays.isEmpty() ? null : plays.get(0);
    }

    private int resolveGroundMove(int currentX, int desiredX) {
        if (desiredX == currentX) {
            return currentX;
        }
        int step = desiredX > currentX ? 1 : -1;
        int resolvedX = currentX;
        int actorBottom = this.getY() + this.getH();
        for (int candidateX = currentX + step; candidateX != desiredX + step; candidateX += step) {
            int frontX = candidateX + (step > 0 ? this.getW() - GROUND_PROBE_INSET : GROUND_PROBE_INSET);
            int frontSurface = GameRuntime.getBattlefieldMaxBottomAt(frontX);
            if (actorBottom - frontSurface > MAX_STEP_UP) {
                break;
            }
            resolvedX = candidateX;
        }
        return resolvedX;
    }
}
