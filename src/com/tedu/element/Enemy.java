package com.tedu.element;

import com.tedu.manager.AudioPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;

public class Enemy extends ElementObj {
    private static final int HITBOX_W = 52;
    private static final int HITBOX_H = 72;
    private static final int MAX_STEP_UP = 12;
    private static final int GROUND_PROBE_INSET = 5;

    private final Random random = new Random();
    private final ElementManager em = ElementManager.getManager();

    private EnemyType type = EnemyType.ENEMY1;
    private int speed = 1;
    private int hp = 2;
    private boolean attacking = false;
    private boolean faceRight = false;
    private boolean firedThisAttack = false;
    private boolean countedKill = false;
    private long attackStartTime = -1L;
    private long nextAttackTime = 0L;
    private int desiredRange = 18;
    private int attackRange = 36;
    private ImageIcon currentFrame = firstFrame(EnemyType.ENEMY1.frames);

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
        ElementObj player = getPlayer();
        int x = this.getX() - GameRuntime.worldScrollX;
        if (player != null) {
            int dx = player.getCenterX() - this.getCenterX();
            faceRight = dx > 0;
            int distance = Math.abs(dx);
            if (!attacking && distance > desiredRange) {
                int desiredX = x + (dx > 0 ? speed : -speed);
                x = resolveGroundMove(x, desiredX);
            }
        }
        int footX = x + this.getW() / 2;
        this.setX(x);
        this.setY(GameRuntime.getBattlefieldMaxBottomAt(footX) - this.getH());
        if (this.getX() + this.getW() < -120 || this.getX() > GameJFrame.GameX + 180) {
            this.setLive(false);
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        ElementObj player = getPlayer();
        if (player != null) {
            faceRight = player.getCenterX() > this.getCenterX();
            int distance = Math.abs(player.getCenterX() - this.getCenterX());
            if (!attacking && distance <= attackRange && gameTime >= nextAttackTime) {
                attacking = true;
                attackStartTime = gameTime;
                firedThisAttack = false;
            }
        }
        if (attacking && attackStartTime >= 0 && gameTime - attackStartTime > type.attackDuration) {
            attacking = false;
            nextAttackTime = gameTime + randomBetween(type.cooldownMin, type.cooldownMax);
        }
        currentFrame = selectFrame(type.frames, gameTime, type.frameGap);
        if (currentFrame != null) {
            this.setIcon(currentFrame);
        }
    }

