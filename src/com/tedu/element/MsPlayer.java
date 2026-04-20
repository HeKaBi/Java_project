package com.tedu.element;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;

public class MsPlayer extends ElementObj {
    private static final double MOVE_SPEED = 5.0;
    private static final double GRAVITY = 0.8;
    private static final double JUMP_SPEED = -15.5;
    private static final double DRAW_SCALE = 1.15;

    private final ElementManager elementManager = ElementManager.getManager();
    private List<ImageIcon> standLeftFrames;
    private List<ImageIcon> standRightFrames;
    private List<ImageIcon> runLeftFrames;
    private List<ImageIcon> runRightFrames;
    private List<ImageIcon> upperLeftFrames;
    private List<ImageIcon> upperRightFrames;

    private boolean moveLeft;
    private boolean moveRight;
    private boolean crouch;
    private boolean shootHeld;
    private boolean knifeQueued;
    private boolean onGround;
    private int facing = 1;
    private int lowerFrameIndex;
    private int upperFrameIndex;
    private int hp = 5;
    private double velocityY;
    private List<ImageIcon> activeLowerFrames;
    private List<ImageIcon> activeUpperFrames;
    private long lastAnimationTick;
    private long lastShotTick = -100;
    private long lastDamageTick = -100;
    private long attackTickUntil = 0;
    private long knifeTickUntil = 0;
    private long currentGameTime = 0;

    @Override
    public void showElement(Graphics2D g2) {
        ImageIcon lowerIcon = getIcon();
        ImageIcon upperIcon = getCurrentUpperIcon();
        if (lowerIcon == null || upperIcon == null) {
            return;
        }
        int screenX = (int) Math.round(getX() - GameRuntime.getInstance().getCameraX());
        int screenY = (int) Math.round(getY() + getH());
        int anchorX = screenX + getW() / 2;

        int lowerDrawW = (int) Math.round(lowerIcon.getIconWidth() * DRAW_SCALE);
        int lowerDrawH = (int) Math.round(lowerIcon.getIconHeight() * DRAW_SCALE);
        int upperDrawW = (int) Math.round(upperIcon.getIconWidth() * DRAW_SCALE);
        int upperDrawH = (int) Math.round(upperIcon.getIconHeight() * DRAW_SCALE);

        int lowerDrawX = anchorX - lowerDrawW / 2;
        int lowerDrawY = screenY - lowerDrawH;
        int upperDrawX = anchorX - upperDrawW / 2;
        int upperDrawY = screenY - upperDrawH - 6;

        g2.drawImage(lowerIcon.getImage(), lowerDrawX, lowerDrawY, lowerDrawW, lowerDrawH, null);
        g2.drawImage(upperIcon.getImage(), upperDrawX, upperDrawY, upperDrawW, upperDrawH, null);
    }

