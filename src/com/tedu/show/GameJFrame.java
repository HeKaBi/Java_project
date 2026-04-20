package com.tedu.show;

import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameJFrame extends JFrame {
    public static final int GAME_WIDTH = 1280;
    public static final int GAME_HEIGHT = 720;

    private JPanel gamePanel;
    private KeyListener gameListener;
    private Thread gameThread;

    public GameJFrame() {
        init();
    }

    private void init() {
        this.setSize(GAME_WIDTH, GAME_HEIGHT);
        this.setTitle("Metal Slug Course Project");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
    }

    public void start() {
        if (gamePanel != null) {
            this.add(gamePanel);
        }
        if (gameListener != null) {
            this.addKeyListener(gameListener);
            if (gamePanel != null) {
                gamePanel.addKeyListener(gameListener);
            }
        }
        if (gameThread != null) {
            gameThread.start();
        }
        this.setVisible(true);
        this.requestFocus();
        if (gamePanel != null) {
            gamePanel.setFocusable(true);
            gamePanel.setFocusTraversalKeysEnabled(false);
            gamePanel.requestFocusInWindow();
        }
        if (gamePanel instanceof Runnable runnable) {
            new Thread(runnable, "panel-refresh-thread").start();
        }
    }

    public void setGamePanel(JPanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void setGameListener(KeyListener gameListener) {
        this.gameListener = gameListener;
    }

    public void setGameThread(Thread gameThread) {
        this.gameThread = gameThread;
    }
}
