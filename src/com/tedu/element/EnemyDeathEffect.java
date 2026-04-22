package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import java.awt.Graphics;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.ImageIcon;

public class EnemyDeathEffect extends ElementObj {
    private static final int FRAME_DURATION_MS = 85;
    private static final List<ImageIcon> DIE1_FRAMES =
            GameLoad.loadFramesFromDirectory("image/images/Enemy/die/die1");
    private static final List<ImageIcon> DIE2_FRAMES =
            GameLoad.loadFramesFromDirectory("image/images/Enemy/die/die2");
    private static final List<ImageIcon> DIE3_FRAMES =
            GameLoad.loadFramesFromDirectory("image/images/Enemy/die/die3");
    private static final List<ImageIcon> DIE4_FRAMES =
            GameLoad.loadFramesFromDirectory("image/images/Enemy/die/die4");
    private static final List<ImageIcon> DIE6_FRAMES =
            GameLoad.loadFramesFromDirectory("image/images/Enemy/enemy6/die");

    private List<ImageIcon> frames = Collections.emptyList();
    private long startTimeMs = -1L;
    private int anchorW = 52;
    private int anchorH = 72;
    private boolean faceRight = false;
    private double renderScale = 1.0;

    @Override
    public void showElement(Graphics g) {
        if (frames.isEmpty()) {
            return;
        }
        updateFrameByClock();
        ImageIcon frame = frames.get(resolveFrameIndex());
        int drawW = Math.max(1, (int) Math.round(frame.getIconWidth() * renderScale));
        int drawH = Math.max(1, (int) Math.round(frame.getIconHeight() * renderScale));
        int drawX = this.getX() + (anchorW - drawW) / 2;
        int drawY = this.getY() + anchorH - drawH;
        if (faceRight) {
            g.drawImage(frame.getImage(), drawX + drawW, drawY, -drawW, drawH, null);
        } else {
            g.drawImage(frame.getImage(), drawX, drawY, drawW, drawH, null);
        }
    }

    @Override
    protected void move() {
        this.setX(this.getX() - GameRuntime.worldScrollX);
    }

    @Override
    protected void updateImage(long time) {
        updateFrameByClock();
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        if (split.length > 2) {
            anchorW = Integer.parseInt(split[2]);
        }
        if (split.length > 3) {
            anchorH = Integer.parseInt(split[3]);
        }
        String enemyKey = split.length > 4 ? split[4] : "enemy1";
        faceRight = split.length > 5 && Boolean.parseBoolean(split[5]);
        if (split.length > 6) {
            renderScale = Double.parseDouble(split[6]);
        }
        frames = resolveFrames(enemyKey);
        this.setW(anchorW);
        this.setH(anchorH);
        this.startTimeMs = System.currentTimeMillis();
        if (!frames.isEmpty()) {
            this.setIcon(frames.get(0));
        } else {
            this.setLive(false);
        }
        return this;
    }

    private void updateFrameByClock() {
        if (frames.isEmpty()) {
            this.setLive(false);
            return;
        }
        if (startTimeMs < 0L) {
            startTimeMs = System.currentTimeMillis();
        }
        if (resolveFrameIndex() >= frames.size() - 1
                && System.currentTimeMillis() - startTimeMs >= (long) frames.size() * FRAME_DURATION_MS) {
            this.setLive(false);
        }
    }

    private int resolveFrameIndex() {
        if (startTimeMs < 0L || frames.isEmpty()) {
            return 0;
        }
        long elapsed = Math.max(0L, System.currentTimeMillis() - startTimeMs);
        int index = (int) (elapsed / FRAME_DURATION_MS);
        return Math.max(0, Math.min(frames.size() - 1, index));
    }

    private List<ImageIcon> resolveFrames(String enemyKey) {
        if (enemyKey == null) {
            return Collections.emptyList();
        }
        switch (enemyKey.trim().toLowerCase(Locale.ROOT)) {
            case "enemy1":
                return DIE1_FRAMES;
            case "enemy2":
                return DIE2_FRAMES;
            case "enemy3":
                return DIE3_FRAMES;
            case "enemy4":
                return DIE4_FRAMES;
            case "enemy6":
                return DIE6_FRAMES;
            default:
                return Collections.emptyList();
        }
    }
}
