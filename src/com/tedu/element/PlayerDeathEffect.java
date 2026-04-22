package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;

public class PlayerDeathEffect extends ElementObj {
    private static final int FRAME_DURATION_MS = 90;
    private static final List<ImageIcon> DIE_FRAMES =
            GameLoad.loadFramesFromDirectory("image/images/plays/die");

    private long startTimeMs = -1L;
    private int anchorW = 34;
    private int anchorH = 54;

    @Override
    public void showElement(Graphics g) {
        if (DIE_FRAMES.isEmpty()) {
            return;
        }
        updateFrameByClock();
        ImageIcon frame = DIE_FRAMES.get(resolveFrameIndex());
        int drawX = this.getX() + (anchorW - frame.getIconWidth()) / 2;
        int drawY = this.getY() + anchorH - frame.getIconHeight();
        g.drawImage(frame.getImage(), drawX, drawY, frame.getIconWidth(), frame.getIconHeight(), null);
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
        this.setW(anchorW);
        this.setH(anchorH);
        this.startTimeMs = System.currentTimeMillis();
        if (!DIE_FRAMES.isEmpty()) {
            this.setIcon(DIE_FRAMES.get(0));
        }
        return this;
    }

    private void updateFrameByClock() {
        if (DIE_FRAMES.isEmpty()) {
            this.setLive(false);
            return;
        }
        if (startTimeMs < 0L) {
            startTimeMs = System.currentTimeMillis();
        }
        if (resolveFrameIndex() >= DIE_FRAMES.size() - 1
                && System.currentTimeMillis() - startTimeMs >= (long) DIE_FRAMES.size() * FRAME_DURATION_MS) {
            this.setLive(false);
        }
    }

    private int resolveFrameIndex() {
        if (startTimeMs < 0L) {
            return 0;
        }
        long elapsed = Math.max(0L, System.currentTimeMillis() - startTimeMs);
        int index = (int) (elapsed / FRAME_DURATION_MS);
        return Math.max(0, Math.min(DIE_FRAMES.size() - 1, index));
    }
}
