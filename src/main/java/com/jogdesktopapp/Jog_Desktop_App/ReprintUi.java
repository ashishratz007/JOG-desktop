package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.event.MouseAdapter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.w3c.dom.events.MouseEvent;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class ReprintUi {
	static GlobalDataClass globalData = GlobalDataClass.getInstance();
    // UI Components
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JPanel statusLabel = new JPanel();
    private JPanel pendingPanel = new JPanel();
    private JPanel completePanel = new JPanel();
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private JPanel pagesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    
    // Data Models
    private ReprintModel pending = new ReprintModel(0, new ArrayList<>());
    private ReprintModel complete = new ReprintModel(0, new ArrayList<>());
    private Date selectedDate = new Date();
    
    // Pagination Constants
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPendingPage = 1;
    private int currentCompletePage = 1;
    
    public ReprintUi() {
        // Initialize panels with empty data
        pendingPanel = createPendingTablePanel(new Object[0][6]);
        completePanel = createCompleteTablePanel(new Object[0][4]);
        
        // Initialize UI
        initializeUI();
        refreshData();
    }
    
    /**
     * Initializes the main UI components and layout
     */
    private void initializeUI() {
        // Configure main panel
        mainPanel.setBackground(Color.WHITE);
        
        // Create and add title panel
        mainPanel.add(createTitlePanel(), BorderLayout.NORTH);
        
        // Create content panel with date picker and tabs
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(createDatePickerPanel(), BorderLayout.NORTH);
        contentPanel.add(createTabbedPane(), BorderLayout.CENTER);
        contentPanel.add(createPageFilterPanel(), BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Initialize status label
        setStatusPanel("Idle");
    }
    
    /**
     * Creates the title panel with status information
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Reprint");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JLabel countLabel = new JLabel("Files for Reprint : " + globalData.reprintPendingData.totalCount);
        countLabel.setForeground(Color.WHITE);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        statusPanel.setBackground(AppColors.yellowBg);
//        statusPanel.add(statusLabel);
        statusPanel.add(countLabel);
        titlePanel.add(statusPanel, BorderLayout.EAST);

        return titlePanel;
    }
    
    /**
     * Creates the date picker panel for filtering by date
     */
    private JPanel createDatePickerPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel dateLabel = new JLabel("Select Date:");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(model);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(selectedDate);
        
        dateSpinner.addChangeListener(e -> {
            selectedDate = (Date) dateSpinner.getValue();
            refreshDataForSelectedDate();
        });

        panel.add(dateLabel);
        panel.add(dateSpinner);

        return panel;
    }
    
    /**
     * Creates the tabbed pane with change listener for pagination
     */
    private JTabbedPane createTabbedPane() {
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 10));
        tabbedPane.addTab("Pending", pendingPanel);
        tabbedPane.addTab("Complete", completePanel);
        tabbedPane.setUI(new CustomTabUIReprint());
        
        // Add tab change listener to update page buttons
        tabbedPane.addChangeListener(e -> {
            updatePageButtonsForCurrentTab();
        });
        
        return tabbedPane;
    }
    
    /**
     * Creates the page filter panel with scrollable page buttons
     */
    private JPanel createPageFilterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);
        
        pagesPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(pagesPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(0, 50));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }
    
    /**
     * Updates the status panel with the given message
     */
    public void setStatusPanel(String status) {
        statusLabel.removeAll();
        statusLabel.setLayout(new GridBagLayout()); 
        statusLabel.setPreferredSize(new Dimension(120, 20));
        statusLabel.setBorder(BorderFactory.createLineBorder(AppColors.BlueBorder, 1)); 
       
        JLabel connectedLabel = new JLabel();
        connectedLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        statusLabel.setBackground(Color.WHITE);
        connectedLabel.setForeground(AppColors.BlueBorder);
        connectedLabel.setText(status);
        statusLabel.add(connectedLabel);
        statusLabel.revalidate();
        statusLabel.repaint();
    }
    
    /**
     * Refreshes data for both tabs
     */
    public void refreshData() {
        fillPendingData();
        fillCompleteData();
    }
    
    /**
     * Fills pending data and resets to first page
     */
    private void fillPendingData() {
        GlobalDataClass globalData = GlobalDataClass.getInstance();
        pending = globalData.reprintPendingData;
        displayPendingPage(currentPendingPage);
        updatePageButtonsForCurrentTab();
    }
    
    /**
     * Fills complete data and resets to first page
     */
    private void fillCompleteData() {
        GlobalDataClass globalData = GlobalDataClass.getInstance();
        complete = globalData.reprintCompleteData;
//        currentCompletePage = 1; // Reset to first page
        displayCompletePage(currentCompletePage);
        updatePageButtonsForCurrentTab();
    }
    
    /**
     * Displays a specific page of pending items
     */
    private void displayPendingPage(int pageNumber) {
        List<ReprintItem> pageItems = pending.data;
        Object[][] data = new Object[pageItems.size()][6];
        
        for (int i = 0; i < pageItems.size(); i++) {
            ReprintItem file = pageItems.get(i);
            data[i][0] = file.fileName;
            data[i][1] = file.orderName;
            data[i][2] = file.exCode;
            data[i][3] = file.printerName;
            data[i][4] = file.synologyPath;
            data[i][5] = file.reprintId;
        }
        
        refreshPendingTable(data);
    }
    
    /**
     * Displays a specific page of complete items
     */
    private void displayCompletePage(int pageNumber) {
        List<ReprintItem> pageItems = complete.data;
        Object[][] data = new Object[pageItems.size()][4];
        
        for (int i = 0; i < pageItems.size(); i++) {
            ReprintItem file = pageItems.get(i);
            data[i][0] = file.fileName;
            data[i][1] = file.orderName;
            data[i][2] = file.exCode;
            data[i][3] = file.printerName;
        }
        
        refreshCompleteTable(data);
    }
    
    /**
     * Refreshes the pending table with new data
     */
    private void refreshPendingTable(Object[][] data) {
        pendingPanel.removeAll();
        pendingPanel.add(createPendingTable(data));
        pendingPanel.revalidate();
        pendingPanel.repaint();
    }
    
    /**
     * Refreshes the complete table with new data
     */
    private void refreshCompleteTable(Object[][] data) {
        completePanel.removeAll();
        completePanel.add(createCompleteTable(data));
        completePanel.revalidate();
        completePanel.repaint();
    }
    
    /**
     * Creates the pending table with download and complete buttons
     */
    private JScrollPane createPendingTable(Object[][] data) {
        String[] columnNames = {"Name of file", "Order name", "Ex code", "Designer", "Download", "Set to Complete"};
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5;
            }
        };
        
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xC4D7E9));
                }
                return c;
            }
        };
        
        table.setRowHeight(30);
        table.setGridColor(Color.DARK_GRAY);
        
        // Configure columns
        configureDownloadColumn(table);
        configureCompleteColumn(table);
        
        return new JScrollPane(table);
    }
    
    /**
     * Creates the complete table (read-only)
     */
    private JScrollPane createCompleteTable(Object[][] data) {
        String[] columnNames = {"Name of file", "Order name", "Ex code", "Designer"};
        
        JTable table = new JTable(new DefaultTableModel(data, columnNames)) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(0xC4D7E9));
                }
                return c;
            }
        };
        
        table.setRowHeight(30);
        table.setGridColor(Color.DARK_GRAY);
        return new JScrollPane(table);
    }
    
    /**
     * Configures the download column with button renderer and editor
     */
    private void configureDownloadColumn(JTable table) {
        table.getColumnModel().getColumn(4).setCellRenderer(getButtonRenderer("icons/download.png"));
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = e.getActionCommand();
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        App.sftpClient.pickAndDownloadFile(filePath);
                        return null;
                    }
                    @Override
                    protected void done() {
                        System.out.println("File retrieval completed.");
                    }
                }.execute();
            }
        }, "icons/download.png"));
    }
    
    /**
     * Configures the complete column with button renderer and editor
     */
    private void configureCompleteColumn(JTable table) {
        table.getColumnModel().getColumn(5).setCellRenderer(getButtonRenderer("icons/complete.png"));
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to set this to complete?", 
                    "Confirm Action", 
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);
                
                if (response == JOptionPane.OK_OPTION) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            ApiCalls.markComplete(true, e.getActionCommand());
                             App.globalData.getReprintData(currentPendingPage);
                            App.globalData.getReprintData(currentCompletePage);
                            SwingUtilities.invokeLater(() -> {
                                fillPendingData();
                            });
                            return null;
                        }
                        @Override
                        protected void done() {
                            SwingUtilities.invokeLater(() -> {
                                fillPendingData();
                            });
                        }
                    }.execute();
                }
            }
        }, "icons/complete.png"));
    }
    
    /**
     * Creates a page navigation button
     */
    private JButton createPageButton(int pageNumber) {
        JButton button = new JButton(String.valueOf(pageNumber));
        button.setPreferredSize(new Dimension(25, 25));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(Color.GRAY);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 10));
        
        button.addActionListener(e -> handlePageButtonClick(button, pageNumber));
        
        return button;
    }
    
    /**
     * Handles page button clicks
     */
    private void handlePageButtonClick(JButton button, int pageNumber) {
        // Reset all buttons
        for (Component comp : pagesPanel.getComponents()) {
            if (comp instanceof JButton) {
                comp.setBackground(Color.GRAY);
            }
        }
        
        // Highlight clicked button
        button.setBackground(Color.BLUE);
        
        // Handle page change
        handlePageSelection(pageNumber);
    }
    
    /**
     * Handles page selection for the current tab
     */
    private void handlePageSelection(int pageNumber) {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == 0) { // Pending tab
            currentPendingPage = pageNumber;
            displayPendingPage(pageNumber);
        } else { // Complete tab
            currentCompletePage = pageNumber;
            displayCompletePage(pageNumber);
        }
        
        // Log the page selection
        logPageSelection(pageNumber);
    }
    
    /**
     * Logs page selection to console and file
     */
    private void logPageSelection(int pageNumber) {
        statusLabel.setToolTipText("Selected Page: " + pageNumber);
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        System.out.println("[" + timestamp + "] Page selected: " + pageNumber);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
            	 int selectedIndex = tabbedPane.getSelectedIndex();
                 if (selectedIndex == 0) {
                        currentPendingPage = pageNumber;
                        App.globalData.getReprintData(currentPendingPage);
                 }
                 else  {
                	 currentCompletePage = pageNumber;
                	 App.globalData.getReprintData(currentCompletePage);
                 }
                
                 

                SwingUtilities.invokeLater(() -> {
                	 fillPendingData();
                     fillCompleteData();
                });
                return null;
            }
            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                	 fillPendingData();
                     fillCompleteData();
                });
            }
        }.execute();
    }
    
    /**
     * Updates page buttons based on current tab
     */
    private void updatePageButtonsForCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        int totalPages, currentPage;
        
        if (selectedIndex == 0) { // Pending tab
            totalPages = pending.pageCount();
            currentPage = currentPendingPage;
        } else { // Complete tab
            totalPages = complete.pageCount();
            currentPage = currentCompletePage;
        }
        
        updatePageButtons(totalPages, currentPage);
    }
    
    /**
     * Updates the page buttons display
     */
    private void updatePageButtons(int totalPages, int currentPage) {
        pagesPanel.removeAll();
        
        for (int i = 1; i <= totalPages; i++) {
            JButton button = createPageButton(i);
            if (i == currentPage) {
                button.setBackground(Color.BLUE);
            }
            pagesPanel.add(button);
        }
        
        pagesPanel.revalidate();
        pagesPanel.repaint();
    }
    
    /**
     * Creates a button renderer for table cells
     */
    private TableCellRenderer getButtonRenderer(String iconPath) {
        return (table, value, isSelected, hasFocus, row, column) -> createIconButton(iconPath);
    }
    
    /**
     * Creates an icon button for table cells
     */
    private JButton createIconButton(String iconPath) {
        JButton button = new JButton(new ImageIcon(iconPath));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    /**
     * Returns the main view panel
     */
    public JPanel getView() {
        return mainPanel;
    }
    
    // table
 // These are the methods you asked about - restored to original form
    private JPanel createPendingTablePanel(Object[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xC4D7E9));
        
        String[] columnNames = {"Name of file", "Order name", "Ex code", "Designer", "Download", "Set to Complete"};
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5;
            }
        };
        
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xC4D7E9));
                }
                return c;
            }
        };
        
        table.setRowHeight(30);
        table.setGridColor(Color.DARK_GRAY);
        
        // Configure columns
        configureDownloadColumn(table);
        configureCompleteColumn(table);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCompleteTablePanel(Object[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xC4D7E9));
        
        String[] columnNames = {"Name of file", "Order name", "Ex code","Designer"};
        
        JTable table = new JTable(new DefaultTableModel(data, columnNames)) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(0xC4D7E9));
                }
                return c;
            }
        };
        
        table.setRowHeight(30);
        table.setGridColor(Color.DARK_GRAY);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
    
   
    /**
     * Updates pending data and refreshes the view
     */
    public void updatePendingData(ReprintModel newData) {
        this.pending = newData;
        fillPendingData();
    }
    
    /**
     * Updates complete data and refreshes the view
     */
    public void updateCompleteData(ReprintModel newData) {
        this.complete = newData;
        fillCompleteData();
    }
    
    /**
     * Sets the selected date and refreshes data
     */
    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        refreshDataForSelectedDate();
    }
    
    /**
     * Refreshes data for the selected date
     */
    private void refreshDataForSelectedDate() {
        System.out.println("Refreshing data for date: " + new SimpleDateFormat("yyyy-MM-dd").format(selectedDate));
        refreshData();
    }
}

