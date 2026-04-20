package com.tedu.manager;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AudioPlayer {
    private static Clip bgmClip;

    private AudioPlayer() {
    }

    public static void playBgmLoop(String filePath) {
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
            System.out.println("BGM播放失败:" + e.getMessage());
        }
    }

    public static void stopBgm() {
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
            clip.start();
        } catch (Exception e) {
            System.out.println("音效播放失败:" + e.getMessage());
        }
    }
}
