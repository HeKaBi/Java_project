package com.tedu.element;

import com.tedu.manager.AudioPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;

public class Enemy extends ElementObj {
    private static final int HITBOX_W = 48;
    private static final int HITBOX_H = 72;
    private static final String ENEMY_RIGHT = "image/images/Enemy/R/";
    private static final String ENEMY_LEFT = "image/images/Enemy/L/";
    private static final String ENEMY_BULLET_LEFT = "image/images/子弹/left/bullet10.png";
    private static final String ENEMY_BULLET_RIGHT = "image/images/子弹/right/bullet11.png";

    private static final List<ImageIcon> STAND_RIGHT = GameLoad.loadFrames(
            ENEMY_RIGHT + "enemy_stand000.png",
            ENEMY_RIGHT + "enemy_stand010.png");
    private static final List<ImageIcon> STAND_LEFT = GameLoad.loadFrames(
            ENEMY_LEFT + "enemy_stand000.png",
            ENEMY_LEFT + "enemy_stand010.png");
    private static final List<ImageIcon> RUN_RIGHT = GameLoad.loadFrames(
            ENEMY_RIGHT + "enemy_run000.png",
            ENEMY_RIGHT + "enemy_run010.png",
            ENEMY_RIGHT + "enemy_run020.png",
            ENEMY_RIGHT + "enemy_run030.png",
            ENEMY_RIGHT + "enemy_run040.png");
    private static final List<ImageIcon> RUN_LEFT = GameLoad.loadFrames(
            ENEMY_LEFT + "enemy_run000.png",
            ENEMY_LEFT + "enemy_run010.png",
            ENEMY_LEFT + "enemy_run020.png",
            ENEMY_LEFT + "enemy_run030.png",
            ENEMY_LEFT + "enemy_run040.png");
    private static final List<ImageIcon> ATTACK_RIGHT = GameLoad.loadFrames(
            ENEMY_RIGHT + "enemy_attack000.png",
            ENEMY_RIGHT + "enemy_attack010.png",
            ENEMY_RIGHT + "enemy_attack020.png",
            ENEMY_RIGHT + "enemy_attack030.png",
            ENEMY_RIGHT + "enemy_attack040.png");
    private static final List<ImageIcon> ATTACK_LEFT = GameLoad.loadFrames(
            ENEMY_LEFT + "enemy_attack000.png",
            ENEMY_LEFT + "enemy_attack010.png",
            ENEMY_LEFT + "enemy_attack020.png",
            ENEMY_LEFT + "enemy_attack030.png",
            ENEMY_LEFT + "enemy_attack040.png");

    private final Random random = new Random();
    private final ElementManager em = ElementManager.getManager();

    private int speed = 2;
    private int hp = 2;
    private boolean running = true;
    private boolean attacking = false;
    private boolean faceRight = false;
    private boolean firedThisAttack = false;
    private boolean countedKill = false;
    private long attackStartTime = -1;
    private long nextAttackTime = 0;
    private ImageIcon currentFrame = RUN_LEFT.isEmpty() ? null : RUN_LEFT.get(0);

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
        g.drawImage(frame.getImage(), drawX, drawY, drawW, drawH, null);
    }

    @Override
    protected void move() {
        ElementObj player = getPlayer();
        int x = this.getX() - GameRuntime.worldScrollX;
        int bottom = GameRuntime.clampBattlefieldBottom(this.getY() + this.getH());
        running = false;
        if (player != null) {
            int dx = player.getCenterX() - this.getCenterX();
            faceRight = dx > 0;
            int distance = Math.abs(dx);
            int desiredRange = 190 + random.nextInt(60);
            if (!attacking && distance > desiredRange) {
                running = true;
                x += dx > 0 ? speed : -speed;
            }
        }
        this.setX(x);
        this.setY(bottom - this.getH());
        if (this.getX() + this.getW() < -100 || this.getX() > GameJFrame.GameX + 160) {
            this.setLive(false);
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        ElementObj player = getPlayer();
        if (player != null) {
            faceRight = player.getCenterX() > this.getCenterX();
            int distance = Math.abs(player.getCenterX() - this.getCenterX());
            if (!attacking && distance <= 260 && gameTime >= nextAttackTime) {
                attacking = true;
                attackStartTime = gameTime;
                firedThisAttack = false;
            }
        }
        if (attacking && gameTime - attackStartTime > 28) {
            attacking = false;
            nextAttackTime = gameTime + 45 + random.nextInt(45);
        }
        List<ImageIcon> frames = getCurrentFrames();
        currentFrame = selectFrame(frames, gameTime, attacking ? 4 : 6);
        if (currentFrame != null) {
            this.setIcon(currentFrame);
        }
    }

    @Override
    protected void add(long gameTime) {
        if (!attacking || firedThisAttack || gameTime - attackStartTime < 10) {
            return;
        }
        firedThisAttack = true;
        ElementObj bulletTemplate = GameLoad.getObj("ebullet");
        if (bulletTemplate == null) {
            return;
        }
        ElementObj player = getPlayer();
        int bulletX = faceRight ? this.getX() + this.getW() - 2 : this.getX() - 12;
        int bulletY = this.getY() + 18;
        int vx = faceRight ? 8 : -8;
        int vy = 0;
        if (player != null) {
            vy = Math.max(-3, Math.min(3, (player.getCenterY() - this.getCenterY()) / 22));
        }
        String bulletPath = vx >= 0 ? ENEMY_BULLET_RIGHT : ENEMY_BULLET_LEFT;
        ElementObj bullet = bulletTemplate.createElement(
                bulletX + "," + bulletY + "," + bulletPath + "," + vx + "," + vy + ",1");
        em.addElement(bullet, GameElement.ENEMYFILE);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        int bottom = GameRuntime.clampBattlefieldBottom(Integer.parseInt(split[1]) + HITBOX_H);
        this.setY(bottom - HITBOX_H);
        this.setW(HITBOX_W);
        this.setH(HITBOX_H);
        if (split.length > 3) {
            this.speed = Integer.parseInt(split[3]);
        }
        if (split.length > 4) {
            this.hp = Integer.parseInt(split[4]);
        }
        if (!RUN_LEFT.isEmpty()) {
            currentFrame = RUN_LEFT.get(0);
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

    private List<ImageIcon> getCurrentFrames() {
        if (attacking) {
            return faceRight ? ATTACK_RIGHT : ATTACK_LEFT;
        }
        if (running) {
            return faceRight ? RUN_RIGHT : RUN_LEFT;
        }
        return faceRight ? STAND_RIGHT : STAND_LEFT;
    }

    private ImageIcon selectFrame(List<ImageIcon> frames, long gameTime, int frameGap) {
        if (frames == null || frames.isEmpty()) {
            return null;
        }
        int index = (int) ((gameTime / Math.max(1, frameGap)) % frames.size());
        return frames.get(index);
    }
}
