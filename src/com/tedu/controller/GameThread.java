package com.tedu.controller;

import com.tedu.element.Boss;
import com.tedu.element.ElementObj;
import com.tedu.element.Enemy;
import com.tedu.element.Grenade;
import com.tedu.element.Hostage;
import com.tedu.element.MapObj;
import com.tedu.element.PaoPao;
import com.tedu.element.PlatformObj;
import com.tedu.element.ScoutEnemy;
import com.tedu.element.SupplyItem;
import com.tedu.manager.AudioPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameThread extends Thread {
    private static final String STAGE1_MAP_PATH = "image/images/\u80cc\u666f/mission1.png";
    private static final String DEFAULT_STAGE_MAP_PATH = "image/images/\u80cc\u666f/map2.png";
    private static final String[] ENEMY_TYPES = {"enemy1", "enemy2", "enemy3", "enemy4"};
    private static final StageConfig[] STAGES = {
            new StageConfig("STAGE 1", STAGE1_MAP_PATH,
                    1200, 420, 950,
                    75, 6, 35,
                    1, 2, 2, 3,
                    3, 1, 24, "weapon2"),
            new StageConfig("STAGE 2", DEFAULT_STAGE_MAP_PATH,
                    3600, 1200, 3050,
                    55, 8, 55,
                    2, 3, 3, 4,
                    4, 2, 36, "grenade")
    };

    private final ElementManager em;
    private final Random random = new Random();

    private long enemyAddTime;
    private boolean hostageSpawned;
    private boolean bossSpawned;
    private boolean missionResolved;
    private int currentStageIndex;

    public GameThread() {
        em = ElementManager.getManager();
    }

    @Override
    public void run() {
        GameRuntime.prepareStartScreen();
        waitForStartSignal();
        while (true) {
            gameLoad();
            gameRun();
            gameOver();
            try {
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForStartSignal() {
        while (!GameRuntime.startRequested) {
            try {
                sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        GameRuntime.beginStartTransition();
    }

    private void gameLoad() {
        GameRuntime.resetForNewGame();
        em.init();
        enemyAddTime = 0L;
        hostageSpawned = false;
        bossSpawned = false;
        missionResolved = false;
        currentStageIndex = 0;
        GameLoad.loadImg();
        GameLoad.loadObj();
        loadStage(0, 0L, null);
        AudioPlayer.playBgmLoop("music/boss_lv.wav");
        GameRuntime.markGameStartNow();
        GameRuntime.finishStartTransition();
    }

    private void loadStage(int stageIndex, long gameTime, PaoPao existingPlayer) {
        currentStageIndex = stageIndex;
        StageConfig stage = getCurrentStage();
        clearStageElements();
        resetStageState(gameTime);
        int resolvedStageLength = loadMap(stage);
        GameRuntime.beginStage(stageIndex + 1, STAGES.length, resolvedStageLength);

        PaoPao player = existingPlayer;
        if (player == null) {
            GameLoad.loadPlay();
            player = getPlayer();
        }
        if (player != null) {
            player.placeAtStageStart();
        }

        if (stageIndex == 0) {
            GameRuntime.showBanner(stage.title + "  |  A/D move  W double jump  E aim up  Ctrl crouch", 2600);
        } else {
            GameRuntime.showBanner(stage.title, 1800);
        }
    }

    private int loadMap(StageConfig stage) {
        ElementObj mapAObj = GameLoad.getObj("map");
        if (mapAObj == null) {
            return stage.minStageLength;
        }
        ElementObj mapA = mapAObj.createElement("0,0," + stage.mapPath);
        em.addElement(mapA, GameElement.MAPS);
        loadSupplementalPlatforms(stage, mapA);
        int actualScrollLength = Math.max(0, mapA.getW() - GameJFrame.GameX);
        return Math.max(stage.minStageLength, actualScrollLength);
    }

    private void loadSupplementalPlatforms(StageConfig stage, ElementObj map) {
        List<int[]> platforms = MapObj.buildSupplementalPlatforms(stage.mapPath,
                map.getX(), map.getY(), map.getW(), map.getH());
        for (int[] platform : platforms) {
            if (platform == null || platform.length < 4) {
                continue;
            }
            ElementObj platformObj = new PlatformObj().createElement(
                    platform[0] + "," + platform[1] + "," + platform[2] + "," + platform[3]);
            em.addElement(platformObj, GameElement.PLATFORM);
        }
    }

    private void clearStageElements() {
        clearElements(GameElement.MAPS);
        clearElements(GameElement.PLATFORM);
        clearElements(GameElement.ENEMY);
        clearElements(GameElement.BOSS);
        clearElements(GameElement.HOSTAGE);
        clearElements(GameElement.ITEM);
        clearElements(GameElement.PLAYFILE);
        clearElements(GameElement.ENEMYFILE);
        clearElements(GameElement.DIE);
    }

    private void resetStageState(long gameTime) {
        enemyAddTime = gameTime;
        hostageSpawned = false;
        bossSpawned = false;
        missionResolved = false;
        GameRuntime.worldScrollX = 0;
    }

    private StageConfig getCurrentStage() {
        return STAGES[currentStageIndex];
    }

    private boolean hasNextStage() {
        return currentStageIndex + 1 < STAGES.length;
    }

    private PaoPao getPlayer() {
        List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        if (plays.isEmpty() || !(plays.get(0) instanceof PaoPao)) {
            return null;
        }
        return (PaoPao) plays.get(0);
    }

    private void gameRun() {
        long gameTime = 0L;
        while (true) {
            Map<GameElement, List<ElementObj>> all = em.getGameElements();
            List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
            List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
            List<ElementObj> bosses = em.getElementsByKey(GameElement.BOSS);
            List<ElementObj> hostages = em.getElementsByKey(GameElement.HOSTAGE);
            List<ElementObj> items = em.getElementsByKey(GameElement.ITEM);
            List<ElementObj> playFiles = em.getElementsByKey(GameElement.PLAYFILE);
            List<ElementObj> enemyFiles = em.getElementsByKey(GameElement.ENEMYFILE);

            spawnSceneObjects(gameTime, enemys, bosses, hostages);
            GameRuntime.worldScrollX = resolveWorldScroll(plays, bosses);
            moveAndUpdate(all, gameTime);

            handlePlayerProjectilesHitEnemies(playFiles, enemys);
            handlePlayerProjectilesHitBoss(playFiles, bosses);
            handleEnemyProjectilesHitPlayer(enemyFiles, plays, gameTime);
            handleHostageRescue(plays, hostages);
            handleItemPickup(plays, items);

            GameRuntime.survivalTimeMs = System.currentTimeMillis() - GameRuntime.startTimeMs;
            if (plays.isEmpty()) {
                GameRuntime.finishTitle = "MISSION FAILED";
                GameRuntime.waitingRestart = true;
                break;
            }
            if (bossSpawned && !missionResolved && bosses.isEmpty()) {
                missionResolved = true;
                if (hasNextStage()) {
                    loadStage(currentStageIndex + 1, gameTime, getPlayer());
                } else {
                    GameRuntime.missionClear = true;
                    GameRuntime.finishTitle = "MISSION COMPLETE";
                    GameRuntime.showBanner("Final boss defeated", 1800);
                    GameRuntime.waitingRestart = true;
                    break;
                }
            }

            gameTime++;
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int resolveWorldScroll(List<ElementObj> plays, List<ElementObj> bosses) {
        if (plays == null || plays.isEmpty() || bossSpawned || (bosses != null && !bosses.isEmpty())) {
            return 0;
        }
        ElementObj playObj = plays.get(0);
        if (!(playObj instanceof PaoPao)) {
            return 0;
        }
        int remainingDistance = Math.max(0, GameRuntime.stageLength - GameRuntime.stageDistance);
        int scroll = ((PaoPao) playObj).prepareWorldScroll(remainingDistance);
        GameRuntime.stageDistance += scroll;
        return scroll;
    }

    private void spawnSceneObjects(long gameTime, List<ElementObj> enemys, List<ElementObj> bosses, List<ElementObj> hostages) {
        StageConfig stage = getCurrentStage();
        int hostageSpawnDistance = stage.resolveHostageSpawnDistance(GameRuntime.stageLength);
        int bossSpawnDistance = stage.resolveBossSpawnDistance(GameRuntime.stageLength);
        if (!hostageSpawned && GameRuntime.stageDistance >= hostageSpawnDistance) {
            hostageSpawned = true;
            ElementObj hostage = new Hostage().createElement((GameJFrame.GameX + 160) + ",0," + stage.hostageRewardType);
            int footX = hostage.getX() + hostage.getW() / 2;
            hostage.setY(GameRuntime.getBattlefieldMaxBottomAt(footX) - hostage.getH());
            em.addElement(hostage, GameElement.HOSTAGE);
            GameRuntime.showBanner("Found a hostage", 1400);
        }
        if (!bossSpawned && GameRuntime.stageDistance >= bossSpawnDistance) {
            bossSpawned = true;
            GameRuntime.stageDistance = GameRuntime.stageLength;
            ElementObj boss = new Boss().createElement((GameJFrame.GameX + 220) + ",0," + stage.bossHp);
            int footX = boss.getX() + boss.getW() / 2;
            boss.setY(GameRuntime.getBattlefieldMaxBottomAt(footX) - boss.getH());
            em.addElement(boss, GameElement.BOSS);
            GameRuntime.showBanner("Boss incoming", 1800);
            return;
        }
        if (bossSpawned || (bosses != null && !bosses.isEmpty())) {
            return;
        }
        spawnEnemy(gameTime, enemys, stage);
    }

    private void spawnEnemy(long gameTime, List<ElementObj> enemys, StageConfig stage) {
        if (gameTime - enemyAddTime < stage.enemyInterval) {
            return;
        }
        if (enemys.size() >= stage.maxEnemies) {
            return;
        }
        enemyAddTime = gameTime;
        int spawnX = GameJFrame.GameX + 20;
        int bottom = randomBattlefieldBottom(spawnX);
        ElementObj enemyObj = GameLoad.getObj("enemy");
        if (enemyObj == null) {
            return;
        }
        int speed = randomBetween(stage.enemySpeedMin, stage.enemySpeedMax);
        int hp = randomBetween(stage.enemyHpMin, stage.enemyHpMax);
        String enemyType = ENEMY_TYPES[random.nextInt(ENEMY_TYPES.length)];
        ElementObj enemy = enemyObj.createElement(
                spawnX + "," + (bottom - 72) + "," + enemyType + "," + speed + "," + hp);
        enemy.setY(bottom - enemy.getH());
        em.addElement(enemy, GameElement.ENEMY);
    }

    private int randomBetween(int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    private int randomBattlefieldBottom(int screenX) {
        return GameRuntime.getBattlefieldMaxBottomAt(screenX);
    }

    private void handlePlayerProjectilesHitEnemies(List<ElementObj> playFiles, List<ElementObj> enemys) {
        for (int i = playFiles.size() - 1; i >= 0; i--) {
            ElementObj projectile = playFiles.get(i);
            if (!projectile.isLive()) {
                continue;
            }
            for (int j = enemys.size() - 1; j >= 0; j--) {
                ElementObj enemy = enemys.get(j);
                if (!enemy.isLive() || !enemy.pk(projectile)) {
                    continue;
                }
                int damage = Math.max(1, projectile.getDamage());
                if (enemy instanceof Enemy) {
                    ((Enemy) enemy).hurt(damage);
                } else if (enemy instanceof ScoutEnemy) {
                    ((ScoutEnemy) enemy).hurt(damage);
                } else {
                    enemy.setLive(false);
                }
                consumePlayerProjectile(projectile);
                break;
            }
        }
    }

    private void handlePlayerProjectilesHitBoss(List<ElementObj> playFiles, List<ElementObj> bosses) {
        for (int i = playFiles.size() - 1; i >= 0; i--) {
            ElementObj projectile = playFiles.get(i);
            if (!projectile.isLive()) {
                continue;
            }
            for (int j = bosses.size() - 1; j >= 0; j--) {
                ElementObj boss = bosses.get(j);
                if (!boss.isLive() || !boss.pk(projectile)) {
                    continue;
                }
                int damage = Math.max(1, projectile.getDamage());
                if (boss instanceof Boss) {
                    ((Boss) boss).hurt(damage);
                } else {
                    boss.setLive(false);
                }
                consumePlayerProjectile(projectile);
                break;
            }
        }
    }

    private void consumePlayerProjectile(ElementObj projectile) {
        if (projectile instanceof Grenade) {
            ((Grenade) projectile).explode();
        } else {
            projectile.setLive(false);
        }
    }

    private void handleEnemyProjectilesHitPlayer(List<ElementObj> enemyFiles, List<ElementObj> plays, long gameTime) {
        for (int i = enemyFiles.size() - 1; i >= 0; i--) {
            ElementObj projectile = enemyFiles.get(i);
            if (!projectile.isLive()) {
                continue;
            }
            for (int j = plays.size() - 1; j >= 0; j--) {
                ElementObj playObj = plays.get(j);
                if (!playObj.isLive() || !projectile.pk(playObj)) {
                    continue;
                }
                if (playObj instanceof PaoPao) {
                    PaoPao play = (PaoPao) playObj;
                    long oldHurtTime = play.getHurtTime();
                    play.hurt(gameTime, Math.max(1, projectile.getDamage()));
                    if (play.getHurtTime() != oldHurtTime) {
                        AudioPlayer.playOnce("music/die.wav");
                    }
                } else {
                    playObj.setLive(false);
                }
                projectile.setLive(false);
                break;
            }
        }
    }

    private void handleHostageRescue(List<ElementObj> plays, List<ElementObj> hostages) {
        for (int i = hostages.size() - 1; i >= 0; i--) {
            ElementObj hostageObj = hostages.get(i);
            for (int j = plays.size() - 1; j >= 0; j--) {
                ElementObj playObj = plays.get(j);
                if (!hostageObj.isLive() || !playObj.isLive() || !hostageObj.pk(playObj)) {
                    continue;
                }
                if (hostageObj instanceof Hostage) {
                    ((Hostage) hostageObj).rescue();
                } else {
                    hostageObj.setLive(false);
                }
                break;
            }
        }
    }

    private void handleItemPickup(List<ElementObj> plays, List<ElementObj> items) {
        if (plays.isEmpty()) {
            return;
        }
        ElementObj playObj = plays.get(0);
        if (!(playObj instanceof PaoPao)) {
            return;
        }
        PaoPao play = (PaoPao) playObj;
        for (int i = items.size() - 1; i >= 0; i--) {
            ElementObj itemObj = items.get(i);
            if (!itemObj.isLive() || !itemObj.pk(play)) {
                continue;
            }
            if (itemObj instanceof SupplyItem) {
                ((SupplyItem) itemObj).applyTo(play);
            } else {
                itemObj.setLive(false);
            }
        }
    }

    public void moveAndUpdate(Map<GameElement, List<ElementObj>> all, long gameTime) {
        for (GameElement ge : GameElement.values()) {
            List<ElementObj> list = all.get(ge);
            synchronized (list) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    ElementObj obj = list.get(i);
                    if (!obj.isLive()) {
                        obj.die();
                        list.remove(i);
                        continue;
                    }
                    obj.model(gameTime);
                }
            }
        }
    }

    private void clearElements(GameElement element) {
        List<ElementObj> list = em.getElementsByKey(element);
        synchronized (list) {
            list.clear();
        }
    }

    private void gameOver() {
        AudioPlayer.stopBgm();
        while (!GameRuntime.restartRequested) {
            try {
                sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        GameRuntime.waitingRestart = false;
    }

    private static final class StageConfig {
        private final String title;
        private final String mapPath;
        private final int minStageLength;
        private final double hostageSpawnRatio;
        private final double bossSpawnRatio;
        private final int enemyInterval;
        private final int maxEnemies;
        private final int scoutChance;
        private final int enemySpeedMin;
        private final int enemySpeedMax;
        private final int enemyHpMin;
        private final int enemyHpMax;
        private final int scoutSpeed;
        private final int scoutHp;
        private final int bossHp;
        private final String hostageRewardType;

        private StageConfig(String title, String mapPath, int stageLength,
                            int hostageSpawnDistance, int bossSpawnDistance,
                            int enemyInterval, int maxEnemies, int scoutChance,
                            int enemySpeedMin, int enemySpeedMax,
                            int enemyHpMin, int enemyHpMax,
                            int scoutSpeed, int scoutHp,
                            int bossHp, String hostageRewardType) {
            this.title = title;
            this.mapPath = mapPath;
            this.minStageLength = Math.max(0, stageLength);
            int baseStageLength = Math.max(1, stageLength);
            this.hostageSpawnRatio = clampProgress(hostageSpawnDistance / (double) baseStageLength);
            this.bossSpawnRatio = clampProgress(bossSpawnDistance / (double) baseStageLength);
            this.enemyInterval = enemyInterval;
            this.maxEnemies = maxEnemies;
            this.scoutChance = scoutChance;
            this.enemySpeedMin = enemySpeedMin;
            this.enemySpeedMax = enemySpeedMax;
            this.enemyHpMin = enemyHpMin;
            this.enemyHpMax = enemyHpMax;
            this.scoutSpeed = scoutSpeed;
            this.scoutHp = scoutHp;
            this.bossHp = bossHp;
            this.hostageRewardType = hostageRewardType;
        }

        private int resolveHostageSpawnDistance(int activeStageLength) {
            return scaleDistance(activeStageLength, hostageSpawnRatio);
        }

        private int resolveBossSpawnDistance(int activeStageLength) {
            return scaleDistance(activeStageLength, bossSpawnRatio);
        }

        private static int scaleDistance(int activeStageLength, double ratio) {
            if (activeStageLength <= 0) {
                return 0;
            }
            return Math.max(0, Math.min(activeStageLength, (int) Math.round(activeStageLength * ratio)));
        }

        private static double clampProgress(double value) {
            return Math.max(0.0, Math.min(1.0, value));
        }
    }
}
