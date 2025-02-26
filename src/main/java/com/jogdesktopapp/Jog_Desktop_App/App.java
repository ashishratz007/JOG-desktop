package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class App {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                AppFrame window = new AppFrame();
                window.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

