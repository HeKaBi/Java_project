package com.tedu.manager;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

public class AudioPlayer {
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
}
