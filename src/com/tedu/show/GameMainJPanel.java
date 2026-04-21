package com.tedu.show;

import com.tedu.element.Boss;
import com.tedu.element.ElementObj;
import com.tedu.element.PaoPao;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameRuntime;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class GameMainJPanel extends JPanel implements Runnable {
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
        g2 = (Graphics2D) g2.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(new Color(8, 18, 28, 180));
        g2.fillRoundRect(12, 12, 320, 154, 18, 18);
        g2.setColor(new Color(214, 232, 255));
        g2.setFont(new Font("Dialog", Font.BOLD, 22));

        ElementObj[] plays = snapshot(all.get(GameElement.PLAY));
        if (plays.length > 0 && plays[0] instanceof PaoPao) {
            PaoPao play = (PaoPao) plays[0];
            g2.drawString("HP: " + play.getHp(), 24, 42);
            g2.drawString("Grenade: " + play.getGrenades(), 24, 70);
            g2.setFont(new Font("Dialog", Font.BOLD, 18));
            g2.drawString("Weapon: " + play.getWeaponName(), 24, 98);
            g2.drawString("W2 Ready: " + (play.hasWeapon2() ? "YES" : "NO"), 24, 122);
        }

        g2.setFont(new Font("Dialog", Font.BOLD, 18));
        g2.drawString("Kill: " + GameRuntime.killCount, 196, 42);
        g2.drawString("Time: " + String.format("%.1f", GameRuntime.survivalTimeMs / 1000.0) + "s", 196, 70);
        g2.drawString("Progress: " + GameRuntime.getStageProgressPercent() + "%", 196, 98);
        g2.drawString("Stage: " + GameRuntime.currentStage + "/" + Math.max(1, GameRuntime.totalStages), 196, 126);

        ElementObj[] bosses = snapshot(all.get(GameElement.BOSS));
        if (bosses.length > 0 && bosses[0] instanceof Boss) {
            Boss boss = (Boss) bosses[0];
            g2.setColor(new Color(40, 10, 10, 180));
            g2.fillRoundRect(348, 18, 320, 34, 16, 16);
            g2.setColor(new Color(255, 220, 220));
            g2.drawString("Boss HP: " + boss.getHp(), 362, 42);
        }

        if (System.currentTimeMillis() < GameRuntime.bannerUntilMs && GameRuntime.bannerText != null
                && !GameRuntime.bannerText.isEmpty()) {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(260, 72, 480, 44, 16, 16);
            g2.setColor(new Color(255, 247, 189));
            g2.setFont(new Font("Dialog", Font.BOLD, 24));
            g2.drawString(GameRuntime.bannerText, 280, 102);
        }
        g2.dispose();
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
