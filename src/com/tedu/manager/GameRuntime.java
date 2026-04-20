package com.tedu.manager;

public class GameRuntime {
    public static volatile boolean waitingRestart = false;
    public static volatile boolean restartRequested = false;
    public static volatile long startTimeMs = 0L;
    public static volatile long survivalTimeMs = 0L;
    public static volatile int killCount = 0;

    private GameRuntime() {
    }

    public static void resetForNewGame() {
        waitingRestart = false;
        restartRequested = false;
        startTimeMs = System.currentTimeMillis();
        survivalTimeMs = 0L;
        killCount = 0;
    }
}
