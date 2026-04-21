package com.tedu.element;

import com.tedu.manager.AudioPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class PaoPao extends ElementObj {
    private static final int HITBOX_W = 34;
    private static final int STAND_H = 54;
    private static final int CROUCH_H = 34;
    private static final int GROUND_LAYER_OVERLAP = 6;
    private static final int CROUCH_LAYER_OVERLAP = 10;
    private static final long INVINCIBLE_WINDOW = 100;
    private static final int MOVE_FRAME_GAP = 6;
    private static final int AIR_FRAME_GAP = 5;
    private static final int SHOOT_FRAME_GAP = 2;
    private static final int KNIFE_FRAME_GAP = 3;
    private static final String PLAYERS_ROOT = "image/images/plays/";
    private static final String LOWER_BODY_ROOT = PLAYERS_ROOT + "\u4e0b\u534a\u8eab/";
    private static final String WEAPON1_UPPER_ROOT = PLAYERS_ROOT + "\u4e0a\u534a\u8eab/\u6b66\u56681/";
    private static final String WEAPON2_UPPER_ROOT = PLAYERS_ROOT + "\u4e0a\u534a\u8eab/\u6b66\u56682/";
    private static final String KNIFE_UPPER_ROOT = PLAYERS_ROOT + "\u4e0a\u534a\u8eab/\u6b66\u56683/";
    private static final DirectionalFrames LOWER_STAND = new DirectionalFrames(
            LOWER_BODY_ROOT + "left/stand",
            LOWER_BODY_ROOT + "right/stand");
    private static final DirectionalFrames LOWER_RUN = new DirectionalFrames(
            LOWER_BODY_ROOT + "left/run",
            LOWER_BODY_ROOT + "right/run");
    private static final DirectionalFrames LOWER_CROUCH_IDLE = new DirectionalFrames(
            LOWER_BODY_ROOT + "left",
            LOWER_BODY_ROOT + "right");
    private static final DirectionalFrames LOWER_CROUCH_RUN = new DirectionalFrames(
            LOWER_BODY_ROOT + "left/squat_run",
            LOWER_BODY_ROOT + "right/squat_run");
    private static final DirectionalFrames LOWER_JUMP = new DirectionalFrames(
            LOWER_BODY_ROOT + "left/jump",
            LOWER_BODY_ROOT + "right/jump");
    private static final DirectionalFrames UPPER_AIM_UP_W1 = new DirectionalFrames(
            WEAPON1_UPPER_ROOT + "left/jump",
            WEAPON1_UPPER_ROOT + "right/jump");
    private static final DirectionalFrames UPPER_ATTACK_W1 = new DirectionalFrames(
            WEAPON1_UPPER_ROOT + "left/attack",
            WEAPON1_UPPER_ROOT + "right/attack");
    private static final DirectionalFrames UPPER_AIM_UP_W2 = new DirectionalFrames(
            WEAPON2_UPPER_ROOT + "left/jump",
            WEAPON2_UPPER_ROOT + "right/jump");
    private static final DirectionalFrames UPPER_ATTACK_W2 = new DirectionalFrames(
            WEAPON2_UPPER_ROOT + "left/attack",
            WEAPON2_UPPER_ROOT + "right/attack");
    private static final DirectionalFrames UPPER_KNIFE = new DirectionalFrames(
            KNIFE_UPPER_ROOT + "left",
            KNIFE_UPPER_ROOT + "right");
    private static final String PLAYER_BULLET_LEFT = "image/images/子弹/left/bullet00.png";
    private static final String PLAYER_BULLET_RIGHT = "image/images/子弹/right/bullet01.png";
    private static final String PLAYER_HEAVY_BULLET_LEFT = "image/images/子弹/left/bullet30.png";
    private static final String PLAYER_HEAVY_BULLET_RIGHT = "image/images/子弹/right/bullet31.png";
    private static final Map<String, Point> MUZZLE_CACHE = new HashMap<>();
    private static final Map<ImageIcon, SpriteMetrics> SPRITE_METRICS_CACHE = new IdentityHashMap<>();

    private final ElementManager em = ElementManager.getManager();

    private long imgtime = 0;
    private WeaponType currentWeapon = WeaponType.RIFLE;
    private boolean weapon2Unlocked = false;
    private ImageIcon currentUpperFrame = lastFrame(UPPER_ATTACK_W1.select(true));
    private ImageIcon currentLowerFrame = firstFrame(LOWER_STAND.select(true));
    private int hp = 3;
    private int grenades = 8;
    private long hurtTime = -1000;
    private int speed = 5;
    private double groundBottom = 0;
    private double vy = 0;
    private boolean onGround = true;
    private final double gravity = 0.75;
    private final double jumpVelocity = -12.8;
    private boolean aimUp;
    private boolean crouch;
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private boolean faceRight = true;
    private boolean firing;
    private boolean knifeQueued;
    private boolean grenadeQueued;
    private long fireTime = -100;
    private long knifeTime = -100;
    private long grenadeTime = -100;
    private long shootAnimStart = -1;
    private long shootAnimUntil = -1;
    private long knifeAnimStart = -1;
    private long knifeAnimUntil = -1;

    @Override
    public void showElement(Graphics g) {
        if (imgtime - hurtTime < INVINCIBLE_WINDOW && ((imgtime / 4) % 2 == 0)) {
            return;
        }
        SpritePose pose = buildPose(currentUpperFrame, currentLowerFrame, isShootAnimating(imgtime), isKnifeAnimating(imgtime));
        if (pose.lower != null) {
            g.drawImage(pose.lower.getImage(),
                    pose.lowerX, pose.lowerY,
                    pose.lower.getIconWidth(), pose.lower.getIconHeight(),
                    null);
        }
        if (pose.upper != null) {
            g.drawImage(pose.upper.getImage(),
                    pose.upperX, pose.upperY,
                    pose.upper.getIconWidth(), pose.upper.getIconHeight(),
                    null);
        }
    }

    @Override
    protected void updateImage(long time) {
        imgtime = time;
        FrameSelection selection = selectFrames(time);
        currentUpperFrame = selection.upper;
        currentLowerFrame = selection.lower;
        ImageIcon representative = currentUpperFrame != null ? currentUpperFrame : currentLowerFrame;
        if (representative != null) {
            this.setIcon(representative);
        }
    }

    @Override
    protected void move() {
        updateHitbox();
        int x = this.getX();
        double y = this.getY();
        int moveSpeed = isGroundCrouching() ? 2 : speed;
        boolean worldScrolling = GameRuntime.worldScrollX > 0
                && right
                && !left
                && this.getX() >= getAnchorX()
                && !isGroundCrouching();
        groundBottom = clampGroundBottom(groundBottom <= 0 ? this.getY() + this.getH() : groundBottom);
        if (left) {
            x -= moveSpeed;
        }
        if (right && !worldScrolling) {
            x += moveSpeed;
        }
        if (onGround) {
            if (up && !down) {
                groundBottom -= moveSpeed;
            } else if (down && !up && !crouch) {
                groundBottom += moveSpeed;
            }
            groundBottom = clampGroundBottom(groundBottom);
            y = groundBottom - this.getH();
        } else {
            vy += gravity;
            y += vy;
            double landingY = groundBottom - this.getH();
            if (y >= landingY) {
                y = landingY;
                vy = 0;
                onGround = true;
            }
        }
        if (x < 0) {
            x = 0;
        }
        if (x > GameJFrame.GameX - this.getW()) {
            x = GameJFrame.GameX - this.getW();
        }
        if (y < 0) {
            y = 0;
            if (vy < 0) {
                vy = 0;
            }
        }
        this.setX(x);
        this.setY((int) y);
        if (onGround) {
            groundBottom = this.getY() + this.getH();
        }
    }

    @Override
    public void keyClick(boolean bl, int key) {
        switch (key) {
            case 37:
            case 65:
                left = bl;
                if (bl) {
                    faceRight = false;
                }
                break;
            case 38:
                up = bl;
                break;
            case 40:
                down = bl;
                break;
            case 87:
                up = bl;
                break;
            case 69:
                aimUp = bl;
                break;
            case 83:
                down = bl;
                break;
            case 16:
                crouch = bl;
                break;
            case 39:
            case 68:
                right = bl;
                if (bl) {
                    faceRight = true;
                }
                break;
            case 74:
                firing = bl;
                break;
            case 32:
                if (bl && onGround) {
                    onGround = false;
                    vy = jumpVelocity;
                }
                break;
            case 76:
                if (bl) {
                    knifeQueued = true;
                }
                break;
            case 85:
                if (bl) {
                    grenadeQueued = true;
                }
                break;
            case 49:
            case 97:
                if (bl) {
                    setWeapon(1);
                }
                break;
            case 50:
            case 98:
                if (bl) {
                    setWeapon(2);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void add(long gameTime) {
        if (knifeQueued) {
            knifeQueued = false;
            if (gameTime - knifeTime >= 28) {
                knifeTime = gameTime;
                knifeAnimStart = gameTime;
                knifeAnimUntil = gameTime + getKnifeAnimationDuration();
                AudioPlayer.playOnce("music/knife.wav");
                performKnifeHit();
            }
        }
        if (grenadeQueued) {
            grenadeQueued = false;
            if (grenades > 0 && gameTime - grenadeTime >= 35) {
                grenades--;
                grenadeTime = gameTime;
                spawnGrenade();
            }
        }
        if (!firing || gameTime < knifeAnimUntil || gameTime - fireTime < currentWeapon.fireInterval) {
            return;
        }
        fireTime = gameTime;
        shootAnimStart = gameTime;
        shootAnimUntil = gameTime + getShootAnimationDuration();
        ElementObj bulletTemplate = GameLoad.getObj("bullet");
        if (bulletTemplate == null) {
            return;
        }
        String bulletPath = resolvePlayerBulletPath();
        ImageIcon bulletIcon = GameLoad.getImage(bulletPath);
        int bulletW = bulletIcon == null ? 24 : bulletIcon.getIconWidth();
        int bulletH = bulletIcon == null ? 8 : bulletIcon.getIconHeight();
        Point muzzle = resolveBulletOrigin();
        int bulletX;
        int bulletY;
        int bulletVx;
        int bulletVy;
        int bulletSpeed = currentWeapon.bulletSpeed;
        if (isGroundCrouching()) {
            bulletX = faceRight ? muzzle.x : muzzle.x - bulletW;
            bulletY = muzzle.y - bulletH / 2;
            bulletVx = faceRight ? bulletSpeed : -bulletSpeed;
            bulletVy = 0;
        } else if (aimUp && onGround) {
            bulletX = muzzle.x - bulletW / 2;
            bulletY = muzzle.y - bulletH;
            bulletVx = 0;
            bulletVy = -bulletSpeed;
        } else if (!onGround && aimUp) {
            bulletX = faceRight ? muzzle.x : muzzle.x - bulletW;
            bulletY = muzzle.y - bulletH / 2;
            bulletVx = faceRight ? Math.max(8, bulletSpeed - 3) : -Math.max(8, bulletSpeed - 3);
            bulletVy = -Math.max(6, bulletSpeed - 5);
        } else {
            bulletX = faceRight ? muzzle.x : muzzle.x - bulletW;
            bulletY = muzzle.y - bulletH / 2;
            bulletVx = faceRight ? bulletSpeed : -bulletSpeed;
            bulletVy = 0;
        }
        ElementObj bullet = bulletTemplate.createElement(
                bulletX + "," + bulletY + "," + bulletPath + "," + bulletVx + "," + bulletVy + "," + currentWeapon.damage);
        em.addElement(bullet, GameElement.PLAYFILE);
        AudioPlayer.playOnce("music/buzi.wav");
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.setW(HITBOX_W);
        this.setH(STAND_H);
        this.currentWeapon = WeaponType.RIFLE;
        this.weapon2Unlocked = false;
        this.currentUpperFrame = lastFrame(currentWeapon.attack.select(true));
        this.currentLowerFrame = firstFrame(LOWER_STAND.select(true));
        this.groundBottom = this.getY() + this.getH();
        ImageIcon representative = currentUpperFrame != null ? currentUpperFrame : currentLowerFrame;
        if (representative != null) {
            this.setIcon(representative);
        }
        return this;
    }

    public void placeAtStageStart() {
        updateHitbox();
        this.setX(GameLoad.resolvePlayerSpawnX(this.getW()));
        this.onGround = true;
        this.vy = 0;
        this.groundBottom = GameRuntime.getBattlefieldMaxBottom();
        this.setY((int) Math.round(groundBottom - this.getH()));
    }

    public void hurt(long gameTime, int damage) {
        if (gameTime - hurtTime < INVINCIBLE_WINDOW) {
            return;
        }
        hurtTime = gameTime;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            this.setLive(false);
        }
    }

    public int getHp() {
        return hp;
    }

    public int getGrenades() {
        return grenades;
    }

    public String getWeaponName() {
        return currentWeapon.label;
    }

    public boolean hasWeapon2() {
        return weapon2Unlocked;
    }

    public long getHurtTime() {
        return hurtTime;
    }

    public void addGrenades(int amount) {
        grenades = Math.max(0, grenades + amount);
    }

    public void unlockWeapon(int weaponId) {
        if (weaponId == 2) {
            weapon2Unlocked = true;
        }
    }

    public void setWeapon(int weaponId) {
        WeaponType nextWeapon = currentWeapon;
        if (weaponId == 1) {
            nextWeapon = WeaponType.RIFLE;
        } else if (weaponId == 2 && weapon2Unlocked) {
            nextWeapon = WeaponType.HEAVY;
        }
        if (nextWeapon == currentWeapon) {
            return;
        }
        currentWeapon = nextWeapon;
        GameRuntime.showBanner("切换武器: " + currentWeapon.label, 1200);
    }

    public int prepareWorldScroll(int remainingDistance) {
        if (remainingDistance <= 0 || !right || left || isGroundCrouching()) {
            return 0;
        }
        if (this.getX() < getAnchorX()) {
            return 0;
        }
        return Math.min(speed, remainingDistance);
    }

    @Override
    public void die() {
        AudioPlayer.playOnce("music/die.wav");
        ElementObj effect = new Effect().createElement((this.getX() - 20) + "," + (this.getY() - 10) + ",effect,96,96");
        em.addElement(effect, GameElement.DIE);
    }

    private boolean isGroundCrouching() {
        return onGround && crouch;
    }

    private boolean isShootAnimating(long time) {
        return time < shootAnimUntil;
    }

    private boolean isKnifeAnimating(long time) {
        return time < knifeAnimUntil;
    }

    private void updateHitbox() {
        int footY = this.getY() + this.getH();
        int targetHeight = isGroundCrouching() ? CROUCH_H : STAND_H;
        this.setW(HITBOX_W);
        if (this.getH() != targetHeight) {
            this.setY(footY - targetHeight);
            this.setH(targetHeight);
        }
    }

    private FrameSelection selectFrames(long time) {
        ImageIcon lowerFrame;
        ImageIcon upperFrame;
        List<ImageIcon> aimUpFrames = currentWeapon.aimUp.select(faceRight);
        List<ImageIcon> attackFrames = currentWeapon.attack.select(faceRight);
        ImageIcon idleUpper = lastFrame(attackFrames);
        if (!onGround) {
            lowerFrame = selectLoopFrame(LOWER_JUMP.select(faceRight), time, AIR_FRAME_GAP);
            if (isShootAnimating(time)) {
                upperFrame = selectOneShotFrame(attackFrames, shootAnimStart, time, SHOOT_FRAME_GAP);
            } else if (aimUp) {
                upperFrame = selectLoopFrame(aimUpFrames, time, AIR_FRAME_GAP);
            } else {
                upperFrame = idleUpper;
            }
            return new FrameSelection(upperFrame, lowerFrame);
        }
        if (isKnifeAnimating(time)) {
            lowerFrame = firstFrame(isGroundCrouching()
                    ? LOWER_CROUCH_IDLE.select(faceRight)
                    : LOWER_STAND.select(faceRight));
            upperFrame = selectOneShotFrame(UPPER_KNIFE.select(faceRight), knifeAnimStart, time, KNIFE_FRAME_GAP);
            return new FrameSelection(upperFrame, lowerFrame);
        }
        if (isGroundCrouching()) {
            if ((left ^ right) || (up ^ down)) {
                lowerFrame = selectLoopFrame(LOWER_CROUCH_RUN.select(faceRight), time, MOVE_FRAME_GAP);
            } else {
                lowerFrame = firstFrame(LOWER_CROUCH_IDLE.select(faceRight));
            }
            if (isShootAnimating(time)) {
                upperFrame = selectOneShotFrame(attackFrames, shootAnimStart, time, SHOOT_FRAME_GAP);
            } else if (aimUp) {
                upperFrame = firstFrame(aimUpFrames);
            } else {
                upperFrame = idleUpper;
            }
            return new FrameSelection(upperFrame, lowerFrame);
        }
        if ((left ^ right) || (up ^ down)) {
            lowerFrame = selectLoopFrame(LOWER_RUN.select(faceRight), time, MOVE_FRAME_GAP);
        } else {
            lowerFrame = firstFrame(LOWER_STAND.select(faceRight));
        }
        if (isShootAnimating(time)) {
            upperFrame = selectOneShotFrame(attackFrames, shootAnimStart, time, SHOOT_FRAME_GAP);
        } else if (aimUp) {
            upperFrame = firstFrame(aimUpFrames);
        } else {
            upperFrame = idleUpper;
        }
        return new FrameSelection(upperFrame, lowerFrame);
    }

    private static ImageIcon firstFrame(List<ImageIcon> frames) {
        return frames == null || frames.isEmpty() ? null : frames.get(0);
    }

    private static ImageIcon lastFrame(List<ImageIcon> frames) {
        return frames == null || frames.isEmpty() ? null : frames.get(frames.size() - 1);
    }

    private ImageIcon selectLoopFrame(List<ImageIcon> frames, long time, int frameGap) {
        if (frames == null || frames.isEmpty()) {
            return null;
        }
        int index = (int) ((time / Math.max(1, frameGap)) % frames.size());
        return frames.get(index);
    }

    private ImageIcon selectOneShotFrame(List<ImageIcon> frames, long startTime, long time, int frameGap) {
        if (frames == null || frames.isEmpty()) {
            return null;
        }
        if (startTime < 0) {
            return frames.get(0);
        }
        int index = (int) ((time - startTime) / Math.max(1, frameGap));
        if (index < 0) {
            index = 0;
        }
        if (index >= frames.size()) {
            index = frames.size() - 1;
        }
        return frames.get(index);
    }

    private SpritePose buildPose(ImageIcon upper, ImageIcon lower, boolean shooting, boolean knife) {
        int footY = this.getY() + this.getH();
        int lowerW = lower == null ? 0 : lower.getIconWidth();
        int lowerH = lower == null ? 0 : lower.getIconHeight();
        int lowerX = this.getX() + (this.getW() - lowerW) / 2;
        int lowerY = footY - lowerH;
        int upperW = upper == null ? 0 : upper.getIconWidth();
        int upperH = upper == null ? 0 : upper.getIconHeight();
        int overlap = isGroundCrouching() ? CROUCH_LAYER_OVERLAP : GROUND_LAYER_OVERLAP;
        SpriteMetrics upperMetrics = getSpriteMetrics(upper);
        SpriteMetrics lowerMetrics = getSpriteMetrics(lower);
        int upperX = resolveUpperX(upperW, lowerX, upperMetrics, lowerMetrics, shooting, knife);
        int upperY = resolveUpperY(upperH, lowerY, upperMetrics, lowerMetrics, overlap);
        return new SpritePose(upper, lower, upperX, upperY, lowerX, lowerY);
    }

    private int resolveUpperX(int upperW, int lowerX, SpriteMetrics upperMetrics, SpriteMetrics lowerMetrics,
                              boolean shooting, boolean knife) {
        int centeredX = this.getX() + (this.getW() - upperW) / 2;
        if (upperMetrics == null || lowerMetrics == null) {
            return centeredX + getUpperAnchorGapX(shooting, knife);
        }
        return lowerX + lowerMetrics.lowerAnchorX - upperMetrics.upperAnchorX + getUpperAnchorGapX(shooting, knife);
    }

    private int resolveUpperY(int upperH, int lowerY, SpriteMetrics upperMetrics,
                              SpriteMetrics lowerMetrics, int overlap) {
        if (upperMetrics == null || lowerMetrics == null) {
            return lowerY - upperH + overlap;
        }
        return lowerY + lowerMetrics.topY - upperMetrics.bottomY + overlap;
    }

    private int getUpperAnchorGapX(boolean shooting, boolean knife) {
        int gap = 5;
        if (knife) {
            gap = 6;
        } else if (shooting && isGroundCrouching()) {
            gap = 4;
        }
        return faceRight ? -gap : gap;
    }

    private Point resolveBulletOrigin() {
        if (aimUp && onGround) {
            ImageIcon upper = firstFrame(currentWeapon.aimUp.select(faceRight));
            ImageIcon lower = currentLowerFrame != null ? currentLowerFrame : firstFrame(LOWER_STAND.select(faceRight));
            SpritePose pose = buildPose(upper, lower, false, false);
            int centerX = pose.upper != null
                    ? pose.upperX + pose.upper.getIconWidth() / 2
                    : this.getCenterX();
            int topY = pose.upper != null
                    ? pose.upperY + 6
                    : this.getY();
            return new Point(centerX, topY);
        }
        ImageIcon attackFrame = firstFrame(currentWeapon.attack.select(faceRight));
        if (attackFrame == null) {
            return new Point(this.getCenterX(), this.getY() + this.getH() / 2);
        }
        ImageIcon lower = currentLowerFrame;
        if (lower == null) {
            lower = firstFrame(isGroundCrouching()
                    ? LOWER_CROUCH_IDLE.select(faceRight)
                    : (!onGround ? LOWER_JUMP.select(faceRight) : LOWER_STAND.select(faceRight)));
        }
        SpritePose pose = buildPose(attackFrame, lower, true, false);
        Point localMuzzle = findMuzzlePoint(attackFrame, faceRight);
        return new Point(pose.upperX + localMuzzle.x, pose.upperY + localMuzzle.y);
    }

    private Point findMuzzlePoint(ImageIcon frame, boolean rightFacing) {
        if (frame == null) {
            return new Point(0, 0);
        }
        String cacheKey = System.identityHashCode(frame) + ":" + (rightFacing ? "R" : "L");
        Point cached = MUZZLE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        BufferedImage image = toBufferedImage(frame);
        Point fallback = new Point(rightFacing ? frame.getIconWidth() : 0, frame.getIconHeight() / 2);
        if (image == null) {
            MUZZLE_CACHE.put(cacheKey, fallback);
            return fallback;
        }
        if (rightFacing) {
            for (int x = image.getWidth() - 1; x >= 0; x--) {
                Point point = averageOpaquePoint(image, Math.max(0, x - 3), x, x + 1);
                if (point != null) {
                    MUZZLE_CACHE.put(cacheKey, point);
                    return point;
                }
            }
        } else {
            for (int x = 0; x < image.getWidth(); x++) {
                Point point = averageOpaquePoint(image, x, Math.min(image.getWidth() - 1, x + 3), x - 1);
                if (point != null) {
                    MUZZLE_CACHE.put(cacheKey, point);
                    return point;
                }
            }
        }
        MUZZLE_CACHE.put(cacheKey, fallback);
        return fallback;
    }

    private Point averageOpaquePoint(BufferedImage image, int startX, int endX, int pointX) {
        int sumY = 0;
        int count = 0;
        for (int x = startX; x <= endX; x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (((image.getRGB(x, y) >>> 24) & 0xff) > 16) {
                    sumY += y;
                    count++;
                }
            }
        }
        if (count == 0) {
            return null;
        }
        return new Point(pointX, sumY / count);
    }

    private BufferedImage toBufferedImage(ImageIcon frame) {
        if (frame == null || frame.getIconWidth() <= 0 || frame.getIconHeight() <= 0) {
            return null;
        }
        Image image = frame.getImage();
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        BufferedImage buffer = new BufferedImage(frame.getIconWidth(), frame.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = buffer.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return buffer;
    }

    private SpriteMetrics getSpriteMetrics(ImageIcon frame) {
        if (frame == null) {
            return null;
        }
        SpriteMetrics cached = SPRITE_METRICS_CACHE.get(frame);
        if (cached != null) {
            return cached;
        }
        BufferedImage image = toBufferedImage(frame);
        if (image == null) {
            return null;
        }
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xff) > 16) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }
        if (maxX < 0 || maxY < 0) {
            return null;
        }
        int upperAnchorX = computeAnchorX(image, minX, maxX, Math.max(minY, maxY - 8), maxY, 0.15);
        int lowerAnchorX = computeAnchorX(image, minX, maxX, minY, Math.min(maxY, minY + 8), 0.10);
        SpriteMetrics metrics = new SpriteMetrics(minY, maxY, upperAnchorX, lowerAnchorX);
        SPRITE_METRICS_CACHE.put(frame, metrics);
        return metrics;
    }

    private int computeAnchorX(BufferedImage image, int minX, int maxX, int startY, int endY, double trimRatio) {
        int trim = (int) Math.round((maxX - minX + 1) * trimRatio);
        int startX = Math.min(maxX, minX + trim);
        int endX = Math.max(startX, maxX - trim);
        int sumX = 0;
        int count = 0;
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xff) > 16) {
                    sumX += x;
                    count++;
                }
            }
        }
        if (count == 0) {
            return (minX + maxX) / 2;
        }
        return Math.round((float) sumX / count);
    }

    private long getShootAnimationDuration() {
        return Math.max(1, currentWeapon.attack.select(faceRight).size()) * SHOOT_FRAME_GAP;
    }

    private long getKnifeAnimationDuration() {
        return Math.max(1, UPPER_KNIFE.select(faceRight).size()) * KNIFE_FRAME_GAP;
    }

    private void performKnifeHit() {
        int attackWidth = 72;
        int attackHeight = isGroundCrouching() ? this.getH() : this.getH() + 8;
        int attackX = faceRight ? this.getX() + this.getW() - 4 : this.getX() - attackWidth + 4;
        int attackY = this.getY() - (isGroundCrouching() ? 0 : 4);
        Rectangle knifeRect = new Rectangle(attackX, attackY, attackWidth, attackHeight);
        List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
        for (ElementObj enemyObj : enemys) {
            if (!enemyObj.isLive() || !knifeRect.intersects(enemyObj.getRectangle())) {
                continue;
            }
            if (enemyObj instanceof Enemy) {
                ((Enemy) enemyObj).hurt(99);
            } else if (enemyObj instanceof ScoutEnemy) {
                ((ScoutEnemy) enemyObj).hurt(99);
            } else {
                enemyObj.setLive(false);
            }
        }
        List<ElementObj> bosses = em.getElementsByKey(GameElement.BOSS);
        for (ElementObj bossObj : bosses) {
            if (!bossObj.isLive() || !knifeRect.intersects(bossObj.getRectangle())) {
                continue;
            }
            if (bossObj instanceof Boss) {
                ((Boss) bossObj).hurt(4);
            } else {
                bossObj.setLive(false);
            }
        }
    }

    private void spawnGrenade() {
        int grenadeX = faceRight ? this.getX() + this.getW() / 2 : this.getX() - 8;
        int grenadeY = this.getY() + Math.max(4, this.getH() / 2);
        double grenadeVx = faceRight ? 8.0 : -8.0;
        double grenadeVy = -10.5;
        if (isGroundCrouching()) {
            grenadeY = this.getY() + this.getH() - 18;
            grenadeVx = faceRight ? 6.0 : -6.0;
            grenadeVy = -8.0;
        } else if (aimUp) {
            grenadeVx = faceRight ? 5.0 : -5.0;
            grenadeVy = -12.5;
        }
        ElementObj grenade = new Grenade().createElement(grenadeX + "," + grenadeY + ",grenade," + grenadeVx + "," + grenadeVy);
        em.addElement(grenade, GameElement.PLAYFILE);
    }

    private int getAnchorX() {
        return Math.max(220, GameJFrame.GameX / 2 - this.getW() / 2);
    }

    private double clampGroundBottom(double targetBottom) {
        return GameRuntime.clampBattlefieldBottom((int) Math.round(targetBottom));
    }

    private String resolvePlayerBulletPath() {
        return faceRight ? currentWeapon.rightBullet : currentWeapon.leftBullet;
    }

    private enum WeaponType {
        RIFLE("步枪", UPPER_AIM_UP_W1, UPPER_ATTACK_W1, PLAYER_BULLET_LEFT, PLAYER_BULLET_RIGHT, 6, 1, 14),
        HEAVY("重机枪", UPPER_AIM_UP_W2, UPPER_ATTACK_W2, PLAYER_HEAVY_BULLET_LEFT, PLAYER_HEAVY_BULLET_RIGHT, 10, 2, 18);

        private final String label;
        private final DirectionalFrames aimUp;
        private final DirectionalFrames attack;
        private final String leftBullet;
        private final String rightBullet;
        private final int fireInterval;
        private final int damage;
        private final int bulletSpeed;

        WeaponType(String label, DirectionalFrames aimUp, DirectionalFrames attack,
                   String leftBullet, String rightBullet, int fireInterval, int damage, int bulletSpeed) {
            this.label = label;
            this.aimUp = aimUp;
            this.attack = attack;
            this.leftBullet = leftBullet;
            this.rightBullet = rightBullet;
            this.fireInterval = fireInterval;
            this.damage = damage;
            this.bulletSpeed = bulletSpeed;
        }
    }

    private static final class DirectionalFrames {
        private final List<ImageIcon> left;
        private final List<ImageIcon> right;

        private DirectionalFrames(String leftDir, String rightDir) {
            this.left = GameLoad.loadFramesFromDirectory(leftDir);
            this.right = GameLoad.loadFramesFromDirectory(rightDir);
        }

        private List<ImageIcon> select(boolean rightFacing) {
            return rightFacing ? right : left;
        }
    }

    private static final class FrameSelection {
        private final ImageIcon upper;
        private final ImageIcon lower;

        private FrameSelection(ImageIcon upper, ImageIcon lower) {
            this.upper = upper;
            this.lower = lower;
        }
    }

    private static final class SpriteMetrics {
        private final int topY;
        private final int bottomY;
        private final int upperAnchorX;
        private final int lowerAnchorX;

        private SpriteMetrics(int topY, int bottomY, int upperAnchorX, int lowerAnchorX) {
            this.topY = topY;
            this.bottomY = bottomY;
            this.upperAnchorX = upperAnchorX;
            this.lowerAnchorX = lowerAnchorX;
        }
    }

    private static final class SpritePose {
        private final ImageIcon upper;
        private final ImageIcon lower;
        private final int upperX;
        private final int upperY;
        private final int lowerX;
        private final int lowerY;

        private SpritePose(ImageIcon upper, ImageIcon lower, int upperX, int upperY, int lowerX, int lowerY) {
            this.upper = upper;
            this.lower = lower;
            this.upperX = upperX;
            this.upperY = upperY;
            this.lowerX = lowerX;
            this.lowerY = lowerY;
        }
    }
}
