package com.tedu.element;

import com.tedu.manager.AudioPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Random;
import javax.swing.ImageIcon;

public class PlaneEnemy extends ElementObj {
    private static final String LEFT_BODY_PATH = "image/images/\u98de\u673a/plane_fly0 (2).png";
    private static final String RIGHT_BODY_PATH = LEFT_BODY_PATH;
    // The selected plane art is a single left-facing sprite; right-facing flight mirrors it at draw time.
    private static final String LEFT_PROPELLER_PATH = null;
    private static final String RIGHT_PROPELLER_PATH = null;

    private static final int LEFT_PROPELLER_X = -86;
    private static final int RIGHT_PROPELLER_X = 188;
    private static final int PROPELLER_Y = 66;
    private static final int HITBOX_INSET_X = 26;
    private static final int HITBOX_INSET_Y = 22;
    private static final int HITBOX_W = 154;
    private static final int HITBOX_H = 48;
    private static final int BODY_CENTER_OFFSET_X = 105;
    private static final int DEFAULT_SPEED = 5;
    private static final int DEFAULT_HP = 6;
    private static final int DEFAULT_BOMBS = 2;
    private static final int DEFAULT_PATROL_RANGE = 128;
    private static final int DEFAULT_HOVER_OFFSET = 124;
    private static final int BOB_AMPLITUDE = 8;
    private static final int EXIT_MARGIN = 240;
    private static final int ALTITUDE_MIN = 54;
    private static final int ALTITUDE_MAX = 176;
    private static final int PATROL_LOCK_X = 210;
    private static final int PATROL_EDGE_MARGIN = 220;
    private static final int PATROL_ALTITUDE_STEP = 2;
    private static final int PATROL_EXIT_SPEED_BONUS = 2;
    private static final int PLANE_DEATH_EFFECT_W = 240;
    private static final int PLANE_DEATH_EFFECT_H = 180;
    private static final double PLANE_DEATH_SCALE = 2.3;
    private static final String PLANE_FLYBY_SFX = "music/music (32).mp3";
    private static final String PLANE_EXPLOSION_SFX = "music/music (27).mp3";

    private static final PlaneFrame LEFT_FRAME =
            buildFrame(LEFT_BODY_PATH, LEFT_PROPELLER_PATH, LEFT_PROPELLER_X, PROPELLER_Y);
    private static final PlaneFrame RIGHT_FRAME =
            buildFrame(RIGHT_BODY_PATH, RIGHT_PROPELLER_PATH, RIGHT_PROPELLER_X, PROPELLER_Y);

    private final ElementManager em = ElementManager.getManager();
    private final Random random = new Random();

    private boolean faceRight;
    private int speed = DEFAULT_SPEED;
    private int hp = DEFAULT_HP;
    private boolean countedKill = false;
    private boolean destroyedByDamage = false;
    private long nextBombTime = -1L;
    private int bombsRemaining = DEFAULT_BOMBS;
    private int baseY = 120;
    private int bobPhase = 0;
    private int flightTicks = 0;
    private PlaneFrame currentFrame = LEFT_FRAME;
    private FlightMode flightMode = FlightMode.STRAFE;
    private int patrolRange = DEFAULT_PATROL_RANGE;
    private int hoverOffset = DEFAULT_HOVER_OFFSET;
    private int patrolAnchorX = GameJFrame.GameX / 2;
    private boolean patrolEngaged = false;
    private long patrolStartTick = -1L;
    private boolean exitRequested = false;
    private boolean flybySoundPlayed = false;

    @Override
    public void showElement(Graphics g) {
        PlaneFrame frame = resolveFrame();
        if (frame.icon == null) {
            return;
        }
        if (faceRight) {
            g.drawImage(frame.icon.getImage(),
                    this.getX() + frame.icon.getIconWidth(), this.getY(),
                    -frame.icon.getIconWidth(), frame.icon.getIconHeight(), null);
        } else {
            g.drawImage(frame.icon.getImage(), this.getX(), this.getY(),
                    frame.icon.getIconWidth(), frame.icon.getIconHeight(), null);
        }
    }

