package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;
import java.net.Socket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.jogdesktopapp.Jog_Desktop_App.models.WindowsDeviceInfo;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
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
//		PermissionRequest.main(args);
		EventQueue.invokeLater(() -> {
			try {  
//                synologyServer.init(); // Now it won't be null
				AppFrame window =  AppFrame.getInstance();
				window.setVisible(true);
				EventQueue.invokeLater(() -> {
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							
 							
	 						
 							/// to schedule the task for the that will execute after 15 seconds to check for pending files
 							// Create a scheduled executor
 					        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 
 					        // Schedule the task to run every 15 seconds with no initial delay
 					        scheduler.scheduleAtFixedRate(() -> {
 					        	
 					            /// Only call the pending API when the the uploading is not occurring
 					        	SftpUploaderStatus status = sftpClient.currentStatus;
 					        	if(status == SftpUploaderStatus.IDLE) {
 					        		 System.out.println("Status IDLE Function called at: " + java.time.LocalTime.now());
 					        		sftpClient.getPendingFiles(); // Runs in background
 					        	}
 					            
 					        }, 0, 60, TimeUnit.SECONDS);
 
 					    
 					        /// device info
 					        WindowsDeviceInfo deviceInfo = WindowsDeviceInfo.getInstance();	
 					       
// 					      JOptionPane.showMessageDialog(null, "Device ID: " + deviceInfo.getDeviceId() + "\n" + "Device Name: " + deviceInfo.getDeviceName() + "\n" + "Product ID: " + deviceInfo.getProductId(), "DEVICE INFO", JOptionPane.INFORMATION_MESSAGE);
 					      			      
 					       System.out.println("\n===== Device Specifications =====");
					        deviceInfo.getDeviceSpecs().forEach((key, value) -> {
					            System.out.printf("%-25s: %s%n", key, value);
					        });
					    	
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
