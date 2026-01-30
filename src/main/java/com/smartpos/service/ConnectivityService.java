package com.smartpos.service;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ConnectivityService {

    private final BooleanProperty online = new SimpleBooleanProperty(true);

    public BooleanProperty onlineProperty() {
        return online;
    }

    public boolean isOnline() {
        return online.get();
    }

    @Scheduled(fixedRate = 30000) // Check every 30 seconds
    public void checkConnectivity() {
        boolean currentStatus = testConnection();
        if (currentStatus != online.get()) {
            javafx.application.Platform.runLater(() -> online.set(currentStatus));
        }
    }

    private boolean testConnection() {
        try {
            // Try to connect to a reliable host (Google DNS or similar)
            URL url = new URL("https://www.google.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.connect();
            int responseCode = conn.getResponseCode();
            return (responseCode == 200);
        } catch (Exception e) {
            return false;
        }
    }
}
