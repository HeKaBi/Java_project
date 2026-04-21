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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class GameMainJPanel extends JPanel implements Runnable {
    private static final String START_SCREEN_PATH = "image/images/\u80cc\u666f/start.jpg";
    private static final Font HUD_SMALL_FONT = new Font("Consolas", Font.BOLD, 12);
    private static final Font HUD_LABEL_FONT = new Font("Consolas", Font.BOLD, 14);
    private static final Font HUD_VALUE_FONT = new Font("Consolas", Font.BOLD, 24);
    private static final Font HUD_BIG_FONT = new Font("Consolas", Font.BOLD, 40);
    private static final Font BANNER_FONT = new Font("Consolas", Font.BOLD, 24);
    private static final Font START_PROMPT_FONT = new Font("Impact", Font.PLAIN, 38);
    private static final Font START_ACTION_FONT = new Font("Consolas", Font.BOLD, 18);
    private static final Font START_INFO_FONT = new Font("Consolas", Font.BOLD, 13);

    private static final Color HUD_PANEL = new Color(12, 24, 60, 220);
    private static final Color HUD_PANEL_DARK = new Color(9, 16, 39, 230);
    private static final Color HUD_BORDER = new Color(108, 166, 255);
    private static final Color HUD_CYAN = new Color(226, 244, 255);
    private static final Color HUD_GOLD = new Color(255, 215, 86);
    private static final Color HUD_ORANGE = new Color(255, 162, 46);
    private static final Color HUD_RED = new Color(214, 64, 52);
    private static final Color HUD_RED_GLOW = new Color(255, 145, 111);
    private static final Color HUD_GREEN = new Color(78, 224, 144);
    private static final Color HUD_GREEN_GLOW = new Color(180, 255, 200);
    private static final Color HUD_MIST = new Color(8, 14, 34, 165);
    private static final Color HUD_MIST_SOFT = new Color(38, 78, 134, 72);
    private static final Color HUD_SEPARATOR = new Color(124, 177, 255, 136);
    private static final Color HUD_TIME_GLOW = new Color(255, 198, 82, 150);

    private ElementManager em;
    private final Image startScreenImage;

    public GameMainJPanel() {
        startScreenImage = loadStartScreenImage();
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
    }

    private Image loadStartScreenImage() {
        javax.swing.ImageIcon icon = GameLoad.loadImage(START_SCREEN_PATH);
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

        drawCenteredShadowedText(g2, loadingStart ? "MISSION START" : "NEW GAME", centerX, titleY,
                START_PROMPT_FONT, titleColor, new Color(24, 20, 34, 110), 2);
        drawCenteredShadowedText(g2, loadingStart ? "ENTERING STAGE 1" : "PRESS ENTER",
                centerX + 4, titleY + 36,
                START_ACTION_FONT, accentColor, new Color(19, 14, 22, 90), 1);
        drawCenteredShadowedText(g2,
                loadingStart ? "Loading terrain and mission units" : "A/D move   W double jump   E aim up   Ctrl crouch",
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
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics(font);
        int drawX = centerX - metrics.stringWidth(text) / 2;
        drawShadowedText(g2, text, drawX, y, font, fill, shadow, depth);
    }

    private void drawShadowedText(Graphics2D g2, String text, int x, int y,
                                  Font font, Color fill, Color shadow, int depth) {
        g2.setFont(font);
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
        drawTimeHud(hud);
        if (boss != null) {
            drawBossHud(hud, boss);
        }
        drawBanner(hud, boss != null);
        hud.dispose();
    }

    private void drawPlayerHud(Graphics2D g2, PaoPao play) {
        int x = 22;
        int y = 22;

        drawOutlinedText(g2, "1UP", x, y + 18, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
        drawFadedLine(g2, x - 4, y + 24, 46, HUD_SEPARATOR);

        drawOutlinedText(g2, "LIFE", x + 62, y + 16, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawSegmentBar(g2, x + 62, y + 24, 172, 16, play.getHp(), play.getMaxHp(), HUD_GREEN, HUD_GREEN_GLOW);

        drawVerticalFadedLine(g2, x + 256, y + 2, 74, HUD_SEPARATOR);
        drawOutlinedText(g2, "ARMS", x + 278, y + 16, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, play.getWeaponHudLabel(), x + 278, y + 48, HUD_VALUE_FONT, HUD_GOLD, Color.BLACK);
        drawWeaponIndicator(g2, x + 280, y + 70, "R", !play.isHeavyWeaponEquipped(), true);
        drawWeaponIndicator(g2, x + 312, y + 70, "H", play.isHeavyWeaponEquipped(), play.hasWeapon2());

        drawVerticalFadedLine(g2, x + 388, y + 2, 74, new Color(255, 190, 96, 116));
        drawOutlinedText(g2, "BOMB", x + 408, y + 16, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, formatCounter(play.getGrenades()), x + 408, y + 74, HUD_BIG_FONT, HUD_ORANGE, Color.BLACK);
        drawFadedLine(g2, x + 406, y + 80, 58, new Color(255, 176, 68, 128));
    }

    private void drawMissionHud(Graphics2D g2) {
        int x = 472;
        int y = 20;

        drawOutlinedText(g2, "MISSION", x, y + 14, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
        drawFadedLine(g2, x - 4, y + 20, 86, HUD_SEPARATOR);
        drawStatCluster(g2, x, y + 40, 68, "KILL", String.valueOf(GameRuntime.killCount), HUD_GOLD);
        drawVerticalFadedLine(g2, x + 88, y + 18, 56, HUD_SEPARATOR);
        drawStatCluster(g2, x + 108, y + 40, 86, "STAGE",
                GameRuntime.currentStage + "/" + Math.max(1, GameRuntime.totalStages), HUD_CYAN);
        drawVerticalFadedLine(g2, x + 214, y + 18, 56, HUD_SEPARATOR);
        drawStatCluster(g2, x + 234, y + 40, 88, "PROG",
                GameRuntime.getStageProgressPercent() + "%", HUD_GOLD);

        drawOutlinedText(g2, "ADVANCE", x, y + 84, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawMeter(g2, x + 72, y + 74, 250, 10,
                GameRuntime.getStageProgressPercent() / 100.0, HUD_GOLD, new Color(255, 240, 170));
    }

    private void drawTimeHud(Graphics2D g2) {
        int x = GameJFrame.GameX - 150;
        int y = 22;

        long seconds = Math.max(0L, GameRuntime.survivalTimeMs / 1000L);
        long tenths = Math.max(0L, (GameRuntime.survivalTimeMs % 1000L) / 100L);

        drawOutlinedText(g2, "TIME", x, y + 18, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
        drawFadedLine(g2, x - 4, y + 24, 94, HUD_TIME_GLOW);
        drawOutlinedText(g2, String.valueOf(seconds), x, y + 76, HUD_BIG_FONT, HUD_GOLD, Color.BLACK);
        drawOutlinedText(g2, "." + tenths + "s", x + 72, y + 76, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
    }

    private void drawBossHud(Graphics2D g2, Boss boss) {
        int w = 418;
        int h = 16;
        int x = (GameJFrame.GameX - w) / 2;
        int y = 118;
        drawOutlinedText(g2, "BOSS", x, y + 13, HUD_LABEL_FONT, new Color(255, 235, 218), Color.BLACK);
        drawMeter(g2, x + 56, y + 1, w - 56, h,
                boss.getMaxHp() <= 0 ? 0.0 : boss.getHp() / (double) boss.getMaxHp(), HUD_RED, HUD_RED_GLOW);
        drawFadedLine(g2, x + 56, y + 22, w - 56, new Color(255, 132, 118, 96));
    }

    private void drawBanner(Graphics2D g2, boolean bossActive) {
        if (System.currentTimeMillis() >= GameRuntime.bannerUntilMs
                || GameRuntime.bannerText == null
                || GameRuntime.bannerText.isEmpty()) {
            return;
        }

        FontMetrics fm = g2.getFontMetrics(BANNER_FONT);
        int textW = fm.stringWidth(GameRuntime.bannerText);
        int centerX = GameJFrame.GameX / 2;
        int baselineY = bossActive ? 170 : 132;
        int lineGap = 26;
        int halfSpan = Math.min(180, Math.max(72, (GameJFrame.GameX - textW) / 4));

        drawCenteredShadowedText(g2, GameRuntime.bannerText, centerX, baselineY, BANNER_FONT,
                new Color(255, 246, 185), new Color(8, 8, 12, 140), 2);
        drawFadedLine(g2, centerX - textW / 2 - halfSpan - lineGap, baselineY - 10, halfSpan, HUD_TIME_GLOW);
        drawFadedLine(g2, centerX + textW / 2 + lineGap, baselineY - 10, halfSpan, HUD_TIME_GLOW);
    }

    private void drawHudAtmosphere(Graphics2D g2) {
        int width = GameJFrame.GameX;
        g2.setPaint(new GradientPaint(0, 0, new Color(4, 10, 26, 196), 0, 150, new Color(4, 10, 26, 0)));
        g2.fillRect(0, 0, width, 164);
        g2.setColor(HUD_MIST_SOFT);
        g2.fillOval(-80, -56, 340, 136);
        g2.fillOval(286, -66, 448, 156);
        g2.fillOval(770, -46, 280, 128);
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

    private void drawWeaponIndicator(Graphics2D g2, int x, int y, String label, boolean selected, boolean enabled) {
        Color text = selected ? HUD_GOLD : (enabled ? HUD_CYAN : new Color(156, 156, 156));
        Color line = selected ? new Color(255, 214, 86, 150)
                : (enabled ? HUD_SEPARATOR : new Color(110, 110, 110, 96));
        drawOutlinedText(g2, label, x, y, HUD_SMALL_FONT, text, Color.BLACK);
        drawFadedLine(g2, x - 2, y + 6, 18, line);
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
        g2.setFont(font);
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
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics(font);
        int drawX = x + (w - metrics.stringWidth(text)) / 2;
        int drawY = y + (h - metrics.getHeight()) / 2 + metrics.getAscent();
        drawOutlinedText(g2, text, drawX, drawY, font, fill, outline);
    }

    private double clamp01(double ratio) {
        return Math.max(0.0, Math.min(1.0, ratio));
    }

    private String formatCounter(int value) {
        return value < 10 ? "0" + Math.max(0, value) : String.valueOf(Math.max(0, value));
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
        g.setFont(new Font("Consolas", Font.BOLD, 42));
        g.drawString(GameRuntime.finishTitle, 220, 220);
        g.setFont(new Font("Consolas", Font.BOLD, 28));
        g.drawString("Survival: " + String.format("%.1f", GameRuntime.survivalTimeMs / 1000.0) + "s", 220, 280);
        g.drawString("Kills: " + GameRuntime.killCount, 220, 320);
        g.drawString("Press R To Restart", 220, 380);
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
