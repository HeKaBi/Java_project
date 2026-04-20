package com.tedu.element;

import java.awt.Graphics2D;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;

public class EnemySoldier extends ElementObj {
    private static final double MOVE_SPEED = 1.6;
    private static final double GRAVITY = 0.8;

    private final ElementManager elementManager = ElementManager.getManager();
    private List<ImageIcon> standLeftFrames;
    private List<ImageIcon> standRightFrames;
    private List<ImageIcon> runLeftFrames;
    private List<ImageIcon> runRightFrames;
    private List<ImageIcon> attackLeftFrames;
    private List<ImageIcon> attackRightFrames;

    private boolean facingRight;
    private boolean onGround;
    private double velocityY;
    private double patrolStartX;
    private int patrolRange = 160;
    private int hp = 2;
    private int frameIndex;
    private List<ImageIcon> activeFrames;
    private long lastAnimationTick;
    private long currentGameTime;
    private long attackTickUntil = 0;
    private long nextAttackTick = 0;
    private boolean alert;

    @Override
    public void showElement(Graphics2D g2) {
        ImageIcon current = getIcon();
        if (current == null) {
            return;
        }
        int screenX = (int) Math.round(getX() - GameRuntime.getInstance().getCameraX());
        int screenBottomY = (int) Math.round(getY() + getH());
        int anchorX = screenX + getW() / 2;
        int drawW = (int) Math.round(current.getIconWidth() * 1.35);
        int drawH = (int) Math.round(current.getIconHeight() * 1.35);
        int drawX = anchorX - drawW / 2;
        int drawY = screenBottomY - drawH;
        g2.drawImage(current.getImage(), drawX, drawY, drawW, drawH, null);
    }

    @Override
    protected void move() {
        MsPlayer player = GameRuntime.getInstance().findPlayer();
        double dx = 0;
        alert = player != null && Math.abs(player.getX() - getX()) < 320 && Math.abs(player.getY() - getY()) < 120;
        if (alert) {
            if (Math.abs(player.getX() - getX()) > 8) {
                facingRight = player.getX() > getX();
            }
            if (currentGameTime >= nextAttackTick) {
                attackTickUntil = currentGameTime + 12;
                nextAttackTick = currentGameTime + 32;
            }
        } else {
            double minX = patrolStartX - patrolRange;
            double maxX = patrolStartX + patrolRange;
            dx = facingRight ? MOVE_SPEED : -MOVE_SPEED;
            if (getX() <= minX) {
                facingRight = true;
                dx = MOVE_SPEED;
            } else if (getX() >= maxX) {
                facingRight = false;
                dx = -MOVE_SPEED;
            }
        }

        setX(getX() + dx);
        velocityY += GRAVITY;
        if (velocityY > 16) {
            velocityY = 16;
        }
        setY(getY() + velocityY);
        onGround = false;
        for (ElementObj blockObj : elementManager.getElementsByKey(GameElement.SCENE)) {
            if (!(blockObj instanceof SceneBlock block)) {
                continue;
            }
            if (!getRectangle().intersects(block.getRectangle())) {
                continue;
            }
            if (velocityY >= 0) {
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
        ensureFrames();
        currentGameTime = gameTime;
        List<ImageIcon> frames = resolveFrames(gameTime);
        if (frames.isEmpty()) {
            return;
        }
        if (frames != activeFrames) {
            activeFrames = frames;
            frameIndex = 0;
            lastAnimationTick = gameTime;
        } else if (gameTime - lastAnimationTick >= 7) {
            frameIndex = (frameIndex + 1) % frames.size();
            lastAnimationTick = gameTime;
        }
        if (frameIndex >= frames.size()) {
            frameIndex = 0;
        }
        setIcon(frames.get(frameIndex));
    }

    private List<ImageIcon> resolveFrames(long gameTime) {
        if (gameTime < attackTickUntil) {
            return facingRight ? attackRightFrames : attackLeftFrames;
        }
        if (alert) {
            return facingRight ? standRightFrames : standLeftFrames;
        }
        return facingRight ? runRightFrames : runLeftFrames;
    }

    @Override
    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            setLive(false);
            GameRuntime.getInstance().addScore(150);
        } else {
            attackTickUntil = currentGameTime + 8;
        }
    }

    @Override
    public void die() {
        elementManager.addElement(new ExplosionEffect(getX(), getY()), GameElement.EFFECT);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] data = str.split(",");
        setX(Integer.parseInt(data[0].trim()));
        setY(Integer.parseInt(data[1].trim()));
        facingRight = "right".equalsIgnoreCase(data[2].trim());
        patrolRange = Integer.parseInt(data[3].trim());
        patrolStartX = getX();
        ensureFrames();
        setIcon((facingRight ? standRightFrames : standLeftFrames).getFirst());
        activeFrames = facingRight ? standRightFrames : standLeftFrames;
        frameIndex = 0;
        setW(36);
        setH(52);
        return this;
    }

    private void ensureFrames() {
        if (runLeftFrames != null) {
            return;
        }
        standLeftFrames = GameLoad.getAnimation("enemy_stand_l");
        standRightFrames = GameLoad.getAnimation("enemy_stand_r");
        runLeftFrames = GameLoad.getAnimation("enemy_run_l");
        runRightFrames = GameLoad.getAnimation("enemy_run_r");
        attackLeftFrames = GameLoad.getAnimation("enemy_attack_l");
        attackRightFrames = GameLoad.getAnimation("enemy_attack_r");
    }

    @Override
    public GameElement getGameElement() {
        return GameElement.ENEMY;
    }
}
