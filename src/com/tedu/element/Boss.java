package com.tedu.element;

import com.tedu.manager.AudioPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public class Boss extends ElementObj {
    private static final int HITBOX_W = 128;
    private static final int HITBOX_H = 118;
    private static final String BOSS_BULLET = "image/images/子弹/boss_bomb.png";
    private static final List<ImageIcon> MOVE_FRAMES = loadBossFrames();

    private final ElementManager em = ElementManager.getManager();

    private int hp = 24;
    private int maxHp = 24;
    private int patrolSpeed = 2;
    private boolean entered = false;
    private boolean patrolRight = false;
    private boolean countedKill = false;
    private long lastAttackTime = -120;
    private ImageIcon currentFrame = MOVE_FRAMES.isEmpty() ? null : MOVE_FRAMES.get(0);

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
        int x = this.getX() - GameRuntime.worldScrollX;
        int minX = GameJFrame.GameX - 430;
        int maxX = GameJFrame.GameX - 170;
        if (!entered) {
            x -= patrolSpeed;
            if (x <= maxX - 60) {
                x = maxX - 60;
                entered = true;
            }
        } else {
            x += patrolRight ? patrolSpeed : -patrolSpeed;
            if (x <= minX) {
                x = minX;
                patrolRight = true;
            } else if (x >= maxX) {
                x = maxX;
                patrolRight = false;
            }
        }
        this.setX(x);
        int footX = this.getX() + this.getW() / 2;
        this.setY(GameRuntime.getBattlefieldMaxBottomAt(footX) - this.getH());
    }

    @Override
    protected void updateImage(long gameTime) {
        if (!MOVE_FRAMES.isEmpty()) {
            currentFrame = MOVE_FRAMES.get((int) ((gameTime / 5) % MOVE_FRAMES.size()));
            this.setIcon(currentFrame);
        }
    }

    @Override
    protected void add(long gameTime) {
        if (!entered || gameTime - lastAttackTime < 30) {
            return;
        }
        lastAttackTime = gameTime;
        spawnBullet(-2, 2);
        spawnBullet(0, 2);
        spawnBullet(2, 2);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.setW(HITBOX_W);
        this.setH(HITBOX_H);
        if (split.length > 2) {
            this.hp = Integer.parseInt(split[2]);
            this.maxHp = this.hp;
        }
        if (!MOVE_FRAMES.isEmpty()) {
            currentFrame = MOVE_FRAMES.get(0);
            this.setIcon(currentFrame);
        }
        return this;
    }

    @Override
    public void die() {
        AudioPlayer.playOnce("music/die.wav");
        ElementObj effect = new Effect().createElement((this.getX() - 40) + "," + (this.getY() - 24) + ",effect,180,180");
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

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    private void spawnBullet(int vy, int damage) {
        ElementObj bulletTemplate = GameLoad.getObj("ebullet");
        if (bulletTemplate == null) {
            return;
        }
        int bulletX = this.getX() - 8;
        int bulletY = this.getY() + 36;
        ElementObj bullet = bulletTemplate.createElement(
                bulletX + "," + bulletY + "," + BOSS_BULLET + ",-10," + vy + "," + damage);
        em.addElement(bullet, GameElement.ENEMYFILE);
    }

    private static List<ImageIcon> loadBossFrames() {
        List<ImageIcon> frames = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            ImageIcon frame = GameLoad.getImage("image/images/boss/D_boss (" + i + ").png");
            if (frame != null) {
                frames.add(frame);
            }
        }
        return frames;
    }
}
