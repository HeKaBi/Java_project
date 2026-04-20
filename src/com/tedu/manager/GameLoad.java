package com.tedu.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;

public class GameLoad {
    private static final ElementManager elementManager = ElementManager.getManager();
    private static final Map<String, ImageIcon> imgMap = new HashMap<>();
    private static final Map<String, Class<?>> objMap = new HashMap<>();
    private static final Properties properties = new Properties();

    private GameLoad() {
    }

    public static void loadImg() {
        imgMap.clear();
        loadProperties("com/tedu/text/GameData.pro");
        Set<Object> keys = properties.keySet();
        for (Object keyObj : keys) {
            String key = keyObj.toString();
            String path = properties.getProperty(key);
            imgMap.put(key, new ImageIcon(path));
        }
    }

    public static void loadObj() {
        objMap.clear();
        loadProperties("com/tedu/text/obj.pro");
        for (Object keyObj : properties.keySet()) {
            String key = keyObj.toString();
            String className = properties.getProperty(key);
            try {
                objMap.put(key, Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load class: " + className, e);
            }
        }
    }

    public static void loadStage(int stageId) {
        loadProperties("com/tedu/text/" + stageId + ".map");
        for (Object keyObj : properties.keySet()) {
            String objKey = keyObj.toString();
            String[] entries = properties.getProperty(objKey).split(";");
            for (String entry : entries) {
                ElementObj obj = getObj(objKey);
                ElementObj element = obj.createElement(entry.trim());
                elementManager.addElement(element, element.getGameElement());
            }
        }
    }

    public static ElementObj getObj(String key) {
        Class<?> clazz = objMap.get(key);
        if (clazz == null) {
            throw new IllegalArgumentException("Object key not found: " + key);
        }
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (instance instanceof ElementObj elementObj) {
                return elementObj;
            }
            throw new IllegalStateException("Class is not ElementObj: " + clazz.getName());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create instance: " + clazz.getName(), e);
        }
    }

    public static ImageIcon getImg(String key) {
        ImageIcon icon = imgMap.get(key);
        if (icon == null) {
            throw new IllegalArgumentException("Image key not found: " + key);
        }
        return icon;
    }

    public static List<ImageIcon> getAnimation(String prefix) {
        List<Map.Entry<String, ImageIcon>> matches = new ArrayList<>();
        for (Map.Entry<String, ImageIcon> entry : imgMap.entrySet()) {
            if (entry.getKey().startsWith(prefix + "_")) {
                matches.add(entry);
            }
        }
        matches.sort(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().substring(prefix.length() + 1))));
        List<ImageIcon> frames = new ArrayList<>();
        for (Map.Entry<String, ImageIcon> entry : matches) {
            frames.add(entry.getValue());
        }
        return frames;
    }

    private static void loadProperties(String resourcePath) {
        properties.clear();
        try (InputStream inputStream = GameLoad.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Cannot read resource: " + resourcePath);
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load properties: " + resourcePath, e);
        }
    }
}
