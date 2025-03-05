package com.jogdesktopapp.Jog_Desktop_App;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class Dashboard {
    public JPanel view() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout()); // Change to BorderLayout for better positioning
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(AppColors.BACKGROUND_GREY);

        // Title at the top
        JLabel titleLabel = new JLabel("  Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT); 
//        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title container to ensure it stays at the top
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST); // Align title to the left
        titlePanel.setBackground(AppColors.WHITE);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Row Container for Printers, NAS Server, and Web Sockets
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rowPanel.setBackground(AppColors.WHITE); 
        rowPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));

        // Add components
        rowPanel.add(createContainer("Printers", new Printers().view()));
   
        rowPanel.add(createContainer("NAS Server", new NASServerInfo().view()));
      
        rowPanel.add(createContainer("Web Sockets", new WebSockets().view()));

        mainPanel.add(rowPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    
    // Get the Container
    private JPanel createContainer(String title, JPanel content) {
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        container.setBackground(AppColors.WHITE);
        container.setPreferredSize(new Dimension(250, 200)); // Set a fixed size for each container

        // Title Bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(AppColors.PrimaryDark);
        titleBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        titleBar.setPreferredSize(new Dimension(0, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleBar.add(titleLabel, BorderLayout.WEST);

        // Random Icon (using a label with text as a placeholder)
        ImageIcon logoIcon = new ImageIcon("icons/retry.png");
        Image logoImage = logoIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setPreferredSize(new Dimension(33, 30));
        logoLabel.setBackground(AppColors.PrimaryDark);
        logoLabel.setOpaque(true);
        
        titleBar.add(logoLabel, BorderLayout.EAST);

        container.add(titleBar, BorderLayout.NORTH);
        container.add(content, BorderLayout.CENTER);

        return container;
    }
    private JPanel SizeBox(int height, int width) {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension((int) width, (int) height));
        emptyPanel.setOpaque(false);
        return emptyPanel;
    }
}



class Printers {
    public JPanel view() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppColors.BACKGROUND_GREY);

        // Container Panel for Padding & Background Color
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding 10px
        container.setPreferredSize(new Dimension(0, 700)); // Fixed height of 700px
        container.setBackground(AppColors.BACKGROUND_GREY); // Set Grey Background

        TableViewPanel tablePanel = new TableViewPanel("Item List", Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4"));
        tablePanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY)); // Table Border

        // Add Table Panel inside the container
        container.add(tablePanel, BorderLayout.CENTER);

        // Add Container Panel to Main Panel
        panel.add(container);

        return panel;
    }
}

class NASServerInfo implements SftpUploaderListener {

	private JPanel statusPanel;
	
	@Override
	public void onStatusChanged(SftpUploaderStatus newStatus) {
	    SwingUtilities.invokeLater(() -> {
	    	setStatusPanel(newStatus);
	    });
	}
	
