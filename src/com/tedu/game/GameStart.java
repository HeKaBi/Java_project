package com.tedu.game;

import com.tedu.controller.GameListener;
import com.tedu.controller.GameThread;
import com.tedu.show.GameJFrame;
import com.tedu.show.GameMainJPanel;

public class GameStart {
    public static void main(String[] args) {
        GameJFrame gameFrame = new GameJFrame();
        GameMainJPanel mainPanel = new GameMainJPanel();
        GameListener listener = new GameListener();
        GameThread gameThread = new GameThread();

        gameFrame.setGamePanel(mainPanel);
        gameFrame.setGameListener(listener);
        gameFrame.setGameThread(gameThread);
        gameFrame.start();
    }
}
