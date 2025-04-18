package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.json.JSONObject;

public class AppFrame extends JFrame {
    private JPanel contentPanel;
    private JPanel[] tabs;
    private JLabel[] tabLabels;
    private BadgeLabel[] tabBadges;
    private final String[] tabNames = {"Dashboard", "NAS Server","Reprint", "Redesign"};
    
    private int reprintCount = GlobalDataClass.getInstance().reprintPendingData.totalCount;
    private int redesignCount = GlobalDataClass.getInstance().redesignPendingData.totalCount;
    
    private static AppFrame instance;
    private StatusDot statusDot;
    private JLabel statusLabel;
    
    public static AppFrame getInstance() {
        if (instance == null) {
            instance = new AppFrame();
        }
        return instance;
    }

    private AppFrame() {
        initialize();
        initializeSocketStatusListener();
    }

    
    private void showLogoutConfirmation() {
        // Create a custom dialog
        JDialog dialog = new JDialog(this, "Logout Confirmation", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // Message panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel messageLabel = new JLabel("<html><center>Are you sure you want to logout from this device?</center></html>");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        
        dialog.add(messagePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
        	
        	String token = "";
        	
        	String response = GlobalDataClass.getInstance().readTokenFromDesktop();
        
        	// Parse JSON string
        	JSONObject jsonObject = new JSONObject(response);
        	boolean isLogout =  ApiCalls.logout(jsonObject.getString("session_token"));
        	if(isLogout) {
        		GlobalDataClass.getInstance().deleteTokenFile();
        		cleanupBeforeRestart();
        	}
        	dialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    
    
    private void cleanupBeforeRestart() {
        // Close any open resources
        SocketModel.getInstance().close(); 
        
        
        // Dispose of any open windows
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            window.dispose();
            App.main(tabNames); 
        }
    }
    
    private void initialize() {
        // Frame setup
        setTitle("JOG Desktop App");
        setBounds(100, 100, 1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Sidebar Panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(150, getHeight()));
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        // Initialize tabs and badges
        tabs = new JPanel[tabNames.length];
        tabLabels = new JLabel[tabNames.length];
        tabBadges = new BadgeLabel[tabNames.length];
        
        for (int i = 0; i < tabNames.length; i++) {
            tabs[i] = createTab(tabNames[i], i);
            sidebar.add(tabs[i]);
        }
        getContentPane().add(sidebar, BorderLayout.WEST);

        // Top App Bar
        JPanel appbar = new JPanel(new BorderLayout());
        appbar.setBackground(Color.WHITE);
        appbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        // Logo
        ImageIcon logoIcon = new ImageIcon("src/main/resources/icons/logo.png");
        Image logoImage = logoIcon.getImage().getScaledInstance(150, 50, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setPreferredSize(new Dimension(150, 50));
        logoLabel.setOpaque(true);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        logoLabel.setBackground(new Color(240, 240, 240));
        appbar.add(logoLabel, BorderLayout.WEST);

        // Title
        JLabel titleLabel = new JLabel("Your Status");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        appbar.add(titleLabel, BorderLayout.CENTER);

     // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        statusPanel.setBackground(Color.WHITE);

        // Logout Button
        ImageIcon logoutIcon = new ImageIcon("src/main/resources/icons/logout.png"); // Make sure you have this icon
        Image logoutImage = logoutIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        JLabel logoutLabel = new JLabel(new ImageIcon(logoutImage));
        logoutLabel.setToolTipText("Logout");
        logoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLogoutConfirmation();
            }
        });

        // Status Dot
        statusDot = new StatusDot(new Color(132, 226, 89));

        // Status Label
        statusLabel = new JLabel("Online");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));

       
        statusPanel.add(statusDot);
        statusPanel.add(statusLabel);
        statusPanel.add(logoutLabel);
        appbar.add(statusPanel, BorderLayout.EAST);
        getContentPane().add(appbar, BorderLayout.NORTH);

        // Main content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Dashboard View", SwingConstants.CENTER), BorderLayout.CENTER);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        
        // Set default selected tab
        setSelectedTab(0);
    }
    


    private void initializeSocketStatusListener() {
        SocketModel socket = SocketModel.getInstance();
        
        socket.addPropertyChangeListener("connected", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean isConnected = (boolean) evt.getNewValue();
                updateConnectionStatus(isConnected);
            }
        });
        
        // Initial status update
        updateConnectionStatus(socket.isOpen());
    }

    private void updateConnectionStatus(boolean isConnected) {
        SwingUtilities.invokeLater(() -> {
            if (isConnected) {
                statusDot.setFillColor(new Color(132, 226, 89)); // Green
                statusLabel.setText("Online");
            } else {
                statusDot.setFillColor(new Color(255, 59, 48)); // Red
                statusLabel.setText("Offline");
            }
            statusDot.repaint();
        });
    }

    private JPanel createTab(String name, int index) {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setPreferredSize(new Dimension(150, 40));
        tab.setMaximumSize(new Dimension(150, 40));
        tab.setBackground(new Color(240, 240, 240));

        JLabel label = new JLabel("  " + name);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        tab.add(label, BorderLayout.WEST);

        JPanel badgeContainer = new JPanel(new BorderLayout());
        badgeContainer.setOpaque(false);
        badgeContainer.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 15));
        
        BadgeLabel badge = new BadgeLabel();
        tabBadges[index] = badge;
        badgeContainer.add(badge, BorderLayout.EAST);
        tab.add(badgeContainer, BorderLayout.EAST);

        tab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelectedTab(index); 
                reprintCount = GlobalDataClass.getInstance().reprintPendingData.totalCount;
                redesignCount = GlobalDataClass.getInstance().redesignPendingData.totalCount;
                updateBadges();
            }
        });

        tabLabels[index] = label;
        return tab;
    }

    private void setSelectedTab(int selectedIndex) {
        for (int i = 0; i < tabs.length; i++) {
            if (i == selectedIndex) {
                tabs[i].setBackground(new Color(0, 102, 204));
                tabLabels[i].setForeground(Color.WHITE);
                
                contentPanel.removeAll();
                switch (i) {
                    case 0: contentPanel.add(new Dashboard().view(), BorderLayout.CENTER); break;                
                    case 1: contentPanel.add(new NasServer().view(), BorderLayout.CENTER); break;
                    case 2: contentPanel.add(new ReprintUi().getView(), BorderLayout.CENTER); break;
                    case 3: contentPanel.add(new RedesignUi().getView(), BorderLayout.CENTER); break;
                }
            } else {
                tabs[i].setBackground(new Color(240, 240, 240));
                tabLabels[i].setForeground(Color.BLACK);
            }
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void updateBadges() {
        tabBadges[2].setCount(reprintCount);
        tabBadges[3].setCount(redesignCount);
        
        for (JPanel tab : tabs) {
            tab.revalidate();
            tab.repaint();
        }
    }

    public int getReprintCount() {
        return reprintCount;
    }

    public void setReprintCount(int count) {
        count = GlobalDataClass.getInstance().reprintPendingData.totalCount;
        this.reprintCount = count;
        updateBadges();
    }

    public int getRedesignCount() {
        return redesignCount;
    }

    public void setRedesignCount(int count) {
        count = GlobalDataClass.getInstance().redesignPendingData.totalCount;
        this.redesignCount = count;
        updateBadges();
    }

    class BadgeLabel extends JLabel {
        private int count = 0;
        private final Color badgeColor = new Color(255, 59, 48);
        private Timer blinkTimer;
        private boolean isVisible = true;
        private int previousCount = 0;

        public BadgeLabel() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 10));
            setVisible(false);
            setOpaque(false);
        }

        public void setCount(int newCount) {
            if (newCount > previousCount) {
                startBlinking();
            }
            previousCount = count;
            this.count = newCount;
            setText(newCount > 99 ? "99+" : String.valueOf(newCount));
            setVisible(newCount > 0);
            repaint();
        }

        public void startBlinking() {
            stopBlinking();
            
            blinkTimer = new Timer(true);
            isVisible = true;
            
            blinkTimer.scheduleAtFixedRate(new TimerTask() {
                private int blinkCount = 0;
                private final int totalBlinks = 10;

                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        isVisible = !isVisible;
                        repaint();
                        blinkCount++;
                        if (blinkCount >= totalBlinks) {
                            stopBlinking();
                        }
                    });
                }
            }, 0, 500);
        }

        public void stopBlinking() {
            if (blinkTimer != null) {
                blinkTimer.cancel();
                blinkTimer = null;
            }
            isVisible = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (count > 0 && isVisible) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int diameter;
                if (count < 10) diameter = 18;
                else if (count < 100) diameter = 22;
                else diameter = 26;
                
                g2.setColor(badgeColor);
                g2.fillOval(0, 0, diameter, diameter);
                
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D textBounds = fm.getStringBounds(getText(), g2);
                int textX = (diameter - (int)textBounds.getWidth()) / 2;
                int textY = (diameter - (int)textBounds.getHeight()) / 2 + fm.getAscent();
                
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            if (count < 10) return new Dimension(18, 18);
            if (count < 100) return new Dimension(22, 22);
            return new Dimension(26, 26);
        }
    }

    class StatusDot extends JPanel {
        private Color fillColor;

        public StatusDot(Color fillColor) {
            this.fillColor = fillColor;
            setPreferredSize(new Dimension(14, 14));
            setOpaque(false);
        }

        public void setFillColor(Color fillColor) {
            this.fillColor = fillColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
        }
    }
}