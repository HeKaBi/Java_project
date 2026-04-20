package com.tedu.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tedu.element.ElementObj;

public class ElementManager {
    private static ElementManager manager;
    private Map<GameElement, List<ElementObj>> gameElements;

    private ElementManager() {
        init();
    }

    public static synchronized ElementManager getManager() {
        if (manager == null) {
            manager = new ElementManager();
        }
        return manager;
    }

    public void init() {
        gameElements = new EnumMap<>(GameElement.class);
        for (GameElement element : GameElement.values()) {
            gameElements.put(element, new CopyOnWriteArrayList<>());
        }
    }

    public void reset() {
        init();
    }

    public Map<GameElement, List<ElementObj>> getGameElements() {
        return gameElements;
    }

    public void addElement(ElementObj obj, GameElement element) {
        gameElements.get(element).add(obj);
    }

    public List<ElementObj> getElementsByKey(GameElement element) {
        return gameElements.get(element);
    }
}
