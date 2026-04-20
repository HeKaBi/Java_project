package com.tedu.element;

import java.awt.Color;
import java.awt.Graphics2D;

import com.tedu.manager.GameElement;
import com.tedu.manager.GameRuntime;

public class SceneBlock extends ElementObj {
    private String blockType = "ground";

    @Override
    public void showElement(Graphics2D g2) {
        if (!GameRuntime.getInstance().isDebugCollision()) {
            return;
        }
        int screenX = (int) Math.round(getX() - GameRuntime.getInstance().getCameraX());
        g2.setColor(new Color(240, 186, 73, 120));
        g2.fillRect(screenX, (int) getY(), getW(), getH());
    }

    @Override
    public ElementObj createElement(String str) {
        String[] data = str.split(",");
        setX(Integer.parseInt(data[0].trim()));
        setY(Integer.parseInt(data[1].trim()));
        setW(Integer.parseInt(data[2].trim()));
        setH(Integer.parseInt(data[3].trim()));
        if (data.length > 4) {
            this.blockType = data[4].trim();
        }
        return this;
    }

    @Override
    public GameElement getGameElement() {
        return GameElement.SCENE;
    }

    public String getBlockType() {
        return blockType;
    }
}
