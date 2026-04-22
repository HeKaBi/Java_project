package com.tedu.element;

import com.tedu.manager.AudioPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.swing.ImageIcon;

public class Boss extends ElementObj {
    private static final int HITBOX_W = 128;
    private static final int HITBOX_H = 118;
    private static final String BOSS_BULLET = "image/images/\u5b50\u5f39/boss_bomb.png";
    private static final String VARIANT_BOSS1 = "boss1";
    private static final String VARIANT_BOSS2 = "boss2";
    private static final String VARIANT_BOSS3 = "boss3";
    private static final double BOSS2_RENDER_SCALE = 0.76;
    private static final double BOSS2_HITBOX_SCALE = 0.80;

    private static final int FRAME_GAP = 5;
    private static final long BOSS1_ATTACK_WINDUP = 8L;
    private static final long BOSS1_ATTACK_DURATION = 24L;
    private static final long BOSS1_ATTACK_COOLDOWN = 30L;
    private static final long BOSS2_ATTACK_WINDUP = 8L;
    private static final long BOSS2_ATTACK_DURATION = 22L;
    private static final long BOSS2_RELOAD_DURATION = 18L;
    private static final long BOSS2_CROUCH_DURATION = 14L;
    private static final long BOSS2_ATTACK_COOLDOWN = 26L;
    private static final long BOSS3_ATTACK_WINDUP = 6L;
    private static final long BOSS3_ATTACK_DURATION = 20L;
    private static final long BOSS3_ATTACK_COOLDOWN = 24L;
    private static final int BOSS3_ATTACK_RANGE = 84;
    private static final int BOSS3_MELEE_REACH = 78;
    private static final int BOSS3_MELEE_DAMAGE = 2;
    private static final int BOSS3_MOVE_SPEED = 3;

    private static final BossAnimationSet BOSS1_ANIMATIONS = BossAnimationSet.loadBoss1();
    private static final BossAnimationSet BOSS2_ANIMATIONS = BossAnimationSet.loadBoss2();
    private static final BossAnimationSet BOSS3_ANIMATIONS = BossAnimationSet.loadBoss3();

    private final ElementManager em = ElementManager.getManager();
    private final Random random = new Random();

    private int hp = 24;
    private int maxHp = 24;
    private int patrolSpeed = 2;
    private boolean entered = false;
    private boolean patrolRight = false;
    private boolean faceRight = false;
    private boolean countedKill = false;
    private boolean dying = false;
    private long deathStartTime = -1L;
    private String variantKey = VARIANT_BOSS1;
    private BossAnimationSet animationSet = BOSS1_ANIMATIONS;
    private BossAction currentAction = BossAction.MOVE;
    private long actionStartTime = 0L;
    private long nextAttackTime = 0L;
    private boolean volleyFired = false;
    private ImageIcon currentFrame = firstFrame(BOSS1_ANIMATIONS.moveFrames);

    @Override
    public void showElement(Graphics g) {
        ImageIcon frame = currentFrame == null ? this.getIcon() : currentFrame;
        if (frame == null) {
            return;
        }
        double scale = resolveRenderScale();
        int drawW = Math.max(1, (int) Math.round(frame.getIconWidth() * scale));
        int drawH = Math.max(1, (int) Math.round(frame.getIconHeight() * scale));
        int drawX = this.getX() + (this.getW() - drawW) / 2;
        int drawY = this.getY() + this.getH() - drawH;
        boolean shouldMirror = faceRight != animationSet.sourceFacesRight;
        if (shouldMirror) {
            g.drawImage(frame.getImage(), drawX + drawW, drawY, -drawW, drawH, null);
        } else {
            g.drawImage(frame.getImage(), drawX, drawY, drawW, drawH, null);
        }
    }

