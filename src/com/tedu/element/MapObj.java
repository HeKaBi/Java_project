package com.tedu.element;

import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

public class MapObj extends ElementObj {
    private static final int GROUND_SCREEN_OFFSET = 18;
    private static final int MAP2_PROFILE_BASE_WIDTH = 3822;
    private static final int MAP2_PROFILE_BASE_HEIGHT = 239;
    private static final String[] MAP2_GUIDE_PATHS = {
            "image/images/背景/指导1.png",
            "image/images/背景/指导.png"
    };
    private static final Map<String, Integer> GROUND_ROW_CACHE = new HashMap<>();
    private static final Map<String, int[]> GROUND_PROFILE_CACHE = new HashMap<>();
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
        int mapWidth = GameJFrame.GameX;
        if (icon != null && icon.getIconWidth() > 0) {
            mapWidth = icon.getIconWidth();
        }
        this.setW(mapWidth);
        this.setH(GameJFrame.GameY);
        this.minX = this.getX() - Math.max(0, mapWidth - GameJFrame.GameX);
        int[] groundProfile = resolveGroundProfile(icon, mapPath);
        if (groundProfile != null && groundProfile.length > 0) {
            int maxBottom = groundProfile[0];
            for (int bottom : groundProfile) {
                maxBottom = Math.max(maxBottom, bottom);
            }
            GameRuntime.setBattlefieldTerrain(groundProfile, this.getX(), this.getW());
            GameRuntime.setBattlefieldMaxBottom(maxBottom);
        } else {
            GameRuntime.setBattlefieldMaxBottom(resolveBattlefieldBottom(icon, mapPath));
        }
        if (split.length > 3) {
            this.scrollRatio = Double.parseDouble(split[3]);
        }
        return this;
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

    private int[] resolveManualGroundRows(String mapPath, int width, int height) {
        String normalizedPath = mapPath == null ? "" : mapPath.replace('\\', '/').toLowerCase();
        if (normalizedPath.endsWith("/map2.png") || normalizedPath.endsWith("map2.png")) {
            int[] guideRows = resolveGuideGroundRows(width, height);
            if (guideRows != null) {
                return guideRows;
            }
            return buildRowsFromAnchors(width, height, MAP2_PROFILE_BASE_WIDTH, MAP2_PROFILE_BASE_HEIGHT,
                    MAP2_MAIN_GROUND_ANCHORS);
        }
        return null;
    }

    private int[] resolveGuideGroundRows(int width, int height) {
        for (String guidePath : MAP2_GUIDE_PATHS) {
            ImageIcon guideIcon = GameLoad.getImage(guidePath);
            BufferedImage guideImage = toBufferedImage(guideIcon);
            if (guideImage == null) {
                continue;
            }
            int[] guideRows = extractGuideBoundaryRows(guideImage);
            if (guideRows == null || guideRows.length == 0) {
                continue;
            }
            return scaleGuideRows(guideRows, width, height, guideImage.getHeight());
        }
        return null;
    }

    private int[] extractGuideBoundaryRows(BufferedImage guideImage) {
        int width = guideImage.getWidth();
        int height = guideImage.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        int[] rows = new int[width];
        Arrays.fill(rows, -1);
        for (int x = 0; x < width; x++) {
            int topmostGuideY = -1;
            for (int y = 0; y < height; y++) {
                if (isGuideBoundaryPixel(guideImage.getRGB(x, y))) {
                    topmostGuideY = y;
                    break;
                }
            }
            rows[x] = topmostGuideY;
        }
        fillGuideGaps(rows);
        return rows;
    }

    private boolean isGuideBoundaryPixel(int rgb) {
        int alpha = (rgb >>> 24) & 0xff;
        if (alpha < 24) {
            return false;
        }
        int red = (rgb >>> 16) & 0xff;
        int green = (rgb >>> 8) & 0xff;
        int blue = rgb & 0xff;
        return red >= 180 && green <= 125 && blue <= 125 && red - Math.max(green, blue) >= 70;
    }

    private void fillGuideGaps(int[] rows) {
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
                for (int gapX = previousKnownIndex + 1; gapX < x; gapX++) {
                    double ratio = (gapX - previousKnownIndex) / (double) (x - previousKnownIndex);
                    rows[gapX] = (int) Math.round(startRow + (endRow - startRow) * ratio);
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

    private int[] buildRowsFromAnchors(int width, int height, int baseWidth, int baseHeight, int[][] anchors) {
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

    private int scaleAnchorX(int x, int width, int baseWidth) {
        if (width <= 1 || baseWidth <= 1) {
            return 0;
        }
        return Math.max(0, Math.min(width - 1, (int) Math.round(x * (width - 1) / (double) (baseWidth - 1))));
    }

    private int scaleAnchorY(int y, int height, int baseHeight) {
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

    private void clampSlope(int[] rows, int maxStep) {
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
