package com.tedu.manager;

import com.tedu.element.ElementObj;
import com.tedu.show.GameJFrame;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class GameLoad {
    private static final ElementManager em = ElementManager.getManager();
    private static final String GAME_DATA_PATH = "com/tedu/text/GameData.pro";
    private static final String OBJ_DATA_PATH = "com/tedu/text/obj.pro";
    private static final int PLAYER_SPAWN_X = 300;
    private static final Map<String, Class<?>> objMap = new HashMap<>();
    private static final Map<String, List<ImageIcon>> frameCache = new HashMap<>();

    public static Map<String, ImageIcon> imgMap = new HashMap<>();
    public static Map<String, List<ImageIcon>> imgMaps;

    private GameLoad() {
    }

    public static void MapLoad(int mapId) {
        String mapName = "com/tedu/text/" + mapId + ".map";
        Properties mapPro = loadProperties(mapName);
        if (mapPro.isEmpty()) {
            System.out.println("Map config read failed.");
            return;
        }
        Enumeration<?> names = mapPro.propertyNames();
        while (names.hasMoreElements()) {
            String key = names.nextElement().toString();
            String value = mapPro.getProperty(key);
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            String[] arrs = value.split(";");
            for (String arr : arrs) {
                ElementObj obj = getObj("map");
                if (obj == null) {
                    continue;
                }
                ElementObj element = obj.createElement(key + "," + arr);
                em.addElement(element, GameElement.MAPS);
            }
        }
    }

    public static void loadImg() {
        Properties imgPro = loadProperties(GAME_DATA_PATH);
        imgMap.clear();
        Set<Object> set = imgPro.keySet();
        for (Object o : set) {
            String key = o.toString();
            String url = imgPro.getProperty(key);
            ImageIcon icon = loadIcon(url);
            if (icon != null) {
                imgMap.put(key, icon);
            } else {
                System.out.println("Image load failed: " + key + " -> " + url);
            }
        }
    }

    public static ImageIcon getImage(String keyOrPath) {
        if (keyOrPath == null) {
            return null;
        }
        ImageIcon icon = imgMap.get(keyOrPath);
        if (icon != null) {
            return icon;
        }
        return loadIcon(keyOrPath);
    }

    public static ImageIcon loadImage(String resourcePath) {
        return loadIcon(resourcePath);
    }

    public static List<ImageIcon> loadFrames(String... resourcePaths) {
        if (resourcePaths == null || resourcePaths.length == 0) {
            return Collections.emptyList();
        }
        String cacheKey = String.join("|", resourcePaths);
        List<ImageIcon> cached = frameCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<ImageIcon> frames = new ArrayList<>();
        for (String resourcePath : resourcePaths) {
            ImageIcon icon = getImage(resourcePath);
            if (icon != null) {
                frames.add(icon);
            }
        }
        List<ImageIcon> immutable = Collections.unmodifiableList(frames);
        frameCache.put(cacheKey, immutable);
        return immutable;
    }

    public static List<ImageIcon> loadFramesFromDirectory(String directoryPath) {
        if (directoryPath == null) {
            return Collections.emptyList();
        }
        String normalizedPath = directoryPath.trim().replace('\\', '/');
        if (normalizedPath.isEmpty()) {
            return Collections.emptyList();
        }
        String cacheKey = "dir:" + normalizedPath;
        List<ImageIcon> cached = frameCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        File directory = resolveFile(normalizedPath);
        if (directory == null || !directory.isDirectory()) {
            return Collections.emptyList();
        }
        File[] files = directory.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        Arrays.sort(files, (left, right) -> {
            int leftNum = extractTrailingNumber(left.getName());
            int rightNum = extractTrailingNumber(right.getName());
            if (leftNum != rightNum) {
                return Integer.compare(leftNum, rightNum);
            }
            return left.getName().compareToIgnoreCase(right.getName());
        });
        List<ImageIcon> frames = new ArrayList<>();
        for (File file : files) {
            ImageIcon icon = getImage(normalizedPath + "/" + file.getName());
            if (icon != null) {
                frames.add(icon);
            }
        }
        List<ImageIcon> immutable = Collections.unmodifiableList(frames);
        frameCache.put(cacheKey, immutable);
        return immutable;
    }

    public static int resolvePlayerSpawnX(int playerWidth) {
        int width = Math.max(0, playerWidth);
        return Math.max(0, Math.min(PLAYER_SPAWN_X, GameJFrame.GameX - width));
    }

    public static void loadPlay() {
        loadObj();
        String playStr = "0,0,paopao";
        ElementObj obj = getObj("paopao");
        if (obj == null) {
            return;
        }
        ElementObj play = obj.createElement(playStr);
        if (play == null) {
            return;
        }
        int spawnX = resolvePlayerSpawnX(play.getW());
        play.setX(spawnX);
        int groundY = GameRuntime.getBattlefieldMaxBottomAt(spawnX + play.getW() / 2) - play.getH();
        play.setY(Math.max(0, groundY));
        em.addElement(play, GameElement.PLAY);
    }

    public static ElementObj getObj(String str) {
        try {
            Class<?> clazz = objMap.get(str);
            if (clazz == null) {
                return null;
            }
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (instance instanceof ElementObj) {
                return (ElementObj) instance;
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadObj() {
        Properties objPro = loadProperties(OBJ_DATA_PATH);
        objMap.clear();
        Set<Object> set = objPro.keySet();
        for (Object o : set) {
            String classUrl = objPro.getProperty(o.toString());
            try {
                Class<?> forName = Class.forName(classUrl);
                objMap.put(o.toString(), forName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static Properties loadProperties(String resourcePath) {
        Properties properties = new Properties();
        try {
            InputStream input = openStream(resourcePath);
            if (input == null) {
                System.out.println("Config read failed: " + resourcePath);
                return properties;
            }
            try (InputStream in = input;
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static ImageIcon loadIcon(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }
        String path = resourcePath.trim();
        if (path.isEmpty()) {
            return null;
        }
        try (InputStream stream = openStream(path)) {
            if (stream != null) {
                java.awt.image.BufferedImage image = ImageIO.read(stream);
                if (image != null) {
                    return new ImageIcon(image);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            java.awt.image.BufferedImage fileImage = ImageIO.read(new File(path));
            if (fileImage != null) {
                return new ImageIcon(fileImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static InputStream openStream(String resourcePath) throws IOException {
        ClassLoader classLoader = GameLoad.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(resourcePath);
        if (stream != null) {
            return stream;
        }
        File file = resolveFile(resourcePath);
        if (file != null && file.isFile()) {
            return new FileInputStream(file);
        }
        return null;
    }

    private static File resolveFile(String resourcePath) {
        File file = new File(resourcePath);
        if (file.exists()) {
            return file;
        }
        File sourceFile = new File("src", resourcePath);
        if (sourceFile.exists()) {
            return sourceFile;
        }
        return null;
    }

    private static int extractTrailingNumber(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        int end = dotIndex >= 0 ? dotIndex : fileName.length();
        int index = end - 1;
        while (index >= 0 && !Character.isDigit(fileName.charAt(index))) {
            index--;
        }
        if (index < 0) {
            return Integer.MAX_VALUE;
        }
        int numberEnd = index + 1;
        while (index >= 0 && Character.isDigit(fileName.charAt(index))) {
            index--;
        }
        int numberStart = index + 1;
        try {
            return Integer.parseInt(fileName.substring(numberStart, numberEnd));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static byte[] readBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = stream.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        return out.toByteArray();
    }
}
