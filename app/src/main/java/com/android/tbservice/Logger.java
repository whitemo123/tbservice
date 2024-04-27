package com.android.tbservice;

import java.util.ArrayList;
import java.util.List;

public class Logger {
    private static List<OnLogUpdateListener> listeners = new ArrayList<>();

    public static void addLog(String logMessage) {
        for (OnLogUpdateListener listener : listeners) {
            listener.onLogAdded(logMessage);
        }
    }

    public interface OnLogUpdateListener {
        void onLogAdded(String log);
    }

    public static int registerListener(OnLogUpdateListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
            return listeners.size() - 1;
        }
    }

    public static void unregisterListener(int index) {
        synchronized (listeners) {
            listeners.remove(index);
        }
    }
}
