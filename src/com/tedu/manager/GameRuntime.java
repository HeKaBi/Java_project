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
    public static volatile String finishTitle = "任务失败";
    public static volatile String bannerText = "";
    public static volatile long bannerUntilMs = 0L;
    public static volatile boolean stageTransitionActive = false;
    public static volatile boolean stageTransitionClosing = false;
    public static volatile long stageTransitionStartMs = 0L;
    public static volatile long stageTransitionDurationMs = 0L;
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
        finishTitle = "任务失败";
        bannerText = "";
        bannerUntilMs = 0L;
        stageTransitionActive = false;
        stageTransitionClosing = false;
        stageTransitionStartMs = 0L;
        stageTransitionDurationMs = 0L;
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
        finishTitle = "任务失败";
        bannerText = "";
        bannerUntilMs = 0L;
        stageTransitionActive = false;
        stageTransitionClosing = false;
        stageTransitionStartMs = 0L;
        stageTransitionDurationMs = 0L;
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

    public static void beginStageTransition(boolean closing, long durationMs) {
        stageTransitionActive = true;
        stageTransitionClosing = closing;
        stageTransitionStartMs = System.currentTimeMillis();
        stageTransitionDurationMs = Math.max(1L, durationMs);
    }

    public static void clearStageTransition() {
        stageTransitionActive = false;
        stageTransitionClosing = false;
        stageTransitionStartMs = 0L;
        stageTransitionDurationMs = 0L;
    }

    public static double getStageTransitionProgress() {
        if (!stageTransitionActive || stageTransitionDurationMs <= 0L) {
            return 0.0;
        }
        double elapsed = (System.currentTimeMillis() - stageTransitionStartMs) / (double) stageTransitionDurationMs;
        return Math.max(0.0, Math.min(1.0, elapsed));
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
        return clampAllowedBottom(terrain.resolveBottom(screenX));
    }

    public static int getBattlefieldWallTopBottomAt(int screenX) {
        TerrainState terrain = terrainState;
        if (terrain == null || terrain.wallTopBottomByX == null || terrain.wallTopBottomByX.length == 0) {
            return -1;
        }
        int value = terrain.resolveWallTopBottom(screenX);
        return value <= 0 ? -1 : clampAllowedBottom(value);
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
        terrainState = new TerrainState(maxBottomByX.clone(), null, mapScreenX, Math.max(1, mapScreenWidth));
    }

    public static void setBattlefieldTerrain(int[] maxBottomByX, int[] wallTopBottomByX, int mapScreenX, int mapScreenWidth) {
        if (maxBottomByX == null || maxBottomByX.length == 0) {
            terrainState = null;
            return;
        }
        int[] wall = (wallTopBottomByX == null || wallTopBottomByX.length == 0) ? null : wallTopBottomByX.clone();
        terrainState = new TerrainState(maxBottomByX.clone(), wall, mapScreenX, Math.max(1, mapScreenWidth));
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
        private final int[] wallTopBottomByX;
        private volatile int mapScreenX;
        private volatile int mapScreenWidth;

        private TerrainState(int[] maxBottomByX, int[] wallTopBottomByX, int mapScreenX, int mapScreenWidth) {
            this.maxBottomByX = maxBottomByX;
            this.wallTopBottomByX = wallTopBottomByX;
            this.mapScreenX = mapScreenX;
            this.mapScreenWidth = mapScreenWidth;
        }

        private int resolveBottom(int screenX) {
            if (maxBottomByX.length == 1) {
                return maxBottomByX[0];
            }
            double position = resolveProfilePosition(screenX);
            int leftIndex = (int) Math.floor(position);
            int rightIndex = Math.min(maxBottomByX.length - 1, leftIndex + 1);
            if (leftIndex >= rightIndex) {
                return maxBottomByX[Math.max(0, Math.min(maxBottomByX.length - 1, leftIndex))];
            }
            double ratio = position - leftIndex;
            return (int) Math.round(maxBottomByX[leftIndex]
                    + (maxBottomByX[rightIndex] - maxBottomByX[leftIndex]) * ratio);
        }

        private int resolveWallTopBottom(int screenX) {
            if (wallTopBottomByX == null || wallTopBottomByX.length == 0) {
                return -1;
            }
            if (wallTopBottomByX.length == 1) {
                return wallTopBottomByX[0];
            }
            double position = resolveProfilePosition(screenX);
            int leftIndex = (int) Math.floor(position);
            int rightIndex = Math.min(wallTopBottomByX.length - 1, leftIndex + 1);
            if (leftIndex >= rightIndex) {
                return wallTopBottomByX[Math.max(0, Math.min(wallTopBottomByX.length - 1, leftIndex))];
            }
            int leftValue = wallTopBottomByX[leftIndex];
            int rightValue = wallTopBottomByX[rightIndex];
            if (leftValue <= 0 && rightValue <= 0) {
                return -1;
            }
            if (leftValue <= 0) {
                return rightValue;
            }
            if (rightValue <= 0) {
                return leftValue;
            }
            double ratio = position - leftIndex;
            return (int) Math.round(leftValue + (rightValue - leftValue) * ratio);
        }

        private double resolveProfilePosition(int screenX) {
            double ratio = (screenX - mapScreenX) / (double) Math.max(1, mapScreenWidth - 1);
            if (ratio <= 0.0) {
                return 0.0;
            }
            if (ratio >= 1.0) {
                return maxBottomByX.length - 1;
            }
            return ratio * (maxBottomByX.length - 1);
        }
    }
}