    @Override
    protected void move() {
        if (dying) {
            return;
        }
        if (!entered || currentAction == BossAction.MOVE) {
            updateFacing();
        }
        int x = this.getX() - GameRuntime.worldScrollX;
        int minX = resolveArenaMinX();
        int maxX = resolveArenaMaxX();
        if (!entered) {
            x -= patrolSpeed;
            if (x <= maxX - 60) {
                x = maxX - 60;
                entered = true;
            }
        } else if (currentAction == BossAction.MOVE) {
            if (isBoss3()) {
                x = resolveBoss3Move(x, minX, maxX);
            } else {
                x = resolvePatrolMove(x, minX, maxX);
            }
        }
        this.setX(x);
        int footX = this.getX() + this.getW() / 2;
        this.setY(GameRuntime.getBattlefieldMaxBottomAt(footX) - this.getH());
        if (!entered || currentAction == BossAction.MOVE) {
            updateFacing();
        }
    }

    @Override
    protected void updateImage(long gameTime) {
        if (dying) {
            List<ImageIcon> dieFrames = animationSet.framesFor(BossAction.DIE);
            if (dieFrames.isEmpty()) {
                this.setLive(false);
                return;
            }
            if (deathStartTime < 0L) {
                deathStartTime = gameTime;
                currentAction = BossAction.DIE;
                actionStartTime = gameTime;
            }
            ImageIcon frame = resolveCurrentFrame(gameTime);
            if (frame != null) {
                currentFrame = frame;
                this.setIcon(currentFrame);
            }
            long elapsed = Math.max(0L, gameTime - deathStartTime);
            if (elapsed >= (long) dieFrames.size() * FRAME_GAP) {
                this.setLive(false);
            }
            return;
        }
        updateActionState(gameTime);
        ImageIcon frame = resolveCurrentFrame(gameTime);
        if (frame != null) {
            currentFrame = frame;
            this.setIcon(currentFrame);
        }
    }

