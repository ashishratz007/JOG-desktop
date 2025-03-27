package com.jogdesktopapp.Jog_Desktop_App;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.*;

/**
 * RedesignUi class provides a user interface for managing redesign files
 * with pagination, date filtering, and status tracking functionality.
 * It follows the same pattern as the Reprint class but works with Redesign data.
 */
public class RedesignUi {
	static GlobalDataClass globalData = GlobalDataClass.getInstance();
    // UI Components
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JPanel statusLabel = new JPanel();
    private JPanel pendingPanel = new JPanel();
    private JPanel completePanel = new JPanel();
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private JPanel pagesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    
    // Data Models
    private RedesignModel pending = new RedesignModel(0, new ArrayList<>());
    private RedesignModel complete = new RedesignModel(0, new ArrayList<>());
    private Date selectedDate = new Date();
    
    // Pagination Constants
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPendingPage = 1;
    private int currentCompletePage = 1;
    
    /**
     * Constructor initializes the UI components and data
     */
    public RedesignUi() {
        // Initialize panels with empty data
        pendingPanel = createPendingTablePanel(new Object[0][3]);
        completePanel = createCompleteTablePanel(new Object[0][3]);
        
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

        JLabel titleLabel = new JLabel("Redesign");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JLabel countLabel = new JLabel("Files for Redesign : " + globalData.redesignPendingData.data.size());
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
     * @param status The status message to display
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
        pending = globalData.redesignPendingData;
//        currentPendingPage = 1; // Reset to first page
        displayPendingPage(currentPendingPage);
        updatePageButtonsForCurrentTab();
    }
    
    /**
     * Fills complete data and resets to first page
     */
    private void fillCompleteData() {
        GlobalDataClass globalData = GlobalDataClass.getInstance();
        complete = globalData.designCompleteData;
//        currentCompletePage = 1; // Reset to first page
        displayCompletePage(currentCompletePage);
        updatePageButtonsForCurrentTab();
    }
    
    /**
     * Displays a specific page of pending items
     * @param pageNumber The page number to display
     */
    private void displayPendingPage(int pageNumber) {
        List<RedesignItem> pageItems = pending.data;
        Object[][] data = new Object[pageItems.size()][3];
        
        for (int i = 0; i < pageItems.size(); i++) {
            RedesignItem file = pageItems.get(i);
            data[i][0] = file.fileName;
            data[i][1] = file.designerName;
            data[i][2] = file.exCode;
        }
        
        refreshPendingTable(data);
    }
    
    /**
     * Displays a specific page of complete items
     * @param pageNumber The page number to display
     */
    private void displayCompletePage(int pageNumber) {
        List<RedesignItem> pageItems = complete.data;
        Object[][] data = new Object[pageItems.size()][3];
        
        for (int i = 0; i < pageItems.size(); i++) {
            RedesignItem file = pageItems.get(i);
            data[i][0] = file.fileName;
            data[i][1] = file.designerName;
            data[i][2] = file.exCode;
        }
        
        refreshCompleteTable(data);
    }
    
    /**
     * Refreshes the pending table with new data
     * @param data The data to display in the table
     */
    private void refreshPendingTable(Object[][] data) {
        pendingPanel.removeAll();
        pendingPanel.add(createPendingTable(data));
        pendingPanel.revalidate();
        pendingPanel.repaint();
    }
    
    /**
     * Refreshes the complete table with new data
     * @param data The data to display in the table
     */
    private void refreshCompleteTable(Object[][] data) {
        completePanel.removeAll();
        completePanel.add(createCompleteTable(data));
        completePanel.revalidate();
        completePanel.repaint();
    }
    
    /**
     * Creates the pending table with download and complete buttons
     * @param data The data to display in the table
     * @return JScrollPane containing the table
     */
    private JScrollPane createPendingTable(Object[][] data) {
        String[] columnNames = {"Name of file", "Designer Name", "Ex code"};
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells non-editable since we're only showing 3 columns
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
        
        return new JScrollPane(table);
    }
    
    /**
     * Creates the complete table (read-only)
     * @param data The data to display in the table
     * @return JScrollPane containing the table
     */
    private JScrollPane createCompleteTable(Object[][] data) {
        String[] columnNames = {"Name of file", "Designer Name", "Ex code"};
        
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
     * Creates a page navigation button
     * @param pageNumber The page number for the button
     * @return Configured JButton
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
     * @param button The clicked button
     * @param pageNumber The page number associated with the button
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
     * @param pageNumber The selected page number
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
     * @param pageNumber The selected page number
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
                    App.globalData.getRedesignData(currentPendingPage);
                }
                else {
                    currentCompletePage = pageNumber;
                    App.globalData.getRedesignCompleteData(currentCompletePage);
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
     * @param totalPages Total number of pages
     * @param currentPage Current page number
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
     * Creates a pending table panel with the specified data
     * @param data The data to display in the table
     * @return JPanel containing the table
     */
    private JPanel createPendingTablePanel(Object[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xC4D7E9));
        
        String[] columnNames = {"Name of file", "Designer Name", "Ex code"};
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells non-editable
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
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates a complete table panel with the specified data
     * @param data The data to display in the table
     * @return JPanel containing the table
     */
    private JPanel createCompleteTablePanel(Object[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xC4D7E9));
        
        String[] columnNames = {"Name of file", "Designer Name", "Ex code"};
        
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
     * @param newData The new pending data to display
     */
    public void updatePendingData(RedesignModel newData) {
        this.pending = newData;
        fillPendingData();
    }
    
    /**
     * Updates complete data and refreshes the view
     * @param newData The new complete data to display
     */
    public void updateCompleteData(RedesignModel newData) {
        this.complete = newData;
        fillCompleteData();
    }
    
    /**
     * Sets the selected date and refreshes data
     * @param date The date to filter by
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
    
    /**
     * Returns the main view panel
     * @return The main JPanel containing the UI
     */
    public JPanel getView() {
        return mainPanel;
    }
}