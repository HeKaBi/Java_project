package com.tedu.element;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.ImageIcon;

import com.tedu.manager.GameElement;

public abstract class ElementObj {
    private double x;
    private double y;
    private int w;
    private int h;
    private ImageIcon icon;
    private boolean live = true;

    public ElementObj() {
    }

    public ElementObj(double x, double y, int w, int h, ImageIcon icon) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.icon = icon;
    }

    public abstract void showElement(Graphics2D g2);

    public abstract ElementObj createElement(String str);

    public abstract GameElement getGameElement();

    public void keyClick(boolean pressed, int keyCode) {
    }

    protected void move() {
    }

    protected void updateImage(long gameTime) {
    }

    protected void add(long gameTime) {
    }

    public void die() {
    }

    public void takeDamage(int amount) {
        this.live = false;
    }

    public void onCameraShift(double deltaX) {
    }

    public final void model(long gameTime) {
        updateImage(gameTime);
        move();
        add(gameTime);
    }

    public Rectangle getRectangle() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), w, h);
    }

    public boolean pk(ElementObj obj) {
        return this.getRectangle().intersects(obj.getRectangle());
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
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