/**
 * Custom button editor for table cells
 */
class ButtonEditor extends DefaultCellEditor {
    private final JButton button;
    private String value;
    
    public ButtonEditor(Action action, String iconPath) {
        super(new JCheckBox());
        this.button = new JButton();
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setIcon(new ImageIcon(iconPath));
        button.addActionListener(action);
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, 
            boolean isSelected, int row, int column) {
        this.value = (value != null) ? value.toString() : "";
        button.setActionCommand(this.value);
        return button;
    }
    
    @Override
    public Object getCellEditorValue() {
        return value;
    }
}

/**
 * Custom UI for tabbed pane
 */
class CustomTabUIReprint extends BasicTabbedPaneUI {
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Define colors
        Color borderColor = new Color(211, 211, 211); // Light Gray (#D3D3D3)
        Color gradientStart = new Color(218, 230, 241); // #DAE6F1
        Color gradientEnd = new Color(194, 213, 232); // #C2D5E8
        Color defaultColor = new Color(220, 220, 220); // Gray for unselected tabs

        // Gradient for selected tab
        if (isSelected) {
            GradientPaint gradient = new GradientPaint(x, y, gradientStart, x, y + h, gradientEnd);
            g2d.setPaint(gradient);
        } else {
            g2d.setColor(defaultColor);
        }

        // Create polygon shape for the tab
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
        // No hard borders
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