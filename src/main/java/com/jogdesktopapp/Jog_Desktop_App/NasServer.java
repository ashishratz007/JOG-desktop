package com.jogdesktopapp.Jog_Desktop_App;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NasServer implements SftpUploaderListener {
	
	private JPanel statusLabel = new JPanel();
	private JPanel livePanel =  createTablePanel(new Object[0][2]);
	@Override
	public void onStatusChanged(SftpUploaderStatus newStatus) {
//		 System.err.println("❌ Status Received:" + newStatus);
		SwingUtilities.invokeLater(() -> {
			setStatusPanel(newStatus);
	    });
	}
	
    /**
     changes the UI for the pending upload
     */
	@Override
	public void onPendingChanged() {
	    SwingUtilities.invokeLater(() -> {
	    	fillPendingData();
	    });
	}
	
	 void fillPendingData(){
		 livePanel.removeAll();

	        // Get the pending uploads
	        List<UploadFile> pendingUpload = App.sftpClient.pendingUpload;
	        System.out.println("📤 pending notification invoked.");
	        // Convert pending uploads to table data
	        Object[][] data = new Object[pendingUpload.size()][2];
	        for (int i = 0; i < pendingUpload.size(); i++) {
	            UploadFile file = pendingUpload.get(i);
	            String fileName = file.getPath().substring(file.getPath().lastIndexOf("/") + 1); // Extract filename from path
	            data[i][0] = fileName;
	            data[i][1] = file.getPath();
	        }
	        livePanel = createTablePanel(data);
	        livePanel.revalidate();
	        livePanel.repaint();
	}

	

	void setStatusPanel(SftpUploaderStatus newStatus) {
		   statusLabel.removeAll(); // Remove old content
	        statusLabel.setLayout(new GridBagLayout()); 
	        statusLabel.setPreferredSize(new Dimension(120, 20));
	        statusLabel.setBorder(BorderFactory.createLineBorder(AppColors.BlueBorder, 1)); 
	        fillPendingData();
	        JLabel connectedLabel = new JLabel();
	        connectedLabel.setFont(new Font("Arial", Font.PLAIN, 10));

	        switch (newStatus) {
	            case UPLOADING:
	                statusLabel.setBackground(AppColors.Uploading);
	                connectedLabel.setForeground(Color.WHITE);
	                connectedLabel.setText("Uploading...");
	                break;
	            case DOWNLOADING:
	                statusLabel.setBackground(Color.BLUE);
	                connectedLabel.setForeground(Color.WHITE);
	                connectedLabel.setText("Downloading...");
	                break;
	            default:
	                statusLabel.setBackground(Color.WHITE);
	                connectedLabel.setForeground(AppColors.BlueBorder);
	                connectedLabel.setText("Idle");
	                break;
	        }

	        statusLabel.add(connectedLabel);
	        statusLabel.revalidate();
	        statusLabel.repaint();
	}


    public JPanel view() {
    	App.sftpClient.addStatusListener(this);
    	
    	setStatusPanel(App.sftpClient.currentStatus); 
    	
    	JPanel frame = new JPanel();
    	frame.setBackground(Color.WHITE);
        frame.setLayout(new BorderLayout());

     // Title Row Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("NAS Server");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        // Status Panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(Color.LIGHT_GRAY);
        // IP Label (Placed in a row after "Connected")
        JLabel ipLabel = new JLabel("IP:" + App.sftpClient.SFTP_HOST);
        ipLabel.setForeground(Color.BLUE);
        ipLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Add components
        JPanel statusAware = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        statusAware.setBackground(AppColors.BACKGROUND_GREY);
//        statusAware.setBorder(new EmptyBorder(5, 5, 5, 5));
        statusAware.add(statusLabel); // "Status" Label Container
        statusAware.add(ipLabel);
        titlePanel.add(statusAware, BorderLayout.EAST);

        // Date Field
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel dateLabel = new JLabel("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        datePanel.add(dateLabel);

        // Tab View Panel
        JTabbedPane tabbedPane = new JTabbedPane();
        // Set font size to 10
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 10));
        tabbedPane.addTab("Live", livePanel);
        Object[][] data = {
                {"File1", "document1.pdf"},
                {"File2", "image1.png"},
                {"File3", "video1.mp4"}
            };
        tabbedPane.addTab("Download", createTablePanel(data));
        tabbedPane.addTab("Upload", createTablePanel(data));

        // Custom Tab UI
        tabbedPane.setUI(new CustomTabUI()); // Apply custom UI

        // Styling Selected Tab
        tabbedPane.addChangeListener(e -> {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getSelectedIndex() == i) {
                    tabbedPane.setBackgroundAt(i, new Color(0xDAE6F1));
                } else {
                    tabbedPane.setBackgroundAt(i, new Color(0xAAAAAA));
                }
            }
        });

        // Add Components to Frame
        frame.add(titlePanel, BorderLayout.NORTH);
//        frame.add(datePanel, BorderLayout.CENTER);
//        frame.add( Utills.space(50));
        frame.add(tabbedPane, BorderLayout.CENTER);

        frame.setVisible(true);
        return frame;
    }

    private JPanel createTablePanel(Object[][] data) { 
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xC4D7E9));

        String[] columnNames = {"Name", "Filename"};
  

        JTable table = new JTable(new DefaultTableModel(data, columnNames)) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(0x7A8A99));
                }
                return c;
            }
        };

        table.setRowHeight(30);
        table.setGridColor(Color.DARK_GRAY);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}


//Custom Tab UI with smaller tabs and smooth gradient effect
class CustomTabUI extends BasicTabbedPaneUI {
 @Override
 protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
     Graphics2D g2d = (Graphics2D) g;
     g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

     // Define colors
     Color borderColor = new Color(211, 211, 211); // Light Gray (#D3D3D3)
     Color gradientStart = new Color(218, 230, 241); // #DAE6F1
     Color gradientMiddle = new Color(255, 255, 255); // #FFFFFF
     Color gradientEnd = new Color(194, 213, 232); // #C2D5E8
     Color defaultColor = new Color(220, 220, 220); // Gray for unselected tabs

     // Gradient for selected tab
     if (isSelected) {
         GradientPaint gradient = new GradientPaint(x, y, gradientStart, x, y + h, gradientEnd);
         g2d.setPaint(gradient);
     } else {
         g2d.setColor(defaultColor);
     }

     // Create polygon shape for the tab (cut-off top-left corner)
     Polygon tabShape = new Polygon();
     tabShape.addPoint(x + 8, y); // Cut top-left
     tabShape.addPoint(x + w, y);
     tabShape.addPoint(x + w, y + h);
     tabShape.addPoint(x, y + h);
     tabShape.addPoint(x, y + 8); // Cut top-left
     g2d.fill(tabShape);

     // Draw the border
     g2d.setColor(borderColor);
     g2d.drawPolygon(tabShape);
 }

 @Override
 protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
     // No hard borders, keeping it smooth with the custom shape
 }

 @Override
 protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
     return 20; // Reduced tab height
 }

 @Override
 protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
     return 60; // Reduced tab width
 }
}
