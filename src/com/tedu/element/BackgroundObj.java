package com.tedu.element;

import java.awt.Graphics2D;

import javax.swing.ImageIcon;

import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;

public class BackgroundObj extends ElementObj {
    private int drawWidth;
    private int drawHeight;

    @Override
    public void showElement(Graphics2D g2) {
        ImageIcon icon = getIcon();
        if (icon == null) {
            return;
        }
        int screenX = (int) Math.round(getX() - GameRuntime.getInstance().getCameraX() * 0.35);
        g2.drawImage(icon.getImage(), screenX, (int) getY(), drawWidth, drawHeight, null);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] data = str.split(",");
        setX(Integer.parseInt(data[0].trim()));
        setY(Integer.parseInt(data[1].trim()));
        this.drawWidth = Integer.parseInt(data[2].trim());
        this.drawHeight = Integer.parseInt(data[3].trim());
        setW(drawWidth);
        setH(drawHeight);
        setIcon(GameLoad.getImg(data[4].trim()));
        return this;
    }

    @Override
    public GameElement getGameElement() {
        return GameElement.BACKGROUND;
    }
}
