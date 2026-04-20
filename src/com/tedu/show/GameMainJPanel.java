package com.tedu.show;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.tedu.element.ElementObj;
import com.tedu.element.MsPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameRuntime;

public class GameMainJPanel extends JPanel implements Runnable {
    private final ElementManager elementManager;
    private final GameRuntime runtime;

    public GameMainJPanel() {
        this.elementManager = ElementManager.getManager();
        this.runtime = GameRuntime.getInstance();
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Map<GameElement, List<ElementObj>> all = elementManager.getGameElements();
        for (GameElement element : GameElement.values()) {
            List<ElementObj> list = all.get(element);
            for (ElementObj obj : list) {
                obj.showElement(g2);
            }
        }
        drawHud(g2);
        drawOverlay(g2);
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(new Color(12, 12, 12, 180));
        g2.fillRoundRect(16, 16, 420, 92, 18, 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 18));
        g2.drawString("Metal Slug Course Prototype", 28, 42);
        g2.setFont(new Font("Consolas", Font.PLAIN, 16));
        g2.drawString("Score: " + runtime.getScore(), 28, 68);
        g2.drawString("CameraX: " + runtime.getCameraX(), 180, 68);
        g2.drawString("Controls: A/D move  W jump  S crouch  J fire  K knife", 28, 94);

        MsPlayer player = runtime.findPlayer();
        if (player != null) {
            g2.drawString("HP: " + player.getHp(), 340, 68);
        }
    }

    private void drawOverlay(Graphics2D g2) {
        if (!runtime.isStageClear() && !runtime.isGameOver()) {
            return;
        }
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, GameJFrame.GAME_WIDTH, GameJFrame.GAME_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 40));
        String title = runtime.isStageClear() ? "Stage Clear" : "Game Over";
        g2.drawString(title, 500, 300);
        g2.setFont(new Font("Consolas", Font.PLAIN, 22));
        g2.drawString("Press R to restart", 500, 350);
    }

    @Override
    public void run() {
        while (true) {
            this.repaint();
            try {
                Thread.sleep(16L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
