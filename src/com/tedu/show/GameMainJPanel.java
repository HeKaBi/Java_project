package com.tedu.show;

import com.tedu.element.Boss;
import com.tedu.element.ElementObj;
import com.tedu.element.PaoPao;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameRuntime;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class GameMainJPanel extends JPanel implements Runnable {
    private static final Font HUD_SMALL_FONT = new Font("Consolas", Font.BOLD, 12);
    private static final Font HUD_LABEL_FONT = new Font("Consolas", Font.BOLD, 14);
    private static final Font HUD_VALUE_FONT = new Font("Consolas", Font.BOLD, 24);
    private static final Font HUD_BIG_FONT = new Font("Consolas", Font.BOLD, 40);
    private static final Font BANNER_FONT = new Font("Consolas", Font.BOLD, 24);

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

    private ElementManager em;

    public GameMainJPanel() {
        init();
    }

    public void init() {
        em = ElementManager.getManager();
        this.setPreferredSize(new Dimension(GameJFrame.GameX, GameJFrame.GameY));
        this.setFocusable(true);
        this.setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Map<GameElement, List<ElementObj>> all = em.getGameElements();

        drawMaps(g, all.get(GameElement.MAPS));
        drawActors(g, all);
        drawEffects(g, all);
        drawHud((Graphics2D) g, all);
        drawFinishOverlay(g);
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
        int x = 16;
        int y = 16;
        int w = 428;
        int h = 92;
        drawPlate(g2, x, y, w, h, HUD_PANEL, HUD_BORDER);

        drawOutlinedText(g2, "1UP", x + 18, y + 25, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, "LIFE", x + 70, y + 25, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawSegmentBar(g2, x + 70, y + 33, 150, 18, play.getHp(), play.getMaxHp(), HUD_GREEN, HUD_GREEN_GLOW);

        drawOutlinedText(g2, "ARMS", x + 242, y + 25, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, play.getWeaponHudLabel(), x + 242, y + 56, HUD_VALUE_FONT, HUD_GOLD, Color.BLACK);
        drawWeaponSlot(g2, x + 244, y + 66, 28, 16, "R", !play.isHeavyWeaponEquipped(), true);
        drawWeaponSlot(g2, x + 278, y + 66, 28, 16, "H", play.isHeavyWeaponEquipped(), play.hasWeapon2());

        drawOutlinedText(g2, "BOMB", x + 336, y + 25, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, formatCounter(play.getGrenades()), x + 338, y + 78, HUD_BIG_FONT, HUD_ORANGE, Color.BLACK);
    }

    private void drawMissionHud(Graphics2D g2) {
        int x = 458;
        int y = 16;
        int w = 350;
        int h = 92;
        drawPlate(g2, x, y, w, h, HUD_PANEL, HUD_BORDER);

        drawOutlinedText(g2, "MISSION", x + 18, y + 25, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
        drawCounterCell(g2, x + 18, y + 32, 86, 40, "KILL", String.valueOf(GameRuntime.killCount));
        drawCounterCell(g2, x + 114, y + 32, 90, 40,
                "STAGE", GameRuntime.currentStage + "/" + Math.max(1, GameRuntime.totalStages));
        drawCounterCell(g2, x + 214, y + 32, 118, 40,
                "PROG", GameRuntime.getStageProgressPercent() + "%");

        drawOutlinedText(g2, "ADVANCE", x + 18, y + 84, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawMeter(g2, x + 82, y + 74, 250, 12,
                GameRuntime.getStageProgressPercent() / 100.0, HUD_GOLD, new Color(255, 240, 170));
    }

    private void drawTimeHud(Graphics2D g2) {
        int x = 820;
        int y = 16;
        int w = 164;
        int h = 92;
        drawPlate(g2, x, y, w, h, HUD_PANEL_DARK, new Color(255, 198, 82));

        long seconds = Math.max(0L, GameRuntime.survivalTimeMs / 1000L);
        long tenths = Math.max(0L, (GameRuntime.survivalTimeMs % 1000L) / 100L);

        drawOutlinedText(g2, "TIME", x + 18, y + 25, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
        drawOutlinedText(g2, String.valueOf(seconds), x + 18, y + 78, HUD_BIG_FONT, HUD_GOLD, Color.BLACK);
        drawOutlinedText(g2, "." + tenths + "s", x + 108, y + 78, HUD_LABEL_FONT, HUD_CYAN, Color.BLACK);
    }

    private void drawBossHud(Graphics2D g2, Boss boss) {
        int w = 372;
        int h = 30;
        int x = (GameJFrame.GameX - w) / 2;
        int y = 118;
        drawPlate(g2, x, y, w, h, new Color(63, 14, 18, 228), new Color(255, 134, 120));
        drawOutlinedText(g2, "BOSS", x + 14, y + 21, HUD_LABEL_FONT, new Color(255, 235, 218), Color.BLACK);
        drawMeter(g2, x + 68, y + 9, 286, 12,
                boss.getMaxHp() <= 0 ? 0.0 : boss.getHp() / (double) boss.getMaxHp(), HUD_RED, HUD_RED_GLOW);
    }

    private void drawBanner(Graphics2D g2, boolean bossActive) {
        if (System.currentTimeMillis() >= GameRuntime.bannerUntilMs
                || GameRuntime.bannerText == null
                || GameRuntime.bannerText.isEmpty()) {
            return;
        }

        FontMetrics fm = g2.getFontMetrics(BANNER_FONT);
        int w = Math.min(GameJFrame.GameX - 140, fm.stringWidth(GameRuntime.bannerText) + 56);
        int h = 38;
        int x = (GameJFrame.GameX - w) / 2;
        int y = bossActive ? 158 : 120;
        drawPlate(g2, x, y, w, h, new Color(16, 20, 44, 220), new Color(255, 224, 131));
        drawCenteredOutlinedText(g2, GameRuntime.bannerText, x, y, w, h, BANNER_FONT,
                new Color(255, 246, 185), Color.BLACK);
    }

    private void drawPlate(Graphics2D g2, int x, int y, int w, int h, Color fill, Color border) {
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(x + 4, y + 4, w, h, 16, 16);

        g2.setColor(fill);
        g2.fillRoundRect(x, y, w, h, 16, 16);

        g2.setColor(new Color(255, 255, 255, 28));
        g2.fillRoundRect(x + 3, y + 3, w - 6, Math.max(10, h / 3), 12, 12);

        Stroke oldStroke = g2.getStroke();
        g2.setColor(border);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(x, y, w, h, 16, 16);
        g2.setStroke(oldStroke);
    }

    private void drawCounterCell(Graphics2D g2, int x, int y, int w, int h, String label, String value) {
        g2.setColor(new Color(8, 14, 34, 200));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(123, 175, 255));
        g2.drawRoundRect(x, y, w, h, 10, 10);
        drawOutlinedText(g2, label, x + 8, y + 14, HUD_SMALL_FONT, HUD_CYAN, Color.BLACK);
        drawCenteredOutlinedText(g2, value, x, y + 10, w, h, HUD_LABEL_FONT, HUD_GOLD, Color.BLACK);
    }

    private void drawWeaponSlot(Graphics2D g2, int x, int y, int w, int h, String label,
                                boolean selected, boolean enabled) {
        Color fill = enabled ? new Color(20, 30, 64, 220) : new Color(40, 40, 40, 180);
        Color border = selected ? HUD_GOLD : (enabled ? HUD_BORDER : new Color(90, 90, 90));
        Color text = selected ? HUD_GOLD : (enabled ? HUD_CYAN : new Color(170, 170, 170));

        g2.setColor(fill);
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setColor(border);
        g2.drawRoundRect(x, y, w, h, 8, 8);
        drawCenteredOutlinedText(g2, label, x, y + 1, w, h, HUD_SMALL_FONT, text, Color.BLACK);
    }

    private void drawSegmentBar(Graphics2D g2, int x, int y, int w, int h, int value, int max,
                                Color fill, Color glow) {
        g2.setColor(new Color(8, 14, 34, 210));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(106, 159, 233));
        g2.drawRoundRect(x, y, w, h, 10, 10);

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
            g2.setColor(i < value ? fill : new Color(42, 53, 79));
            g2.fillRoundRect(sx, innerY, segmentW, innerH, 6, 6);
            if (i < value) {
                g2.setColor(glow);
                g2.fillRoundRect(sx, innerY, segmentW, Math.max(3, innerH / 2), 6, 6);
            }
        }
    }

    private void drawMeter(Graphics2D g2, int x, int y, int w, int h, double ratio, Color fill, Color glow) {
        g2.setColor(new Color(8, 14, 34, 220));
        g2.fillRoundRect(x, y, w, h, h, h);
        g2.setColor(new Color(124, 177, 255));
        g2.drawRoundRect(x, y, w, h, h, h);

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

        g2.setColor(new Color(255, 255, 255, 22));
        for (int px = x + 12; px < x + w - 4; px += 14) {
            g2.drawLine(px, y + 2, px, y + h - 3);
        }
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
