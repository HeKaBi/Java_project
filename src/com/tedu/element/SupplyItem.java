package com.tedu.element;

import com.tedu.manager.AudioPlayer;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;

public class SupplyItem extends ElementObj {
    private static final ImageIcon HEAVY_MACHINE_GUN_ICON = GameLoad.getImage("image/Heav_machine_gun.png");
    private static final double HEAVY_MACHINE_GUN_SCALE = 0.80;
    private static final long PICKUP_DELAY_MS = 2000L;
    private static final String HEAVY_MACHINE_GUN_SFX = "music/Heavy Machine Gun.wav";
    private static final double DROP_INITIAL_VY = -6.2;
    private static final double DROP_GRAVITY = 0.48;
    private static final List<ImageIcon> GIFT_FRAMES = GameLoad.loadFrames(
            "image/images/子弹/gift0.png",
            "image/images/子弹/gift1.png",
            "image/images/子弹/gift2.png",
            "image/images/子弹/gift3.png");

    private String itemType = "weapon2";
    private int baseY = 0;
    private double floatY = 0.0;
    private double verticalVelocity = DROP_INITIAL_VY;
    private boolean settling = true;
    private long settleTick = 0L;
    private long spawnTimeMs = 0L;
    private ImageIcon currentFrame = GIFT_FRAMES.isEmpty() ? null : GIFT_FRAMES.get(0);

    @Override
    public void showElement(Graphics g) {
        ImageIcon frame = currentFrame == null ? this.getIcon() : currentFrame;
        if (frame == null) {
            return;
        }
        g.drawImage(frame.getImage(), this.getX(), this.getY(), this.getW(), this.getH(), null);
    }

    @Override
    protected void move() {
        this.setX(this.getX() - GameRuntime.worldScrollX);
    }

    @Override
    protected void updateImage(long gameTime) {
        if ("weapon2".equalsIgnoreCase(itemType)) {
            currentFrame = HEAVY_MACHINE_GUN_ICON;
            this.setIcon(currentFrame);
        } else if (!GIFT_FRAMES.isEmpty()) {
            currentFrame = GIFT_FRAMES.get((int) ((gameTime / 4) % GIFT_FRAMES.size()));
            this.setIcon(currentFrame);
        }
        updateVerticalMotion(gameTime);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.baseY = this.getY();
        this.floatY = this.baseY;
        this.verticalVelocity = DROP_INITIAL_VY;
        this.settling = true;
        this.settleTick = 0L;
        this.spawnTimeMs = System.currentTimeMillis();
        if (split.length > 2) {
            this.itemType = split[2];
        }
        currentFrame = resolveInitialFrame();
        int width = resolveItemWidth(currentFrame);
        int height = resolveItemHeight(currentFrame);
        this.setW(width);
        this.setH(height);
        this.setIcon(currentFrame);
        return this;
    }

    public void applyTo(PaoPao player) {
        if ("weapon2".equalsIgnoreCase(itemType)) {
            player.unlockWeapon(2);
            player.setWeapon(2);
            player.addGrenades(2);
            AudioPlayer.playOnce(HEAVY_MACHINE_GUN_SFX);
            GameRuntime.showBanner("获得重机枪补给", 1600);
        } else {
            player.addGrenades(1);
            GameRuntime.showBanner("获得手雷补给", 1200);
        }
        this.setLive(false);
    }

    public boolean canPickup() {
        return System.currentTimeMillis() - spawnTimeMs >= PICKUP_DELAY_MS;
    }

    private ImageIcon resolveInitialFrame() {
        if ("weapon2".equalsIgnoreCase(itemType) && HEAVY_MACHINE_GUN_ICON != null) {
            return HEAVY_MACHINE_GUN_ICON;
        }
        return GIFT_FRAMES.isEmpty() ? null : GIFT_FRAMES.get(0);
    }

    private int resolveItemWidth(ImageIcon frame) {
        if (frame == null) {
            return 24;
        }
        if ("weapon2".equalsIgnoreCase(itemType)) {
            return Math.max(28, (int) Math.round(frame.getIconWidth() * HEAVY_MACHINE_GUN_SCALE));
        }
        return Math.max(frame.getIconWidth(), frame.getIconHeight());
    }

    private int resolveItemHeight(ImageIcon frame) {
        if (frame == null) {
            return 24;
        }
        if ("weapon2".equalsIgnoreCase(itemType)) {
            return Math.max(20, (int) Math.round(frame.getIconHeight() * HEAVY_MACHINE_GUN_SCALE));
        }
        return Math.max(frame.getIconWidth(), frame.getIconHeight());
    }

    private void updateVerticalMotion(long gameTime) {
        if (settling) {
            floatY += verticalVelocity;
            verticalVelocity += DROP_GRAVITY;
            if (floatY >= baseY) {
                floatY = baseY;
                verticalVelocity = 0.0;
                settling = false;
                settleTick = gameTime;
            }
            this.setY((int) Math.round(floatY));
            return;
        }
        this.setY(baseY + (int) Math.round(Math.sin((gameTime - settleTick) / 5.0) * 3.0));
    }
}
