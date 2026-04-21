package com.tedu.manager;

import com.tedu.show.GameJFrame;

public class GameRuntime {
    public static volatile boolean waitingStart = true;
    public static volatile boolean loadingStart = false;
    public static volatile boolean startRequested = false;
    public static volatile boolean waitingRestart = false;
    public static volatile boolean restartRequested = false;
    public static volatile long startTimeMs = 0L;
    public static volatile long survivalTimeMs = 0L;
    public static volatile int killCount = 0;
    public static volatile int worldScrollX = 0;
    public static volatile int stageDistance = 0;
    public static volatile int stageLength = 5600;
    public static volatile int currentStage = 0;
    public static volatile int totalStages = 0;
    public static volatile boolean missionClear = false;
    public static volatile String finishTitle = "MISSION FAILED";
    public static volatile String bannerText = "";
    public static volatile long bannerUntilMs = 0L;

    public static final int FLOOR_MARGIN = 26;
    public static final int BATTLEFIELD_DEPTH = 180;

    private GameRuntime() {
    }

    public static void prepareStartScreen() {
        waitingStart = true;
        loadingStart = false;
        startRequested = false;
        waitingRestart = false;
        restartRequested = false;
        startTimeMs = 0L;
        survivalTimeMs = 0L;
        killCount = 0;
        worldScrollX = 0;
        stageDistance = 0;
        stageLength = 5600;
        currentStage = 0;
        totalStages = 0;
        missionClear = false;
        finishTitle = "MISSION FAILED";
        bannerText = "";
        bannerUntilMs = 0L;
    }

    public static void resetForNewGame() {
        startRequested = false;
        waitingRestart = false;
        restartRequested = false;
        startTimeMs = 0L;
        survivalTimeMs = 0L;
        killCount = 0;
        worldScrollX = 0;
        stageDistance = 0;
        stageLength = 5600;
        currentStage = 0;
        totalStages = 0;
        missionClear = false;
        finishTitle = "MISSION FAILED";
        bannerText = "";
        bannerUntilMs = 0L;
    }

    public static void beginStartTransition() {
        waitingStart = true;
        loadingStart = true;
        startRequested = false;
    }

    public static void finishStartTransition() {
        waitingStart = false;
        loadingStart = false;
        startRequested = false;
    }

    public static void markGameStartNow() {
        startTimeMs = System.currentTimeMillis();
        survivalTimeMs = 0L;
    }

    public static void beginStage(int stageNumber, int stageCount, int length) {
        currentStage = Math.max(1, stageNumber);
        totalStages = Math.max(currentStage, stageCount);
        worldScrollX = 0;
        stageDistance = 0;
        stageLength = Math.max(0, length);
    }

    public static void showBanner(String text, long durationMs) {
        bannerText = text == null ? "" : text;
        bannerUntilMs = System.currentTimeMillis() + Math.max(0L, durationMs);
    }

    public static int getStageProgressPercent() {
        if (stageLength <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(100, stageDistance * 100 / stageLength));
    }

    public static int getBattlefieldMaxBottom() {
        return GameJFrame.GameY - FLOOR_MARGIN;
    }

    public static int getBattlefieldMinBottom() {
        return getBattlefieldMaxBottom() - BATTLEFIELD_DEPTH;
    }

    public static int clampBattlefieldBottom(int bottom) {
        return Math.max(getBattlefieldMinBottom(), Math.min(getBattlefieldMaxBottom(), bottom));
    }
}
