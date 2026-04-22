package com.tedu.element;

import com.tedu.manager.GameRuntime;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;

public class Corpse extends ElementObj {
    private List<ImageIcon> frames = Collections.emptyList();
    private ImageIcon currentFrame;
    private long animStartTime = -1L;
    private int anchorW = 52;
    private int anchorH = 72;
    private boolean faceRight = false;
    private boolean sourceFacesRight = false;
    private double renderScale = 1.0;
    private int frameGap = 8;

    public Corpse configure(int x, int y, int anchorW, int anchorH,
                            List<ImageIcon> frames, ImageIcon fallbackFrame,
                            boolean faceRight, boolean sourceFacesRight,
                            double renderScale, int frameGap) {
        this.setX(x);
        this.setY(y);
        this.anchorW = Math.max(1, anchorW);
        this.anchorH = Math.max(1, anchorH);
        this.setW(this.anchorW);
        this.setH(this.anchorH);
        this.frames = freeze(frames);
        this.currentFrame = !this.frames.isEmpty() ? this.frames.get(0) : fallbackFrame;
        this.faceRight = faceRight;
        this.sourceFacesRight = sourceFacesRight;
        this.renderScale = Math.max(0.1, renderScale);
        this.frameGap = Math.max(1, frameGap);
        this.animStartTime = -1L;
        this.setIcon(currentFrame);
        if (currentFrame == null) {
            this.setLive(false);
        }
        return this;
    }

    @Override
    public void showElement(Graphics g) {
        ImageIcon frame = currentFrame == null ? this.getIcon() : currentFrame;
        if (frame == null) {
            return;
        }
        int drawW = Math.max(1, (int) Math.round(frame.getIconWidth() * renderScale));
        int drawH = Math.max(1, (int) Math.round(frame.getIconHeight() * renderScale));
        int drawX = this.getX() + (anchorW - drawW) / 2;
        int drawY = this.getY() + anchorH - drawH;
        boolean shouldMirror = faceRight != sourceFacesRight;
        if (shouldMirror) {
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
    protected void updateImage(long gameTime) {
        if (frames.isEmpty()) {
            return;
        }
        if (animStartTime < 0L) {
            animStartTime = gameTime;
        }
        long elapsed = Math.max(0L, gameTime - animStartTime);
        int index = (int) Math.min(frames.size() - 1, elapsed / frameGap);
        currentFrame = frames.get(index);
        this.setIcon(currentFrame);
    }

    private static List<ImageIcon> freeze(List<ImageIcon> frames) {
        if (frames == null || frames.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(frames));
    }
}
