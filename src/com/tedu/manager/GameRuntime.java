package com.tedu.manager;

import com.tedu.show.GameJFrame;

public class GameRuntime {
    public static final int FLOOR_MARGIN = 26;
    public static final int BATTLEFIELD_DEPTH = 180;
    private static final int DEFAULT_BATTLEFIELD_MAX_BOTTOM = GameJFrame.GameY - FLOOR_MARGIN;

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
    public static volatile int battlefieldMaxBottom = DEFAULT_BATTLEFIELD_MAX_BOTTOM;
    private static volatile TerrainState terrainState = null;

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
        resetBattlefield();
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
        resetBattlefield();
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
        TerrainState terrain = terrainState;
        if (terrain != null) {
            return getBattlefieldMaxBottomAt(GameJFrame.GameX / 2);
        }
        return battlefieldMaxBottom;
    }

    public static int getBattlefieldMinBottom() {
        return getBattlefieldMaxBottom() - BATTLEFIELD_DEPTH;
    }

    public static int clampBattlefieldBottom(int bottom) {
        return Math.max(getBattlefieldMinBottom(), Math.min(getBattlefieldMaxBottom(), bottom));
    }

    public static int getBattlefieldMaxBottomAt(int screenX) {
        TerrainState terrain = terrainState;
        if (terrain == null || terrain.maxBottomByX == null || terrain.maxBottomByX.length == 0) {
            return clampAllowedBottom(battlefieldMaxBottom);
        }
        int profileIndex = terrain.resolveProfileIndex(screenX);
        return clampAllowedBottom(terrain.maxBottomByX[profileIndex]);
    }

    public static int getBattlefieldMinBottomAt(int screenX) {
        return getBattlefieldMaxBottomAt(screenX) - BATTLEFIELD_DEPTH;
    }

    public static int clampBattlefieldBottomAt(int bottom, int screenX) {
        int minBottom = getBattlefieldMinBottomAt(screenX);
        int maxBottom = getBattlefieldMaxBottomAt(screenX);
        return Math.max(minBottom, Math.min(maxBottom, bottom));
    }

    public static void resetBattlefield() {
        battlefieldMaxBottom = DEFAULT_BATTLEFIELD_MAX_BOTTOM;
        terrainState = null;
    }

    public static void setBattlefieldMaxBottom(int bottom) {
        battlefieldMaxBottom = clampAllowedBottom(bottom);
    }

    public static void setBattlefieldTerrain(int[] maxBottomByX, int mapScreenX, int mapScreenWidth) {
        if (maxBottomByX == null || maxBottomByX.length == 0) {
            terrainState = null;
            return;
        }
        terrainState = new TerrainState(maxBottomByX.clone(), mapScreenX, Math.max(1, mapScreenWidth));
    }

    public static void updateBattlefieldTerrainViewport(int mapScreenX, int mapScreenWidth) {
        TerrainState terrain = terrainState;
        if (terrain == null) {
            return;
        }
        terrain.mapScreenX = mapScreenX;
        terrain.mapScreenWidth = Math.max(1, mapScreenWidth);
    }

    private static int clampAllowedBottom(int bottom) {
        int minAllowedBottom = Math.max(BATTLEFIELD_DEPTH + 32, GameJFrame.GameY / 3);
        int maxAllowedBottom = GameJFrame.GameY - 8;
        return Math.max(minAllowedBottom, Math.min(maxAllowedBottom, bottom));
    }

    private static final class TerrainState {
        private final int[] maxBottomByX;
        private volatile int mapScreenX;
        private volatile int mapScreenWidth;

        private TerrainState(int[] maxBottomByX, int mapScreenX, int mapScreenWidth) {
            this.maxBottomByX = maxBottomByX;
            this.mapScreenX = mapScreenX;
            this.mapScreenWidth = mapScreenWidth;
        }

        private int resolveProfileIndex(int screenX) {
            if (maxBottomByX.length == 1) {
                return 0;
            }
            double ratio = (screenX - mapScreenX) / (double) Math.max(1, mapScreenWidth - 1);
            int index = (int) Math.round(ratio * (maxBottomByX.length - 1));
            if (index < 0) {
                return 0;
            }
            if (index >= maxBottomByX.length) {
                return maxBottomByX.length - 1;
            }
            return index;
        }
    }
}
