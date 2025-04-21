package com.jogdesktopapp.Jog_Desktop_App;

import javax.swing.*;

import com.jogdesktopapp.Jog_Desktop_App.models.WindowsDeviceInfo;

import okio.AsyncTimeout;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton togglePasswordButton;
    private boolean isPasswordVisible = false;

    public LoginFrame() {
    	
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel userLabel = new JLabel("Username:");
        usernameField = new JTextField(15);

        JLabel passLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);
        togglePasswordButton = new JButton("\uD83D\uDC41"); // Eye emoji as button

        togglePasswordButton.setPreferredSize(new Dimension(45, 25));
        togglePasswordButton.setFocusable(false);
        togglePasswordButton.addActionListener(e -> togglePasswordVisibility());

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());

        // Layout constraints
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(userLabel, gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passLabel, gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 2;
        panel.add(togglePasswordButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        add(panel);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordField.setEchoChar('•');
            togglePasswordButton.setText("\uD83D\uDC41");
        } else {
            passwordField.setEchoChar((char) 0);
            togglePasswordButton.setText("\uD83D\uDC41\u200D\uD83D\uDD12"); // Eye with slash
        }
        isPasswordVisible = !isPasswordVisible;
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
	        /// device info
	        WindowsDeviceInfo deviceInfo = WindowsDeviceInfo.getInstance();	
	       
//	      JOptionPane.showMessageDialog(null, "Device ID: " + deviceInfo.getDeviceId() + "\n" + "Device Name: " + deviceInfo.getDeviceName() + "\n" + "Product ID: " + deviceInfo.getProductId(), "DEVICE INFO", JOptionPane.INFORMATION_MESSAGE);
	      			      
	       System.out.println("\n===== Device Specifications =====");
        deviceInfo.getDeviceSpecs().forEach((key, value) -> {
            System.out.printf("%-25s: %s%n", key, value);
        });
        
        	String sucess = ApiCalls.login(username, password, deviceInfo.getProductId());
        if (sucess != "sucess") {
        	JOptionPane.showMessageDialog(this, sucess, "Login Failed", JOptionPane.ERROR_MESSAGE);
        }else {
        	// Close the login frame
            this.dispose();
            
            
            initilizeData();
//            App.globalData.pendingAndReprint();// pending and reprint data
            // Open the main application frame/panel
            runMainView();
        }
    }

    public static void view() {
    	String tokenData = GlobalDataClass.getInstance().readTokenFromDesktop();
    	if(tokenData != null) {
    		initilizeData();
//    		 App.globalData.pendingAndReprint();// pending and reprint data
    		runMainView();
    	}
    	else  SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
    
    
	
	public static void runMainView(){
		EventQueue.invokeLater(() -> {
		try {  
//            synologyServer.init(); // Now it won't be null
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
					        	SftpUploaderStatus status = App.globalData.sftpClient.currentStatus;
					        	if(status == SftpUploaderStatus.IDLE) {
					        		 System.out.println("Status IDLE Function called at: " + java.time.LocalTime.now());
					        		 App.globalData.sftpClient.getPendingFiles(); // Runs in background
					        	}
					            
					        }, 0, 60, TimeUnit.SECONDS);
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

	  public static void initilizeData() {
	    	EventQueue.invokeLater(() -> {
				new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						GlobalDataClass instance =  GlobalDataClass.getInstance();
						instance.initilizeData(); 
						instance.pendingAndReprint(); 
						return null;
					}
					@Override
					protected void done() {
					}
				}.execute();

			});
	      
	    }
	   

}