	void setStatusPanel(SftpUploaderStatus newStatus){
		  statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5)); // Left-aligned with padding
	        statusPanel.setPreferredSize(new Dimension(0, 50)); // Set height to 70px
	        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Ensure it stretches
	        statusPanel.setBackground(AppColors.GREEN); // Green Background

	        // White Dot Indicator (Circular)
	        JPanel whiteDot = new JPanel();
	        whiteDot.setPreferredSize(new Dimension(2, 2)); // Size increased to make it fully circular
	        whiteDot.setBackground(Color.WHITE);
	        whiteDot.setOpaque(true);
	        whiteDot.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Black outline

	        // Make it circular
	        whiteDot.setLayout(null);
	        whiteDot.setBorder(BorderFactory.createEmptyBorder());
	        whiteDot.setSize(2, 2);

	        // Make sure the dot is fully circular by overriding paintComponent
	        whiteDot = new JPanel() {
	            @Override
	            protected void paintComponent(Graphics g) {
	                super.paintComponent(g);
	                g.setColor(Color.WHITE);
	                g.fillOval(0, 0, getWidth(), getHeight()); // Draw filled circle
	            }

	            @Override
	            public Dimension getPreferredSize() {
	                return new Dimension(5, 5);
	            }
	        };
	        whiteDot.setBackground(new Color(0, 153, 0)); // Match background
	        whiteDot.setOpaque(false);

	        JLabel connectedLabel = new JLabel("Connected", SwingConstants.CENTER);
	        connectedLabel.setForeground(Color.WHITE);
	        connectedLabel.setFont(new Font("Arial", Font.BOLD, 14));
	        
	     // Update background color and text based on status
	        switch (newStatus) {
	            case UPLOADING:
	                statusPanel.setBackground(Color.YELLOW);
	                connectedLabel.setText("Uploading...");
	                break;
	            case DOWNLOADING:
	                statusPanel.setBackground(Color.BLUE);
	                connectedLabel.setText("Downloading...");
	                break;
	            default: // IDLE or any other state
	                statusPanel.setBackground(AppColors.GREEN);
	                connectedLabel.setText("IDLE");
	                break;
	        }

	        statusPanel.add(whiteDot);
	        statusPanel.add(connectedLabel);
	      
	}
	
    public JPanel view() {
    	setStatusPanel(SftpUploaderStatus.IDLE);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppColors.BACKGROUND_GREY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding 

        // First Container (IP Address)
        JPanel ipPanel = new JPanel(new BorderLayout());
        ipPanel.setPreferredSize(new Dimension(0, 50)); // Set height to 70px
        ipPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Ensure it stretches
        ipPanel.setBackground(Color.WHITE);
        ipPanel.setBorder(BorderFactory.createLineBorder(AppColors.BlueBorder, 1)); // Border

        JLabel ipLabel = new JLabel("IP: "+ App.sftpClient.SFTP_HOST , SwingConstants.CENTER);
        ipLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        ipLabel.setForeground(AppColors.BlueText); // Apply custom color

        ipPanel.add(ipLabel, BorderLayout.CENTER);
        mainPanel.add(ipPanel);

        // Spacer
        mainPanel.add(Box.createVerticalStrut(20)); // Space between containers

        // Second Container (actions Status)
    
        mainPanel.add(statusPanel);

        return mainPanel;
    }

    // Test the UI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("NAS Server UI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.add(new NASServerInfo().view());
            frame.setVisible(true);
        });
    }
}
class WebSockets {
    public JPanel view() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout()); // Center align content
        mainPanel.setBackground(AppColors.BACKGROUND_GREY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        // Status Panel (Active Status)
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        statusPanel.setPreferredSize(new Dimension(200, 30));
        statusPanel.setBackground(AppColors.GREEN);

        // White Dot Indicator (Circular)
        JPanel whiteDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.fillOval(0, 0, getWidth(), getHeight());
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(5, 5);
            }
        };
        whiteDot.setOpaque(false);

        JLabel activeLabel = new JLabel("Active", SwingConstants.CENTER);
        activeLabel.setForeground(Color.WHITE);
        activeLabel.setFont(new Font("Arial", Font.BOLD, 14));

        statusPanel.add(whiteDot);
        statusPanel.add(activeLabel);

        // Add status panel to center
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(statusPanel, gbc);

        return mainPanel;
    }

    // Test the UI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("NAS Server UI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.add(new WebSockets().view());
            frame.setLocationRelativeTo(null); // Center the frame
            frame.setVisible(true);
        });
    }
}

 
 class TableViewPanel extends JPanel {
	    public TableViewPanel(String headerTitle, List<String> items) {
	        setLayout(new BorderLayout());
	        setBackground(Color.WHITE);

	        // Header Row
	        JPanel headerPanel = createTableRow(headerTitle, "Status", true);
	        add(headerPanel, BorderLayout.NORTH);

	        // Items List
	        JPanel itemListPanel = new JPanel();
	        itemListPanel.setLayout(new GridLayout(items.size(), 1, 0, 0)); // No gaps between rows
	        itemListPanel.setBackground(Color.WHITE);

	        for (String item : items) {
	            itemListPanel.add(createTableRow(item, "Active", false));
	        }

	        add(itemListPanel, BorderLayout.CENTER);
	    }

	    private JPanel createTableRow(String title, String status, boolean isHeader) {
	        JPanel rowPanel = new JPanel(new GridBagLayout()); // Using GridBagLayout for centering
	        rowPanel.setPreferredSize(new Dimension(0, 30)); // Uniform height for all rows
	        rowPanel.setBackground(isHeader ? AppColors.PrimaryLite : AppColors.WHITE);
	        rowPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY)); // Top border

	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.weightx = 1.0;
	        gbc.weighty = 1.0;

	        // Title Label
	        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
	        titleLabel.setFont(new Font("Arial", isHeader ? Font.BOLD : Font.PLAIN, 14));
	        titleLabel.setForeground(isHeader ? Color.WHITE : Color.BLACK);
	        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Padding

	        gbc.gridx = 0;
	        gbc.anchor = GridBagConstraints.WEST;
	        rowPanel.add(titleLabel, gbc);

	        // Status Panel with Fixed Width
	        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
	        statusPanel.setOpaque(false);
	        statusPanel.setPreferredSize(new Dimension(100, 30)); // Fixed width of 100
	        statusPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY)); // Left border

	        if (isHeader) {
	            JLabel statusLabel = new JLabel(status);
	            statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
	            statusLabel.setForeground(Color.WHITE);
	            statusPanel.add(statusLabel);
	        } else {
	            statusPanel.add(new StatusIndicator());
	        }

	        gbc.gridx = 1;
	        gbc.anchor = GridBagConstraints.CENTER;
	        rowPanel.add(statusPanel, gbc);

	        return rowPanel;
	    }

	    // Custom Green Rectangle (10x5 px)
	    private static class StatusIndicator extends JPanel {
	        public StatusIndicator() {
	            setPreferredSize(new Dimension(10, 5));
	            setBackground(Color.GREEN);
	        }
	    }
	}