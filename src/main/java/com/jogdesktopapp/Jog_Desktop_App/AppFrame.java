package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
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
import org.json.*;

public class AppFrame extends JFrame {
    // UI Components
    private JPanel contentPanel;
    private JPanel sidebar;
    private JPanel[] tabs;
    private JLabel[] tabLabels;
    private BadgeLabel[] tabBadges;
    private StatusDot statusDot;
    private JLabel statusLabel;
    
    // Data
    private GlobalDataClass globalData = GlobalDataClass.getInstance();
    private final String[] allTabNames = {"Dashboard", "NAS Server", "Reprint", "Redesign"};
    private List<String> visibleTabNames = new ArrayList<>();
    private int reprintCount;
    private int redesignCount;
    private int currentSelectedTab = 0;
    
    // Singleton instance
    private static AppFrame instance;

    public static AppFrame getInstance() {
        if (instance == null) {
            instance = new AppFrame();
        }
        return instance;
    }

    private AppFrame() {
        initializeData();
        initializeUI();
        initializeSocketStatusListener();
    }

    // Initialization Methods
    private void initializeData() {
        reprintCount = globalData.reprintPendingData.total;
        redesignCount = globalData.redesignPendingData.total;
        updateVisibleTabs();
    }

    private void updateVisibleTabs() {
        visibleTabNames.clear();
        visibleTabNames.add("Dashboard"); // Always visible
        
        if (globalData.isAdobe()) {
            visibleTabNames.add("NAS Server");
        }
        if (globalData.canReprint()) {
            visibleTabNames.add("Reprint");
        }
        if (globalData.canRedesign()) {
            visibleTabNames.add("Redesign");
        }
    }

    private void initializeUI() {
        configureMainFrame();
        createSidebar();
        createAppBar();
        createContentPanel();
        setSelectedTab(0);
    }

    private void configureMainFrame() {
        setTitle("JOG Desktop App");
        setBounds(100, 100, 1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
    }

    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(150, getHeight()));
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        tabs = new JPanel[visibleTabNames.size()];
        tabLabels = new JLabel[visibleTabNames.size()];
        tabBadges = new BadgeLabel[visibleTabNames.size()];

        for (int i = 0; i < visibleTabNames.size(); i++) {
            tabs[i] = createTab(visibleTabNames.get(i), i);
            sidebar.add(tabs[i]);
        }

