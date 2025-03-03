package com.jogdesktopapp.Jog_Desktop_App;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

class AppFrame extends JFrame {
    private JPanel contentPanel; // Main content panel for displaying views
    private JPanel[] tabs; // Array to hold sidebar tabs
    private JLabel[] tabLabels; // Labels for sidebar tabs
    private final String[] tabNames = {"Dashboard", "Printers", "NAS Server", "Web Sockets"}; // Tab names

    public AppFrame() {
        initialize();
    }

    private void initialize() {
        // Set up the main frame
        setTitle("App Frame");
        setBounds(100, 100, 1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Sidebar Panel Setup
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(150, getHeight()));
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200))); // Adds right border

        // Initialize sidebar tabs
        tabs = new JPanel[tabNames.length];
        tabLabels = new JLabel[tabNames.length];
        for (int i = 0; i < tabNames.length; i++) {
            tabs[i] = createTab(tabNames[i], i);
            sidebar.add(tabs[i]);
        }
        getContentPane().add(sidebar, BorderLayout.WEST);

        // Top App Bar Setup
        JPanel appbar = new JPanel(new BorderLayout());
        appbar.setBackground(Color.WHITE);
        appbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200))); // Bottom border

        // Logo setup
        ImageIcon logoIcon = new ImageIcon("icons/logo.png");
        Image logoImage = logoIcon.getImage().getScaledInstance(150, 50, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setPreferredSize(new Dimension(150, 50));
        logoLabel.setOpaque(true);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        logoLabel.setBackground(new Color(240, 240, 240));
        appbar.add(logoLabel, BorderLayout.WEST);

        // Title in app bar
        JLabel titleLabel = new JLabel("Your Status");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        appbar.add(titleLabel, BorderLayout.CENTER);

     // Status Panel Setup
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        statusPanel.setBackground(Color.WHITE);

        // Gradient Container for Status
        GradientPanel statusContainer = new GradientPanel(AppColors.GRADIENT_START, AppColors.GRADIENT_END);
        statusContainer.setPreferredSize(new Dimension(80, 30)); // Fixed width & height

        // Circular Status Dot with Green Color
        StatusDot statusDot = new StatusDot(new Color(132, 226, 89));

        // Status Label
        JLabel statusLabel = new JLabel("Online");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
     // Add click listener
        statusLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	 System.out.println("Clicked event");
            	 App.synologyServer.uploadFile();
            }
        });


        statusContainer.add(statusDot);
        statusContainer.add(statusLabel);

        // Add statusContainer to statusPanel
        statusPanel.add(statusContainer);
        appbar.add(statusPanel, BorderLayout.EAST);



        // allign appbar
        getContentPane().add(appbar, BorderLayout.NORTH);

        // Main content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Dashboard View", SwingConstants.CENTER), BorderLayout.CENTER);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        
        // Set default selected tab
        setSelectedTab(0);
    }

    // Creates a sidebar tab
    private JPanel createTab(String name, int index) {
        JPanel tab = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        tab.setPreferredSize(new Dimension(150, 40));
        tab.setMaximumSize(new Dimension(150, 40));
        tab.setBackground(new Color(240, 240, 240));

        JLabel label = new JLabel(name);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        tab.add(label);

        // Add click listener for tab selection
        tab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelectedTab(index);
            }
        });

        tabLabels[index] = label;
        return tab;
    }

    // Handles tab selection and updates the content panel
    private void setSelectedTab(int selectedIndex) {
        for (int i = 0; i < tabs.length; i++) {
            if (i == selectedIndex) {
                // Highlight selected tab
                tabs[i].setBackground(new Color(0, 102, 204));
                tabLabels[i].setForeground(Color.WHITE);
                
                // Update content panel based on selected tab 
                contentPanel.removeAll();
                switch (i) {
                    case 0: contentPanel.add(new Dashboard().view(), BorderLayout.CENTER); break;
                    case 1: contentPanel.add(new PrinterModel().view(), BorderLayout.CENTER); break;
                    case 2: contentPanel.add(new NasServer().view(), BorderLayout.CENTER); break;
                    case 3: contentPanel.add(new WebSockets().view(), BorderLayout.CENTER); break;
                }
            } else {
                // Reset non-selected tabs
                tabs[i].setBackground(new Color(240, 240, 240));
                tabLabels[i].setForeground(Color.BLACK);
            }
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}


class GradientPanel extends JPanel {
    private final Color startColor;
    private final Color endColor;

    public GradientPanel(Color startColor, Color endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        setOpaque(false); // Allow custom painting
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw gradient background
        GradientPaint gradient = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Rounded corners

        // Draw border with GRADIENT_END color
        g2.setColor(AppColors.GRADIENT_END);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10); // Border inside container
    }
}

//Custom JPanel for a perfect circular dot
class StatusDot extends JPanel {
 private final Color fillColor;

 public StatusDot(Color fillColor) {
     this.fillColor = fillColor;
     setPreferredSize(new Dimension(14, 14)); // Fixed size for a perfect circle
     setOpaque(false);
 }

 @Override
 protected void paintComponent(Graphics g) {
     super.paintComponent(g);
     Graphics2D g2 = (Graphics2D) g;

     // Enable anti-aliasing for smooth edges
     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

     // Draw filled circle with GREEN color
     g2.setColor(fillColor);
     g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4); // Centered

     // Draw white border
     g2.setColor(Color.WHITE);
     g2.setStroke(new BasicStroke(2)); // Border thickness
     g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
 }
}


