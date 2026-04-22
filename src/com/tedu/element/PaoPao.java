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
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class PaoPao extends ElementObj {
    private static final int HITBOX_W = 34;
    private static final int STAND_H = 54;
    private static final int CROUCH_H = 34;
    private static final int MAX_JUMPS = 2;
    private static final int GROUND_LAYER_OVERLAP = 6;
    private static final int MAX_HP = 30;
    private static final int CROUCH_LAYER_OVERLAP = 10;
    private static final long INVINCIBLE_WINDOW = 100;
    private static final int MOVE_FRAME_GAP = 6;
    private static final int AIR_FRAME_GAP = 5;
    private static final int SHOOT_FRAME_GAP = 2;
    private static final int KNIFE_FRAME_GAP = 3;
    private static final int MAX_STEP_UP = 14;
    private static final int MAX_SNAP_DOWN = 8;
    private static final int AIR_WALL_MARGIN = 6;
    private static final int GROUND_PROBE_INSET = 4;
    private static final int PLATFORM_EDGE_MARGIN = 6;
    private static final int PLATFORM_LAND_TOLERANCE = 4;
    private static final int WALL_BODY_MARGIN = 4;
    private static final int WALL_FRONT_PROBE_RADIUS = 2;
    private static final long DOWN_TAP_WINDOW_MS = 220L;
    private static final double DROP_THROUGH_START_VELOCITY = 3.5;
    private static final int DROP_THROUGH_MIN_GAP = 14;
    private static final int PLAYER_MOVE_SPEED = 3;
    private static final int CROUCH_MOVE_SPEED = 2;
    private static final int KNIFE_DAMAGE = 4;
    private static final int KNIFE_BOSS_DAMAGE = 2;
    private static final int RIFLE_BULLET_SPEED = 11;
    private static final int HEAVY_BULLET_SPEED = 13;
    private static final int ROCKET_BULLET_SPEED = 9;
    private static final String PLAYERS_ROOT = "image/images/plays/";
    private static final String LOWER_BODY_ROOT = PLAYERS_ROOT + "\u4e0b\u534a\u8eab/";
    private static final String WEAPON1_UPPER_ROOT = PLAYERS_ROOT + "\u4e0a\u534a\u8eab/\u6b66\u56681/";
    private static final String WEAPON2_UPPER_ROOT = PLAYERS_ROOT + "\u4e0a\u534a\u8eab/\u6b66\u56682/";
    private static final String KNIFE_UPPER_ROOT = PLAYERS_ROOT + "\u4e0a\u534a\u8eab/\u6b66\u56683/";
    private static final String WEAPON4_UPPER_ROOT = PLAYERS_ROOT + "\u4e0a\u534a\u8eab/\u6b66\u56684/";
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
            WEAPON1_UPPER_ROOT + "left/attack1",
            WEAPON1_UPPER_ROOT + "right/attack1");
    private static final DirectionalFrames UPPER_ATTACK_W1 = new DirectionalFrames(
            new String[]{WEAPON1_UPPER_ROOT + "left/attack0", WEAPON1_UPPER_ROOT + "left/attack"},
            new String[]{WEAPON1_UPPER_ROOT + "right/attack"});
    private static final DirectionalFrames UPPER_AIM_UP_W2 = new DirectionalFrames(
            WEAPON2_UPPER_ROOT + "left/attack1",
            WEAPON2_UPPER_ROOT + "right/attack1");
    private static final DirectionalFrames UPPER_ATTACK_W2 = new DirectionalFrames(
            WEAPON2_UPPER_ROOT + "left/attack",
            WEAPON2_UPPER_ROOT + "right/attack");
    private static final DirectionalFrames UPPER_AIM_UP_W4 = new DirectionalFrames(
            WEAPON4_UPPER_ROOT + "left/attack1",
            WEAPON4_UPPER_ROOT + "right/attack1");
    private static final DirectionalFrames UPPER_ATTACK_W4 = new DirectionalFrames(
            WEAPON4_UPPER_ROOT + "left/attack",
            WEAPON4_UPPER_ROOT + "right/attack");
    private static final DirectionalFrames UPPER_KNIFE = new DirectionalFrames(
            KNIFE_UPPER_ROOT + "left",
            KNIFE_UPPER_ROOT + "right");
    private static final String PLAYER_BULLET_LEFT = "image/images/子弹/left/bullet00.png";
    private static final String PLAYER_BULLET_RIGHT = "image/images/子弹/right/bullet01.png";
    private static final String PLAYER_HEAVY_BULLET_LEFT = "image/images/子弹/left/bullet10.png";
    private static final String PLAYER_HEAVY_BULLET_RIGHT = "image/images/子弹/right/bullet11.png";
    private static final String PLAYER_ROCKET_BULLET_LEFT = "image/images/子弹/left/bullet20.png";
    private static final String PLAYER_ROCKET_BULLET_RIGHT = "image/images/子弹/right/bullet21.png";
    private static final Map<String, Point> MUZZLE_CACHE = new HashMap<>();
    private static final Map<ImageIcon, SpriteMetrics> SPRITE_METRICS_CACHE = new IdentityHashMap<>();

    private final ElementManager em = ElementManager.getManager();

    private long imgtime = 0;
    private WeaponType currentWeapon = WeaponType.RIFLE;
    private boolean weapon2Unlocked = false;
    private boolean weapon4Unlocked = false;
    private ImageIcon currentUpperFrame = lastFrame(UPPER_ATTACK_W1.select(true));
    private ImageIcon currentLowerFrame = firstFrame(LOWER_STAND.select(true));
    private int hp = MAX_HP;
    private int grenades = 8;
    private long hurtTime = -1000;
    private int speed = PLAYER_MOVE_SPEED;
    private double groundBottom = 0;
    private double vy = 0;
    private boolean onGround = true;
    private int remainingJumps = MAX_JUMPS;
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
    private boolean standingOnPlatform;
    private boolean jumpedFromPlatform;
    private boolean allowRaisedPlatformLanding;
    private PlatformObj ignoredLandingPlatform;
    private long fireTime = -100;
    private long knifeTime = -100;
    private long grenadeTime = -100;
    private long lastDownTapTimeMs = -1L;
    private long shootAnimStart = -1;
    private long shootAnimUntil = -1;
    private long knifeAnimStart = -1;
    private long knifeAnimUntil = -1;
    private long knifeHitTime = -1;
    private boolean knifeHitPending;

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
        currentUpperFrame = selection.upper != null ? selection.upper : resolveFallbackUpperFrame();
        currentLowerFrame = selection.lower != null ? selection.lower : resolveFallbackLowerFrame();
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
        int moveSpeed = isGroundCrouching() ? CROUCH_MOVE_SPEED : speed;
        int horizontalInput = 0;
        if (left ^ right) {
            horizontalInput = left ? -moveSpeed : moveSpeed;
        }
        boolean worldScrolling = GameRuntime.worldScrollX > 0
                && horizontalInput > 0
                && this.getX() >= getAnchorX()
                && !isGroundCrouching();
        int nextX = x;
        GroundTraverseResult groundMove = null;
        if (horizontalInput != 0 && !worldScrolling) {
            nextX = clampHorizontalPosition(x + horizontalInput);
            if (onGround) {
                groundMove = resolveGroundMove(x, nextX, (int) Math.round(y) + this.getH());
                nextX = groundMove.x;
            } else {
                nextX = resolveAirHorizontalMove(x, nextX, y);
            }
        }
        if (onGround) {
            int currentBottom = (int) Math.round(y) + this.getH();
            int nextGroundBottom = groundMove != null
                    ? groundMove.bottom
                    : getGroundSupportBottomAt(resolveSupportX(nextX), currentBottom);
            boolean shouldLeaveGround = groundMove != null
                    ? groundMove.leftGround
                    : nextGroundBottom - currentBottom > resolveMaxSnapDown();
            if (shouldLeaveGround) {
                jumpedFromPlatform = standingOnPlatform;
                standingOnPlatform = false;
                onGround = false;
                if (remainingJumps == MAX_JUMPS) {
                    remainingJumps = MAX_JUMPS - 1;
                }
                vy = Math.max(0.0, vy);
                allowRaisedPlatformLanding = false;
            } else {
                groundBottom = nextGroundBottom;
                y = groundBottom - this.getH();
            }
        }
        if (!onGround) {
            int previousBottom = (int) Math.round(y) + this.getH();
            vy += gravity;
            y += vy;
            double landingBottom = findLandingBottom(resolveSupportX(nextX), previousBottom,
                    (int) Math.round(y) + this.getH());
            double landingY = landingBottom - this.getH();
            if (y >= landingY) {
                y = landingY;
                vy = 0;
                onGround = true;
                remainingJumps = MAX_JUMPS;
                groundBottom = landingBottom;
                jumpedFromPlatform = false;
                allowRaisedPlatformLanding = false;
                ignoredLandingPlatform = null;
            }
        }
        if (y < 0) {
            y = 0;
            if (vy < 0) {
                vy = 0;
            }
        }
        this.setX(nextX);
        this.setY((int) Math.round(y));
        if (onGround) {
            int currentBottom = this.getY() + this.getH();
            int supportX = resolveSupportX(this.getX());
            standingOnPlatform = isStandingOnPlatform(supportX, currentBottom);
            groundBottom = standingOnPlatform
                    ? getWalkSupportBottomAt(supportX, currentBottom)
                    : getTerrainBottomAt(supportX);
        } else {
            groundBottom = this.getY() + this.getH();
            standingOnPlatform = false;
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
                if (bl) {
                    triggerJump();
                }
                break;
            case 69:
                aimUp = bl;
                break;
            case 83:
                if (bl) {
                    handleDownTap();
                }
                down = bl;
                break;
            case 17:
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
            case 51:
            case 99:
                if (bl) {
                    setWeapon(3);
                }
                break;
            case 81:
                if (bl) {
                    cycleWeapon();
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
                knifeHitTime = gameTime + getKnifeHitDelay();
                knifeHitPending = true;
                AudioPlayer.playOnce("music/knife.wav");
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
        if (knifeHitPending && gameTime >= knifeHitTime) {
            knifeHitPending = false;
            performKnifeHit();
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
        this.weapon4Unlocked = false;
        this.currentUpperFrame = lastFrame(currentWeapon.attack.select(true));
        this.currentLowerFrame = firstFrame(LOWER_STAND.select(true));
        this.groundBottom = this.getY() + this.getH();
        this.remainingJumps = MAX_JUMPS;
        this.onGround = true;
        this.vy = 0;
        this.knifeAnimStart = -1;
        this.knifeAnimUntil = -1;
        this.knifeHitTime = -1;
        this.knifeHitPending = false;
        this.allowRaisedPlatformLanding = false;
        this.ignoredLandingPlatform = null;
        this.lastDownTapTimeMs = -1L;
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
        this.remainingJumps = MAX_JUMPS;
        this.groundBottom = getTerrainBottomAt(resolveSupportX(this.getX()));
        this.setY((int) Math.round(groundBottom - this.getH()));
        this.standingOnPlatform = isStandingOnPlatform(resolveSupportX(this.getX()), (int) Math.round(this.groundBottom));
        this.jumpedFromPlatform = false;
        this.allowRaisedPlatformLanding = false;
        this.ignoredLandingPlatform = null;
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

    public int getMaxHp() {
        return MAX_HP;
    }

    public int getGrenades() {
        return grenades;
    }

    public String getWeaponName() {
        return currentWeapon.label;
    }

    public String getWeaponHudLabel() {
        return currentWeapon.hudLabel;
    }

    public boolean hasWeapon2() {
        return weapon2Unlocked;
    }

    public boolean hasWeapon4() {
        return weapon4Unlocked;
    }

    public boolean hasWeapon3() {
        return weapon4Unlocked;
    }

    public boolean isRifleEquipped() {
        return currentWeapon == WeaponType.RIFLE;
    }

    public boolean isHeavyWeaponEquipped() {
        return currentWeapon == WeaponType.HEAVY;
    }

    public boolean isWeapon4Equipped() {
        return currentWeapon == WeaponType.ROCKET;
    }

    public boolean isWeapon3Equipped() {
        return currentWeapon == WeaponType.ROCKET;
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
        } else if (weaponId == 3 || weaponId == 4) {
            weapon4Unlocked = true;
        }
    }

    public void setWeapon(int weaponId) {
        WeaponType nextWeapon = currentWeapon;
        if (weaponId == 1) {
            nextWeapon = WeaponType.RIFLE;
        } else if (weaponId == 2 && weapon2Unlocked) {
            nextWeapon = WeaponType.HEAVY;
        } else if ((weaponId == 3 || weaponId == 4) && weapon4Unlocked) {
            nextWeapon = WeaponType.ROCKET;
        }
        if (nextWeapon == currentWeapon) {
            return;
        }
        currentWeapon = nextWeapon;
        GameRuntime.showBanner("Weapon switched: " + currentWeapon.hudLabel, 1200);
    }

    public int prepareWorldScroll(int remainingDistance) {
        if (remainingDistance <= 0 || !right || left || isGroundCrouching()) {
            return 0;
        }
        if (this.getX() < getAnchorX()) {
            return 0;
        }
        int scroll = Math.min(speed, remainingDistance);
        return wouldBlockWorldScroll(scroll) ? 0 : scroll;
    }

    @Override
    public void die() {
        AudioPlayer.playOnce("music/die.wav");
        ElementObj effect = new PlayerDeathEffect().createElement(
                this.getX() + "," + this.getY() + "," + this.getW() + "," + this.getH());
        em.addElement(effect, GameElement.DIE);
    }

    private boolean isGroundCrouching() {
        return onGround && (crouch || down);
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
        List<ImageIcon> shootFrames = resolveShootFrames(attackFrames, aimUpFrames);
        ImageIcon idleUpper = lastFrame(attackFrames);
        if (!onGround) {
            lowerFrame = selectLoopFrame(LOWER_JUMP.select(faceRight), time, AIR_FRAME_GAP);
            if (isShootAnimating(time)) {
                upperFrame = selectOneShotFrame(shootFrames, shootAnimStart, time, SHOOT_FRAME_GAP);
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
            if (left ^ right) {
                List<ImageIcon> crouchRunFrames = LOWER_CROUCH_RUN.select(faceRight);
                if (crouchRunFrames == null || crouchRunFrames.isEmpty()) {
                    crouchRunFrames = LOWER_RUN.select(faceRight);
                }
                lowerFrame = selectLoopFrame(crouchRunFrames, time, MOVE_FRAME_GAP);
            } else {
                lowerFrame = firstFrame(LOWER_CROUCH_IDLE.select(faceRight));
            }
            if (isShootAnimating(time)) {
                upperFrame = selectOneShotFrame(shootFrames, shootAnimStart, time, SHOOT_FRAME_GAP);
            } else if (aimUp) {
                upperFrame = firstFrame(aimUpFrames);
            } else {
                upperFrame = idleUpper;
            }
            return new FrameSelection(upperFrame, lowerFrame);
        }
        if (left ^ right) {
            lowerFrame = selectLoopFrame(LOWER_RUN.select(faceRight), time, MOVE_FRAME_GAP);
        } else {
            lowerFrame = firstFrame(LOWER_STAND.select(faceRight));
        }
        if (isShootAnimating(time)) {
            upperFrame = selectOneShotFrame(shootFrames, shootAnimStart, time, SHOOT_FRAME_GAP);
        } else if (aimUp) {
            upperFrame = firstFrame(aimUpFrames);
        } else {
            upperFrame = idleUpper;
        }
        return new FrameSelection(upperFrame, lowerFrame);
    }

    private boolean isShootingUp() {
        return aimUp && !isGroundCrouching();
    }

    private List<ImageIcon> resolveShootFrames(List<ImageIcon> attackFrames, List<ImageIcon> aimUpFrames) {
        if (isShootingUp() && aimUpFrames != null && !aimUpFrames.isEmpty()) {
            return aimUpFrames;
        }
        return attackFrames == null ? Collections.emptyList() : attackFrames;
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
        ImageIcon safeUpper = upper != null ? upper : resolveFallbackUpperFrame();
        ImageIcon safeLower = lower != null ? lower : resolveFallbackLowerFrame();
        int footY = this.getY() + this.getH();
        int lowerW = safeLower == null ? 0 : safeLower.getIconWidth();
        int lowerH = safeLower == null ? 0 : safeLower.getIconHeight();
        int lowerX = this.getX() + (this.getW() - lowerW) / 2;
        int lowerY = footY - lowerH;
        int upperW = safeUpper == null ? 0 : safeUpper.getIconWidth();
        int upperH = safeUpper == null ? 0 : safeUpper.getIconHeight();
        int overlap = isGroundCrouching() ? CROUCH_LAYER_OVERLAP : GROUND_LAYER_OVERLAP;
        SpriteMetrics upperMetrics = getSpriteMetrics(safeUpper);
        SpriteMetrics lowerMetrics = getSpriteMetrics(safeLower);
        int upperX = resolveUpperX(upperW, lowerX, upperMetrics, lowerMetrics, shooting, knife);
        int upperY = resolveUpperY(upperH, lowerY, upperMetrics, lowerMetrics, overlap);
        return new SpritePose(safeUpper, safeLower, upperX, upperY, lowerX, lowerY);
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

    private ImageIcon resolveFallbackUpperFrame() {
        if (currentUpperFrame != null) {
            return currentUpperFrame;
        }
        ImageIcon attackFrame = lastFrame(currentWeapon.attack.select(faceRight));
        if (attackFrame != null) {
            return attackFrame;
        }
        ImageIcon aimUpFrame = firstFrame(currentWeapon.aimUp.select(faceRight));
        if (aimUpFrame != null) {
            return aimUpFrame;
        }
        ImageIcon rifleFrame = lastFrame(WeaponType.RIFLE.attack.select(faceRight));
        if (rifleFrame != null) {
            return rifleFrame;
        }
        return lastFrame(WeaponType.RIFLE.attack.select(!faceRight));
    }

    private ImageIcon resolveFallbackLowerFrame() {
        if (currentLowerFrame != null) {
            return currentLowerFrame;
        }
        if (!onGround) {
            ImageIcon jumpFrame = firstFrame(LOWER_JUMP.select(faceRight));
            if (jumpFrame != null) {
                return jumpFrame;
            }
        }
        if (isGroundCrouching()) {
            ImageIcon crouchFrame = firstFrame(LOWER_CROUCH_IDLE.select(faceRight));
            if (crouchFrame != null) {
                return crouchFrame;
            }
        }
        ImageIcon standFrame = firstFrame(LOWER_STAND.select(faceRight));
        if (standFrame != null) {
            return standFrame;
        }
        return firstFrame(LOWER_STAND.select(!faceRight));
    }

    private Point resolveBulletOrigin() {
        if (isShootingUp()) {
            ImageIcon upper = firstFrame(currentWeapon.aimUp.select(faceRight));
            ImageIcon lower = currentLowerFrame;
            if (lower == null) {
                lower = firstFrame(!onGround
                        ? LOWER_JUMP.select(faceRight)
                        : LOWER_STAND.select(faceRight));
            }
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
        return Math.max(1, resolveShootFrames(
                currentWeapon.attack.select(faceRight),
                currentWeapon.aimUp.select(faceRight)).size()) * SHOOT_FRAME_GAP;
    }

    private long getKnifeAnimationDuration() {
        return Math.max(1, UPPER_KNIFE.select(faceRight).size()) * KNIFE_FRAME_GAP;
    }

    private long getKnifeHitDelay() {
        long animationDuration = getKnifeAnimationDuration();
        long strikeDelay = KNIFE_FRAME_GAP * 2L;
        return Math.max(1L, Math.min(animationDuration - 1, strikeDelay));
    }

    private void performKnifeHit() {
        Rectangle knifeRect = resolveKnifeHitbox();
        List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
        for (ElementObj enemyObj : enemys) {
            if (!enemyObj.isLive() || !knifeRect.intersects(enemyObj.getRectangle())) {
                continue;
            }
            if (enemyObj instanceof Enemy) {
                ((Enemy) enemyObj).hurt(KNIFE_DAMAGE);
            } else if (enemyObj instanceof ScoutEnemy) {
                ((ScoutEnemy) enemyObj).hurt(KNIFE_DAMAGE);
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
                ((Boss) bossObj).hurt(KNIFE_BOSS_DAMAGE);
            } else {
                bossObj.setLive(false);
            }
        }
    }

    private Rectangle resolveKnifeHitbox() {
        ImageIcon knifeFrame = getKnifeStrikeFrame();
        if (knifeFrame == null) {
            int fallbackWidth = isGroundCrouching() ? 88 : 104;
            int fallbackHeight = Math.max(48, this.getH() + 12);
            int fallbackX = faceRight ? this.getCenterX() - 4 : this.getCenterX() - fallbackWidth + 4;
            int fallbackY = this.getY() - 6;
            return new Rectangle(fallbackX, fallbackY, fallbackWidth, fallbackHeight);
        }
        ImageIcon lowerFrame = resolveKnifeLowerFrame();
        SpritePose pose = buildPose(knifeFrame, lowerFrame, false, true);
        Rectangle swingRect = new Rectangle(
                pose.upperX,
                pose.upperY,
                knifeFrame.getIconWidth(),
                knifeFrame.getIconHeight());
        int reachWidth = Math.max(44, knifeFrame.getIconWidth() / 2);
        int reachHeight = Math.max(this.getH() + 12, knifeFrame.getIconHeight());
        int reachY = pose.upperY + Math.max(0, knifeFrame.getIconHeight() / 10 - 2);
        Rectangle reachRect;
        if (faceRight) {
            int reachX = pose.upperX + knifeFrame.getIconWidth() / 2 - 6;
            reachRect = new Rectangle(reachX, reachY, reachWidth + 12, reachHeight);
        } else {
            int reachX = pose.upperX - reachWidth - 6;
            reachRect = new Rectangle(reachX, reachY, reachWidth + 12, reachHeight);
        }
        swingRect.add(reachRect);
        return swingRect;
    }

    private ImageIcon getKnifeStrikeFrame() {
        List<ImageIcon> knifeFrames = UPPER_KNIFE.select(faceRight);
        if (knifeFrames == null || knifeFrames.isEmpty()) {
            return null;
        }
        int strikeIndex = Math.min(knifeFrames.size() - 1, 3);
        return knifeFrames.get(strikeIndex);
    }

    private ImageIcon resolveKnifeLowerFrame() {
        if (!onGround) {
            return firstFrame(LOWER_JUMP.select(faceRight));
        }
        if (isGroundCrouching()) {
            return firstFrame(LOWER_CROUCH_IDLE.select(faceRight));
        }
        return firstFrame(LOWER_STAND.select(faceRight));
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

    private void triggerJump() {
        if (remainingJumps <= 0) {
            return;
        }
        jumpedFromPlatform = standingOnPlatform;
        allowRaisedPlatformLanding = true;
        ignoredLandingPlatform = null;
        standingOnPlatform = false;
        onGround = false;
        remainingJumps--;
        vy = jumpVelocity;
    }

    private void handleDownTap() {
        long now = System.currentTimeMillis();
        if (now - lastDownTapTimeMs <= DOWN_TAP_WINDOW_MS && triggerDropThrough()) {
            lastDownTapTimeMs = -1L;
            return;
        }
        lastDownTapTimeMs = now;
    }

    private boolean triggerDropThrough() {
        if (!onGround || !standingOnPlatform) {
            return false;
        }
        int currentBottom = this.getY() + this.getH();
        int supportX = resolveSupportX(this.getX());
        PlatformObj standingPlatform = resolveStandingPlatform(supportX, currentBottom);
        if (standingPlatform == null) {
            return false;
        }
        int landingBottom = findDropLandingBottom(supportX, currentBottom, standingPlatform);
        if (landingBottom <= currentBottom + DROP_THROUGH_MIN_GAP) {
            return false;
        }
        ignoredLandingPlatform = standingPlatform;
        jumpedFromPlatform = false;
        allowRaisedPlatformLanding = true;
        standingOnPlatform = false;
        onGround = false;
        if (remainingJumps == MAX_JUMPS) {
            remainingJumps = MAX_JUMPS - 1;
        }
        vy = Math.max(DROP_THROUGH_START_VELOCITY, Math.max(0.0, vy) + 1.0);
        return true;
    }

    private void cycleWeapon() {
        if (currentWeapon == WeaponType.RIFLE) {
            if (weapon2Unlocked) {
                setWeapon(2);
            } else if (weapon4Unlocked) {
                setWeapon(3);
            }
            return;
        }
        if (currentWeapon == WeaponType.HEAVY) {
            if (weapon4Unlocked) {
                setWeapon(3);
            } else {
                setWeapon(1);
            }
            return;
        }
        setWeapon(1);
    }

    private int getTerrainBottomAt(int footX) {
        return GameRuntime.getBattlefieldMaxBottomAt(footX);
    }

    private int getGroundSupportBottomAt(int footX, int referenceBottom) {
        if (isStandingOnPlatform(footX, referenceBottom)) {
            return getWalkSupportBottomAt(footX, referenceBottom);
        }
        return getTerrainBottomAt(footX);
    }

    private int getWalkSupportBottomAt(int footX, int referenceBottom) {
        int bestBottom = getTerrainBottomAt(footX);
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
            if (referenceBottom - platformBottom > resolveMaxStepUp()) {
                continue;
            }
            if (platformBottom - referenceBottom > resolveMaxSnapDown()) {
                continue;
            }
            if (platformBottom < bestBottom) {
                bestBottom = platformBottom;
            }
        }
        return bestBottom;
    }

    private int findLandingBottom(int footX, int previousBottom, int nextBottom) {
        int bestBottom = Integer.MAX_VALUE;
        int terrainBottom = getTerrainBottomAt(footX);
        if (nextBottom >= terrainBottom) {
            bestBottom = terrainBottom;
        }
        if (!allowPlatformLanding()) {
            return bestBottom == Integer.MAX_VALUE ? terrainBottom : bestBottom;
        }
        List<ElementObj> platforms = em.getElementsByKey(GameElement.PLATFORM);
        for (ElementObj elementObj : platforms) {
            if (!(elementObj instanceof PlatformObj)) {
                continue;
            }
            PlatformObj platform = (PlatformObj) elementObj;
            if (!platform.isLive() || !isWithinPlatformSpan(platform, footX)) {
                continue;
            }
            if (shouldIgnoreLandingPlatform(platform, previousBottom)) {
                continue;
            }
            int platformBottom = platform.getTopSurfaceY();
            if (previousBottom > platformBottom + PLATFORM_LAND_TOLERANCE) {
                continue;
            }
            if (nextBottom < platformBottom) {
                continue;
            }
            if (platformBottom < bestBottom) {
                bestBottom = platformBottom;
            }
        }
        return bestBottom == Integer.MAX_VALUE ? terrainBottom : bestBottom;
    }

    private boolean allowPlatformLanding() {
        return allowRaisedPlatformLanding || jumpedFromPlatform;
    }

    private PlatformObj resolveStandingPlatform(int footX, int supportBottom) {
        List<ElementObj> platforms = em.getElementsByKey(GameElement.PLATFORM);
        for (ElementObj elementObj : platforms) {
            if (!(elementObj instanceof PlatformObj)) {
                continue;
            }
            PlatformObj platform = (PlatformObj) elementObj;
            if (!platform.isLive() || !isWithinPlatformSpan(platform, footX)) {
                continue;
            }
            if (Math.abs(platform.getTopSurfaceY() - supportBottom) <= 2) {
                return platform;
            }
        }
        return null;
    }

    private int findDropLandingBottom(int footX, int currentBottom, PlatformObj currentPlatform) {
        int bestBottom = getTerrainBottomAt(footX);
        List<ElementObj> platforms = em.getElementsByKey(GameElement.PLATFORM);
        for (ElementObj elementObj : platforms) {
            if (!(elementObj instanceof PlatformObj)) {
                continue;
            }
            PlatformObj platform = (PlatformObj) elementObj;
            if (platform == currentPlatform || !platform.isLive() || !isWithinPlatformSpan(platform, footX)) {
                continue;
            }
            int platformBottom = platform.getTopSurfaceY();
            if (platformBottom <= currentBottom + PLATFORM_LAND_TOLERANCE) {
                continue;
            }
            if (platformBottom < bestBottom) {
                bestBottom = platformBottom;
            }
        }
        return bestBottom;
    }

    private boolean shouldIgnoreLandingPlatform(PlatformObj platform, int previousBottom) {
        if (ignoredLandingPlatform != platform) {
            return false;
        }
        if (previousBottom <= platform.getTopSurfaceY() + PLATFORM_LAND_TOLERANCE) {
            return true;
        }
        ignoredLandingPlatform = null;
        return false;
    }

    private boolean isWithinPlatformSpan(PlatformObj platform, int footX) {
        int margin = resolvePlatformEdgeMargin(platform);
        return footX >= platform.getX() + margin
                && footX <= platform.getX() + platform.getW() - margin;
    }

    private int resolvePlatformEdgeMargin(PlatformObj platform) {
        return Math.min(PLATFORM_EDGE_MARGIN, Math.max(0, platform.getW() / 3));
    }

    private boolean isStandingOnPlatform(int footX, int supportBottom) {
        int terrainBottom = getTerrainBottomAt(footX);
        if (supportBottom >= terrainBottom - 1) {
            return false;
        }
        List<ElementObj> platforms = em.getElementsByKey(GameElement.PLATFORM);
        for (ElementObj elementObj : platforms) {
            if (!(elementObj instanceof PlatformObj)) {
                continue;
            }
            PlatformObj platform = (PlatformObj) elementObj;
            if (!platform.isLive() || !isWithinPlatformSpan(platform, footX)) {
                continue;
            }
            if (Math.abs(platform.getTopSurfaceY() - supportBottom) <= 2) {
                return true;
            }
        }
        return false;
    }

    private int clampHorizontalPosition(int targetX) {
        if (targetX < 0) {
            return 0;
        }
        int maxX = GameJFrame.GameX - this.getW();
        if (targetX > maxX) {
            return maxX;
        }
        return targetX;
    }

    private int resolveAirHorizontalMove(int currentX, int desiredX, double currentY) {
        if (desiredX == currentX) {
            return currentX;
        }
        int step = desiredX > currentX ? 1 : -1;
        int resolvedX = currentX;
        int actorBottom = (int) Math.round(currentY) + this.getH();
        for (int candidateX = currentX + step; candidateX != desiredX + step; candidateX += step) {
            if (isWallBlockedAt(candidateX, actorBottom, step > 0)) {
                break;
            }
            resolvedX = candidateX;
        }
        return resolvedX;
    }

    private GroundTraverseResult resolveGroundMove(int currentX, int desiredX, int currentBottom) {
        if (desiredX == currentX) {
            return new GroundTraverseResult(currentX, currentBottom, false);
        }
        int step = desiredX > currentX ? 1 : -1;
        int resolvedX = currentX;
        int resolvedBottom = currentBottom;
        for (int candidateX = currentX + step; candidateX != desiredX + step; candidateX += step) {
            if (isWallBlockedAt(candidateX, resolvedBottom, step > 0)) {
                break;
            }
            int candidateBottom = getGroundSupportBottomAt(resolveSupportX(candidateX), resolvedBottom);
            if (isGroundStepBlocked(resolvedBottom, candidateBottom)) {
                break;
            }
            resolvedX = candidateX;
            if (candidateBottom - resolvedBottom > resolveMaxSnapDown()) {
                return new GroundTraverseResult(resolvedX, resolvedBottom, true);
            }
            resolvedBottom = candidateBottom;
        }
        return new GroundTraverseResult(resolvedX, resolvedBottom, false);
    }

    private boolean isWallBlockedAt(int candidateX, int actorBottom, boolean movingRight) {
        return isWallBlockedAt(candidateX, actorBottom, movingRight, onGround);
    }

    private boolean isWallBlockedAt(int candidateX, int actorBottom, boolean movingRight, boolean treatAsGrounded) {
        int wallTopBottom = resolveWallTopBottomAtFront(candidateX, movingRight);
        int frontSurface = resolveFrontSurfaceBottom(candidateX, movingRight);
        if (wallTopBottom > 0) {
            // Wall should block walking unless the player is above the wall top (jumping over).
            if (!treatAsGrounded) {
                return actorBottom > wallTopBottom - AIR_WALL_MARGIN;
            }
            return actorBottom > wallTopBottom - WALL_BODY_MARGIN;
        }
        if (!treatAsGrounded) {
            return actorBottom > frontSurface - AIR_WALL_MARGIN;
        }
        // Let step-up handling absorb ordinary terrain ripples; otherwise shallow slopes feel
        // like invisible walls, especially on the second map.
        return actorBottom - frontSurface > resolveGroundFrontBlockMargin();
    }

    private int resolveWallTopBottomAtFront(int candidateX, boolean movingRight) {
        int frontX = resolveFrontX(candidateX, movingRight);
        int best = -1;
        for (int offset = -WALL_FRONT_PROBE_RADIUS; offset <= WALL_FRONT_PROBE_RADIUS; offset++) {
            int wallTopBottom = GameRuntime.getBattlefieldWallTopBottomAt(frontX + offset);
            if (wallTopBottom <= 0) {
                continue;
            }
            if (best <= 0 || wallTopBottom < best) {
                best = wallTopBottom;
            }
        }
        return best;
    }

    private int resolveFrontSurfaceBottom(int candidateX, boolean movingRight) {
        int frontX = resolveFrontX(candidateX, movingRight);
        int best = Integer.MAX_VALUE;
        for (int offset = -WALL_FRONT_PROBE_RADIUS; offset <= WALL_FRONT_PROBE_RADIUS; offset++) {
            best = Math.min(best, getTerrainBottomAt(frontX + offset));
        }
        return best == Integer.MAX_VALUE ? getTerrainBottomAt(frontX) : best;
    }

    private boolean isGroundStepBlocked(int currentBottom, int candidateBottom) {
        return currentBottom - candidateBottom > resolveMaxStepUp();
    }

    private boolean wouldBlockWorldScroll(int scroll) {
        if (scroll <= 0) {
            return false;
        }
        int actorBottom = this.getY() + this.getH();
        if (!onGround) {
            for (int delta = 1; delta <= scroll; delta++) {
                int candidateX = this.getX() + delta;
                if (isWallBlockedAt(candidateX, actorBottom, true, false)) {
                    return true;
                }
            }
            return false;
        }
        boolean grounded = true;
        int simulatedBottom = actorBottom;
        for (int delta = 1; delta <= scroll; delta++) {
            int candidateX = this.getX() + delta;
            int collisionBottom = grounded ? simulatedBottom : actorBottom;
            if (isWallBlockedAt(candidateX, collisionBottom, true, grounded)) {
                return true;
            }
            if (grounded) {
                int candidateBottom = getGroundSupportBottomAt(resolveSupportX(candidateX), simulatedBottom);
                if (isGroundStepBlocked(simulatedBottom, candidateBottom)) {
                    return true;
                }
                if (candidateBottom - simulatedBottom > resolveMaxSnapDown()) {
                    grounded = false;
                    continue;
                }
                simulatedBottom = candidateBottom;
            }
        }
        return false;
    }

    private int resolveMaxStepUp() {
        return GameRuntime.currentStage == 2 ? MAX_STEP_UP + 2 : MAX_STEP_UP;
    }

    private int resolveMaxSnapDown() {
        return GameRuntime.currentStage == 2 ? MAX_SNAP_DOWN + 2 : MAX_SNAP_DOWN;
    }

    private int resolveGroundFrontBlockMargin() {
        return resolveMaxStepUp();
    }

    private int resolveSupportX(int baseX) {
        return baseX + this.getW() / 2;
    }

    private int resolveFrontX(int baseX, boolean movingRight) {
        return baseX + (movingRight ? this.getW() - GROUND_PROBE_INSET : GROUND_PROBE_INSET);
    }

    private String resolvePlayerBulletPath() {
        return faceRight ? currentWeapon.rightBullet : currentWeapon.leftBullet;
    }

    private enum WeaponType {
        RIFLE("步枪", "RIFLE", UPPER_AIM_UP_W1, UPPER_ATTACK_W1, PLAYER_BULLET_LEFT, PLAYER_BULLET_RIGHT, 7, 1, 12),
        HEAVY("重机枪", "HEAVY", UPPER_AIM_UP_W2, UPPER_ATTACK_W2, PLAYER_HEAVY_BULLET_LEFT, PLAYER_HEAVY_BULLET_RIGHT, 12, 2, 16),
        ROCKET("火箭筒", "ROCKET", UPPER_AIM_UP_W4, UPPER_ATTACK_W4, PLAYER_ROCKET_BULLET_LEFT, PLAYER_ROCKET_BULLET_RIGHT, 20, 4, 10);

        private final String label;
        private final String hudLabel;
        private final DirectionalFrames aimUp;
        private final DirectionalFrames attack;
        private final String leftBullet;
        private final String rightBullet;
        private final int fireInterval;
        private final int damage;
        private final int bulletSpeed;

        WeaponType(String label, String hudLabel, DirectionalFrames aimUp, DirectionalFrames attack,
                   String leftBullet, String rightBullet, int fireInterval, int damage, int bulletSpeed) {
            this.label = label;
            this.hudLabel = hudLabel;
            this.aimUp = aimUp;
            this.attack = attack;
            this.leftBullet = leftBullet;
            this.rightBullet = rightBullet;
            this.fireInterval = fireInterval;
            this.damage = rebalanceDamage(hudLabel, damage);
            this.bulletSpeed = rebalanceBulletSpeed(hudLabel, bulletSpeed);
        }

        private static int rebalanceDamage(String hudLabel, int damage) {
            if ("HEAVY".equalsIgnoreCase(hudLabel) || "ROCKET".equalsIgnoreCase(hudLabel)) {
                return 1;
            }
            return Math.max(1, damage);
        }

        private static int rebalanceBulletSpeed(String hudLabel, int bulletSpeed) {
            if ("ROCKET".equalsIgnoreCase(hudLabel)) {
                return ROCKET_BULLET_SPEED;
            }
            if ("HEAVY".equalsIgnoreCase(hudLabel)) {
                return HEAVY_BULLET_SPEED;
            }
            return RIFLE_BULLET_SPEED;
        }
    }

    private static final class DirectionalFrames {
        private final List<ImageIcon> left;
        private final List<ImageIcon> right;

        private DirectionalFrames(String leftDir, String rightDir) {
            this(new String[]{leftDir}, new String[]{rightDir});
        }

        private DirectionalFrames(String[] leftDirs, String[] rightDirs) {
            this.left = loadFirstAvailable(leftDirs);
            this.right = loadFirstAvailable(rightDirs);
        }

        private static List<ImageIcon> loadFirstAvailable(String[] dirs) {
            if (dirs == null || dirs.length == 0) {
                return Collections.emptyList();
            }
            for (String dir : dirs) {
                List<ImageIcon> frames = GameLoad.loadFramesFromDirectory(dir);
                if (frames != null && !frames.isEmpty()) {
                    return frames;
                }
            }
            return Collections.emptyList();
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

    private static final class GroundTraverseResult {
        private final int x;
        private final int bottom;
        private final boolean leftGround;

        private GroundTraverseResult(int x, int bottom, boolean leftGround) {
            this.x = x;
            this.bottom = bottom;
            this.leftGround = leftGround;
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