    @Override
    protected void add(long gameTime) {
        if (dying || !entered || !usesActionDrivenAttack()) {
            return;
        }
        handleActionDrivenAttack(gameTime);
    }

    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        if (split.length > 2) {
            this.hp = Integer.parseInt(split[2]);
            this.maxHp = this.hp;
        }
        applyVariant(split.length > 3 ? split[3] : VARIANT_BOSS1);
        applyHitboxForVariant();
        patrolSpeed = resolveMoveSpeed();
        entered = false;
        patrolRight = false;
        countedKill = false;
        dying = false;
        deathStartTime = -1L;
        currentAction = BossAction.MOVE;
        actionStartTime = 0L;
        nextAttackTime = 0L;
        volleyFired = false;
        currentFrame = firstFrame(animationSet.framesFor(BossAction.MOVE));
        if (currentFrame != null) {
            this.setIcon(currentFrame);
        }
        return this;
    }

    @Override
    public void die() {
        AudioPlayer.playOnce("music/die.wav");
        ImageIcon corpseFrame = currentFrame == null ? lastFrame(animationSet.framesFor(BossAction.DIE)) : currentFrame;
        ElementObj corpse = new Corpse().configure(
                this.getX(), this.getY(), this.getW(), this.getH(),
                Collections.emptyList(), corpseFrame,
                faceRight, animationSet.sourceFacesRight, resolveRenderScale(), FRAME_GAP);
        em.addElement(corpse, GameElement.CORPSE);
        ElementObj effect = new Effect().createElement((this.getX() - 40) + "," + (this.getY() - 24) + ",effect,180,180");
        em.addElement(effect, GameElement.DIE);
    }

    public boolean hurt(int damage) {
        if (!this.isLive() || dying) {
            return false;
        }
        hp -= damage;
        if (hp > 0) {
            return false;
        }
        hp = 0;
        if (!countedKill) {
            countedKill = true;
            GameRuntime.killCount++;
        }
        startDying();
        return true;
    }

    public boolean isDying() {
        return dying;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    private void spawnBullet(int vy, int damage) {
        ElementObj bulletTemplate = GameLoad.getObj("ebullet");
        if (bulletTemplate == null) {
            return;
        }
        boolean firingRight = resolveFacingToNearestPlayer();
        faceRight = firingRight;
        int bulletW = 16;
        ImageIcon bulletIcon = GameLoad.getImage(BOSS_BULLET);
        if (bulletIcon != null && bulletIcon.getIconWidth() > 0) {
            bulletW = bulletIcon.getIconWidth();
        }
        int bulletX = firingRight ? (this.getX() + this.getW() - 6) : (this.getX() - bulletW + 6);
        int bulletY = this.getY() + 36;
        int vx = firingRight ? 10 : -10;
        ElementObj bullet = bulletTemplate.createElement(
                bulletX + "," + bulletY + "," + BOSS_BULLET + "," + vx + "," + vy + "," + damage + ",0.0,0,120");
        em.addElement(bullet, GameElement.ENEMYFILE);
    }

    private void updateActionState(long gameTime) {
        if (dying) {
            currentAction = BossAction.DIE;
            return;
        }
        if (!usesActionDrivenAttack()) {
            currentAction = BossAction.MOVE;
            return;
        }
        if (!entered) {
            if (currentAction != BossAction.MOVE) {
                setAction(BossAction.MOVE, gameTime);
            }
            return;
        }

        long elapsed = gameTime - actionStartTime;
        switch (currentAction) {
            case MOVE:
                if (shouldStartAttack(gameTime)) {
                    setAction(BossAction.ATTACK, gameTime);
                }
                break;
            case ATTACK:
                if (elapsed >= resolveAttackDuration()) {
                    BossAction followUpAction = resolvePostAttackAction();
                    if (followUpAction == BossAction.RELOAD || followUpAction == BossAction.CROUCH) {
                        setAction(followUpAction, gameTime);
                    } else {
                        setAction(BossAction.MOVE, gameTime);
                        nextAttackTime = gameTime + resolveAttackCooldown();
                    }
                }
                break;
            case RELOAD:
                if (elapsed >= resolveReloadDuration()) {
                    setAction(BossAction.MOVE, gameTime);
                    nextAttackTime = gameTime + resolveAttackCooldown();
                }
                break;
            case CROUCH:
                if (elapsed >= resolveCrouchDuration()) {
                    setAction(BossAction.MOVE, gameTime);
                    nextAttackTime = gameTime + resolveAttackCooldown();
                }
                break;
            default:
                setAction(BossAction.MOVE, gameTime);
                break;
        }
    }

    private void handleActionDrivenAttack(long gameTime) {
        if (currentAction != BossAction.ATTACK) {
            return;
        }
        if (volleyFired || gameTime - actionStartTime < resolveAttackWindup()) {
            return;
        }
        volleyFired = true;
        if (isBoss3()) {
            performBoss3MeleeAttack(gameTime);
        } else {
            fireStandardVolley();
        }
    }

    private void fireStandardVolley() {
        updateFacing();
        spawnBullet(-2, 2);
        spawnBullet(0, 2);
        spawnBullet(2, 2);
    }

    private void updateFacing() {
        ElementObj nearest = findNearestLivePlayer();
        if (nearest == null) {
            return;
        }
        faceRight = nearest.getCenterX() >= this.getCenterX();
    }

    private int resolveArenaMinX() {
        return isBoss3() ? 48 : GameJFrame.GameX - 430;
    }

    private int resolveArenaMaxX() {
        return isBoss3() ? GameJFrame.GameX - this.getW() - 48 : GameJFrame.GameX - 170;
    }

    private int resolvePatrolMove(int x, int minX, int maxX) {
        x += patrolRight ? patrolSpeed : -patrolSpeed;
        if (x <= minX) {
            patrolRight = true;
            return minX;
        }
        if (x >= maxX) {
            patrolRight = false;
            return maxX;
        }
        return x;
    }

    private int resolveBoss3Move(int x, int minX, int maxX) {
        ElementObj nearest = findNearestLivePlayer();
        if (nearest == null) {
            return resolvePatrolMove(x, minX, maxX);
        }
        int dx = nearest.getCenterX() - (x + this.getW() / 2);
        if (Math.abs(dx) <= Math.max(24, BOSS3_ATTACK_RANGE - 12)) {
            return x;
        }
        int desiredX = x + (dx > 0 ? patrolSpeed : -patrolSpeed);
        return Math.max(minX, Math.min(maxX, desiredX));
    }

    private double resolveRenderScale() {
        return isBoss2() ? BOSS2_RENDER_SCALE : 1.0;
    }

    private void applyHitboxForVariant() {
        double scale = isBoss2() ? BOSS2_HITBOX_SCALE : 1.0;
        this.setW(Math.max(96, (int) Math.round(HITBOX_W * scale)));
        this.setH(Math.max(90, (int) Math.round(HITBOX_H * scale)));
    }

    private int resolveMoveSpeed() {
        return isBoss3() ? BOSS3_MOVE_SPEED : 2;
    }

    private boolean usesActionDrivenAttack() {
        return animationSet.hasFrames(BossAction.ATTACK);
    }

    private boolean shouldStartAttack(long gameTime) {
        if (gameTime < nextAttackTime) {
            return false;
        }
        if (!isBoss3()) {
            return true;
        }
        ElementObj nearest = findNearestLivePlayer();
        if (nearest == null) {
            return false;
        }
        return Math.abs(nearest.getCenterX() - this.getCenterX()) <= BOSS3_ATTACK_RANGE;
    }

    private long resolveAttackWindup() {
        if (isBoss1()) {
            return BOSS1_ATTACK_WINDUP;
        }
        return isBoss2() ? BOSS2_ATTACK_WINDUP : BOSS3_ATTACK_WINDUP;
    }

    private long resolveAttackDuration() {
        long baseDuration;
        if (isBoss1()) {
            baseDuration = BOSS1_ATTACK_DURATION;
        } else if (isBoss2()) {
            baseDuration = BOSS2_ATTACK_DURATION;
        } else {
            baseDuration = BOSS3_ATTACK_DURATION;
        }
        List<ImageIcon> attackFrames = animationSet.framesFor(BossAction.ATTACK);
        long minimumDuration = (long) Math.max(0, attackFrames.size() - 1) * FRAME_GAP;
        return Math.max(baseDuration, minimumDuration);
    }

    private long resolveAttackCooldown() {
        if (isBoss1()) {
            return BOSS1_ATTACK_COOLDOWN;
        }
        return isBoss2() ? BOSS2_ATTACK_COOLDOWN : BOSS3_ATTACK_COOLDOWN;
    }

    private long resolveReloadDuration() {
        return BOSS2_RELOAD_DURATION;
    }

    private long resolveCrouchDuration() {
        return BOSS2_CROUCH_DURATION;
    }

    private BossAction resolvePostAttackAction() {
        if (!isBoss2()) {
            return BossAction.MOVE;
        }
        boolean canReload = animationSet.hasFrames(BossAction.RELOAD);
        boolean canCrouch = animationSet.hasFrames(BossAction.CROUCH);
        if (!canReload && !canCrouch) {
            return BossAction.MOVE;
        }
        int roll = random.nextInt(100);
        if (canReload && canCrouch) {
            if (roll < 40) {
                return BossAction.RELOAD;
            }
            if (roll < 70) {
                return BossAction.CROUCH;
            }
            return BossAction.MOVE;
        }
        if (canReload) {
            return roll < 60 ? BossAction.RELOAD : BossAction.MOVE;
        }
        return roll < 50 ? BossAction.CROUCH : BossAction.MOVE;
    }

    private boolean resolveFacingToNearestPlayer() {
        ElementObj nearest = findNearestLivePlayer();
        if (nearest == null) {
            return faceRight;
        }
        return nearest.getCenterX() >= this.getCenterX();
    }

    private void performBoss3MeleeAttack(long gameTime) {
        ElementObj playerObj = findNearestLivePlayer();
        if (!(playerObj instanceof PaoPao)) {
            return;
        }
        if (!resolveBoss3MeleeHitbox().intersects(playerObj.getRectangle())) {
            return;
        }
        PaoPao player = (PaoPao) playerObj;
        long oldHurtTime = player.getHurtTime();
        player.hurt(gameTime, BOSS3_MELEE_DAMAGE);
        if (player.getHurtTime() != oldHurtTime && player.getHp() > 0) {
            AudioPlayer.playOnce("music/die.wav");
        }
    }

    private Rectangle resolveBoss3MeleeHitbox() {
        int hitX = faceRight ? this.getCenterX() - 6 : this.getCenterX() - BOSS3_MELEE_REACH + 6;
        int hitY = this.getY() - 6;
        return new Rectangle(hitX, hitY, BOSS3_MELEE_REACH, this.getH() + 14);
    }

    private ElementObj findNearestLivePlayer() {
        List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        if (plays == null || plays.isEmpty()) {
            return null;
        }
        ElementObj nearest = null;
        int bestDx = Integer.MAX_VALUE;
        int bossX = this.getCenterX();
        for (ElementObj obj : plays) {
            if (obj == null || !obj.isLive()) {
                continue;
            }
            int dx = Math.abs(obj.getCenterX() - bossX);
            if (dx < bestDx) {
                bestDx = dx;
                nearest = obj;
            }
        }
        return nearest;
    }

    private ImageIcon resolveCurrentFrame(long gameTime) {
        List<ImageIcon> frames = animationSet.framesFor(currentAction);
        if (frames.isEmpty()) {
            frames = animationSet.framesFor(BossAction.MOVE);
        }
        if (frames.isEmpty()) {
            return null;
        }
        long elapsed = Math.max(0L, gameTime - actionStartTime);
        if (currentAction == BossAction.MOVE) {
            long timeline = usesActionDrivenAttack() ? elapsed : gameTime;
            int index = (int) ((timeline / FRAME_GAP) % frames.size());
            return frames.get(index);
        }
        int index = (int) Math.min(frames.size() - 1, elapsed / FRAME_GAP);
        return frames.get(index);
    }

    private void setAction(BossAction action, long gameTime) {
        if (dying || action == null || currentAction == action) {
            return;
        }
        currentAction = action;
        actionStartTime = gameTime;
        if (action == BossAction.ATTACK) {
            volleyFired = false;
            updateFacing();
        }
    }

    private void startDying() {
        List<ImageIcon> dieFrames = animationSet.framesFor(BossAction.DIE);
        if (dieFrames.isEmpty()) {
            this.setLive(false);
            return;
        }
        dying = true;
        deathStartTime = -1L;
        currentAction = BossAction.DIE;
        volleyFired = true;
    }

    private void applyVariant(String rawVariant) {
        String normalized = normalizeVariant(rawVariant);
        variantKey = normalized;
        if (VARIANT_BOSS2.equals(normalized)) {
            animationSet = BOSS2_ANIMATIONS;
        } else if (VARIANT_BOSS3.equals(normalized)) {
            animationSet = BOSS3_ANIMATIONS;
        } else {
            animationSet = BOSS1_ANIMATIONS;
        }
    }

    private boolean isBoss1() {
        return VARIANT_BOSS1.equals(variantKey);
    }

    private boolean isBoss2() {
        return VARIANT_BOSS2.equals(variantKey);
    }

    private boolean isBoss3() {
        return VARIANT_BOSS3.equals(variantKey);
    }

    private static String normalizeVariant(String rawVariant) {
        if (rawVariant == null) {
            return VARIANT_BOSS1;
        }
        String normalized = rawVariant.trim().toLowerCase(Locale.ROOT);
        if (VARIANT_BOSS2.equals(normalized)) {
            return VARIANT_BOSS2;
        }
        if (VARIANT_BOSS3.equals(normalized)) {
            return VARIANT_BOSS3;
        }
        return VARIANT_BOSS1;
    }

    private static ImageIcon firstFrame(List<ImageIcon> frames) {
        return frames == null || frames.isEmpty() ? null : frames.get(0);
    }

    private static ImageIcon lastFrame(List<ImageIcon> frames) {
        return frames == null || frames.isEmpty() ? null : frames.get(frames.size() - 1);
    }

    private static List<ImageIcon> loadLegacyBoss1Frames() {
        List<ImageIcon> frames = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            String inBoss1Dir = "image/images/boss/boss1/D_boss (" + i + ").png";
            String oldRootDir = "image/images/boss/D_boss (" + i + ").png";
            ImageIcon frame = GameLoad.getImage(inBoss1Dir);
            if (frame == null) {
                frame = GameLoad.getImage(oldRootDir);
            }
            if (frame != null) {
                frames.add(frame);
            }
        }
        return Collections.unmodifiableList(frames);
    }

    private enum BossAction {
        MOVE,
        ATTACK,
        RELOAD,
        CROUCH,
        DIE
    }

    private static final class BossAnimationSet {
        private final List<ImageIcon> moveFrames;
        private final List<ImageIcon> attackFrames;
        private final List<ImageIcon> reloadFrames;
        private final List<ImageIcon> crouchFrames;
        private final List<ImageIcon> dieFrames;
        private final boolean sourceFacesRight;

        private BossAnimationSet(List<ImageIcon> moveFrames,
                                 List<ImageIcon> attackFrames,
                                 List<ImageIcon> reloadFrames,
                                 List<ImageIcon> crouchFrames,
                                 List<ImageIcon> dieFrames,
                                 boolean sourceFacesRight) {
            this.moveFrames = freeze(moveFrames);
            this.attackFrames = freeze(attackFrames);
            this.reloadFrames = freeze(reloadFrames);
            this.crouchFrames = freeze(crouchFrames);
            this.dieFrames = freeze(dieFrames);
            this.sourceFacesRight = sourceFacesRight;
        }

        private List<ImageIcon> framesFor(BossAction action) {
            if (action == null) {
                return moveFrames;
            }
            switch (action) {
                case ATTACK:
                    return attackFrames;
                case RELOAD:
                    return reloadFrames;
                case CROUCH:
                    return crouchFrames;
                case DIE:
                    return dieFrames;
                case MOVE:
                default:
                    return moveFrames;
            }
        }

        private boolean hasFrames(BossAction action) {
            return !framesFor(action).isEmpty();
        }

        private static BossAnimationSet loadBoss1() {
            List<ImageIcon> moveFrames = sanitizeFrames(
                    GameLoad.loadFramesFromDirectory("image/images/boss/boss1/move"));
            List<ImageIcon> attackFrames = sanitizeFrames(
                    GameLoad.loadFramesFromDirectory("image/images/boss/boss1/attack"));
            List<ImageIcon> dieFrames = sanitizeFrames(
                    GameLoad.loadFramesFromDirectory("image/images/boss/boss1/die"));
            if (attackFrames.isEmpty()) {
                attackFrames = sanitizeFrames(loadLegacyBoss1Frames());
            }
            if (moveFrames.isEmpty()) {
                moveFrames = attackFrames;
            }
            return new BossAnimationSet(
                    moveFrames,
                    attackFrames,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    dieFrames,
                    false);
        }

        private static BossAnimationSet loadBoss2() {
            List<ImageIcon> moveFrames = sanitizeFrames(
                    GameLoad.loadFramesFromDirectory("image/images/boss/boss2/move"));
            if (moveFrames.isEmpty()) {
                moveFrames = BOSS1_ANIMATIONS.moveFrames;
            }
            return new BossAnimationSet(
                    moveFrames,
                    sanitizeFrames(GameLoad.loadFramesFromDirectory("image/images/boss/boss2/attack")),
                    sanitizeFrames(GameLoad.loadFramesFromDirectory("image/images/boss/boss2/\u6362\u5b50\u5f39")),
                    sanitizeFrames(GameLoad.loadFramesFromDirectory("image/images/boss/boss2/\u4e0b\u8e72")),
                    sanitizeFrames(GameLoad.loadFramesFromDirectory("image/images/boss/boss2/\u6b7b\u4ea1\u52a8\u4f5c")),
                    true);
        }

        private static BossAnimationSet loadBoss3() {
            List<ImageIcon> moveFrames = sanitizeFrames(
                    GameLoad.loadFramesFromDirectory("image/images/boss/boss3/walking"));
            if (moveFrames.isEmpty()) {
                moveFrames = BOSS1_ANIMATIONS.moveFrames;
            }
            return new BossAnimationSet(
                    moveFrames,
                    sanitizeFrames(GameLoad.loadFramesFromDirectory("image/images/boss/boss3/attack")),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    sanitizeFrames(GameLoad.loadFramesFromDirectory("image/images/boss/boss3/die")),
                    false);
        }

        private static List<ImageIcon> freeze(List<ImageIcon> frames) {
            if (frames == null || frames.isEmpty()) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(new ArrayList<>(frames));
        }

        private static List<ImageIcon> sanitizeFrames(List<ImageIcon> frames) {
            if (frames == null || frames.isEmpty()) {
                return Collections.emptyList();
            }
            List<ImageIcon> filtered = filterOversizedFrames(frames);
            List<ImageIcon> sanitized = new ArrayList<>(filtered.size());
            for (ImageIcon frame : filtered) {
                sanitized.add(stripWhiteMatte(frame));
            }
            return sanitized;
        }

        private static List<ImageIcon> filterOversizedFrames(List<ImageIcon> frames) {
            if (frames == null || frames.size() < 3) {
                return frames == null ? Collections.emptyList() : frames;
            }
            List<Integer> widths = new ArrayList<>(frames.size());
            List<Integer> heights = new ArrayList<>(frames.size());
            for (ImageIcon frame : frames) {
                if (frame == null || frame.getIconWidth() <= 0 || frame.getIconHeight() <= 0) {
                    continue;
                }
                widths.add(frame.getIconWidth());
                heights.add(frame.getIconHeight());
            }
            if (widths.size() < 3 || heights.size() < 3) {
                return frames;
            }
            Collections.sort(widths);
            Collections.sort(heights);
            int medianWidth = widths.get(widths.size() / 2);
            int medianHeight = heights.get(heights.size() / 2);
            int maxWidth = Math.max(480, medianWidth * 3);
            int maxHeight = Math.max(480, medianHeight * 3);

            List<ImageIcon> filtered = new ArrayList<>(frames.size());
            for (ImageIcon frame : frames) {
                if (frame == null) {
                    continue;
                }
                if (frame.getIconWidth() > maxWidth || frame.getIconHeight() > maxHeight) {
                    continue;
                }
                filtered.add(frame);
            }
            return filtered.isEmpty() ? frames : filtered;
        }

        private static ImageIcon stripWhiteMatte(ImageIcon icon) {
            if (icon == null) {
                return null;
            }
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            if (width <= 0 || height <= 0) {
                return icon;
            }
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.drawImage(icon.getImage(), 0, 0, null);
            g2.dispose();
            if (!shouldStripMatte(image)) {
                return icon;
            }

            boolean[] visited = new boolean[width * height];
            Deque<Integer> queue = new ArrayDeque<>();
            for (int x = 0; x < width; x++) {
                enqueueIfMatte(image, x, 0, visited, queue);
                enqueueIfMatte(image, x, height - 1, visited, queue);
            }
            for (int y = 1; y < height - 1; y++) {
                enqueueIfMatte(image, 0, y, visited, queue);
                enqueueIfMatte(image, width - 1, y, visited, queue);
            }

            while (!queue.isEmpty()) {
                int index = queue.removeFirst();
                int x = index % width;
                int y = index / width;
                int argb = image.getRGB(x, y);
                image.setRGB(x, y, argb & 0x00FFFFFF);

                if (x > 0) {
                    enqueueIfMatte(image, x - 1, y, visited, queue);
                }
                if (x + 1 < width) {
                    enqueueIfMatte(image, x + 1, y, visited, queue);
                }
                if (y > 0) {
                    enqueueIfMatte(image, x, y - 1, visited, queue);
                }
                if (y + 1 < height) {
                    enqueueIfMatte(image, x, y + 1, visited, queue);
                }
            }
            return new ImageIcon(image);
        }

        private static void enqueueIfMatte(BufferedImage image, int x, int y,
                                           boolean[] visited, Deque<Integer> queue) {
            int width = image.getWidth();
            int index = y * width + x;
            if (visited[index]) {
                return;
            }
            visited[index] = true;
            if (isMattePixel(image.getRGB(x, y))) {
                queue.addLast(index);
            }
        }

        private static boolean shouldStripMatte(BufferedImage image) {
            int width = image.getWidth();
            int height = image.getHeight();
            return isMattePixel(image.getRGB(0, 0))
                    && isMattePixel(image.getRGB(width - 1, 0))
                    && isMattePixel(image.getRGB(0, height - 1))
                    && isMattePixel(image.getRGB(width - 1, height - 1));
        }

        private static boolean isMattePixel(int argb) {
            int alpha = (argb >>> 24) & 0xFF;
            if (alpha < 250) {
                return false;
            }
            int red = (argb >>> 16) & 0xFF;
            int green = (argb >>> 8) & 0xFF;
            int blue = argb & 0xFF;
            return red >= 245 && green >= 245 && blue >= 245;
        }
    }
}
