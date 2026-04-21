package com.tedu.element;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public abstract class ElementObj {
    private int x;
    private int y;
    private int w;
    private int h;
    private ImageIcon icon;
    private boolean live = true;

    public ElementObj() {
    }

    public ElementObj(int x, int y, int w, int h, ImageIcon icon) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.icon = icon;
    }

    public abstract void showElement(Graphics g);

    public void keyClick(boolean bl, int key) {
    }

    protected void move() {
    }

    public final void model(long gameTime) {
        updateImage(gameTime);
        move();
        add(gameTime);
    }

    protected void updateImage(long time) {
    }

    protected void add(long gameTime) {
    }

    public void die() {
    }

    public ElementObj createElement(String str) {
        return null;
    }

    public Rectangle getRectangle() {
        return new Rectangle(x, y, w, h);
    }

    public boolean pk(ElementObj obj) {
        return this.getRectangle().intersects(obj.getRectangle());
    }

    public int getCenterX() {
        return x + w / 2;
    }

    public int getCenterY() {
        return y + h / 2;
    }

    public int getBottom() {
        return y + h;
    }

    public int getDamage() {
        return 1;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }
}
