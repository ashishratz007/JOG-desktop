package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;
import java.net.Socket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class App {
	// Initialize socket connection
	static SocketModel socketModel = SocketModel.getInstance();
//    static SynologyServerModel synologyServer = SynologyServerModel.getInstance(); // Initialize the singleton
	static SftpUploader sftpClient = SftpUploader.getInstance(); // actions client for upload download and many more
																	// functions

	public static void main(String[] args) {
		PermissionRequest.main(args);
		EventQueue.invokeLater(() -> {
			try {
//                synologyServer.init(); // Now it won't be null
				AppFrame window = new AppFrame();
				window.setVisible(true);
				EventQueue.invokeLater(() -> {
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							// Create an instance of PrinterStatusChecker
					        PrinterStatusChecker checker = new PrinterStatusChecker();

					        System.out.println("Checking locally connected printers...");
					     // Start scanning for printers
					        checker.scanNetworkForPrinters(); // Call method to scan network printers
							sftpClient.getPendingFiles(); // Runs in background
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