    @Override
    protected void move() {
        flightTicks++;
        if (flightMode == FlightMode.PATROL_PLAYER) {
            movePatrol();
        } else {
            moveStrafe();
        }
        syncFrame();
        updateVerticalPosition();
        if (!flybySoundPlayed && isWithinBattlefield()) {
            flybySoundPlayed = true;
            AudioPlayer.playOnce(PLANE_FLYBY_SFX);
        }
        if (isBeyondExitBounds()) {
            this.setLive(false);
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        syncFrame();
    }

    @Override
    protected void add(long gameTime) {
        if (bombsRemaining <= 0) {
            if (flightMode == FlightMode.PATROL_PLAYER && patrolEngaged) {
                exitRequested = true;
            }
            return;
        }
        if (nextBombTime < 0L) {
            nextBombTime = gameTime + initialBombDelay();
            return;
        }
        if (!isWithinBattlefield() || gameTime < nextBombTime) {
            return;
        }
        ElementObj player = getPlayer();
        if (!shouldDropBomb(player)) {
            nextBombTime = gameTime + 6L;
            return;
        }
        spawnBomb();
        bombsRemaining--;
        if (bombsRemaining <= 0 && flightMode == FlightMode.PATROL_PLAYER && patrolEngaged) {
            exitRequested = true;
        }
        nextBombTime = gameTime + nextBombDelay();
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.baseY = split.length > 1 ? Integer.parseInt(split[1]) : 120;
        this.faceRight = split.length > 2 && ("right".equalsIgnoreCase(split[2]) || "1".equals(split[2]));
        this.speed = split.length > 3 ? Math.max(2, Integer.parseInt(split[3])) : DEFAULT_SPEED;
        this.hp = split.length > 4 ? Math.max(1, Integer.parseInt(split[4])) : DEFAULT_HP;
        this.flightMode = split.length > 5 ? FlightMode.fromKey(split[5]) : FlightMode.STRAFE;
        this.bombsRemaining = split.length > 6 ? Math.max(1, Integer.parseInt(split[6])) : DEFAULT_BOMBS;
        this.patrolRange = split.length > 7 ? Math.max(88, Integer.parseInt(split[7])) : DEFAULT_PATROL_RANGE;
        this.hoverOffset = split.length > 8 ? Math.max(84, Integer.parseInt(split[8])) : DEFAULT_HOVER_OFFSET;
        this.countedKill = false;
        this.destroyedByDamage = false;
        this.nextBombTime = -1L;
        this.bobPhase = randomBetween(0, 359);
        this.flightTicks = 0;
        this.patrolEngaged = false;
        this.patrolStartTick = -1L;
        this.exitRequested = false;
        this.flybySoundPlayed = false;
        this.patrolAnchorX = clamp(this.getX() + 220, PATROL_EDGE_MARGIN, GameJFrame.GameX - PATROL_EDGE_MARGIN);
        syncFrame();
        updateVerticalPosition();
        return this;
    }

    @Override
    public Rectangle getRectangle() {
        PlaneFrame frame = resolveFrame();
        return new Rectangle(
                this.getX() + frame.bodyX + HITBOX_INSET_X,
                this.getY() + frame.bodyY + HITBOX_INSET_Y,
                HITBOX_W,
                HITBOX_H);
    }

    @Override
    public void die() {
        if (!destroyedByDamage) {
            return;
        }
        AudioPlayer.playOnce(PLANE_EXPLOSION_SFX);
        int effectX = getBodyCenterX() - PLANE_DEATH_EFFECT_W / 2;
        int effectY = this.getY() + currentFrame.bodyY - 18;
        ElementObj effect = new PlaneDeathEffect().createElement(
                effectX + "," + effectY + ","
                        + PLANE_DEATH_EFFECT_W + "," + PLANE_DEATH_EFFECT_H + ","
                        + faceRight + "," + PLANE_DEATH_SCALE);
        em.addElement(effect, GameElement.DIE);
    }

    public boolean hurt(int damage) {
        if (!this.isLive()) {
            return false;
        }
        hp -= Math.max(1, damage);
        if (hp > 0) {
            return false;
        }
        hp = 0;
        destroyedByDamage = true;
        if (!countedKill) {
            countedKill = true;
            GameRuntime.killCount++;
        }
        this.setLive(false);
        return true;
    }

    private void moveStrafe() {
        int velocityX = faceRight ? speed : -speed;
        this.setX(this.getX() + velocityX - GameRuntime.worldScrollX);
    }

    private void movePatrol() {
        ElementObj player = getPlayer();
        int x = this.getX() - GameRuntime.worldScrollX;

        if (player == null || !player.isLive()) {
            exitRequested = true;
        }

        if (!patrolEngaged && !exitRequested) {
            faceRight = true;
            PlaneFrame frame = RIGHT_FRAME;
            int desiredLockCenterX = resolvePatrolLockCenterX(player);
            int desiredLockX = desiredLockCenterX - frame.bodyX - BODY_CENTER_OFFSET_X;
            int delta = desiredLockX - x;
            int step = Math.max(1, Math.min(speed, Math.max(delta, 1)));
            x += step;
            if (getBodyCenterX(x, frame) >= desiredLockCenterX) {
                patrolEngaged = true;
                patrolStartTick = flightTicks;
                patrolAnchorX = clamp(desiredLockCenterX, PATROL_EDGE_MARGIN, GameJFrame.GameX - PATROL_EDGE_MARGIN);
            }
        } else if (exitRequested) {
            faceRight = true;
            x += speed + PATROL_EXIT_SPEED_BONUS;
        } else {
            updatePatrolAnchor(player);
            int oscillation = (int) Math.round(Math.sin((flightTicks - patrolStartTick) / 14.0) * patrolRange);
            int desiredCenterX = patrolAnchorX + oscillation;
            int currentCenterX = getBodyCenterX(x, resolveFrame());
            if (desiredCenterX >= currentCenterX) {
                faceRight = true;
            } else if (desiredCenterX < currentCenterX) {
                faceRight = false;
            }
            PlaneFrame frame = resolveFrame();
            int desiredX = desiredCenterX - frame.bodyX - BODY_CENTER_OFFSET_X;
            int delta = desiredX - x;
            x += clamp(delta, -speed, speed);
        }

        this.setX(x);
        updatePatrolAltitude(player);
    }

    private void updateVerticalPosition() {
        int waveOffset = (int) Math.round(Math.sin((flightTicks + bobPhase) / 7.0) * BOB_AMPLITUDE);
        this.setY(baseY + waveOffset);
    }

    private void updatePatrolAnchor(ElementObj player) {
        if (player == null) {
            return;
        }
        int desiredAnchor = clamp(player.getCenterX(), PATROL_EDGE_MARGIN, GameJFrame.GameX - PATROL_EDGE_MARGIN);
        patrolAnchorX = approach(patrolAnchorX, desiredAnchor, Math.max(1, speed - 1));
    }

    private void updatePatrolAltitude(ElementObj player) {
        if (player == null) {
            return;
        }
        int desiredBaseY = clamp(player.getY() - hoverOffset, ALTITUDE_MIN, ALTITUDE_MAX);
        baseY = approach(baseY, desiredBaseY, PATROL_ALTITUDE_STEP);
    }

    private int resolvePatrolLockCenterX(ElementObj player) {
        if (player == null) {
            return PATROL_LOCK_X;
        }
        int desired = player.getCenterX() - patrolRange / 2;
        return clamp(desired, PATROL_LOCK_X, GameJFrame.GameX - PATROL_EDGE_MARGIN);
    }

    private boolean shouldDropBomb(ElementObj player) {
        int planeCenterX = getBodyCenterX();
        if (flightMode == FlightMode.PATROL_PLAYER) {
            if (!patrolEngaged || player == null) {
                return false;
            }
            return Math.abs(player.getCenterX() - planeCenterX) <= Math.max(84, patrolRange / 2 + 12);
        }
        boolean overMidfield = planeCenterX > GameJFrame.GameX / 3
                && planeCenterX < (GameJFrame.GameX * 2) / 3;
        boolean overPlayer = player != null && Math.abs(player.getCenterX() - planeCenterX) <= 170;
        return overMidfield || overPlayer;
    }

    private void spawnBomb() {
        int bombX = getBodyCenterX() - 14 + (faceRight ? 10 : -10);
        int bombY = this.getY() + currentFrame.bodyY + 64;
        double vx = faceRight ? 1.3 : -1.3;
        ElementObj bomb = new PlaneBomb().createElement(
                bombX + "," + bombY + "," + vx + ",2.2,1,0.22,104");
        em.addElement(bomb, GameElement.ENEMYFILE);
    }

    private ElementObj getPlayer() {
        java.util.List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        return plays.isEmpty() ? null : plays.get(0);
    }

    private boolean isWithinBattlefield() {
        return this.getX() < GameJFrame.GameX - 40 && this.getX() + this.getW() > 40;
    }

    private boolean isBeyondExitBounds() {
        return this.getX() + this.getW() < -EXIT_MARGIN || this.getX() > GameJFrame.GameX + EXIT_MARGIN;
    }

    private int getBodyCenterX() {
        return getBodyCenterX(this.getX(), resolveFrame());
    }

    private int getBodyCenterX(int x, PlaneFrame frame) {
        return x + frame.bodyX + BODY_CENTER_OFFSET_X;
    }

    private void syncFrame() {
        currentFrame = resolveFrame();
        this.setIcon(currentFrame.icon);
        this.setW(currentFrame.icon.getIconWidth());
        this.setH(currentFrame.icon.getIconHeight());
    }

    private PlaneFrame resolveFrame() {
        return faceRight ? RIGHT_FRAME : LEFT_FRAME;
    }

    private int initialBombDelay() {
        return flightMode == FlightMode.PATROL_PLAYER ? randomBetween(20, 34) : randomBetween(26, 44);
    }

    private int nextBombDelay() {
        return flightMode == FlightMode.PATROL_PLAYER ? randomBetween(30, 48) : randomBetween(44, 72);
    }

    private int randomBetween(int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    private int approach(int current, int target, int step) {
        int delta = target - current;
        if (Math.abs(delta) <= step) {
            return target;
        }
        return current + (delta > 0 ? step : -step);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static PlaneFrame buildFrame(String bodyPath, String propellerPath, int propellerX, int propellerY) {
        ImageIcon body = GameLoad.getImage(bodyPath);
        if (body == null) {
            return new PlaneFrame(new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)), 0, 0);
        }
        ImageIcon propeller = GameLoad.getImage(propellerPath);
        int bodyW = body.getIconWidth();
        int bodyH = body.getIconHeight();
        int propW = propeller == null ? 0 : propeller.getIconWidth();
        int propH = propeller == null ? 0 : propeller.getIconHeight();

        int minX = Math.min(0, propeller == null ? 0 : propellerX);
        int minY = Math.min(0, propeller == null ? 0 : propellerY);
        int maxX = Math.max(bodyW, propeller == null ? bodyW : propellerX + propW);
        int maxY = Math.max(bodyH, propeller == null ? bodyH : propellerY + propH);
        int canvasW = Math.max(1, maxX - minX);
        int canvasH = Math.max(1, maxY - minY);

        BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        int bodyDrawX = -minX;
        int bodyDrawY = -minY;
        if (propeller != null) {
            g2.drawImage(propeller.getImage(), bodyDrawX + propellerX, bodyDrawY + propellerY, null);
        }
        g2.drawImage(body.getImage(), bodyDrawX, bodyDrawY, null);
        g2.dispose();
        return new PlaneFrame(new ImageIcon(canvas), bodyDrawX, bodyDrawY);
    }

    private enum FlightMode {
        STRAFE,
        PATROL_PLAYER;

        private static FlightMode fromKey(String key) {
            if (key == null) {
                return STRAFE;
            }
            String normalized = key.trim().toLowerCase(Locale.ROOT);
            if ("patrol".equals(normalized) || "hover".equals(normalized) || "track".equals(normalized)) {
                return PATROL_PLAYER;
            }
            return STRAFE;
        }
    }

    private static final class PlaneFrame {
        private final ImageIcon icon;
        private final int bodyX;
        private final int bodyY;

        private PlaneFrame(ImageIcon icon, int bodyX, int bodyY) {
            this.icon = icon;
            this.bodyX = bodyX;
            this.bodyY = bodyY;
        }
    }
}
