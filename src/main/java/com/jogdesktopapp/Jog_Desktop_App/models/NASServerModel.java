package com.jogdesktopapp.Jog_Desktop_App.models;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class NASServerModel {

   static JPanel createUploadPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton uploadButton = new JButton("Upload File");
        uploadButton.addActionListener(e -> uploadFile());
        panel.add(uploadButton, BorderLayout.CENTER);
        return panel;
    }

   static void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                sendFileToNas(selectedFile.getName(), fileData);
                JOptionPane.showMessageDialog(null, "File uploaded successfully!");
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error uploading file: " + ex.getMessage());
            }
        }
    }

   static List<String> getFilesystem() {
        // Simulated function to retrieve file list from NAS server
        return List.of("document1.pdf", "image1.png", "video1.mp4");
    }

   static void sendFileToNas(String fileName, byte[] fileData) {
        // Simulated function to send a file to NAS server
        System.out.println("Uploading: " + fileName);
    }
}
