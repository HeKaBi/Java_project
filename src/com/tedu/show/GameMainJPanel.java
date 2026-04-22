package com.tedu.show;

import com.tedu.element.Boss;
import com.tedu.element.ElementObj;
import com.tedu.element.PaoPao;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class GameMainJPanel extends JPanel implements Runnable {
    private static final String START_SCREEN_PATH = "image/images/\u80cc\u666f/start.jpg";
    private static final String PLAYER_PORTRAIT_PATH = "image/images/plays/\u4e0a\u534a\u8eab/\u6b66\u56681/right/attack/attack001.png";
    private static final Font HUD_SMALL_FONT = new Font("DialogInput", Font.BOLD, 13);
    private static final Font HUD_LABEL_FONT = new Font("DialogInput", Font.BOLD, 16);
    private static final Font HUD_VALUE_FONT = new Font("DialogInput", Font.BOLD, 26);
    private static final Font HUD_BIG_FONT = new Font("DialogInput", Font.BOLD, 42);
    private static final Font HUD_CHINESE_FONT = new Font("Microsoft YaHei UI", Font.BOLD, 16);
    private static final Font BANNER_FONT = new Font("DialogInput", Font.BOLD, 24);
    private static final Font START_PROMPT_FONT = new Font("Impact", Font.PLAIN, 38);
    private static final Font START_ACTION_FONT = new Font("DialogInput", Font.BOLD, 18);
    private static final Font START_INFO_FONT = new Font("DialogInput", Font.BOLD, 13);

    private static final Color HUD_PANEL = new Color(31, 30, 20, 224);
    private static final Color HUD_PANEL_DARK = new Color(20, 18, 12, 236);
    private static final Color HUD_BORDER = new Color(126, 109, 56);
    private static final Color HUD_CYAN = new Color(234, 230, 216);
    private static final Color HUD_GOLD = new Color(246, 196, 44);
    private static final Color HUD_ORANGE = new Color(255, 173, 38);
    private static final Color HUD_RED = new Color(191, 67, 46);
    private static final Color HUD_RED_GLOW = new Color(240, 153, 103);
    private static final Color HUD_GREEN = new Color(78, 214, 128);
    private static final Color HUD_GREEN_GLOW = new Color(184, 255, 205);
    private static final Color HUD_MIST = new Color(13, 12, 8, 188);
    private static final Color HUD_MIST_SOFT = new Color(132, 112, 62, 46);
    private static final Color HUD_SEPARATOR = new Color(172, 149, 82, 132);
    private static final Color HUD_TIME_GLOW = new Color(255, 208, 85, 156);
    private static final Color HUD_SLOT_FILL = new Color(36, 36, 28, 220);
    private static final Color HUD_SLOT_ACTIVE = new Color(56, 88, 155, 236);
    private static final Color HUD_SLOT_DISABLED = new Color(67, 63, 54, 180);
    private static final Color STAGE_TRANSITION_BLACK = new Color(0, 0, 0, 242);
    private static final Color STAGE_TRANSITION_DOT = new Color(255, 235, 198, 46);

    private ElementManager em;
    private final Image startScreenImage;
    private final Image playerPortraitImage;

    public GameMainJPanel() {
        startScreenImage = loadStartScreenImage();
        playerPortraitImage = loadPlayerPortraitImage();
        init();
    }

    public void init() {
        em = ElementManager.getManager();
        this.setPreferredSize(new Dimension(GameJFrame.GameX, GameJFrame.GameY));
        this.setFocusable(true);
        this.setDoubleBuffered(true);
        this.setBackground(new Color(4, 8, 22));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (GameRuntime.waitingStart) {
            drawStartScreen((Graphics2D) g);
            return;
        }
        Map<GameElement, List<ElementObj>> all = em.getGameElements();

        drawSceneBackdrop((Graphics2D) g);
        drawMaps(g, all.get(GameElement.MAPS));
        drawActors(g, all);
        drawEffects(g, all);
        drawHud((Graphics2D) g, all);
        drawFinishOverlay(g);
        drawStageTransitionOverlay((Graphics2D) g);
    }

    private Image loadStartScreenImage() {
        javax.swing.ImageIcon icon = GameLoad.loadImage(START_SCREEN_PATH);
        return icon == null ? null : icon.getImage();
    }

    private Image loadPlayerPortraitImage() {
        javax.swing.ImageIcon icon = GameLoad.loadImage(PLAYER_PORTRAIT_PATH);
        return icon == null ? null : icon.getImage();
    }

    private void drawStartScreen(Graphics2D g2) {
        Graphics2D screen = (Graphics2D) g2.create();
        screen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        screen.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        screen.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        drawStartBackground(screen);
        drawStartPrompt(screen);
        screen.dispose();
    }

    private void drawStartBackground(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();
        if (startScreenImage != null) {
            int imageWidth = startScreenImage.getWidth(null);
            int imageHeight = startScreenImage.getHeight(null);
            if (imageWidth > 0 && imageHeight > 0) {
                double scale = Math.max(width / (double) imageWidth, height / (double) imageHeight);
                int drawWidth = (int) Math.ceil(imageWidth * scale);
                int drawHeight = (int) Math.ceil(imageHeight * scale);
                int drawX = (width - drawWidth) / 2;
                int drawY = (height - drawHeight) / 2;
                g2.drawImage(startScreenImage, drawX, drawY, drawWidth, drawHeight, null);
            }
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(70, 109, 148), 0, height, new Color(11, 18, 34)));
            g2.fillRect(0, 0, width, height);
        }
        g2.setPaint(new GradientPaint(0, 0, new Color(4, 12, 24, 10), 0, height, new Color(2, 6, 12, 105)));
        g2.fillRect(0, 0, width, height);
        g2.setPaint(new GradientPaint(0, height / 2, new Color(0, 0, 0, 0), 0, height, new Color(0, 0, 0, 68)));
        g2.fillRect(0, 0, width, height);
    }

    private void drawStartPrompt(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();
        int centerX = Math.min(width - 140, Math.max(730, (int) (width * 0.80)));
        int titleY = Math.min(height - 215, Math.max(300, (int) (height * 0.61)));
        int pulse = (int) (Math.abs(Math.sin(System.currentTimeMillis() / 320.0)) * 18);
        boolean loadingStart = GameRuntime.loadingStart;

        Color titleColor = loadingStart
                ? new Color(255, 238, 188)
                : (pulse > 9 ? new Color(247, 239, 255) : new Color(228, 221, 252));
        Color accentColor = loadingStart
                ? new Color(255, 196, 92, 236)
                : (pulse > 9 ? new Color(255, 214, 114, 240) : new Color(239, 196, 93, 220));

        drawCenteredShadowedText(g2, loadingStart ? "任务开始" : "新的游戏", centerX, titleY,
                START_PROMPT_FONT, titleColor, new Color(24, 20, 34, 110), 2);
        drawCenteredShadowedText(g2, loadingStart ? "进入第一关" : "按回车开始",
                centerX + 4, titleY + 36,
                START_ACTION_FONT, accentColor, new Color(19, 14, 22, 90), 1);
        drawCenteredShadowedText(g2,
                loadingStart ? "正在装载地形和任务单位" : "A/D 移动   W 跳跃   E 上瞄   Ctrl 下蹲",
                centerX + 4, titleY + 62, START_INFO_FONT,
                new Color(230, 238, 244, loadingStart ? 230 : 210), new Color(8, 10, 18, 110), 1);
        drawAccentLine(g2, centerX, titleY + 76, 132 + pulse, accentColor);
    }

    private void drawSceneBackdrop(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();
        Graphics2D backdrop = (Graphics2D) g2.create();
        backdrop.setPaint(new GradientPaint(0, 0, new Color(5, 10, 24), 0, height, new Color(2, 5, 14)));
        backdrop.fillRect(0, 0, width, height);
        backdrop.dispose();
    }

    private void drawAccentLine(Graphics2D g2, int centerX, int y, int width, Color color) {
        int drawX = centerX - width / 2;
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(drawX, y + 2, width, 4, 4, 4);
        g2.setPaint(new GradientPaint(drawX, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0),
                centerX, y, color));
        g2.fillRoundRect(drawX, y, width, 3, 3, 3);
    }

    private void drawCenteredShadowedText(Graphics2D g2, String text, int centerX, int y,
                                          Font font, Color fill, Color shadow, int depth) {
        Font displayFont = resolveDisplayFont(font, text);
        g2.setFont(displayFont);
        FontMetrics metrics = g2.getFontMetrics(displayFont);
        int drawX = centerX - metrics.stringWidth(text) / 2;
        drawShadowedText(g2, text, drawX, y, displayFont, fill, shadow, depth);
    }

    private void drawShadowedText(Graphics2D g2, String text, int x, int y,
                                  Font font, Color fill, Color shadow, int depth) {
        Font displayFont = resolveDisplayFont(font, text);
        g2.setFont(displayFont);
        for (int offset = depth; offset >= 1; offset--) {
            int alpha = Math.max(18, shadow.getAlpha() / (offset + 2));
            g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), alpha));
            g2.drawString(text, x + 1, y + offset);
        }
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }

    private void drawMaps(Graphics g, List<ElementObj> maps) {
        if (maps == null) {
            return;
        }
        for (ElementObj map : snapshot(maps)) {
            map.showElement(g);
        }
    }

    private void drawActors(Graphics g, Map<GameElement, List<ElementObj>> all) {
        List<ElementObj> actors = new ArrayList<>();
        addAll(actors, snapshot(all.get(GameElement.HOSTAGE)));
        addAll(actors, snapshot(all.get(GameElement.PLAY)));
        addAll(actors, snapshot(all.get(GameElement.ENEMY)));
        addAll(actors, snapshot(all.get(GameElement.BOSS)));
        actors.sort(Comparator.comparingInt(ElementObj::getBottom));
        for (ElementObj actor : actors) {
            actor.showElement(g);
        }
    }

    private void drawEffects(Graphics g, Map<GameElement, List<ElementObj>> all) {
        for (GameElement ge : GameElement.values()) {
            if (ge == GameElement.MAPS
                    || ge == GameElement.PLATFORM
                    || ge == GameElement.PLAY
                    || ge == GameElement.ENEMY
                    || ge == GameElement.BOSS
                    || ge == GameElement.HOSTAGE) {
                continue;
            }
            List<ElementObj> list = all.get(ge);
            if (list == null) {
                continue;
            }
            for (ElementObj obj : snapshot(list)) {
                obj.showElement(g);
            }
        }
    }

    private void drawHud(Graphics2D g2, Map<GameElement, List<ElementObj>> all) {
        Graphics2D hud = (Graphics2D) g2.create();
        hud.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hud.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        PaoPao player = findPlayer(all);
        Boss boss = findBoss(all);

        drawHudAtmosphere(hud);
        if (player != null) {
            drawPlayerHud(hud, player);
        }
        drawMissionHud(hud);
        drawTimeHud(hud, player);
        if (boss != null) {
            drawBossHud(hud, boss);
        }
        drawBanner(hud, boss != null);
        hud.dispose();
    }

    private void drawPlayerHud(Graphics2D g2, PaoPao play) {
        int x = 18;
        int y = GameJFrame.GameY - 92;
        int w = 332;
        int h = 64;

        drawHudPlate(g2, x, y, w, h);
        drawInsetPanel(g2, x + 10, y + 8, 58, 48);
        drawPlayerPortrait(g2, x + 10, y + 8, 58, 48);
        drawOutlinedText(g2, "1P", x + 20, y + 58, HUD_SMALL_FONT, HUD_GOLD, Color.BLACK);

        double hpRatio = play.getMaxHp() <= 0 ? 0.0 : play.getHp() / (double) play.getMaxHp();
        Color hpFill = hpRatio <= 0.20 ? HUD_RED : HUD_GREEN;
        Color hpGlow = hpRatio <= 0.20 ? HUD_RED_GLOW : HUD_GREEN_GLOW;
        drawOutlinedText(g2, "生命", x + 80, y + 18, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawMeter(g2, x + 80, y + 24, 128, 12, hpRatio, hpFill, hpGlow);

        drawOutlinedText(g2, "武器", x + 80, y + 48, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawWeaponSlot(g2, x + 122, y + 34, 28, 24, "轻", !play.isHeavyWeaponEquipped(), true);
        drawWeaponSlot(g2, x + 156, y + 34, 28, 24, "重", play.isHeavyWeaponEquipped(), play.hasWeapon2());

        drawVerticalFadedLine(g2, x + 216, y + 9, 46, HUD_SEPARATOR);
        drawOutlinedText(g2, "手雷", x + 234, y + 18, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, "x" + formatCounter(play.getGrenades()), x + 234, y + 48, HUD_LABEL_FONT, HUD_GOLD, Color.BLACK);
    }

    private void drawMissionHud(Graphics2D g2) {
        int w = 248;
        int h = 58;
        int x = GameJFrame.GameX - w - 16;
        int y = 16;

        drawHudPlate(g2, x, y, w, h);
        drawOutlinedText(g2, "任务 " + GameRuntime.currentStage, x + 12, y + 18, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, "击杀 " + formatCounter(Math.max(0, GameRuntime.killCount)), x + 12, y + 42,
                HUD_SMALL_FONT, HUD_GOLD, Color.BLACK);
        drawOutlinedText(g2, "进度 " + GameRuntime.getStageProgressPercent() + "%", x + 126, y + 18,
                HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawMeter(g2, x + 126, y + 26, 108, 10,
                GameRuntime.getStageProgressPercent() / 100.0, HUD_GOLD, new Color(255, 236, 166));
        drawOutlinedText(g2, "关卡 " + GameRuntime.currentStage + "/" + Math.max(1, GameRuntime.totalStages),
                x + 126, y + 46, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
    }

    private void drawTimeHud(Graphics2D g2, PaoPao player) {
        int w = 270;
        int h = 58;
        int x = (GameJFrame.GameX - w) / 2;
        int y = 14;

        long seconds = Math.max(0L, GameRuntime.survivalTimeMs / 1000L);
        long tenths = Math.max(0L, (GameRuntime.survivalTimeMs % 1000L) / 100L);

        drawHudPlate(g2, x, y, w, h);
        drawOutlinedText(g2, "武器", x + 12, y + 18, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, playWeaponCode(player), x + 12, y + 42, HUD_LABEL_FONT, HUD_GOLD, Color.BLACK);
        drawVerticalFadedLine(g2, x + 78, y + 10, 38, HUD_SEPARATOR);
        drawOutlinedText(g2, "手雷", x + 96, y + 18, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        String bombText = player == null ? "x00" : "x" + formatCounter(player.getGrenades());
        drawOutlinedText(g2, bombText, x + 96, y + 42, HUD_LABEL_FONT, HUD_GOLD, Color.BLACK);
        drawVerticalFadedLine(g2, x + 162, y + 8, 42, new Color(215, 173, 85, 118));
        drawOutlinedText(g2, "时间", x + 180, y + 18, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, String.valueOf(seconds), x + 180, y + 48, HUD_BIG_FONT, HUD_ORANGE, Color.BLACK);
        drawOutlinedText(g2, "." + tenths, x + 228, y + 48, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
    }

    private void drawBossHud(Graphics2D g2, Boss boss) {
        int w = 384;
        int h = 14;
        int x = (GameJFrame.GameX - w) / 2;
        int y = 82;
        drawOutlinedText(g2, "首领", x, y + 12, HUD_LABEL_FONT, new Color(255, 235, 218), Color.BLACK);
        drawMeter(g2, x + 50, y + 1, w - 50, h,
                boss.getMaxHp() <= 0 ? 0.0 : boss.getHp() / (double) boss.getMaxHp(), HUD_RED, HUD_RED_GLOW);
        drawFadedLine(g2, x + 50, y + 20, w - 50, new Color(255, 132, 118, 96));
    }

    private void drawBanner(Graphics2D g2, boolean bossActive) {
        if (System.currentTimeMillis() >= GameRuntime.bannerUntilMs
                || GameRuntime.bannerText == null
                || GameRuntime.bannerText.isEmpty()) {
            return;
        }

        Font bannerFont = resolveDisplayFont(BANNER_FONT, GameRuntime.bannerText);
        FontMetrics fm = g2.getFontMetrics(bannerFont);
        int textW = fm.stringWidth(GameRuntime.bannerText);
        int centerX = GameJFrame.GameX / 2;
        int baselineY = bossActive ? 146 : 114;
        int lineGap = 26;
        int halfSpan = Math.min(180, Math.max(72, (GameJFrame.GameX - textW) / 4));

        drawCenteredShadowedText(g2, GameRuntime.bannerText, centerX, baselineY, BANNER_FONT,
                new Color(255, 246, 185), new Color(8, 8, 12, 140), 2);
        drawFadedLine(g2, centerX - textW / 2 - halfSpan - lineGap, baselineY - 10, halfSpan, HUD_TIME_GLOW);
        drawFadedLine(g2, centerX + textW / 2 + lineGap, baselineY - 10, halfSpan, HUD_TIME_GLOW);
    }

    private void drawHudAtmosphere(Graphics2D g2) {
        int width = GameJFrame.GameX;
        g2.setPaint(new GradientPaint(0, 0, new Color(16, 15, 10, 206), 0, 120, new Color(16, 15, 10, 0)));
        g2.fillRect(0, 0, width, 164);
        g2.setPaint(new GradientPaint(0, GameJFrame.GameY - 120, new Color(14, 12, 8, 0),
                0, GameJFrame.GameY, new Color(14, 12, 8, 170)));
        g2.fillRect(0, GameJFrame.GameY - 120, width, 120);
        g2.setColor(HUD_MIST_SOFT);
        g2.fillOval(-60, -40, 280, 100);
        g2.fillOval(300, -50, 360, 118);
        g2.fillOval(840, -34, 220, 88);
    }

    private void drawHudPlate(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(HUD_PANEL);
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(255, 242, 204, 18));
        g2.fillRoundRect(x + 2, y + 2, Math.max(1, w - 4), Math.max(8, h / 3), 8, 8);
        g2.setColor(HUD_BORDER);
        g2.drawRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(77, 65, 37, 220));
        g2.drawRoundRect(x + 1, y + 1, Math.max(1, w - 2), Math.max(1, h - 2), 8, 8);
    }

    private void drawInsetPanel(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(HUD_PANEL_DARK);
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setColor(new Color(158, 140, 84));
        g2.drawRoundRect(x, y, w, h, 8, 8);
    }

    private void drawPlayerPortrait(Graphics2D g2, int x, int y, int w, int h) {
        if (playerPortraitImage == null) {
            drawCenteredOutlinedText(g2, "兵", x, y, w, h, HUD_LABEL_FONT, HUD_GOLD, Color.BLACK);
            return;
        }
        int imageW = playerPortraitImage.getWidth(null);
        int imageH = playerPortraitImage.getHeight(null);
        if (imageW <= 0 || imageH <= 0) {
            drawCenteredOutlinedText(g2, "兵", x, y, w, h, HUD_LABEL_FONT, HUD_GOLD, Color.BLACK);
            return;
        }
        double scale = Math.min((w - 8) / (double) imageW, (h - 8) / (double) imageH);
        int drawW = Math.max(1, (int) Math.round(imageW * scale));
        int drawH = Math.max(1, (int) Math.round(imageH * scale));
        int drawX = x + (w - drawW) / 2;
        int drawY = y + (h - drawH) / 2;
        g2.drawImage(playerPortraitImage, drawX, drawY, drawW, drawH, null);
    }

    private void drawStatCluster(Graphics2D g2, int x, int y, int width, String label, String value, Color valueColor) {
        drawOutlinedText(g2, label, x, y - 6, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        g2.setFont(HUD_LABEL_FONT);
        FontMetrics metrics = g2.getFontMetrics(HUD_LABEL_FONT);
        int valueX = x + (width - metrics.stringWidth(value)) / 2;
        drawOutlinedText(g2, value, valueX, y + 18, HUD_LABEL_FONT, valueColor, Color.BLACK);
        drawFadedLine(g2, x, y + 26, width,
                new Color(valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue(), 110));
    }

    private void drawWeaponSlot(Graphics2D g2, int x, int y, int w, int h, String label, boolean selected, boolean enabled) {
        g2.setColor(enabled ? (selected ? HUD_SLOT_ACTIVE : HUD_SLOT_FILL) : HUD_SLOT_DISABLED);
        g2.fillRoundRect(x, y, w, h, 6, 6);
        g2.setColor(enabled ? (selected ? HUD_GOLD : HUD_BORDER) : new Color(108, 101, 85));
        g2.drawRoundRect(x, y, w, h, 6, 6);
        drawCenteredOutlinedText(g2, label, x, y, w, h, HUD_SMALL_FONT,
                selected ? HUD_GOLD : (enabled ? HUD_CYAN : new Color(138, 134, 122)), Color.BLACK);
    }

    private String playWeaponCode(PaoPao player) {
        if (player == null) {
            return "步枪";
        }
        return player.isHeavyWeaponEquipped() ? "重机枪" : "步枪";
    }

    private void drawSegmentBar(Graphics2D g2, int x, int y, int w, int h, int value, int max,
                                Color fill, Color glow) {
        g2.setColor(HUD_MIST);
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(255, 255, 255, 20));
        g2.fillRoundRect(x + 1, y + 1, Math.max(1, w - 2), Math.max(3, h / 2), 9, 9);

        if (max <= 0) {
            return;
        }
        int gap = 4;
        int innerX = x + 4;
        int innerY = y + 4;
        int innerH = h - 8;
        int innerW = w - 8;
        int segmentW = Math.max(8, (innerW - gap * (max - 1)) / max);
        for (int i = 0; i < max; i++) {
            int sx = innerX + i * (segmentW + gap);
            g2.setColor(i < value ? fill : new Color(42, 53, 79, 150));
            g2.fillRoundRect(sx, innerY, segmentW, innerH, 6, 6);
            if (i < value) {
                g2.setColor(glow);
                g2.fillRoundRect(sx, innerY, segmentW, Math.max(3, innerH / 2), 6, 6);
            }
        }
    }

    private void drawMeter(Graphics2D g2, int x, int y, int w, int h, double ratio, Color fill, Color glow) {
        g2.setColor(HUD_MIST);
        g2.fillRoundRect(x, y, w, h, h, h);
        g2.setColor(new Color(255, 255, 255, 18));
        g2.fillRoundRect(x + 1, y + 1, Math.max(1, w - 2), Math.max(3, h / 2), h, h);

        int innerX = x + 2;
        int innerY = y + 2;
        int innerW = w - 4;
        int innerH = h - 4;
        int fillW = (int) Math.round(innerW * clamp01(ratio));
        if (fillW > 0) {
            g2.setColor(fill);
            g2.fillRoundRect(innerX, innerY, fillW, innerH, innerH, innerH);
            g2.setColor(glow);
            g2.fillRoundRect(innerX, innerY, fillW, Math.max(3, innerH / 2), innerH, innerH);
        }

        g2.setColor(new Color(255, 255, 255, 18));
        for (int px = x + 12; px < x + w - 4; px += 14) {
            g2.drawLine(px, y + 2, px, y + h - 3);
        }
    }

    private void drawFadedLine(Graphics2D g2, int x, int y, int width, Color color) {
        if (width <= 0) {
            return;
        }
        int half = Math.max(1, width / 2);
        int thickness = 3;
        g2.setPaint(new GradientPaint(x, y, transparent(color), x + half, y, color));
        g2.fillRoundRect(x, y, half, thickness, thickness, thickness);
        g2.setPaint(new GradientPaint(x + half, y, color, x + width, y, transparent(color)));
        g2.fillRoundRect(x + half, y, width - half, thickness, thickness, thickness);
    }

    private void drawVerticalFadedLine(Graphics2D g2, int x, int y, int height, Color color) {
        if (height <= 0) {
            return;
        }
        int half = Math.max(1, height / 2);
        int thickness = 2;
        g2.setPaint(new GradientPaint(x, y, transparent(color), x, y + half, color));
        g2.fillRoundRect(x, y, thickness, half, thickness, thickness);
        g2.setPaint(new GradientPaint(x, y + half, color, x, y + height, transparent(color)));
        g2.fillRoundRect(x, y + half, thickness, height - half, thickness, thickness);
    }

    private Color transparent(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
    }

    private void drawOutlinedText(Graphics2D g2, String text, int x, int y, Font font, Color fill, Color outline) {
        g2.setFont(resolveDisplayFont(font, text));
        g2.setColor(outline);
        g2.drawString(text, x - 1, y);
        g2.drawString(text, x + 1, y);
        g2.drawString(text, x, y - 1);
        g2.drawString(text, x, y + 1);
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }

    private void drawCenteredOutlinedText(Graphics2D g2, String text, int x, int y, int w, int h,
                                          Font font, Color fill, Color outline) {
        Font displayFont = resolveDisplayFont(font, text);
        g2.setFont(displayFont);
        FontMetrics metrics = g2.getFontMetrics(displayFont);
        int drawX = x + (w - metrics.stringWidth(text)) / 2;
        int drawY = y + (h - metrics.getHeight()) / 2 + metrics.getAscent();
        drawOutlinedText(g2, text, drawX, drawY, displayFont, fill, outline);
    }

    private double clamp01(double ratio) {
        return Math.max(0.0, Math.min(1.0, ratio));
    }

    private String formatCounter(int value) {
        return value < 10 ? "0" + Math.max(0, value) : String.valueOf(Math.max(0, value));
    }

    private Font resolveDisplayFont(Font base, String text) {
        if (text == null || text.isEmpty()) {
            return base;
        }
        if (containsCjk(text)) {
            return new Font(HUD_CHINESE_FONT.getFamily(), base.getStyle(), base.getSize());
        }
        return base;
    }

    private boolean containsCjk(String text) {
        for (int i = 0; i < text.length(); i++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(text.charAt(i));
            if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                    || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                    || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
                return true;
            }
        }
        return false;
    }

    private PaoPao findPlayer(Map<GameElement, List<ElementObj>> all) {
        ElementObj[] plays = snapshot(all.get(GameElement.PLAY));
        if (plays.length > 0 && plays[0] instanceof PaoPao) {
            return (PaoPao) plays[0];
        }
        return null;
    }

    private Boss findBoss(Map<GameElement, List<ElementObj>> all) {
        ElementObj[] bosses = snapshot(all.get(GameElement.BOSS));
        if (bosses.length > 0 && bosses[0] instanceof Boss) {
            return (Boss) bosses[0];
        }
        return null;
    }

    private void drawFinishOverlay(Graphics g) {
        if (!GameRuntime.waitingRestart) {
            return;
        }
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(Color.WHITE);
        Graphics2D g2 = (Graphics2D) g;
        drawOutlinedText(g2, GameRuntime.finishTitle, 220, 220,
                new Font(HUD_CHINESE_FONT.getFamily(), Font.BOLD, 42), Color.WHITE, Color.BLACK);
        drawOutlinedText(g2, "生存: " + String.format("%.1f", GameRuntime.survivalTimeMs / 1000.0) + "秒", 220, 280,
                new Font(HUD_CHINESE_FONT.getFamily(), Font.BOLD, 28), Color.WHITE, Color.BLACK);
        drawOutlinedText(g2, "击杀: " + GameRuntime.killCount, 220, 320,
                new Font(HUD_CHINESE_FONT.getFamily(), Font.BOLD, 28), Color.WHITE, Color.BLACK);
        drawOutlinedText(g2, "按 R 重新开始", 220, 380,
                new Font(HUD_CHINESE_FONT.getFamily(), Font.BOLD, 28), Color.WHITE, Color.BLACK);
    }

    private void drawStageTransitionOverlay(Graphics2D g2) {
        if (!GameRuntime.stageTransitionActive) {
            return;
        }
        double progress = clamp01(GameRuntime.getStageTransitionProgress());
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        double apertureRatio = GameRuntime.stageTransitionClosing ? (1.0 - progress) : progress;
        double eased = Math.pow(clamp01(apertureRatio), 0.82);
        double centerX = width * 0.63;
        double centerY = height * 0.56;
        double maxRadiusX = width * 0.84;
        double maxRadiusY = height * 0.88;
        double radiusX = maxRadiusX * eased;
        double radiusY = maxRadiusY * eased;

        Graphics2D overlay = (Graphics2D) g2.create();
        Area blackout = new Area(new java.awt.Rectangle(0, 0, width, height));
        if (radiusX > 1.0 && radiusY > 1.0) {
            blackout.subtract(new Area(new Ellipse2D.Double(centerX - radiusX, centerY - radiusY,
                    radiusX * 2.0, radiusY * 2.0)));
        }
        overlay.setColor(STAGE_TRANSITION_BLACK);
        overlay.fill(blackout);

        if (radiusX > 4.0 && radiusY > 4.0) {
            drawTransitionHalftone(overlay, centerX, centerY, radiusX, radiusY, progress);
        }
        overlay.dispose();
    }

    private void drawTransitionHalftone(Graphics2D g2, double centerX, double centerY,
                                        double radiusX, double radiusY, double progress) {
        double ringThickness = Math.max(20.0, Math.min(92.0, Math.min(radiusX, radiusY) * 0.22));
        double innerRadiusX = Math.max(1.0, radiusX - ringThickness);
        double innerRadiusY = Math.max(1.0, radiusY - ringThickness);
        Area ring = new Area(new Ellipse2D.Double(centerX - radiusX, centerY - radiusY,
                radiusX * 2.0, radiusY * 2.0));
        ring.subtract(new Area(new Ellipse2D.Double(centerX - innerRadiusX, centerY - innerRadiusY,
                innerRadiusX * 2.0, innerRadiusY * 2.0)));

        double densityScale = 0.88 + (1.0 - clamp01(progress)) * 0.36;
        int spacing = Math.max(8, (int) Math.round(12 * densityScale));
        int dotSize = Math.max(3, (int) Math.round(spacing * 0.45));
        int minX = Math.max(0, (int) Math.floor(centerX - radiusX - ringThickness));
        int maxX = Math.min(getWidth(), (int) Math.ceil(centerX + radiusX + ringThickness));
        int minY = Math.max(0, (int) Math.floor(centerY - radiusY - ringThickness));
        int maxY = Math.min(getHeight(), (int) Math.ceil(centerY + radiusY + ringThickness));

        for (int y = minY; y < maxY; y += spacing) {
            int rowOffset = ((y / spacing) & 1) == 0 ? 0 : spacing / 2;
            for (int x = minX + rowOffset; x < maxX; x += spacing) {
                if (!ring.contains(x, y)) {
                    continue;
                }
                double dx = (x - centerX) / Math.max(1.0, radiusX);
                double dy = (y - centerY) / Math.max(1.0, radiusY);
                double dist = Math.sqrt(dx * dx + dy * dy);
                double alphaScale = clamp01((dist - 0.76) / 0.24);
                int alpha = (int) Math.round(STAGE_TRANSITION_DOT.getAlpha() * alphaScale);
                if (alpha <= 0) {
                    continue;
                }
                g2.setColor(new Color(STAGE_TRANSITION_DOT.getRed(), STAGE_TRANSITION_DOT.getGreen(),
                        STAGE_TRANSITION_DOT.getBlue(), alpha));
                g2.fillOval(x - dotSize / 2, y - dotSize / 2, dotSize, dotSize);
            }
        }
    }

    private void addAll(List<ElementObj> actors, ElementObj[] objs) {
        if (objs == null) {
            return;
        }
        for (ElementObj obj : objs) {
            if (obj != null) {
                actors.add(obj);
            }
        }
    }

    private ElementObj[] snapshot(List<ElementObj> objs) {
        if (objs == null) {
            return new ElementObj[0];
        }
        synchronized (objs) {
            return objs.toArray(new ElementObj[0]);
        }
    }

    @Override
    public void run() {
        while (true) {
            this.repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
