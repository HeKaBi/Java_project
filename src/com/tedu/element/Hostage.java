package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import java.awt.Graphics;
import java.util.List;
import javax.swing.ImageIcon;

public class Hostage extends ElementObj {
    private static final int HITBOX_W = 42;
    private static final int HITBOX_H = 78;
    private static final List<ImageIcon> HOSTAGE_FRAMES =
            GameLoad.loadFramesFromDirectory("image/images/人质");

    private final ElementManager em = ElementManager.getManager();

    private boolean rescued = false;
    private boolean itemDropped = false;
    private String rewardType = "weapon2";
    private ImageIcon currentFrame = HOSTAGE_FRAMES.isEmpty() ? null : HOSTAGE_FRAMES.get(0);

    @Override
    public void showElement(Graphics g) {
        ImageIcon frame = currentFrame == null ? this.getIcon() : currentFrame;
        if (frame == null) {
            return;
        }
        int drawW = frame.getIconWidth();
        int drawH = frame.getIconHeight();
        int drawX = this.getX() + (this.getW() - drawW) / 2;
        int drawY = this.getY() + this.getH() - drawH;
        g.drawImage(frame.getImage(), drawX, drawY, drawW, drawH, null);
    }

    @Override
    protected void move() {
        this.setX(this.getX() - GameRuntime.worldScrollX);
        this.setY(GameRuntime.getBattlefieldMaxBottom() - this.getH());
    }

    @Override
    protected void updateImage(long gameTime) {
        if (!HOSTAGE_FRAMES.isEmpty()) {
            currentFrame = HOSTAGE_FRAMES.get((int) ((gameTime / 5) % Math.min(HOSTAGE_FRAMES.size(), 8)));
            this.setIcon(currentFrame);
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.setW(HITBOX_W);
        this.setH(HITBOX_H);
        if (split.length > 2) {
            this.rewardType = split[2];
        }
        if (!HOSTAGE_FRAMES.isEmpty()) {
            currentFrame = HOSTAGE_FRAMES.get(0);
            this.setIcon(currentFrame);
        }
        return this;
    }

    @Override
    public void die() {
        if (!rescued || itemDropped) {
            return;
        }
        itemDropped = true;
        ElementObj item = new SupplyItem().createElement(
                (this.getX() + this.getW() / 2 - 14) + "," + (this.getY() + this.getH() / 2 - 12) + "," + rewardType);
        em.addElement(item, GameElement.ITEM);
    }

    public void rescue() {
        if (rescued) {
            return;
        }
        rescued = true;
        GameRuntime.showBanner("人质获救，补给掉落", 1500);
        this.setLive(false);
    }
}
