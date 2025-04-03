package com.jogdesktopapp.Jog_Desktop_App.models;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.util.Random;
import javax.sound.sampled.*;

public class NotificationService {

    private static final String[] SOUND_FILES = {
//            "src/main/resources/sounds/1.wav",
            "src/main/resources/sounds/2.wav",
    };

    public static void showNotification(String title, String message) {
        // Show system tray notification
        if (SystemTray.isSupported()) {
            displayTrayNotification(title, message);
        } else {
            System.out.println("SystemTray is not supported!");
        }

        // Play random sound
        playRandomSound();
    }

    private static void displayTrayNotification(String title, String message) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("src/icons/logo.png");

            TrayIcon trayIcon = new TrayIcon(image, "Notification");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Notification System");
            tray.add(trayIcon);

            trayIcon.displayMessage(title, message, MessageType.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playRandomSound() {
        Random random = new Random();
        String soundFile = SOUND_FILES[random.nextInt(SOUND_FILES.length)];

        try {
            File file = new File(soundFile);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
