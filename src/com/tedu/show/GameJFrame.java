package com.tedu.show;

import com.tedu.controller.GameListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

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
                installInputBindings(jPanel);
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

    private void installInputBindings(JComponent component) {
        if (!(keyListener instanceof GameListener)) {
            component.addKeyListener(keyListener);
            return;
        }
        GameListener listener = (GameListener) keyListener;
        bindKey(component, listener, KeyEvent.VK_LEFT, "left");
        bindKey(component, listener, KeyEvent.VK_RIGHT, "right");
        bindKey(component, listener, KeyEvent.VK_UP, "up");
        bindKey(component, listener, KeyEvent.VK_DOWN, "down");
        bindKey(component, listener, KeyEvent.VK_A, "a");
        bindKey(component, listener, KeyEvent.VK_D, "d");
        bindKey(component, listener, KeyEvent.VK_W, "w");
        bindKey(component, listener, KeyEvent.VK_S, "s");
        bindKey(component, listener, KeyEvent.VK_I, "i");
        bindKey(component, listener, KeyEvent.VK_J, "j");
        bindKey(component, listener, KeyEvent.VK_L, "l");
        bindKey(component, listener, KeyEvent.VK_U, "u");
        bindKey(component, listener, KeyEvent.VK_SHIFT, "shift");
        bindKey(component, listener, KeyEvent.VK_K, "k");
        bindKey(component, listener, KeyEvent.VK_1, "1");
        bindKey(component, listener, KeyEvent.VK_2, "2");
        bindKey(component, listener, KeyEvent.VK_NUMPAD1, "num1");
        bindKey(component, listener, KeyEvent.VK_NUMPAD2, "num2");
        bindKey(component, listener, KeyEvent.VK_R, "r");
    }

    private void bindKey(JComponent component, GameListener listener, int keyCode, String actionName) {
        String pressedAction = actionName + ".pressed";
        String releasedAction = actionName + ".released";
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, false), pressedAction);
        component.getActionMap().put(pressedAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.handleKeyPressed(keyCode);
            }
        });
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0, true), releasedAction);
        component.getActionMap().put(releasedAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.handleKeyReleased(keyCode);
            }
        });
    }
}
