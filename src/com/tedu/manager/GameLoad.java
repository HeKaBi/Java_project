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
import java.util.LinkedHashSet;
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
    private static final List<File> SEARCH_ROOTS = Collections.unmodifiableList(new ArrayList<>(discoverSearchRoots()));

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

    public static File resolveResourceFile(String resourcePath) {
        return resolveFile(resourcePath);
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
        File file = resolveFile(path);
        if (file != null && file.isFile()) {
            try {
                java.awt.image.BufferedImage fileImage = ImageIO.read(file);
                if (fileImage != null) {
                    return new ImageIcon(fileImage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static InputStream openStream(String resourcePath) throws IOException {
        ClassLoader classLoader = GameLoad.class.getClassLoader();
        String classpathPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        InputStream stream = classLoader.getResourceAsStream(classpathPath);
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
        if (resourcePath == null) {
            return null;
        }
        String normalizedPath = resourcePath.trim()
                .replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);
        if (normalizedPath.isEmpty()) {
            return null;
        }
        File directFile = new File(normalizedPath);
        if (directFile.exists()) {
            return directFile;
        }
        boolean alreadyStartsWithSrc = normalizedPath.equals("src")
                || normalizedPath.startsWith("src" + File.separator);
        for (File searchRoot : SEARCH_ROOTS) {
            File candidate = new File(searchRoot, normalizedPath);
            if (candidate.exists()) {
                return candidate;
            }
            if (!alreadyStartsWithSrc) {
                File sourceCandidate = new File(new File(searchRoot, "src"), normalizedPath);
                if (sourceCandidate.exists()) {
                    return sourceCandidate;
                }
            }
        }
        return null;
    }

    private static Set<File> discoverSearchRoots() {
        LinkedHashSet<File> roots = new LinkedHashSet<>();
        addSearchRoots(new File(System.getProperty("user.dir", ".")), roots);
        addNearbyProjectRoots(new File(System.getProperty("user.dir", ".")), roots);
        addSearchRoots(resolveCodeSourceDirectory(), roots);
        addNearbyProjectRoots(resolveCodeSourceDirectory(), roots);
        for (File classPathEntry : resolveClassPathEntries()) {
            addSearchRoots(classPathEntry, roots);
            addNearbyProjectRoots(classPathEntry, roots);
        }
        return roots;
    }

    private static void addSearchRoots(File start, Set<File> roots) {
        File current = normalizeFile(start);
        while (current != null) {
            roots.add(current);
            current = current.getParentFile();
        }
    }

    private static void addNearbyProjectRoots(File start, Set<File> roots) {
        File current = normalizeFile(start);
        for (int depth = 0; current != null && depth < 3; depth++) {
            File[] children = current.listFiles(File::isDirectory);
            if (children != null) {
                for (File child : children) {
                    File normalizedChild = normalizeFile(child);
                    if (looksLikeProjectRoot(normalizedChild)) {
                        roots.add(normalizedChild);
                    }
                }
            }
            current = current.getParentFile();
        }
    }

    private static File resolveCodeSourceDirectory() {
        try {
            java.net.URL location = GameLoad.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return null;
            }
            File file = new File(location.toURI());
            return file.isFile() ? file.getParentFile() : file;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<File> resolveClassPathEntries() {
        String classPath = System.getProperty("java.class.path", "");
        if (classPath == null || classPath.isEmpty()) {
            return Collections.emptyList();
        }
        String[] entries = classPath.split(java.io.File.pathSeparator);
        List<File> files = new ArrayList<>(entries.length);
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            files.add(new File(entry));
        }
        return files;
    }

    private static boolean looksLikeProjectRoot(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return false;
        }
        File srcDir = new File(directory, "src");
        if (!srcDir.isDirectory()) {
            return false;
        }
        File imageDir = new File(directory, "image");
        File musicDir = new File(directory, "music");
        File ideaDir = new File(directory, ".idea");
        File gameData = new File(directory, "src" + File.separator + "com"
                + File.separator + "tedu" + File.separator + "text" + File.separator + "GameData.pro");
        File[] moduleFiles = directory.listFiles(file -> file.isFile() && file.getName().endsWith(".iml"));
        return imageDir.isDirectory()
                || musicDir.isDirectory()
                || ideaDir.isDirectory()
                || gameData.isFile()
                || (moduleFiles != null && moduleFiles.length > 0);
    }

    private static File normalizeFile(File file) {
        if (file == null) {
            return null;
        }
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return file.getAbsoluteFile();
        }
    }

    private static int extractTrailingNumber(String fileName) {
        String normalizedName = stripCopySuffix(fileName);
        int dotIndex = normalizedName.lastIndexOf('.');
        int end = dotIndex >= 0 ? dotIndex : normalizedName.length();
        int index = end - 1;
        while (index >= 0 && !Character.isDigit(normalizedName.charAt(index))) {
            index--;
        }
        if (index < 0) {
            return Integer.MAX_VALUE;
        }
        int numberEnd = index + 1;
        while (index >= 0 && Character.isDigit(normalizedName.charAt(index))) {
            index--;
        }
        int numberStart = index + 1;
        try {
            return Integer.parseInt(normalizedName.substring(numberStart, numberEnd));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static String stripCopySuffix(String fileName) {
        if (fileName == null) {
            return "";
        }
        return fileName.replaceFirst("\\s*\\(\\d+\\)(?=\\.[^.]+$)", "");
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
