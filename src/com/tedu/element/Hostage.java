package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;

public class Hostage extends ElementObj {
    private static final int HITBOX_W = 42;
    private static final int HITBOX_H = 78;
    private static final int IDLE_FRAME_GAP = 10;
    private static final int RESCUE_FRAME_GAP = 10;
    private static final String HOSTAGE_ROOT_DIR = "image/images/\u4eba\u8d28";
    private static final String HOSTAGE_ORDER0_DIR = HOSTAGE_ROOT_DIR + "/order0";
    private static final String HOSTAGE_ORDER1_DIR = HOSTAGE_ROOT_DIR + "/order1";
    private static final List<ImageIcon> HOSTAGE_FALLBACK_FRAMES = GameLoad.loadFramesFromDirectory(HOSTAGE_ROOT_DIR);
    private static final AnimationSet HOSTAGE_ORDER0_SET = buildAnimationSet(HOSTAGE_ORDER0_DIR, 2182, 2206);
    private static final AnimationSet HOSTAGE_ORDER1_SET = buildAnimationSet(HOSTAGE_ORDER1_DIR, 10, 12);

    private final ElementManager em = ElementManager.getManager();

    private boolean rescued = false;
    private boolean itemDropped = false;
    private boolean rescueSequenceComplete = false;
    private long rescueAnimStartTime = -1L;
    private String rewardType = "weapon2";
    private AnimationSet animationSet = HOSTAGE_ORDER0_SET;
    private ImageIcon currentFrame = firstFrame(HOSTAGE_ORDER0_SET.idleFrames);

    @Override
    public void showElement(Graphics g) {
        ImageIcon frame = currentFrame == null ? this.getIcon() : currentFrame;
        if (frame == null) {
            return;
        }
        int drawW = frame.getIconWidth();
        int drawH = frame.getIconHeight();
        int drawX = this.getX() + (this.getW() - drawW) / 2;
        int drawY = this.getY() + this.getH() - drawH;
        g.drawImage(frame.getImage(), drawX, drawY, drawW, drawH, null);
    }

    @Override
    protected void move() {
        this.setX(this.getX() - GameRuntime.worldScrollX);
        int footX = this.getX() + this.getW() / 2;
        this.setY(GameRuntime.getBattlefieldMaxBottomAt(footX) - this.getH());
    }

    @Override
    protected void updateImage(long gameTime) {
        List<ImageIcon> activeFrames = rescued ? animationSet.rescueFrames : animationSet.idleFrames;
        if (activeFrames == null || activeFrames.isEmpty()) {
            activeFrames = HOSTAGE_FALLBACK_FRAMES;
        }
        if (activeFrames == null || activeFrames.isEmpty()) {
            return;
        }
        if (!rescued) {
            currentFrame = activeFrames.get((int) ((gameTime / IDLE_FRAME_GAP) % activeFrames.size()));
        } else {
            if (rescueAnimStartTime < 0L) {
                rescueAnimStartTime = gameTime;
            }
            long elapsed = Math.max(0L, gameTime - rescueAnimStartTime);
            int index = (int) (elapsed / RESCUE_FRAME_GAP);
            if (index >= activeFrames.size()) {
                index = activeFrames.size() - 1;
                rescueSequenceComplete = true;
            }
            currentFrame = activeFrames.get(Math.max(0, index));
        }
        this.setIcon(currentFrame);
    }

