package com.tedu.manager;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

public class AudioPlayer {
    private static final float BGM_GAIN_DB = -12.0f;
    private static final float SFX_GAIN_DB = -8.0f;
    private static Clip bgmClip;

    private AudioPlayer() {
    }

    public static synchronized void playBgmLoop(String filePath) {
        stopBgm();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioInputStream);
            applyGain(bgmClip, BGM_GAIN_DB);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) {
            System.out.println("BGM play failed: " + e.getMessage());
        }
    }

    public static synchronized void stopBgm() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    public static void playOnce(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            applyGain(clip, SFX_GAIN_DB);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        } catch (Exception e) {
            System.out.println("SFX play failed: " + e.getMessage());
        }
    }

    private static void applyGain(Clip clip, float gainDb) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float clamped = Math.max(control.getMinimum(), Math.min(control.getMaximum(), gainDb));
        control.setValue(clamped);
    }
}
