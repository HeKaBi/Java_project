package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class MapObj extends ElementObj {
    private static final int GROUND_SCREEN_OFFSET = 18;
    private static final int WALL_MIN_SPAN_PIXELS = 6;
    private static final int WALL_MIN_HEIGHT_PIXELS = 10;
    private static final String MAP2_PATH_SUFFIX = "/map2.png";
    private static final String MAP3_PATH_SUFFIX = "/map3.png";
    private static final int GUIDE_INTERPOLATE_GAP_MAX = 12;
    private static final int SECONDARY_GUIDE_GAP_MAX = 18;
    private static final int SECONDARY_GUIDE_MIN_SEPARATION = 7;
    private static final int SECONDARY_GUIDE_MIN_LENGTH = 80;
    private static final int SECONDARY_GUIDE_EXTENSION_STEP_MAX = 8;
    private static final int SECONDARY_GUIDE_EXTENSION_LIMIT = 95;
    private static final int SECONDARY_GUIDE_RIGHT_EXTENSION_LIMIT = 220;
    private static final int PLATFORM_SEGMENT_SOURCE_WIDTH = 1;
    private static final int PLATFORM_SCREEN_HEIGHT = 12;
    private static final String[] MISSION1_GUIDE_PATHS = {
            "image/images/背景/mission1红线图.png"
    };
    private static final int MISSION1_PROFILE_BASE_WIDTH = 2171;
    private static final int MISSION1_PROFILE_BASE_HEIGHT = 197;
    private static final int[][] MISSION1_UPPER_PLATFORM_ANCHORS = {
            {842, 164},
            {860, 163},
            {880, 161},
            {900, 151},
            {930, 150},
            {950, 149},
            {970, 141},
            {980, 134},
            {990, 126},
            {1000, 114},
            {1010, 104},
            {1020, 99},
            {1040, 100},
            {1060, 104},
            {1080, 108},
            {1100, 111},
            {1120, 113},
            {1140, 115},
            {1160, 117},
            {1180, 120},
            {1200, 124},
            {1220, 127},
            {1240, 128},
            {1260, 130},
            {1280, 133},
            {1300, 137},
            {1320, 143},
            {1340, 149},
            {1360, 154},
            {1380, 158}
    };
    private static final int MAP2_PROFILE_BASE_WIDTH = 3822;
    private static final int MAP2_PROFILE_BASE_HEIGHT = 239;
    private static final String[] MAP2_GUIDE_PATHS = {
            // Stage 2 terrain + walls are authored in this red-line guide.
            "image/images/背景/指导.png"
    };
    private static final String[] MAP3_GUIDE_PATHS = {
            "image/images/\u80cc\u666f/\u6307\u5bfc2.png"
    };
    private static final Map<String, Integer> GROUND_ROW_CACHE = new HashMap<>();
    private static final Map<String, int[]> GROUND_PROFILE_CACHE = new HashMap<>();
    private static final Map<String, int[]> WALL_TOP_PROFILE_CACHE = new HashMap<>();
    private static final int[][] MAP2_MAIN_GROUND_ANCHORS = {
            {0, 134},
            {120, 137},
            {240, 144},
            {360, 168},
            {480, 147},
            {600, 151},
            {720, 118},
            {840, 116},
            {960, 169},
            {1080, 124},
            {1200, 111},
            {1320, 135},
            {1440, 136},
            {1560, 97},
            {1680, 155},
            {1800, 165},
            {1920, 165},
            {2040, 130},
            {2160, 137},
            {2280, 152},
            {2400, 162},
            {2520, 167},
            {2640, 156},
            {2760, 161},
            {2880, 134},
            {3000, 152},
            {3120, 143},
            {3240, 179},
            {3360, 171},
            {3480, 176},
            {3600, 183},
            {3720, 183},
            {3821, 183}
    };

    private double scrollRatio = 1.0;
    private int minX = 0;

    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(),
                    this.getX(), this.getY(),
                    this.getW(), this.getH(),
                    null);
        }
    }

    @Override
    protected void move() {
        int shift = (int) Math.round(GameRuntime.worldScrollX * scrollRatio);
        if (shift > 0) {
            int nextX = this.getX() - shift;
            if (nextX < minX) {
                nextX = minX;
            }
            this.setX(nextX);
        }
        GameRuntime.updateBattlefieldTerrainViewport(this.getX(), this.getW());
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        String mapPath = split[2];
        ImageIcon icon = GameLoad.getImage(mapPath);
        this.setIcon(icon);
        int mapWidth = resolveMapWidth(icon);
        this.setW(mapWidth);
        this.setH(GameJFrame.GameY);
        this.minX = this.getX() - Math.max(0, mapWidth - GameJFrame.GameX);
        int[] groundProfile = resolveGroundProfile(icon, mapPath);
        int[] wallTopProfile = resolveWallTopProfile(icon, mapPath);
        if (groundProfile != null && groundProfile.length > 0) {
            int maxBottom = groundProfile[0];
            for (int bottom : groundProfile) {
                maxBottom = Math.max(maxBottom, bottom);
            }
            if (wallTopProfile != null && wallTopProfile.length == groundProfile.length) {
                GameRuntime.setBattlefieldTerrain(groundProfile, wallTopProfile, this.getX(), this.getW());
            } else {
                GameRuntime.setBattlefieldTerrain(groundProfile, this.getX(), this.getW());
            }
            GameRuntime.setBattlefieldMaxBottom(maxBottom);
        } else {
            GameRuntime.setBattlefieldMaxBottom(resolveBattlefieldBottom(icon, mapPath));
        }
        if (split.length > 3) {
            this.scrollRatio = Double.parseDouble(split[3]);
        }
        return this;
    }

    private int resolveMapWidth(ImageIcon icon) {
        if (icon == null || icon.getIconWidth() <= 0) {
            return GameJFrame.GameX;
        }
        if (icon.getIconHeight() <= 0) {
            return Math.max(GameJFrame.GameX, icon.getIconWidth());
        }
        double scale = GameJFrame.GameY / (double) icon.getIconHeight();
        return Math.max(GameJFrame.GameX, (int) Math.round(icon.getIconWidth() * scale));
    }

    private int[] resolveGroundProfile(ImageIcon icon, String mapPath) {
        if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 1) {
            return null;
        }
        int[] cachedProfile = GROUND_PROFILE_CACHE.get(mapPath);
        if (cachedProfile != null) {
            return cachedProfile;
        }
        BufferedImage image = toBufferedImage(icon);
        if (image == null) {
            return null;
        }
        int[] groundRows = resolveManualGroundRows(mapPath, image.getWidth(), image.getHeight());
        if (groundRows == null || groundRows.length == 0) {
            groundRows = detectGroundRows(image);
        }
        if (groundRows == null || groundRows.length == 0) {
            return null;
        }
        double scaleY = this.getH() / (double) icon.getIconHeight();
        int[] maxBottomByX = new int[groundRows.length];
        for (int x = 0; x < groundRows.length; x++) {
            maxBottomByX[x] = this.getY() + (int) Math.round((groundRows[x] + 1) * scaleY) + GROUND_SCREEN_OFFSET;
        }
        GROUND_PROFILE_CACHE.put(mapPath, maxBottomByX);
        return maxBottomByX;
    }

    private int[] resolveWallTopProfile(ImageIcon icon, String mapPath) {
        if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 1) {
            return null;
        }
        int[] cached = WALL_TOP_PROFILE_CACHE.get(mapPath);
        if (cached != null) {
            return cached;
        }
        String normalizedPath = mapPath == null ? "" : mapPath.replace('\\', '/').toLowerCase();
        if (!(matchesMapPath(normalizedPath, MAP2_PATH_SUFFIX) || matchesMapPath(normalizedPath, MAP3_PATH_SUFFIX)
                || normalizedPath.endsWith("/mission1.png") || normalizedPath.endsWith("mission1.png"))) {
            return null;
        }

        BufferedImage image = toBufferedImage(icon);
        if (image == null) {
            return null;
        }
        int[] wallTopRows = resolveGuideWallTopRows(image.getWidth(), image.getHeight(), normalizedPath);
        if (wallTopRows == null || wallTopRows.length == 0) {
            return null;
        }
        double scaleY = this.getH() / (double) icon.getIconHeight();
        int[] wallTopBottomByX = new int[wallTopRows.length];
        for (int x = 0; x < wallTopRows.length; x++) {
            if (wallTopRows[x] < 0) {
                wallTopBottomByX[x] = -1;
                continue;
            }
            wallTopBottomByX[x] = this.getY() + (int) Math.round((wallTopRows[x] + 1) * scaleY) + GROUND_SCREEN_OFFSET;
        }
        WALL_TOP_PROFILE_CACHE.put(mapPath, wallTopBottomByX);
        return wallTopBottomByX;
    }

    private int[] resolveGuideWallTopRows(int targetWidth, int targetHeight, String normalizedMapPath) {
        String[] guidePaths;
        if (normalizedMapPath.endsWith("/mission1.png") || normalizedMapPath.endsWith("mission1.png")) {
            guidePaths = MISSION1_GUIDE_PATHS;
        } else if (matchesMapPath(normalizedMapPath, MAP2_PATH_SUFFIX)) {
            guidePaths = MAP2_GUIDE_PATHS;
        } else if (matchesMapPath(normalizedMapPath, MAP3_PATH_SUFFIX)) {
            guidePaths = MAP3_GUIDE_PATHS;
        } else {
            return null;
        }
        for (String guidePath : guidePaths) {
            ImageIcon guideIcon = GameLoad.getImage(guidePath);
            BufferedImage guideImage = toBufferedImageStatic(guideIcon);
            if (guideImage == null) {
                continue;
            }
            int[] sourceWallTopRows = extractGuideWallTopRows(guideImage);
            if (sourceWallTopRows == null || sourceWallTopRows.length == 0) {
                continue;
            }
            int[] scaled = scaleOptionalGuideRows(sourceWallTopRows, targetWidth, targetHeight, guideImage.getHeight());
            if (scaled == null) {
                continue;
            }
            if (!hasGuideRows(scaled)) {
                continue;
            }
            return scaled;
        }
        return null;
    }

    private static int[] extractGuideWallTopRows(BufferedImage guideImage) {
        if (guideImage == null || guideImage.getWidth() <= 0 || guideImage.getHeight() <= 0) {
            return null;
        }
        // Walls in the guide are thin vertical red strokes. Keep the detected profile sparse so
        // separated cliff edges do not turn into a continuous invisible barrier after scaling.
        List<int[]> runsByX = extractGuideRuns(guideImage);
        int width = guideImage.getWidth();
        int[] wallTop = new int[width];
        Arrays.fill(wallTop, -1);
        for (int x = 0; x < width; x++) {
            int[] runs = runsByX.get(x);
            if (runs == null || runs.length == 0) {
                continue;
            }
            int top = selectWallTopRun(guideImage, x, runs);
            if (top < 0) {
                continue;
            }
            wallTop[x] = top;
        }
        return wallTop;
    }

    /**
     * Like {@link #scaleGuideRows(int[], int, int, int)} but preserves -1 "unknown" entries.
     * Wall data is sparse by design; clamping -1 to 0 would create bogus walls.
     */
    private int[] scaleOptionalGuideRows(int[] sourceRows, int targetWidth, int targetHeight, int sourceHeight) {
        if (sourceRows == null || sourceRows.length == 0 || targetWidth <= 0 || targetHeight <= 0 || sourceHeight <= 0) {
            return null;
        }
        int[] scaled = new int[targetWidth];
        Arrays.fill(scaled, -1);
        if (targetWidth == 1) {
            int y = sourceRows[0];
            if (y >= 0) {
                scaled[0] = Math.max(0, Math.min(targetHeight - 1,
                        (int) Math.round(y * (targetHeight - 1) / (double) Math.max(1, sourceHeight - 1))));
            }
            return scaled;
        }
        for (int x = 0; x < targetWidth; x++) {
            double sourceX = x * (sourceRows.length - 1) / (double) Math.max(1, targetWidth - 1);
            int leftIndex = (int) Math.floor(sourceX);
            int rightIndex = Math.min(sourceRows.length - 1, leftIndex + 1);
            double ratio = sourceX - leftIndex;
            int leftY = sourceRows[leftIndex];
            int rightY = sourceRows[rightIndex];
            if (leftY < 0 && rightY < 0) {
                continue;
            }
            if (leftY < 0 || rightY < 0) {
                int nearestIndex = ratio < 0.5 ? leftIndex : rightIndex;
                int nearestY = sourceRows[nearestIndex];
                if (nearestY < 0) {
                    continue;
                }
                int scaledY = (int) Math.round(nearestY * (targetHeight - 1) / (double) Math.max(1, sourceHeight - 1));
                scaled[x] = Math.max(0, Math.min(targetHeight - 1, scaledY));
                continue;
            }
            int chosenY = (int) Math.round(leftY + (rightY - leftY) * ratio);
            int scaledY = (int) Math.round(chosenY * (targetHeight - 1) / (double) Math.max(1, sourceHeight - 1));
            scaled[x] = Math.max(0, Math.min(targetHeight - 1, scaledY));
        }
        dropSinglePixelIslands(scaled);
        return scaled;
    }

    private int[] resolveManualGroundRows(String mapPath, int width, int height) {
        String normalizedPath = mapPath == null ? "" : mapPath.replace('\\', '/').toLowerCase();
        if (normalizedPath.endsWith("/mission1.png") || normalizedPath.endsWith("mission1.png")) {
            return resolveMission1GuideGroundRows(width, height);
        }
        if (matchesMapPath(normalizedPath, MAP2_PATH_SUFFIX)) {
            int[] guideRows = resolveGuideGroundRows(width, height, MAP2_GUIDE_PATHS);
            if (guideRows != null) {
                return guideRows;
            }
            // No usable guide image: fall back to auto-detection on the map itself.
            return null;
        }
        if (matchesMapPath(normalizedPath, MAP3_PATH_SUFFIX)) {
            return resolveGuideGroundRows(width, height, MAP3_GUIDE_PATHS);
        }
        return null;
    }

    private boolean matchesMapPath(String normalizedPath, String suffix) {
        return normalizedPath.endsWith(suffix) || normalizedPath.endsWith(suffix.substring(1));
    }

    public static List<int[]> buildSupplementalPlatforms(String mapPath, int mapScreenX, int mapScreenY,
                                                         int mapScreenWidth, int mapScreenHeight) {
        String normalizedPath = mapPath == null ? "" : mapPath.replace('\\', '/').toLowerCase();
        if (!normalizedPath.endsWith("/mission1.png") && !normalizedPath.endsWith("mission1.png")) {
            return List.of();
        }
        BufferedImage guideImage = loadGuideImage(MISSION1_GUIDE_PATHS);
        if (guideImage == null) {
            return List.of();
        }
        int[] primaryRows = extractBottomGuideRows(guideImage);
        int[] secondaryRows = extractMission1UpperRows(guideImage, primaryRows);
        if (secondaryRows == null) {
            return List.of();
        }
        return buildPlatformSegments(secondaryRows, guideImage.getWidth(), guideImage.getHeight(),
                mapScreenX, mapScreenY, mapScreenWidth, mapScreenHeight);
    }

    private int[] resolveGuideGroundRows(int width, int height, String[] guidePaths) {
        if (guidePaths == null || guidePaths.length == 0) {
            return null;
        }
        for (String guidePath : guidePaths) {
            ImageIcon guideIcon = GameLoad.getImage(guidePath);
            BufferedImage guideImage = toBufferedImage(guideIcon);
            if (guideImage == null) {
                continue;
            }
            int[] guideRows = extractBottomGuideRows(guideImage);
            if (guideRows == null || guideRows.length == 0) {
                continue;
            }
            int[] scaledRows = scaleGuideRows(guideRows, width, height, guideImage.getHeight());
            if (scaledRows != null) {
                return smoothGroundRows(scaledRows);
            }
        }
        return null;
    }

    private int[] resolveMission1GuideGroundRows(int width, int height) {
        BufferedImage guideImage = loadGuideImage(MISSION1_GUIDE_PATHS);
        if (guideImage == null) {
            return null;
        }
        int[] guideRows = extractBottomGuideRows(guideImage);
        if (guideRows == null || guideRows.length == 0) {
            return null;
        }
        return scaleGuideRows(guideRows, width, height, guideImage.getHeight());
    }

    private static BufferedImage loadGuideImage(String[] guidePaths) {
        if (guidePaths == null || guidePaths.length == 0) {
            return null;
        }
        for (String guidePath : guidePaths) {
            ImageIcon guideIcon = GameLoad.getImage(guidePath);
            BufferedImage guideImage = toBufferedImageStatic(guideIcon);
            if (guideImage != null) {
                return guideImage;
            }
        }
        return null;
    }

    private static int[] extractBottomGuideRows(BufferedImage guideImage) {
        List<int[]> runsByX = extractGuideRuns(guideImage);
        int[] rows = new int[guideImage.getWidth()];
        Arrays.fill(rows, -1);
        for (int x = 0; x < runsByX.size(); x++) {
            int[] runs = runsByX.get(x);
            if (runs.length == 0) {
                continue;
            }
            rows[x] = runs[runs.length - 1];
        }
        fillGuideGaps(rows);
        return rows;
    }

    private static int[] extractMission1UpperRows(BufferedImage guideImage, int[] primaryRows) {
        if (guideImage == null || primaryRows == null || primaryRows.length == 0) {
            return null;
        }
        List<int[]> runsByX = extractGuideRuns(guideImage);
        int[] rows = new int[guideImage.getWidth()];
        Arrays.fill(rows, -1);
        int previousUpperRow = -1;
        for (int x = 0; x < runsByX.size(); x++) {
            int[] runs = runsByX.get(x);
            if (runs.length < 2) {
                continue;
            }
            int primaryRow = primaryRows[Math.min(primaryRows.length - 1, x)];
            int bestRow = -1;
            int bestScore = Integer.MAX_VALUE;
            for (int i = runs.length - 2; i >= 0; i--) {
                int row = runs[i];
                if (primaryRow - row < SECONDARY_GUIDE_MIN_SEPARATION) {
                    continue;
                }
                int target = previousUpperRow >= 0 ? previousUpperRow : primaryRow - 20;
                int score = Math.abs(row - target);
                if (score < bestScore || (score == bestScore && row > bestRow)) {
                    bestScore = score;
                    bestRow = row;
                }
            }
            if (bestRow >= 0) {
                rows[x] = bestRow;
                previousUpperRow = bestRow;
            }
        }
        fillSparseGuideRows(rows, SECONDARY_GUIDE_GAP_MAX);
        keepLongestGuideRun(rows, SECONDARY_GUIDE_MIN_LENGTH);
        extendGuideRunIntoSharedSlope(rows, runsByX);
        extendGuideRunAlongDescent(rows, runsByX);
        return hasGuideRows(rows) ? rows : null;
    }

    private static void extendGuideRunIntoSharedSlope(int[] rows, List<int[]> runsByX) {
        int firstKnown = -1;
        for (int x = 0; x < rows.length; x++) {
            if (rows[x] >= 0) {
                firstKnown = x;
                break;
            }
        }
        if (firstKnown <= 0) {
            return;
        }
        int currentRow = rows[firstKnown];
        int minX = Math.max(0, firstKnown - SECONDARY_GUIDE_EXTENSION_LIMIT);
        for (int x = firstKnown - 1; x >= minX; x--) {
            int[] runs = runsByX.get(x);
            if (runs.length == 0) {
                break;
            }
            int bestRow = -1;
            int bestDistance = Integer.MAX_VALUE;
            for (int row : runs) {
                int distance = Math.abs(row - currentRow);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestRow = row;
                }
            }
            if (bestRow < 0 || bestDistance > SECONDARY_GUIDE_EXTENSION_STEP_MAX) {
                break;
            }
            rows[x] = bestRow;
            currentRow = bestRow;
        }
    }

    private static void extendGuideRunAlongDescent(int[] rows, List<int[]> runsByX) {
        int lastKnown = -1;
        for (int x = rows.length - 1; x >= 0; x--) {
            if (rows[x] >= 0) {
                lastKnown = x;
                break;
            }
        }
        if (lastKnown < 0 || lastKnown >= rows.length - 1) {
            return;
        }
        int currentRow = rows[lastKnown];
        int maxX = Math.min(rows.length - 1, lastKnown + SECONDARY_GUIDE_RIGHT_EXTENSION_LIMIT);
        for (int x = lastKnown + 1; x <= maxX; x++) {
            int[] runs = runsByX.get(x);
            if (runs.length == 0) {
                break;
            }
            int bestRow = -1;
            int bestDistance = Integer.MAX_VALUE;
            for (int row : runs) {
                int distance = Math.abs(row - currentRow);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestRow = row;
                }
            }
            if (bestRow < 0 || bestDistance > SECONDARY_GUIDE_EXTENSION_STEP_MAX) {
                break;
            }
            rows[x] = bestRow;
            currentRow = bestRow;
        }
    }

    private static List<int[]> buildPlatformSegments(int[] sourceRows, int sourceWidth, int sourceHeight,
                                                     int mapScreenX, int mapScreenY, int mapScreenWidth,
                                                     int mapScreenHeight) {
        if (sourceRows == null || sourceRows.length == 0 || sourceWidth <= 0 || sourceHeight <= 0) {
            return List.of();
        }
        double scaleX = mapScreenWidth / (double) Math.max(1, sourceWidth);
        double scaleY = mapScreenHeight / (double) Math.max(1, sourceHeight);
        List<int[]> platforms = new ArrayList<>();
        int runStart = -1;
        for (int x = 0; x <= sourceRows.length; x++) {
            boolean valid = x < sourceRows.length && sourceRows[x] >= 0;
            if (valid) {
                if (runStart < 0) {
                    runStart = x;
                }
                continue;
            }
            if (runStart < 0) {
                continue;
            }
            int runEnd = x - 1;
            for (int chunkStart = runStart; chunkStart <= runEnd; chunkStart += PLATFORM_SEGMENT_SOURCE_WIDTH) {
                int chunkEnd = Math.min(runEnd, chunkStart + PLATFORM_SEGMENT_SOURCE_WIDTH - 1);
                int sum = 0;
                int count = 0;
                for (int i = chunkStart; i <= chunkEnd; i++) {
                    if (sourceRows[i] >= 0) {
                        sum += sourceRows[i];
                        count++;
                    }
                }
                if (count == 0) {
                    continue;
                }
                int averageRow = Math.round(sum / (float) count);
                int screenX = mapScreenX + (int) Math.round(chunkStart * scaleX);
                int screenRight = mapScreenX + (int) Math.round((chunkEnd + 1) * scaleX);
                int screenWidth = Math.max(18, screenRight - screenX);
                int screenY = mapScreenY + (int) Math.round((averageRow + 1) * scaleY) + GROUND_SCREEN_OFFSET;
                platforms.add(new int[] {screenX, screenY, screenWidth, PLATFORM_SCREEN_HEIGHT});
            }
            runStart = -1;
        }
        return platforms;
    }

    private static List<int[]> extractGuideRuns(BufferedImage guideImage) {
        List<int[]> runsByX = new ArrayList<>(guideImage.getWidth());
        int height = guideImage.getHeight();
        for (int x = 0; x < guideImage.getWidth(); x++) {
            List<Integer> centers = new ArrayList<>();
            int runStart = -1;
            for (int y = 0; y <= height; y++) {
                boolean guidePixel = y < height && isGuideBoundaryPixelStatic(guideImage.getRGB(x, y));
                if (guidePixel) {
                    if (runStart < 0) {
                        runStart = y;
                    }
                    continue;
                }
                if (runStart >= 0) {
                    int runEnd = y - 1;
                    centers.add((runStart + runEnd) / 2);
                    runStart = -1;
                }
            }
            int[] centersArray = new int[centers.size()];
            for (int i = 0; i < centers.size(); i++) {
                centersArray[i] = centers.get(i);
            }
            runsByX.add(centersArray);
        }
        return runsByX;
    }

    private static void fillSparseGuideRows(int[] rows, int maxGap) {
        int previousKnownIndex = -1;
        for (int x = 0; x < rows.length; x++) {
            if (rows[x] < 0) {
                continue;
            }
            if (previousKnownIndex >= 0 && previousKnownIndex + 1 < x && x - previousKnownIndex - 1 <= maxGap) {
                for (int gapX = previousKnownIndex + 1; gapX < x; gapX++) {
                    double ratio = (gapX - previousKnownIndex) / (double) (x - previousKnownIndex);
                    rows[gapX] = (int) Math.round(rows[previousKnownIndex] + (rows[x] - rows[previousKnownIndex]) * ratio);
                }
            }
            previousKnownIndex = x;
        }
    }

    private static void dropSinglePixelIslands(int[] rows) {
        if (rows == null || rows.length < 3) {
            return;
        }
        int[] copy = rows.clone();
        for (int x = 1; x < rows.length - 1; x++) {
            if (copy[x] < 0) {
                continue;
            }
            if (copy[x - 1] < 0 && copy[x + 1] < 0) {
                rows[x] = -1;
            }
        }
    }

    private static int selectWallTopRun(BufferedImage image, int x, int[] runs) {
        if (image == null || runs == null || runs.length == 0) {
            return -1;
        }
        for (int runCenter : runs) {
            int top = runCenter;
            while (top > 0 && isGuideBoundaryPixelStatic(image.getRGB(x, top - 1))) {
                top--;
            }
            int bottom = runCenter;
            while (bottom + 1 < image.getHeight() && isGuideBoundaryPixelStatic(image.getRGB(x, bottom + 1))) {
                bottom++;
            }
            if (bottom - top + 1 >= Math.max(WALL_MIN_HEIGHT_PIXELS, WALL_MIN_SPAN_PIXELS + 1)) {
                return top;
            }
        }
        return -1;
    }

    private static void keepLongestGuideRun(int[] rows, int minLength) {
        int bestStart = -1;
        int bestLength = 0;
        int runStart = -1;
        for (int x = 0; x <= rows.length; x++) {
            boolean valid = x < rows.length && rows[x] >= 0;
            if (valid) {
                if (runStart < 0) {
                    runStart = x;
                }
                continue;
            }
            if (runStart >= 0) {
                int length = x - runStart;
                if (length > bestLength) {
                    bestLength = length;
                    bestStart = runStart;
                }
                runStart = -1;
            }
        }
        if (bestStart < 0 || bestLength < minLength) {
            Arrays.fill(rows, -1);
            return;
        }
        int bestEnd = bestStart + bestLength;
        for (int x = 0; x < rows.length; x++) {
            if (x < bestStart || x >= bestEnd) {
                rows[x] = -1;
            }
        }
    }

    private static boolean hasGuideRows(int[] rows) {
        for (int row : rows) {
            if (row >= 0) {
                return true;
            }
        }
        return false;
    }

    private int[] extractGuideBoundaryRows(BufferedImage guideImage) {
        int width = guideImage.getWidth();
        int height = guideImage.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        int[] rows = new int[width];
        Arrays.fill(rows, -1);
        int previousGuideY = -1;
        int defaultGuideY = Math.max(0, Math.min(height - 1, (int) Math.round(height * 0.72)));
        for (int x = 0; x < width; x++) {
            int trackedGuideY = resolveTrackedGuideY(guideImage, x,
                    previousGuideY >= 0 ? previousGuideY : defaultGuideY);
            rows[x] = trackedGuideY;
            if (trackedGuideY >= 0) {
                previousGuideY = trackedGuideY;
            }
        }
        if (!hasGuideRows(rows)) {
            return null;
        }
        fillGuideGaps(rows);
        return rows;
    }

    private int resolveTrackedGuideY(BufferedImage guideImage, int x, int preferredY) {
        int bestCenterY = -1;
        int bestDistance = Integer.MAX_VALUE;
        int runStart = -1;
        int height = guideImage.getHeight();
        for (int y = 0; y <= height; y++) {
            boolean guidePixel = y < height && isGuideBoundaryPixel(guideImage.getRGB(x, y));
            if (guidePixel) {
                if (runStart < 0) {
                    runStart = y;
                }
                continue;
            }
            if (runStart < 0) {
                continue;
            }
            int runEnd = y - 1;
            int centerY = (runStart + runEnd) / 2;
            int distance = Math.abs(centerY - preferredY);
            if (distance < bestDistance || (distance == bestDistance && centerY > bestCenterY)) {
                bestDistance = distance;
                bestCenterY = centerY;
            }
            runStart = -1;
        }
        return bestCenterY;
    }

    private boolean isGuideBoundaryPixel(int rgb) {
        return isGuideBoundaryPixelStatic(rgb);
    }

    private static boolean isGuideBoundaryPixelStatic(int rgb) {
        int alpha = (rgb >>> 24) & 0xff;
        if (alpha < 24) {
            return false;
        }
        int red = (rgb >>> 16) & 0xff;
        int green = (rgb >>> 8) & 0xff;
        int blue = rgb & 0xff;
        return red >= 180 && green <= 125 && blue <= 125 && red - Math.max(green, blue) >= 70;
    }

    private static void fillGuideGaps(int[] rows) {
        int firstKnownIndex = -1;
        int previousKnownIndex = -1;
        for (int x = 0; x < rows.length; x++) {
            if (rows[x] < 0) {
                continue;
            }
            if (firstKnownIndex < 0) {
                firstKnownIndex = x;
            }
            if (previousKnownIndex >= 0 && previousKnownIndex + 1 < x) {
                int startRow = rows[previousKnownIndex];
                int endRow = rows[x];
                int gapLength = x - previousKnownIndex - 1;
                if (gapLength <= GUIDE_INTERPOLATE_GAP_MAX) {
                    for (int gapX = previousKnownIndex + 1; gapX < x; gapX++) {
                        double ratio = (gapX - previousKnownIndex) / (double) (x - previousKnownIndex);
                        rows[gapX] = (int) Math.round(startRow + (endRow - startRow) * ratio);
                    }
                } else {
                    int fallbackRow = Math.max(startRow, endRow);
                    Arrays.fill(rows, previousKnownIndex + 1, x, fallbackRow);
                }
            }
            previousKnownIndex = x;
        }
        if (previousKnownIndex < 0) {
            Arrays.fill(rows, 0);
            return;
        }
        for (int x = 0; x < firstKnownIndex; x++) {
            if (rows[x] < 0) {
                rows[x] = rows[firstKnownIndex];
            }
        }
        for (int x = previousKnownIndex + 1; x < rows.length; x++) {
            if (rows[x] < 0) {
                rows[x] = rows[previousKnownIndex];
            } else {
                previousKnownIndex = x;
            }
        }
    }

    private int[] scaleGuideRows(int[] sourceRows, int targetWidth, int targetHeight, int sourceHeight) {
        if (sourceRows == null || sourceRows.length == 0 || targetWidth <= 0 || targetHeight <= 0 || sourceHeight <= 0) {
            return null;
        }
        int[] scaledRows = new int[targetWidth];
        if (targetWidth == 1) {
            scaledRows[0] = Math.max(0, Math.min(targetHeight - 1,
                    (int) Math.round(sourceRows[0] * (targetHeight - 1) / (double) Math.max(1, sourceHeight - 1))));
            return scaledRows;
        }
        for (int x = 0; x < targetWidth; x++) {
            double sourceX = x * (sourceRows.length - 1) / (double) Math.max(1, targetWidth - 1);
            int leftIndex = (int) Math.floor(sourceX);
            int rightIndex = Math.min(sourceRows.length - 1, leftIndex + 1);
            double ratio = sourceX - leftIndex;
            double sourceY = sourceRows[leftIndex] + (sourceRows[rightIndex] - sourceRows[leftIndex]) * ratio;
            int scaledY = (int) Math.round(sourceY * (targetHeight - 1) / (double) Math.max(1, sourceHeight - 1));
            scaledRows[x] = Math.max(0, Math.min(targetHeight - 1, scaledY));
        }
        return scaledRows;
    }

    private static int[] buildRowsFromAnchors(int width, int height, int baseWidth, int baseHeight, int[][] anchors) {
        if (width <= 0 || height <= 0 || anchors == null || anchors.length == 0) {
            return null;
        }
        int[] rows = new int[width];
        int previousX = scaleAnchorX(anchors[0][0], width, baseWidth);
        int previousY = scaleAnchorY(anchors[0][1], height, baseHeight);
        Arrays.fill(rows, 0, Math.max(1, previousX + 1), previousY);
        for (int i = 1; i < anchors.length; i++) {
            int currentX = scaleAnchorX(anchors[i][0], width, baseWidth);
            int currentY = scaleAnchorY(anchors[i][1], height, baseHeight);
            if (currentX <= previousX) {
                rows[Math.max(0, Math.min(width - 1, previousX))] = currentY;
                previousY = currentY;
                continue;
            }
            for (int x = previousX; x <= currentX && x < width; x++) {
                double ratio = (x - previousX) / (double) (currentX - previousX);
                rows[x] = (int) Math.round(previousY + (currentY - previousY) * ratio);
            }
            previousX = currentX;
            previousY = currentY;
        }
        if (previousX < width - 1) {
            Arrays.fill(rows, previousX + 1, width, previousY);
        }
        clampSlope(rows, 2);
        return rows;
    }

    private static int scaleAnchorX(int x, int width, int baseWidth) {
        if (width <= 1 || baseWidth <= 1) {
            return 0;
        }
        return Math.max(0, Math.min(width - 1, (int) Math.round(x * (width - 1) / (double) (baseWidth - 1))));
    }

    private static int scaleAnchorY(int y, int height, int baseHeight) {
        if (height <= 1 || baseHeight <= 1) {
            return 0;
        }
        return Math.max(0, Math.min(height - 1, (int) Math.round(y * (height - 1) / (double) (baseHeight - 1))));
    }

    private int resolveBattlefieldBottom(ImageIcon icon, String mapPath) {
        int defaultBottom = this.getY() + this.getH() - GameRuntime.FLOOR_MARGIN;
        if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 1) {
            return defaultBottom;
        }
        Integer cachedRow = GROUND_ROW_CACHE.get(mapPath);
        int groundRow;
        if (cachedRow != null) {
            groundRow = cachedRow;
        } else {
            BufferedImage image = toBufferedImage(icon);
            if (image == null) {
                return defaultBottom;
            }
            groundRow = detectGroundRow(image);
            GROUND_ROW_CACHE.put(mapPath, groundRow);
        }
        double scaleY = this.getH() / (double) icon.getIconHeight();
        return this.getY() + (int) Math.round((groundRow + 1) * scaleY) + GROUND_SCREEN_OFFSET;
    }

    private int detectGroundRow(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 1 || height <= 2) {
            return Math.max(0, height - 1);
        }

        int sampleStep = Math.max(4, width / 320);
        double[] horizontalEdge = new double[height];
        double[] verticalDiff = new double[height - 1];
        double sum = 0.0;
        for (int y = 0; y < height; y++) {
            double edge = 0.0;
            int edgeSamples = 0;
            double previousBrightness = Double.NaN;
            for (int x = 0; x < width; x += sampleStep) {
                double currentBrightness = brightness(image.getRGB(x, y));
                if (!Double.isNaN(previousBrightness)) {
                    edge += Math.abs(currentBrightness - previousBrightness);
                    edgeSamples++;
                }
                previousBrightness = currentBrightness;
            }
            horizontalEdge[y] = edge / Math.max(1, edgeSamples);
        }
        for (int y = 0; y < height - 1; y++) {
            double diff = 0.0;
            int samples = 0;
            for (int x = 0; x < width; x += sampleStep) {
                diff += Math.abs(brightness(image.getRGB(x, y + 1)) - brightness(image.getRGB(x, y)));
                samples++;
            }
            verticalDiff[y] = diff / Math.max(1, samples);
            sum += verticalDiff[y];
        }

        double mean = sum / verticalDiff.length;
        double variance = 0.0;
        for (double value : verticalDiff) {
            double delta = value - mean;
            variance += delta * delta;
        }
        double stdDev = Math.sqrt(variance / verticalDiff.length);
        double threshold = mean + stdDev * 1.35;
        int topBound = Math.max(12, height / 5);
        int bottomBound = Math.max(topBound + 1, height - Math.max(12, height / 12) - 1);
        int bestCandidate = -1;
        double bestContrast = Double.NEGATIVE_INFINITY;

        for (int y = bottomBound; y >= topBound; y--) {
            double current = verticalDiff[y];
            if (current < threshold) {
                continue;
            }
            double prev = y > 0 ? verticalDiff[y - 1] : Double.NEGATIVE_INFINITY;
            double next = y < verticalDiff.length - 1 ? verticalDiff[y + 1] : Double.NEGATIVE_INFINITY;
            if (current >= prev && current >= next) {
                double aboveEdge = average(horizontalEdge, Math.max(0, y - 60), Math.max(0, y - 12));
                double belowEdge = average(horizontalEdge, Math.min(height - 1, y + 12), Math.min(height - 1, y + 60));
                double contrast = aboveEdge - belowEdge;
                if (contrast >= 2.0) {
                    return y;
                }
                if (contrast > bestContrast) {
                    bestContrast = contrast;
                    bestCandidate = y;
                }
            }
        }

        if (bestCandidate >= 0) {
            return bestCandidate;
        }
        return Math.max(topBound, height - Math.max(18, height / 4));
    }

    private int[] detectGroundRows(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 0 || height <= 2) {
            return new int[] {Math.max(0, height - 1)};
        }
        int[] rows = new int[width];
        int topBound = Math.max(12, (int) Math.round(height * 0.32));
        int bottomBound = Math.max(topBound + 1, height - 8);
        double centerBias = height * 0.68;
        int fallbackRow = detectGroundRow(image);
        for (int x = 0; x < width; x++) {
            double[] brightnessPrefix = buildBrightnessPrefix(image, x);
            int bestY = fallbackRow;
            double bestScore = Double.NEGATIVE_INFINITY;
            for (int y = topBound; y < bottomBound; y++) {
                double above = averagePrefix(brightnessPrefix, y - 10, y - 1);
                double below = averagePrefix(brightnessPrefix, y + 1, y + 13);
                double diff = Math.abs(brightness(image.getRGB(x, Math.min(height - 1, y + 1)))
                        - brightness(image.getRGB(x, y)));
                double score = (below - above) * 2.0 + diff - Math.abs(y - centerBias) * 0.08;
                if (score > bestScore) {
                    bestScore = score;
                    bestY = y;
                }
            }
            rows[x] = bestY;
        }
        return smoothGroundRows(rows);
    }

    private double[] buildBrightnessPrefix(BufferedImage image, int x) {
        int height = image.getHeight();
        double[] prefix = new double[height + 1];
        for (int y = 0; y < height; y++) {
            prefix[y + 1] = prefix[y] + brightness(image.getRGB(x, y));
        }
        return prefix;
    }

    private double averagePrefix(double[] prefix, int start, int end) {
        int from = Math.max(0, start);
        int to = Math.min(prefix.length - 2, end);
        if (to < from) {
            return 0.0;
        }
        return (prefix[to + 1] - prefix[from]) / (to - from + 1);
    }

    private int[] smoothGroundRows(int[] rows) {
        int[] median = new int[rows.length];
        int[] scratch = new int[49];
        for (int x = 0; x < rows.length; x++) {
            int start = Math.max(0, x - 24);
            int end = Math.min(rows.length - 1, x + 24);
            int size = end - start + 1;
            System.arraycopy(rows, start, scratch, 0, size);
            Arrays.sort(scratch, 0, size);
            median[x] = scratch[size / 2];
        }

        int[] smoothed = new int[rows.length];
        for (int x = 0; x < rows.length; x++) {
            int start = Math.max(0, x - 40);
            int end = Math.min(rows.length - 1, x + 40);
            int sum = 0;
            for (int i = start; i <= end; i++) {
                sum += median[i];
            }
            smoothed[x] = Math.round(sum / (float) (end - start + 1));
        }

        clampSlope(smoothed, 2);
        for (int x = smoothed.length - 2; x >= 0; x--) {
            if (smoothed[x] > smoothed[x + 1] + 2) {
                smoothed[x] = smoothed[x + 1] + 2;
            } else if (smoothed[x] < smoothed[x + 1] - 2) {
                smoothed[x] = smoothed[x + 1] - 2;
            }
        }
        return smoothed;
    }

    private static void clampSlope(int[] rows, int maxStep) {
        for (int x = 1; x < rows.length; x++) {
            if (rows[x] > rows[x - 1] + maxStep) {
                rows[x] = rows[x - 1] + maxStep;
            } else if (rows[x] < rows[x - 1] - maxStep) {
                rows[x] = rows[x - 1] - maxStep;
            }
        }
    }

    private double brightness(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        return (r + g + b) / 3.0;
    }

    private BufferedImage toBufferedImage(ImageIcon icon) {
        return toBufferedImageStatic(icon);
    }

    private static BufferedImage toBufferedImageStatic(ImageIcon icon) {
        if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            return null;
        }
        Image image = icon.getImage();
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        BufferedImage buffer = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = buffer.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return buffer;
    }

    private double average(double[] values, int start, int end) {
        if (values.length == 0) {
            return 0.0;
        }
        int from = Math.max(0, Math.min(values.length - 1, start));
        int to = Math.max(from, Math.min(values.length - 1, end));
        double sum = 0.0;
        int count = 0;
        for (int i = from; i <= to; i++) {
            sum += values[i];
            count++;
        }
        return count == 0 ? 0.0 : sum / count;
    }
}
