package com.tedu.show;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameJFrame extends JFrame {
    public static int GameX = 1000;
    public static int GameY = 640;

    private JPanel jPanel = null;
    private KeyListener keyListener = null;
    private MouseMotionListener mouseMotionListener = null;
    private MouseListener mouseListener = null;
    private Thread thead = null;

    public GameJFrame() {
        init();
    }

    public void init() {
        this.setTitle("Game1 - Metal Slug Prototype");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setFocusable(true);
    }

    public void addButton() {
    }

    public void start() {
        if (jPanel != null) {
            this.setContentPane(jPanel);
            if (keyListener != null) {
                jPanel.addKeyListener(keyListener);
            }
            if (mouseMotionListener != null) {
                jPanel.addMouseMotionListener(mouseMotionListener);
            }
            if (mouseListener != null) {
                jPanel.addMouseListener(mouseListener);
            }
        } else {
            if (keyListener != null) {
                this.addKeyListener(keyListener);
            }
            if (mouseMotionListener != null) {
                this.addMouseMotionListener(mouseMotionListener);
            }
            if (mouseListener != null) {
                this.addMouseListener(mouseListener);
            }
        }

        this.pack();
        this.setLocationRelativeTo(null);

        if (thead != null) {
            thead.start();
        }
        this.setVisible(true);

        if (jPanel != null) {
            jPanel.requestFocusInWindow();
        } else {
            this.requestFocusInWindow();
        }

        if (this.jPanel instanceof Runnable) {
            Runnable run = (Runnable) this.jPanel;
            Thread th = new Thread(run);
            th.start();
        }
    }

    public void setjPanel(JPanel jPanel) {
        this.jPanel = jPanel;
    }

    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    public void setMouseMotionListener(MouseMotionListener mouseMotionListener) {
        this.mouseMotionListener = mouseMotionListener;
    }

    public void setMouseListener(MouseListener mouseListener) {
        this.mouseListener = mouseListener;
    }

    public void setThead(Thread thead) {
        this.thead = thead;
    }
}
