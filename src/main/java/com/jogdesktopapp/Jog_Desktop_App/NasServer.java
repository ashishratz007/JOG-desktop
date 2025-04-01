package com.jogdesktopapp.Jog_Desktop_App;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NasServer implements SftpUploaderListener {
    
    private JPanel statusLabel = new JPanel();
    private JPanel livePanel = createTablePanel(new Object[0][2], "Live");
    private JPanel downloadPanel = createTablePanel(new Object[0][2], "Download");
    private JPanel uploadPanel = createTablePanel(new Object[0][2], "Upload");
    
    // Pagination variables
    private JPanel pagesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    private int currentDownloadPage = 1;
    private int currentUploadPage = 1;
    private static final int ITEMS_PER_PAGE = 10;

    @Override
    public void onStatusChanged(SftpUploaderStatus newStatus) {
        SwingUtilities.invokeLater(() -> {
            setStatusPanel(newStatus);
        });
    }
    
    @Override
    public void onPendingChanged() {
        SwingUtilities.invokeLater(() -> {
            fillPendingData();
        });
    }
    
    void fillPendingData() {
        List<UploadFile> pendingUpload = App.sftpClient.pendingUpload;
        System.out.println("📤 pending notification invoked.");
        
        Object[][] data = new Object[pendingUpload.size()][2];
        for (int i = 0; i < pendingUpload.size(); i++) {
            UploadFile file = pendingUpload.get(i);
            String fileName = file.getPath().substring(file.getPath().lastIndexOf("/") + 1);
            data[i][0] = fileName;
            data[i][1] = file.getPath();
        }
        
        livePanel.removeAll();
        livePanel.add(createTable(data, "Live"));
        livePanel.revalidate();
        livePanel.repaint();
    }

    void setStatusPanel(SftpUploaderStatus newStatus) {
        statusLabel.removeAll();
        statusLabel.setLayout(new GridBagLayout()); 
        statusLabel.setPreferredSize(new Dimension(120, 20));
        statusLabel.setBorder(BorderFactory.createLineBorder(AppColors.BlueBorder, 1)); 
        
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
        JLabel ipLabel = new JLabel("IP:" + App.sftpClient.SFTP_HOST);
        ipLabel.setForeground(Color.BLUE);
        ipLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel statusAware = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        statusAware.setBackground(AppColors.BACKGROUND_GREY);
        statusAware.add(statusLabel);
        statusAware.add(ipLabel);
        titlePanel.add(statusAware, BorderLayout.EAST);

        // Tab View Panel
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 10));
        
        // Add tabs with empty data initially
        tabbedPane.addTab("Live", livePanel);
        tabbedPane.addTab("Download", downloadPanel);
        tabbedPane.addTab("Upload", uploadPanel);
        
        tabbedPane.setUI(new CustomTabUI());
        
        // Add change listener to load data when tabs are selected
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 1) { // Download tab
                loadDownloadData(currentDownloadPage);
            } else if (selectedIndex == 2) { // Upload tab
                loadUploadData(currentUploadPage);
            }
        });

        // Create pagination panel
        JPanel pageControlPanel = new JPanel(new BorderLayout());
        pageControlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        pageControlPanel.setBackground(Color.WHITE);
        pageControlPanel.add(pagesPanel, BorderLayout.CENTER);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        contentPanel.add(pageControlPanel, BorderLayout.SOUTH);

        // Add components to frame
        frame.add(titlePanel, BorderLayout.NORTH);
        frame.add(contentPanel, BorderLayout.CENTER);

        return frame;
    }

    private void loadDownloadData(int page) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DownloadedFilesModel model = ApiCalls.getDownlaodedList(ITEMS_PER_PAGE, page);
                SwingUtilities.invokeLater(() -> updateDownloadTable(model));
                return null;
            }
        }.execute();
    }

    private void loadUploadData(int page) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                UploadedFilesModel model = ApiCalls.getUploadedList(ITEMS_PER_PAGE, page);
                SwingUtilities.invokeLater(() -> updateUploadTable(model));
                return null;
            }
        }.execute();
    }

    private void updateDownloadTable(DownloadedFilesModel model) {
        if (model == null) return;
        
        Object[][] data = new Object[model.data.size()][2];
        for (int i = 0; i < model.data.size(); i++) {
            DownloadedFile file = model.data.get(i);
            data[i][0] = file.fileName;
            data[i][1] = file.synologyPath;
        }
        
        downloadPanel.removeAll();
        downloadPanel.add(createTable(data, "Download"));
        updatePageButtons(model.totalCount, currentDownloadPage, "download");
        downloadPanel.revalidate();
        downloadPanel.repaint();
    }

    private void updateUploadTable(UploadedFilesModel model) {
        if (model == null) return;
        
        Object[][] data = new Object[model.data.size()][2];
        for (int i = 0; i < model.data.size(); i++) {
            UploadedFile file = model.data.get(i);
            data[i][0] = file.fileName;
            data[i][1] = file.synologyPath;
        }
        
        uploadPanel.removeAll();
        uploadPanel.add(createTable(data, "Upload"));
        updatePageButtons(model.totalCount, currentUploadPage, "upload");
        uploadPanel.revalidate();
        uploadPanel.repaint();
    }

    private void updatePageButtons(int totalCount, int currentPage, String type) {
        pagesPanel.removeAll();
        
        int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        for (int i = 1; i <= totalPages; i++) {
            JButton button = createPageButton(i, i == currentPage);
            int page = i;
            button.addActionListener(e -> {
                if ("download".equals(type)) {
                    currentDownloadPage = page;
                    loadDownloadData(page);
                } else {
                    currentUploadPage = page;
                    loadUploadData(page);
                }
            });
            pagesPanel.add(button);
        }
        
        pagesPanel.revalidate();
        pagesPanel.repaint();
    }

    private JButton createPageButton(int pageNumber, boolean isActive) {
        JButton button = new JButton(String.valueOf(pageNumber));
        button.setPreferredSize(new Dimension(25, 25));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(isActive ? Color.BLUE : Color.GRAY);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 10));
        return button;
    }

    private JPanel createTablePanel(Object[][] data, String type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xC4D7E9));
        panel.add(createTable(data, type), BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createTable(Object[][] data, String type) {
        String[] columnNames = {"Name", "Path"};
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model) {
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
        return new JScrollPane(table);
    }
}

class CustomTabUI extends BasicTabbedPaneUI {
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color borderColor = new Color(211, 211, 211);
        Color gradientStart = new Color(218, 230, 241);
        Color gradientEnd = new Color(194, 213, 232);
        Color defaultColor = new Color(220, 220, 220);

        if (isSelected) {
            GradientPaint gradient = new GradientPaint(x, y, gradientStart, x, y + h, gradientEnd);
            g2d.setPaint(gradient);
        } else {
            g2d.setColor(defaultColor);
        }

        Polygon tabShape = new Polygon();
        tabShape.addPoint(x + 8, y);
        tabShape.addPoint(x + w, y);
        tabShape.addPoint(x + w, y + h);
        tabShape.addPoint(x, y + h);
        tabShape.addPoint(x, y + 8);
        g2d.fill(tabShape);

        g2d.setColor(borderColor);
        g2d.drawPolygon(tabShape);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        // No hard borders
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return 20;
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return 60;
    }
}