        getContentPane().add(sidebar, BorderLayout.WEST);
    }

    private void createAppBar() {
        JPanel appbar = new JPanel(new BorderLayout());
        appbar.setBackground(Color.WHITE);
        appbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        // Logo
        JLabel logoLabel = createLogoLabel();
        appbar.add(logoLabel, BorderLayout.WEST);

        // Title
        JLabel titleLabel = new JLabel("Your Status");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        appbar.add(titleLabel, BorderLayout.CENTER);

        // Status Panel
        JPanel statusPanel = createStatusPanel();
        appbar.add(statusPanel, BorderLayout.EAST);

        getContentPane().add(appbar, BorderLayout.NORTH);
    }

    private JLabel createLogoLabel() {
        ImageIcon logoIcon = new ImageIcon("src/main/resources/icons/logo.png");
        Image logoImage = logoIcon.getImage().getScaledInstance(150, 50, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setPreferredSize(new Dimension(150, 50));
        logoLabel.setOpaque(true);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        logoLabel.setBackground(new Color(240, 240, 240));
        return logoLabel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        statusPanel.setBackground(Color.WHITE);

        statusDot = new StatusDot(new Color(132, 226, 89));
        statusLabel = new JLabel("Online");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel logoutLabel = createLogoutButton();

        statusPanel.add(statusDot);
        statusPanel.add(statusLabel);
        statusPanel.add(logoutLabel);

        return statusPanel;
    }

    private JLabel createLogoutButton() {
        ImageIcon logoutIcon = new ImageIcon("src/main/resources/icons/logout.png");
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
        return logoutLabel;
    }

    private void createContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);
    }

    // Tab Management
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
                currentSelectedTab = index;
                setSelectedTab(index);
                refreshCounts();
                updateBadges();
            }
        });

        tabLabels[index] = label;
        return tab;
    }

    public void refreshUI() {
        initializeData();
        getContentPane().remove(sidebar);
        createSidebar();
        setSelectedTab(currentSelectedTab < visibleTabNames.size() ? currentSelectedTab : 0);
        revalidate();
        repaint();
    }

    private void refreshCounts() {
        reprintCount = globalData.reprintPendingData.total;
        redesignCount = globalData.redesignPendingData.total;
    }

    private void setSelectedTab(int selectedIndex) {
        for (int i = 0; i < tabs.length; i++) {
            if (i == selectedIndex) {
                tabs[i].setBackground(new Color(0, 102, 204));
                tabLabels[i].setForeground(Color.WHITE);
                
                contentPanel.removeAll();
                String tabName = visibleTabNames.get(i);
                
                switch (tabName) {
                    case "Dashboard":
                        contentPanel.add(new Dashboard().view(), BorderLayout.CENTER);
                        break;                
                    case "NAS Server":
                        contentPanel.add(new NasServer().view(), BorderLayout.CENTER);
                        break;
                    case "Reprint":
                        contentPanel.add(new ReprintUi().getView(), BorderLayout.CENTER);
                        break;
                    case "Redesign":
                        contentPanel.add(new RedesignUi().getView(), BorderLayout.CENTER);
                        break;
                }
            } else {
                tabs[i].setBackground(new Color(240, 240, 240));
                tabLabels[i].setForeground(Color.BLACK);
            }
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Badge Management
    public void updateBadges() {
        int reprintIndex = visibleTabNames.indexOf("Reprint");
        int redesignIndex = visibleTabNames.indexOf("Redesign");
        
        if (reprintIndex != -1) {
            tabBadges[reprintIndex].setCount(reprintCount);
        }
        if (redesignIndex != -1) {
            tabBadges[redesignIndex].setCount(redesignCount);
        }
        
        for (JPanel tab : tabs) {
            tab.revalidate();
            tab.repaint();
        }
    }

    // Socket Status
    private void initializeSocketStatusListener() {
        SocketModel socket = SocketModel.getInstance();
        socket.addPropertyChangeListener("connected", evt -> {
            boolean isConnected = (boolean) evt.getNewValue();
            updateConnectionStatus(isConnected);
        });
        updateConnectionStatus(socket.isOpen());
    }

    void updateConnectionStatus(boolean isConnected) {
        SwingUtilities.invokeLater(() -> {
            statusDot.setFillColor(isConnected ? new Color(132, 226, 89) : new Color(255, 59, 48));
            statusLabel.setText(isConnected ? "Online" : "Offline");
            statusDot.repaint();
        });
    }

    // Logout Handling
    private void showLogoutConfirmation() {
        JDialog dialog = new JDialog(this, "Logout Confirmation", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        JLabel messageLabel = new JLabel("<html><center>Are you sure you want to logout?</center></html>");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        dialog.add(messagePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> handleLogout(dialog));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void handleLogout(JDialog dialog) {
        String response = GlobalDataClass.getInstance().readTokenFromDesktop();
        JSONObject jsonObject = new JSONObject(response);
        if (ApiCalls.logout(jsonObject.getString("session_token"))) {
            GlobalDataClass.getInstance().deleteTokenFile();
            cleanupBeforeRestart();
        }
        dialog.dispose();
    }

    private void cleanupBeforeRestart() {
        SocketModel.getInstance().close();
        dispose();
        LoginFrame.view();
    }

    // Inner Classes
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
            if (newCount > previousCount) startBlinking();
            previousCount = count;
            this.count = newCount;
            setText(newCount > 99 ? "99+" : String.valueOf(newCount));
            setVisible(newCount > 0);
            repaint();
        }

        private void startBlinking() {
            stopBlinking();
            blinkTimer = new Timer(true);
            blinkTimer.scheduleAtFixedRate(new TimerTask() {
                private int blinkCount = 0;
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        isVisible = !isVisible;
                        repaint();
                        if (++blinkCount >= 10) stopBlinking();
                    });
                }
            }, 0, 500);
        }

        private void stopBlinking() {
            if (blinkTimer != null) blinkTimer.cancel();
            isVisible = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (count > 0 && isVisible) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int diameter = count < 10 ? 18 : count < 100 ? 22 : 26;
                g2.setColor(badgeColor);
                g2.fillOval(0, 0, diameter, diameter);
                
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D textBounds = fm.getStringBounds(getText(), g2);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), 
                    (diameter - (int)textBounds.getWidth())/2, 
                    (diameter - (int)textBounds.getHeight())/2 + fm.getAscent());
                g2.dispose();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return count < 10 ? new Dimension(18, 18) : 
                   count < 100 ? new Dimension(22, 22) : 
                   new Dimension(26, 26);
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
            g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(2, 2, getWidth()-4, getHeight()-4);
        }
    }

    // Getters and Setters
    public int getReprintCount() { return reprintCount; }
    public int getRedesignCount() { return redesignCount; }

    public void setReprintCount(int count) { 
        this.reprintCount = GlobalDataClass.getInstance().reprintPendingData.total;
        updateBadges();
    }

    public void setRedesignCount(int count) {
        this.redesignCount = GlobalDataClass.getInstance().redesignDownloadingData.total;
        updateBadges();
    }
}