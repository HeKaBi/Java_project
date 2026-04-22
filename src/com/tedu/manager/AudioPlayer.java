package com.tedu.manager;

import java.io.File;
import java.util.Locale;
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
            File file = GameLoad.resolveResourceFile(filePath);
            if (file == null || !file.exists()) {
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
        File file = GameLoad.resolveResourceFile(filePath);
        if (file == null || !file.exists()) {
            return;
        }
        if (tryPlayClipOnce(file)) {
            return;
        }
        if (isMp3(file) && tryPlayMp3WithWindowsMedia(file)) {
            return;
        }
        System.out.println("SFX play failed: unsupported format " + filePath);
    }

    private static boolean tryPlayClipOnce(File file) {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            applyGain(clip, SFX_GAIN_DB);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isMp3(File file) {
        return file != null && file.getName().toLowerCase(Locale.ROOT).endsWith(".mp3");
    }

    private static boolean tryPlayMp3WithWindowsMedia(File file) {
        if (file == null || !isWindows()) {
            return false;
        }
        try {
            String uri = file.toURI().toString().replace("'", "''");
            String script = "$ErrorActionPreference='Stop';"
                    + "Add-Type -AssemblyName PresentationCore;"
                    + "$player=New-Object System.Windows.Media.MediaPlayer;"
                    + "$player.Open([Uri]'" + uri + "');"
                    + "$player.Play();"
                    + "$deadline=[DateTime]::UtcNow.AddSeconds(5);"
                    + "while(-not $player.NaturalDuration.HasTimeSpan -and [DateTime]::UtcNow -lt $deadline){Start-Sleep -Milliseconds 50};"
                    + "$waitMs=1200;"
                    + "if($player.NaturalDuration.HasTimeSpan){$waitMs=[Math]::Ceiling($player.NaturalDuration.TimeSpan.TotalMilliseconds)+150};"
                    + "Start-Sleep -Milliseconds $waitMs;"
                    + "$player.Close();";
            new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-NonInteractive",
                    "-WindowStyle",
                    "Hidden",
                    "-Command",
                    script)
                    .start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
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