    @Override
    public void keyClick(boolean pressed, int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> moveLeft = pressed;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> moveRight = pressed;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> crouch = pressed;
            case KeyEvent.VK_J, KeyEvent.VK_Z -> shootHeld = pressed;
            case KeyEvent.VK_K, KeyEvent.VK_X -> {
                if (pressed) {
                    knifeQueued = true;
                }
            }
            case KeyEvent.VK_W, KeyEvent.VK_UP, KeyEvent.VK_SPACE -> {
                if (pressed && onGround) {
                    velocityY = JUMP_SPEED;
                    onGround = false;
                }
            }
            default -> {
            }
        }
    }

    @Override
    protected void move() {
        double dx = 0;
        if (moveLeft && !crouch) {
            dx -= MOVE_SPEED;
            facing = -1;
        }
        if (moveRight && !crouch) {
            dx += MOVE_SPEED;
            facing = 1;
        }

        moveHorizontal(dx);

        velocityY += GRAVITY;
        if (velocityY > 18) {
            velocityY = 18;
        }
        moveVertical(velocityY);

        if (getY() > GameRuntime.SCREEN_HEIGHT + 120) {
            takeDamage(999);
        }
    }

    private void moveHorizontal(double dx) {
        if (dx == 0) {
            return;
        }
        setX(getX() + dx);
        Rectangle playerRect = getRectangle();
        for (ElementObj blockObj : elementManager.getElementsByKey(GameElement.SCENE)) {
            if (!(blockObj instanceof SceneBlock block)) {
                continue;
            }
            if (playerRect.intersects(block.getRectangle())) {
                if (dx > 0) {
                    setX(block.getX() - getW());
                } else {
                    setX(block.getX() + block.getW());
                }
                break;
            }
        }
    }

    private void moveVertical(double dy) {
        setY(getY() + dy);
        onGround = false;
        Rectangle playerRect = getRectangle();
        for (ElementObj blockObj : elementManager.getElementsByKey(GameElement.SCENE)) {
            if (!(blockObj instanceof SceneBlock block)) {
                continue;
            }
            if (!playerRect.intersects(block.getRectangle())) {
                continue;
            }
            if (dy >= 0) {
                setY(block.getY() - getH());
                onGround = true;
            } else {
                setY(block.getY() + block.getH());
            }
            velocityY = 0;
            break;
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        this.currentGameTime = gameTime;
        ensureFrames();
        List<ImageIcon> lowerFrames = resolveLowerFrames();
        List<ImageIcon> upperFrames = resolveUpperFrames();
        if (lowerFrames.isEmpty() || upperFrames.isEmpty()) {
            return;
        }

        if (lowerFrames != activeLowerFrames) {
            activeLowerFrames = lowerFrames;
            lowerFrameIndex = 0;
            lastAnimationTick = gameTime;
        } else if (gameTime - lastAnimationTick >= 5) {
            lowerFrameIndex = (lowerFrameIndex + 1) % lowerFrames.size();
            lastAnimationTick = gameTime;
        }
        if (upperFrames != activeUpperFrames) {
            activeUpperFrames = upperFrames;
            upperFrameIndex = 0;
        } else if (moveLeft || moveRight || shootHeld) {
            upperFrameIndex = (upperFrameIndex + 1) % upperFrames.size();
        }
        if (lowerFrameIndex >= lowerFrames.size()) {
            lowerFrameIndex = 0;
        }
        if (upperFrameIndex >= upperFrames.size()) {
            upperFrameIndex = 0;
        }

        setIcon(lowerFrames.get(lowerFrameIndex));
    }

    private List<ImageIcon> resolveLowerFrames() {
        if (moveLeft || moveRight) {
            return facing > 0 ? runRightFrames : runLeftFrames;
        }
        return facing > 0 ? standRightFrames : standLeftFrames;
    }

    private List<ImageIcon> resolveUpperFrames() {
        return facing > 0 ? upperRightFrames : upperLeftFrames;
    }

    private ImageIcon getCurrentUpperIcon() {
        if (activeUpperFrames == null || activeUpperFrames.isEmpty()) {
            return null;
        }
        int index = Math.max(0, Math.min(upperFrameIndex, activeUpperFrames.size() - 1));
        return activeUpperFrames.get(index);
    }

    @Override
    protected void add(long gameTime) {
        if (knifeQueued && gameTime >= knifeTickUntil) {
            knifeTickUntil = gameTime + 12;
            knifeQueued = false;
            upperFrameIndex = 0;
        }
        if (!shootHeld || crouch) {
            return;
        }
        if (gameTime - lastShotTick < 10) {
            return;
        }
        lastShotTick = gameTime;
        attackTickUntil = gameTime + 8;
        upperFrameIndex = 0;
        double bulletX = facing > 0 ? getX() + getW() - 6 : getX() - 22;
        double bulletY = getY() + getH() * 0.42;
        PlayerBullet bullet = new PlayerBullet(bulletX, bulletY, facing);
        elementManager.addElement(bullet, bullet.getGameElement());
    }

    @Override
    public void takeDamage(int amount) {
        if (currentGameTime - lastDamageTick < 45) {
            return;
        }
        hp -= amount;
        lastDamageTick = currentGameTime;
        if (hp <= 0) {
            hp = 0;
            setLive(false);
            GameRuntime.getInstance().markGameOver();
        }
    }

    @Override
    public void die() {
        elementManager.addElement(new ExplosionEffect(getX(), getY() + 20), GameElement.EFFECT);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] data = str.split(",");
        setX(Integer.parseInt(data[0].trim()));
        setY(Integer.parseInt(data[1].trim()));
        ensureFrames();
        setIcon(standRightFrames.getFirst());
        activeLowerFrames = standRightFrames;
        activeUpperFrames = upperRightFrames;
        lowerFrameIndex = 0;
        upperFrameIndex = 0;
        setW(36);
        setH(54);
        return this;
    }

    private void ensureFrames() {
        if (standLeftFrames != null) {
            return;
        }
        standLeftFrames = GameLoad.getAnimation("player_stand_l");
        standRightFrames = GameLoad.getAnimation("player_stand_r");
        runLeftFrames = GameLoad.getAnimation("player_run_l");
        runRightFrames = GameLoad.getAnimation("player_run_r");
        upperLeftFrames = GameLoad.getAnimation("player_upper_l");
        upperRightFrames = GameLoad.getAnimation("player_upper_r");
    }

    @Override
    public GameElement getGameElement() {
        return GameElement.PLAY;
    }

    public int getHp() {
        return hp;
    }

    public Rectangle getMeleeRectangle() {
        if (currentGameTime >= knifeTickUntil) {
            return null;
        }
        int x = facing > 0 ? (int) getX() + getW() - 12 : (int) getX() - 36;
        return new Rectangle(x, (int) getY() + 12, 48, getH() - 24);
    }
}
