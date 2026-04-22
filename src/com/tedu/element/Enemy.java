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
    private static final int MAX_SNAP_DOWN = 8;
    private static final int GROUND_PROBE_INSET = 5;
    private static final int PLATFORM_EDGE_MARGIN = 6;
    private static final int WALL_BODY_MARGIN = 4;

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
    private boolean attackFaceRight = false;
    private int attackTargetCenterX = Integer.MIN_VALUE;
    private int attackTargetCenterY = Integer.MIN_VALUE;
    private int attackTargetDistanceX = 150;
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
        // Enemy source frames are authored facing left; flip only when logically facing right.
        if (faceRight) {
            g.drawImage(frame.getImage(), drawX + drawW, drawY, -drawW, drawH, null);
        } else {
            g.drawImage(frame.getImage(), drawX, drawY, drawW, drawH, null);
        }
    }

    @Override
    protected void move() {
        ElementObj player = getPlayer();
        int x = this.getX() - GameRuntime.worldScrollX;
        int currentBottom = this.getY() + this.getH();
        if (player != null && !attacking) {
            int dx = player.getCenterX() - (x + this.getW() / 2);
            faceRight = dx > 0;
            int distance = Math.abs(dx);
            if (distance > desiredRange) {
                int desiredX = x + (dx > 0 ? speed : -speed);
                x = resolveGroundMove(x, desiredX);
            }
        }
        if (attacking) {
            faceRight = attackFaceRight;
        }
        int footX = x + this.getW() / 2;
        this.setX(x);
        this.setY(getWalkSupportBottomAt(footX, currentBottom) - this.getH());
        if (this.getX() + this.getW() < -120 || this.getX() > GameJFrame.GameX + 180) {
            this.setLive(false);
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        ElementObj player = getPlayer();
        if (player != null && !attacking) {
            faceRight = player.getCenterX() > this.getCenterX();
            int distance = Math.abs(player.getCenterX() - this.getCenterX());
            if (distance <= attackRange && gameTime >= nextAttackTime) {
                beginAttack(player, gameTime);
            }
        }
        if (attacking && attackStartTime >= 0 && gameTime - attackStartTime > resolveAttackDuration()) {
            attacking = false;
            nextAttackTime = gameTime + randomBetween(type.cooldownMin, type.cooldownMax);
            clearAttackTarget();
        }
        if (attacking && attackStartTime >= 0) {
            currentFrame = selectAttackFrame(type.frames, gameTime - attackStartTime, type.attackFrameGap);
        } else {
            currentFrame = selectLoopFrame(type.frames, gameTime, type.frameGap);
        }
        if (currentFrame != null) {
            this.setIcon(currentFrame);
        }
    }

    @Override
    protected void add(long gameTime) {
        if (!attacking || firedThisAttack || attackStartTime < 0) {
            return;
        }
        ElementObj player = getPlayer();
        refreshAttackAim(player);
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
        this.setY(getWalkSupportBottomAt(footX, GameRuntime.getBattlefieldMaxBottomAt(footX)) - this.getH());
        this.desiredRange = randomBetween(type.rangeMin, type.rangeMax);
        this.attackRange = desiredRange + type.attackRangePadding;
        this.attacking = false;
        this.firedThisAttack = false;
        this.attackStartTime = -1L;
        this.nextAttackTime = 0L;
        this.countedKill = false;
        this.attackFaceRight = false;
        clearAttackTarget();
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
        refreshAttackAim(player);
        ImageIcon projectileIcon = GameLoad.getImage(type.projectilePath);
        int projectileW = projectileIcon == null ? 18 : projectileIcon.getIconWidth();
        boolean firingRight = attacking ? attackFaceRight : faceRight;
        int bulletX = firingRight ? this.getX() + this.getW() - 6 : this.getX() + 6 - projectileW;
        int bulletY = this.getY() + type.projectileSpawnOffsetY;
        int targetCenterX = resolveAttackTargetCenterX(player);
        int targetCenterY = resolveAttackTargetCenterY(player);
        double bulletVx;
        double bulletVy;
        if (type == EnemyType.ENEMY1) {
            double horizontalDistance = Math.max(24.0, attackTargetDistanceX);
            bulletVx = clamp(horizontalDistance / 24.0, 4.5, 8.0);
            bulletVx = firingRight ? bulletVx : -bulletVx;
            bulletVy = -10.0 - Math.min(4.0, horizontalDistance / 90.0);
        } else {
            bulletVx = firingRight ? type.projectileSpeed : -type.projectileSpeed;
            if (targetCenterY == Integer.MIN_VALUE) {
                bulletVy = 0.0;
            } else {
                bulletVy = clamp(
                        (targetCenterY - (this.getY() + type.projectileSpawnOffsetY)) / (double) type.verticalAimDivisor,
                        type.minProjectileVy,
                        type.maxProjectileVy);
            }
        }
        ElementObj bullet = bulletTemplate.createElement(
                bulletX + "," + bulletY + "," + type.projectilePath + ","
                        + bulletVx + "," + bulletVy + "," + type.damage + ","
                        + type.projectileGravity + "," + (type.stopOnGround ? "1" : "0") + ","
                        + type.impactEffectSize);
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
        boolean rightFacing = attacking ? attackFaceRight : faceRight;
        int hitX = rightFacing ? this.getCenterX() - 4 : this.getCenterX() - reach + 4;
        int hitY = this.getY() - 4;
        return new Rectangle(hitX, hitY, reach, this.getH() + 8);
    }

    private ElementObj getPlayer() {
        List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        return plays.isEmpty() ? null : plays.get(0);
    }

    private long resolveAttackDuration() {
        if (type.frames == null || type.frames.isEmpty()) {
            return type.attackDuration;
        }
        long minimumDuration = (long) Math.max(0, type.frames.size() - 1) * Math.max(1, type.attackFrameGap);
        return Math.max(type.attackDuration, minimumDuration);
    }

    private ImageIcon selectLoopFrame(List<ImageIcon> frames, long gameTime, int frameGap) {
        if (frames == null || frames.isEmpty()) {
            return null;
        }
        int index = (int) ((gameTime / Math.max(1, frameGap)) % frames.size());
        return frames.get(index);
    }

    private ImageIcon selectAttackFrame(List<ImageIcon> frames, long elapsedTime, int frameGap) {
        if (frames == null || frames.isEmpty()) {
            return null;
        }
        int index = (int) (elapsedTime / Math.max(1, frameGap));
        if (index < 0) {
            index = 0;
        }
        if (index >= frames.size()) {
            index = frames.size() - 1;
        }
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
            int wallTopBottom = GameRuntime.getBattlefieldWallTopBottomAt(frontX);
            if (wallTopBottom > 0 && actorBottom > wallTopBottom - WALL_BODY_MARGIN) {
                break;
            }
            int frontSurface = getWalkSupportBottomAt(frontX, actorBottom);
            if (actorBottom - frontSurface > MAX_STEP_UP) {
                break;
            }
            if (frontSurface - actorBottom > MAX_SNAP_DOWN) {
                break;
            }
            resolvedX = candidateX;
            actorBottom = frontSurface;
        }
        return resolvedX;
    }

    private int getWalkSupportBottomAt(int footX, int referenceBottom) {
        int bestBottom = GameRuntime.getBattlefieldMaxBottomAt(footX);
        List<ElementObj> platforms = em.getElementsByKey(GameElement.PLATFORM);
        for (ElementObj elementObj : platforms) {
            if (!(elementObj instanceof PlatformObj)) {
                continue;
            }
            PlatformObj platform = (PlatformObj) elementObj;
            if (!platform.isLive() || !isWithinPlatformSpan(platform, footX)) {
                continue;
            }
            int platformBottom = platform.getTopSurfaceY();
            if (referenceBottom - platformBottom > MAX_STEP_UP) {
                continue;
            }
            if (platformBottom - referenceBottom > MAX_SNAP_DOWN) {
                continue;
            }
            if (platformBottom < bestBottom) {
                bestBottom = platformBottom;
            }
        }
        return bestBottom;
    }

    private boolean isWithinPlatformSpan(PlatformObj platform, int footX) {
        int margin = resolvePlatformEdgeMargin(platform);
        return footX >= platform.getX() + margin
                && footX <= platform.getX() + platform.getW() - margin;
    }

    private int resolvePlatformEdgeMargin(PlatformObj platform) {
        return Math.min(PLATFORM_EDGE_MARGIN, Math.max(0, platform.getW() / 3));
    }

    private int randomBetween(int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    private void beginAttack(ElementObj player, long gameTime) {
        attacking = true;
        attackStartTime = gameTime;
        firedThisAttack = false;
        refreshAttackAim(player);
    }

    private void refreshAttackAim(ElementObj player) {
        int selfCenterX = this.getCenterX();
        if (player == null || !player.isLive()) {
            attackFaceRight = faceRight;
            attackTargetCenterX = selfCenterX + (attackFaceRight ? 150 : -150);
            attackTargetCenterY = this.getCenterY();
            attackTargetDistanceX = 150;
            return;
        }
        attackFaceRight = player.getCenterX() >= selfCenterX;
        faceRight = attackFaceRight;
        attackTargetCenterX = player.getCenterX();
        attackTargetCenterY = player.getCenterY();
        attackTargetDistanceX = Math.max(24, Math.abs(player.getCenterX() - selfCenterX));
    }

    private int resolveAttackTargetCenterX(ElementObj player) {
        if (attackTargetCenterX != Integer.MIN_VALUE) {
            return attackTargetCenterX;
        }
        if (player != null) {
            return player.getCenterX();
        }
        return this.getCenterX() + ((attacking ? attackFaceRight : faceRight) ? 150 : -150);
    }

    private int resolveAttackTargetCenterY(ElementObj player) {
        if (attackTargetCenterY != Integer.MIN_VALUE) {
            return attackTargetCenterY;
        }
        return player == null ? Integer.MIN_VALUE : player.getCenterY();
    }

    private void clearAttackTarget() {
        attackTargetCenterX = Integer.MIN_VALUE;
        attackTargetCenterY = Integer.MIN_VALUE;
        attackTargetDistanceX = 150;
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
                20, 72, 110, 150,
                5, 12, 1,
                0.0, 0.45, true,
                18, 24, -2.0, 2.0,
                false, 0, 96),
        ENEMY2("enemy2",
                GameLoad.loadFrames(
                        "image/images/Enemy/enemy2/enemy_attack000.png",
                        "image/images/Enemy/enemy2/enemy_attack010.png",
                        "image/images/Enemy/enemy2/enemy_attack020.png",
                        "image/images/Enemy/enemy2/enemy_attack030.png"),
                null,
                1, 1,
                6, 14, 18,
                18, 42, 90, 120,
                7, 14, 1,
                0.0, 0.0, false,
                14, 18, -1.0, 1.0,
                true, 46, 0),
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
                20, 70, 95, 135,
                5, 12, 1,
                7.0, 0.12, true,
                20, 26, -2.5, 3.0,
                false, 0, 110),
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
                18, 60, 75, 110,
                5, 12, 1,
                11.0, 0.0, false,
                18, 18, -3.0, 3.0,
                false, 0, 120);

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
        private final int attackFrameGap;
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
        private final int impactEffectSize;

        EnemyType(String key, List<ImageIcon> frames, String projectilePath,
                  int speedOffset, int hpOffset,
                  int rangeMin, int rangeMax, int attackRangePadding,
                  int attackWindup, int attackDuration, int cooldownMin, int cooldownMax,
                  int frameGap, int attackFrameGap, int damage,
                  double projectileSpeed, double projectileGravity, boolean stopOnGround,
                  int projectileSpawnOffsetY, int verticalAimDivisor,
                  double minProjectileVy, double maxProjectileVy,
                  boolean melee, int meleeReach, int impactEffectSize) {
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
            this.attackFrameGap = attackFrameGap;
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
            this.impactEffectSize = impactEffectSize;
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
