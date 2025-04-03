package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;
import java.net.Socket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.jogdesktopapp.Jog_Desktop_App.models.WindowsDeviceInfo;
import com.jogdesktopapp.Jog_Desktop_App.models.WindowsSystemInfo;

import java.net.URI;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class App {
	// Initialize socket connection
	static SocketModel socketModel = SocketModel.getInstance();
//    static SynologyServerModel synologyServer = SynologyServerModel.getInstance(); // Initialize the singleton
	static SftpUploader sftpClient = SftpUploader.getInstance(); // actions client for upload download and many more
	// to make work easier to filter you data
	static GlobalDataClass globalData = GlobalDataClass.getInstance();
	public static void main(String[] args) {
		PermissionRequest.main(args);
		EventQueue.invokeLater(() -> {
			try {  
//                synologyServer.init(); // Now it won't be null
				AppFrame window =  AppFrame.getInstance();
				window.setVisible(true);
				EventQueue.invokeLater(() -> {
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							sftpClient.getPendingFiles(); // Runs in background
							WindowsDeviceInfo deviceInfo = WindowsDeviceInfo.getInstance();
					        
					        System.out.println("===== Device Identification =====");
					        System.out.println("Device ID: " + deviceInfo.getDeviceId());
					        System.out.println("Device Name: " + deviceInfo.getDeviceName());
					        System.out.println("Product ID: " + deviceInfo.getProductId());
					        
					        System.out.println("\n===== Device Specifications =====");
					        deviceInfo.getDeviceSpecs().forEach((key, value) -> {
					            System.out.printf("%-25s: %s%n", key, value);
					        });
							return null;
						}
						@Override
						protected void done() {
							System.out.println("File retrieval completed.");
						}
					}.execute();
	
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