    @Override
    protected void add(long gameTime) {
        if (rescued && rescueSequenceComplete) {
            this.setLive(false);
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        this.setW(HITBOX_W);
        this.setH(HITBOX_H);
        if (split.length > 2) {
            this.rewardType = split[2];
        }
        String orderType = split.length > 3 ? split[3] : "order0";
        animationSet = resolveAnimationSet(orderType);
        currentFrame = firstFrame(animationSet.idleFrames);
        if (currentFrame == null) {
            currentFrame = firstFrame(HOSTAGE_FALLBACK_FRAMES);
        }
        if (currentFrame != null) {
            this.setIcon(currentFrame);
        }
        return this;
    }

    @Override
    public void die() {
        if (!rescued || itemDropped) {
            return;
        }
        itemDropped = true;
        ElementObj item = new SupplyItem().createElement(
                (this.getX() + this.getW() / 2 - 14) + "," + (this.getY() + this.getH() / 2 - 12) + "," + rewardType);
        em.addElement(item, GameElement.ITEM);
    }

    public void rescue() {
        if (rescued) {
            return;
        }
        rescued = true;
        rescueAnimStartTime = -1L;
        rescueSequenceComplete = false;
        GameRuntime.showBanner("\u4eba\u8d28\u83b7\u6551\uff0c\u8865\u7ed9\u6389\u843d", 1500);
    }

    private static AnimationSet resolveAnimationSet(String orderType) {
        if ("order1".equalsIgnoreCase(orderType) && !HOSTAGE_ORDER1_SET.isEmpty()) {
            return HOSTAGE_ORDER1_SET;
        }
        if (!HOSTAGE_ORDER0_SET.isEmpty()) {
            return HOSTAGE_ORDER0_SET;
        }
        if (!HOSTAGE_ORDER1_SET.isEmpty()) {
            return HOSTAGE_ORDER1_SET;
        }
        return new AnimationSet(HOSTAGE_FALLBACK_FRAMES, HOSTAGE_FALLBACK_FRAMES);
    }

    private static AnimationSet buildAnimationSet(String directoryPath, int idleStartInclusive, int idleEndInclusive) {
        List<FrameInfo> frames = loadFrameInfo(directoryPath);
        if (frames.isEmpty()) {
            return new AnimationSet(Collections.emptyList(), Collections.emptyList());
        }
        List<ImageIcon> allFrames = new ArrayList<>(frames.size());
        List<ImageIcon> idleFrames = new ArrayList<>();
        List<ImageIcon> rescueFrames = new ArrayList<>();
        for (FrameInfo frame : frames) {
            allFrames.add(frame.icon);
            if (frame.order >= idleStartInclusive && frame.order <= idleEndInclusive) {
                idleFrames.add(frame.icon);
            } else if (frame.order > idleEndInclusive) {
                rescueFrames.add(frame.icon);
            }
        }
        if (idleFrames.isEmpty()) {
            idleFrames.addAll(allFrames);
        }
        if (rescueFrames.isEmpty() && !allFrames.isEmpty()) {
            int rescueStartIndex = Math.min(allFrames.size() - 1, idleFrames.size());
            rescueFrames.addAll(allFrames.subList(rescueStartIndex, allFrames.size()));
        }
        return new AnimationSet(idleFrames, rescueFrames);
    }

    private static List<FrameInfo> loadFrameInfo(String directoryPath) {
        if (directoryPath == null || directoryPath.isBlank()) {
            return Collections.emptyList();
        }
        File directory = GameLoad.resolveResourceFile(directoryPath);
        if (directory == null || !directory.isDirectory()) {
            return Collections.emptyList();
        }
        File[] files = directory.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        Arrays.sort(files, (left, right) -> {
            int leftOrder = extractTrailingNumber(left.getName());
            int rightOrder = extractTrailingNumber(right.getName());
            if (leftOrder != rightOrder) {
                return Integer.compare(leftOrder, rightOrder);
            }
            return left.getName().compareToIgnoreCase(right.getName());
        });
        List<FrameInfo> frames = new ArrayList<>();
        String normalizedDir = directoryPath.replace('\\', '/');
        for (File file : files) {
            ImageIcon icon = GameLoad.getImage(normalizedDir + "/" + file.getName());
            if (icon == null) {
                continue;
            }
            frames.add(new FrameInfo(extractTrailingNumber(file.getName()), icon));
        }
        return Collections.unmodifiableList(frames);
    }

    private static int extractTrailingNumber(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        int dotIndex = fileName.lastIndexOf('.');
        int end = dotIndex >= 0 ? dotIndex : fileName.length();
        int index = end - 1;
        while (index >= 0 && !Character.isDigit(fileName.charAt(index))) {
            index--;
        }
        if (index < 0) {
            return Integer.MAX_VALUE;
        }
        int numberEnd = index + 1;
        while (index >= 0 && Character.isDigit(fileName.charAt(index))) {
            index--;
        }
        int numberStart = index + 1;
        try {
            return Integer.parseInt(fileName.substring(numberStart, numberEnd));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static ImageIcon firstFrame(List<ImageIcon> frameList) {
        return frameList == null || frameList.isEmpty() ? null : frameList.get(0);
    }

    private static final class FrameInfo {
        private final int order;
        private final ImageIcon icon;

        private FrameInfo(int order, ImageIcon icon) {
            this.order = order;
            this.icon = icon;
        }
    }

    private static final class AnimationSet {
        private final List<ImageIcon> idleFrames;
        private final List<ImageIcon> rescueFrames;

        private AnimationSet(List<ImageIcon> idleFrames, List<ImageIcon> rescueFrames) {
            this.idleFrames = idleFrames == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(idleFrames));
            this.rescueFrames = rescueFrames == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(rescueFrames));
        }

        private boolean isEmpty() {
            return idleFrames.isEmpty() && rescueFrames.isEmpty();
        }
    }
}