    @Override
    protected void add(long gameTime) {
        if (!attacking || firedThisAttack || attackStartTime < 0) {
            return;
        }
        if (gameTime - attackStartTime < type.attackWindup) {
            return;
        }
        firedThisAttack = true;
        if (type.melee) {
            performMeleeAttack(gameTime);
        } else {
            spawnProjectile();
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.type = EnemyType.fromKey(split.length > 2 ? split[2] : EnemyType.ENEMY1.key);
        this.speed = split.length > 3 ? Integer.parseInt(split[3]) : 2;
        this.hp = split.length > 4 ? Integer.parseInt(split[4]) : 2;
        this.speed = Math.max(1, speed + type.speedOffset);
        this.hp = Math.max(1, hp + type.hpOffset);
        this.setW(HITBOX_W);
        this.setH(HITBOX_H);
        int footX = this.getX() + this.getW() / 2;
        this.setY(GameRuntime.getBattlefieldMaxBottomAt(footX) - this.getH());
        this.desiredRange = randomBetween(type.rangeMin, type.rangeMax);
        this.attackRange = desiredRange + type.attackRangePadding;
        this.attacking = false;
        this.firedThisAttack = false;
        this.attackStartTime = -1L;
        this.nextAttackTime = 0L;
        this.countedKill = false;
        this.currentFrame = firstFrame(type.frames);
        if (currentFrame != null) {
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

    private void spawnProjectile() {
        ElementObj bulletTemplate = GameLoad.getObj("ebullet");
        if (bulletTemplate == null || type.projectilePath == null) {
            return;
        }
        ElementObj player = getPlayer();
        ImageIcon projectileIcon = GameLoad.getImage(type.projectilePath);
        int projectileW = projectileIcon == null ? 18 : projectileIcon.getIconWidth();
        int bulletX = faceRight ? this.getX() + this.getW() - 6 : this.getX() + 6 - projectileW;
        int bulletY = this.getY() + type.projectileSpawnOffsetY;
        double bulletVx;
        double bulletVy;
        if (type == EnemyType.ENEMY1) {
            int dx = player == null
                    ? (faceRight ? 150 : -150)
                    : player.getCenterX() - this.getCenterX();
            bulletVx = clamp(Math.abs(dx) / 24.0, 4.5, 8.0);
            bulletVx = dx >= 0 ? bulletVx : -bulletVx;
            bulletVy = -10.0 - Math.min(4.0, Math.abs(dx) / 90.0);
        } else {
            bulletVx = faceRight ? type.projectileSpeed : -type.projectileSpeed;
            if (player == null) {
                bulletVy = 0.0;
            } else {
                bulletVy = clamp(
                        (player.getCenterY() - (this.getY() + type.projectileSpawnOffsetY)) / (double) type.verticalAimDivisor,
                        type.minProjectileVy,
                        type.maxProjectileVy);
            }
        }
        ElementObj bullet = bulletTemplate.createElement(
                bulletX + "," + bulletY + "," + type.projectilePath + ","
                        + bulletVx + "," + bulletVy + "," + type.damage + ","
                        + type.projectileGravity + "," + (type.stopOnGround ? "1" : "0"));
        em.addElement(bullet, GameElement.ENEMYFILE);
    }

    private void performMeleeAttack(long gameTime) {
        ElementObj playerObj = getPlayer();
        if (!(playerObj instanceof PaoPao)) {
            return;
        }
        if (!resolveMeleeHitbox().intersects(playerObj.getRectangle())) {
            return;
        }
        PaoPao player = (PaoPao) playerObj;
        long oldHurtTime = player.getHurtTime();
        player.hurt(gameTime, type.damage);
        if (player.getHurtTime() != oldHurtTime) {
            AudioPlayer.playOnce("music/die.wav");
        }
    }

    private Rectangle resolveMeleeHitbox() {
        int reach = type.meleeReach;
        int hitX = faceRight ? this.getCenterX() - 4 : this.getCenterX() - reach + 4;
        int hitY = this.getY() - 4;
        return new Rectangle(hitX, hitY, reach, this.getH() + 8);
    }

    private ElementObj getPlayer() {
        List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        return plays.isEmpty() ? null : plays.get(0);
    }

    private ImageIcon selectFrame(List<ImageIcon> frames, long gameTime, int frameGap) {
        if (frames == null || frames.isEmpty()) {
            return null;
        }
        int index = (int) ((gameTime / Math.max(1, frameGap)) % frames.size());
        return frames.get(index);
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

    private int randomBetween(int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static ImageIcon firstFrame(List<ImageIcon> frames) {
        return frames == null || frames.isEmpty() ? null : frames.get(0);
    }

    private enum EnemyType {
        ENEMY1("enemy1",
                GameLoad.loadFrames(
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-217.png",
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-218.png",
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-219.png",
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-220.png",
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-221.png",
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-222.png",
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-223.png",
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-224.png",
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-225.png",
                        "image/images/Enemy/enemy1/20070130041731OFQDOJuj-226.png"),
                "image/images/Enemy/enemy1/20070130041731OFQDOJuj-215.png",
                0, 0,
                120, 180, 36,
                10, 36, 58, 90,
                4, 1,
                0.0, 0.45, true,
                18, 24, -2.0, 2.0,
                false, 0),
        ENEMY2("enemy2",
                GameLoad.loadFrames(
                        "image/images/Enemy/enemy2/enemy_attack000.png",
                        "image/images/Enemy/enemy2/enemy_attack010.png",
                        "image/images/Enemy/enemy2/enemy_attack020.png",
                        "image/images/Enemy/enemy2/enemy_attack030.png"),
                null,
                1, 1,
                6, 14, 18,
                8, 20, 22, 36,
                5, 1,
                0.0, 0.0, false,
                14, 18, -1.0, 1.0,
                true, 46),
        ENEMY3("enemy3",
                GameLoad.loadFrames(
                        "image/images/Enemy/enemy3/20070130041731OFQDOJuj-211.png",
                        "image/images/Enemy/enemy3/20070130041731OFQDOJuj-212.png",
                        "image/images/Enemy/enemy3/20070130041731OFQDOJuj-213.png",
                        "image/images/Enemy/enemy3/20070130041731OFQDOJuj-214.png",
                        "image/images/Enemy/enemy3/20070130041731OFQDOJuj-215.png",
                        "image/images/Enemy/enemy3/20070130041731OFQDOJuj-227.png",
                        "image/images/Enemy/enemy3/20070130041731OFQDOJuj-228.png",
                        "image/images/Enemy/enemy3/20070130041731OFQDOJuj-229.png"),
                "image/images/Enemy/enemy3/20070130041731OFQDOJuj-230.png",
                0, 0,
                130, 190, 42,
                12, 30, 48, 72,
                5, 1,
                7.0, 0.12, true,
                20, 26, -2.5, 3.0,
                false, 0),
        ENEMY4("enemy4",
                GameLoad.loadFrames(
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-200.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-201.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-202.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-203.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-204.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-205.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-206.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-207.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-208.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-209.png",
                        "image/images/Enemy/enemy4/20070130041731OFQDOJuj-210.png"),
                "image/images/Enemy/enemy4/bomb1.png",
                1, 0,
                110, 160, 52,
                8, 18, 20, 36,
                5, 1,
                11.0, 0.0, false,
                18, 18, -3.0, 3.0,
                false, 0);

        private final String key;
        private final List<ImageIcon> frames;
        private final String projectilePath;
        private final int speedOffset;
        private final int hpOffset;
        private final int rangeMin;
        private final int rangeMax;
        private final int attackRangePadding;
        private final int attackWindup;
        private final int attackDuration;
        private final int cooldownMin;
        private final int cooldownMax;
        private final int frameGap;
        private final int damage;
        private final double projectileSpeed;
        private final double projectileGravity;
        private final boolean stopOnGround;
        private final int projectileSpawnOffsetY;
        private final int verticalAimDivisor;
        private final double minProjectileVy;
        private final double maxProjectileVy;
        private final boolean melee;
        private final int meleeReach;

        EnemyType(String key, List<ImageIcon> frames, String projectilePath,
                  int speedOffset, int hpOffset,
                  int rangeMin, int rangeMax, int attackRangePadding,
                  int attackWindup, int attackDuration, int cooldownMin, int cooldownMax,
                  int frameGap, int damage,
                  double projectileSpeed, double projectileGravity, boolean stopOnGround,
                  int projectileSpawnOffsetY, int verticalAimDivisor,
                  double minProjectileVy, double maxProjectileVy,
                  boolean melee, int meleeReach) {
            this.key = key;
            this.frames = frames;
            this.projectilePath = projectilePath;
            this.speedOffset = speedOffset;
            this.hpOffset = hpOffset;
            this.rangeMin = rangeMin;
            this.rangeMax = rangeMax;
            this.attackRangePadding = attackRangePadding;
            this.attackWindup = attackWindup;
            this.attackDuration = attackDuration;
            this.cooldownMin = cooldownMin;
            this.cooldownMax = cooldownMax;
            this.frameGap = frameGap;
            this.damage = damage;
            this.projectileSpeed = projectileSpeed;
            this.projectileGravity = projectileGravity;
            this.stopOnGround = stopOnGround;
            this.projectileSpawnOffsetY = projectileSpawnOffsetY;
            this.verticalAimDivisor = verticalAimDivisor;
            this.minProjectileVy = minProjectileVy;
            this.maxProjectileVy = maxProjectileVy;
            this.melee = melee;
            this.meleeReach = meleeReach;
        }

        private static EnemyType fromKey(String key) {
            if (key != null) {
                for (EnemyType value : values()) {
                    if (value.key.equalsIgnoreCase(key.trim())) {
                        return value;
                    }
                }
            }
            return ENEMY1;
        }
    }
}
