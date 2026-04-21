package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;

public class SupplyItem extends ElementObj {
    private static final List<ImageIcon> GIFT_FRAMES = GameLoad.loadFrames(
            "image/images/子弹/gift0.png",
            "image/images/子弹/gift1.png",
            "image/images/子弹/gift2.png",
            "image/images/子弹/gift3.png");

    private String itemType = "weapon2";
    private int baseY = 0;
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
        if (GIFT_FRAMES.isEmpty()) {
            return;
        }
        currentFrame = GIFT_FRAMES.get((int) ((gameTime / 4) % GIFT_FRAMES.size()));
        this.setIcon(currentFrame);
        this.setY(baseY + (int) Math.round(Math.sin(gameTime / 5.0) * 3.0));
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.baseY = this.getY();
        if (split.length > 2) {
            this.itemType = split[2];
        }
        int size = currentFrame == null ? 24 : Math.max(currentFrame.getIconWidth(), currentFrame.getIconHeight());
        this.setW(size);
        this.setH(size);
        this.setIcon(currentFrame);
        return this;
    }

    public void applyTo(PaoPao player) {
        if ("weapon2".equalsIgnoreCase(itemType)) {
            player.unlockWeapon(2);
            player.setWeapon(2);
            player.addGrenades(2);
            GameRuntime.showBanner("获得重机枪补给", 1600);
        } else {
            player.addGrenades(1);
            GameRuntime.showBanner("获得手雷补给", 1200);
        }
        this.setLive(false);
    }
}
