package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.*;

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
    private LocalDate startDate = LocalDate.now().minusYears(1);
    private LocalDate endDate = LocalDate.now();
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    
    // Pagination Constants
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPendingPage = 1;
    private int currentCompletePage = 1;
    int maxWidth = 100;
    
    public RedesignUi() {
        pendingPanel = createPendingTablePanel(new Object[0][7]); // Changed to 7 columns (added Complete column)
        completePanel = createCompleteTablePanel(new Object[0][5]);
        initializeUI();
        refreshData();
    }
    
    private void initializeUI() {
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(createTitlePanel(), BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(createDateRangePickerPanel(), BorderLayout.NORTH);
        contentPanel.add(createTabbedPane(), BorderLayout.CENTER);
        contentPanel.add(createPageFilterPanel(), BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setStatusPanel("Idle");
    }
    
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
        statusPanel.add(countLabel);
        titlePanel.add(statusPanel, BorderLayout.EAST);

        return titlePanel;
    }
    
    private JPanel createDateRangePickerPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Start Date components
        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        SpinnerDateModel startModel = new SpinnerDateModel(
            Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            null, null, Calendar.DAY_OF_MONTH);
        startDateSpinner = new JSpinner(startModel);
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        
        startDateSpinner.addChangeListener(e -> {
            Date selectedDate = (Date) startDateSpinner.getValue();
            startDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (startDate.isAfter(endDate)) {
                endDate = startDate;
                endDateSpinner.setValue(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            refreshDataForDateRange();
        });

        // Space between date pickers
        Component rigidArea = Box.createRigidArea(new Dimension(100, 0));

        // End Date components
        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        SpinnerDateModel endModel = new SpinnerDateModel(
            Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            null, null, Calendar.DAY_OF_MONTH);
        endDateSpinner = new JSpinner(endModel);
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        
        endDateSpinner.addChangeListener(e -> {
            Date selectedDate = (Date) endDateSpinner.getValue();
            endDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (endDate.isBefore(startDate)) {
                startDate = endDate;
                startDateSpinner.setValue(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            refreshDataForDateRange();
        });

        panel.add(startDateLabel);
        panel.add(startDateSpinner);
        panel.add(rigidArea);
        panel.add(endDateLabel);
        panel.add(endDateSpinner);

        return panel;
    }
    
    private JTabbedPane createTabbedPane() {
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 10));
        tabbedPane.addTab("Pending", pendingPanel);
        tabbedPane.addTab("Complete", completePanel);
        tabbedPane.setUI(new CustomTabUIReprint());
        
        tabbedPane.addChangeListener(e -> {
            tabbedPaneChanged();
        });
        
        return tabbedPane;
    }
    
    private void tabbedPaneChanged() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == 0) {
            // When switching to Pending tab
            displayPendingPage(currentPendingPage);
            updatePageButtons(pending.pageCount(), currentPendingPage);
        } else {
            // When switching to Complete tab
            displayCompletePage(currentCompletePage);
            updatePageButtons(complete.pageCount(), currentCompletePage);
        }
    }
    
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
    
    public void refreshData() {
        fillPendingData();
        fillCompleteData();
    }
    
    private void fillPendingData() {
        pending = globalData.redesignPendingData;
        displayPendingPage(currentPendingPage);
        updatePageButtonsForCurrentTab();
    }
    
    private void fillCompleteData() {
        complete = globalData.designCompleteData;
        displayCompletePage(currentCompletePage);
        updatePageButtonsForCurrentTab();
    }
    
    private void displayPendingPage(int pageNumber) {
        if (pending == null || pending.data == null) {
            System.err.println("Pending data is null");
            return;
        }
        
        List<RedesignItem> pageItems = pending.data;
        Object[][] data = new Object[pageItems.size()][7]; // Changed to 7 columns
        
        for (int i = 0; i < pageItems.size(); i++) {
            RedesignItem file = pageItems.get(i);
            data[i][0] = file.fileName;
            data[i][1] = file.designerName;
            data[i][2] = file.exCode;
            data[i][3] = formatDate(file.created_on);
            data[i][4] = file.synologyPath + "," + file.file_id;
            data[i][5] = file.redesignId; // For Complete action
            data[i][6] = file.note != null ? file.note : "";
        }
        
        refreshPendingTable(data);
    }
    
    private void displayCompletePage(int pageNumber) {
        if (complete == null || complete.data == null) {
            System.err.println("Complete data is null");
            return;
        }
        
        List<RedesignItem> pageItems = complete.data;
        Object[][] data = new Object[pageItems.size()][5];
        
        for (int i = 0; i < pageItems.size(); i++) {
            RedesignItem file = pageItems.get(i);
            data[i][0] = file.fileName;
            data[i][1] = file.designerName;
            data[i][2] = file.exCode;
            data[i][3] = formatDate(file.created_on);
            data[i][4] = file.note != null ? file.note : "";
        }
        
        refreshCompleteTable(data);
    }
    
    private String formatDate(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime;
            
            try {
                dateTime = LocalDateTime.parse(dateString, formatter);
            } catch (Exception e) {
                dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            
            dateTime = dateTime.plusHours(7);
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            System.err.println("Error formatting date: " + dateString + ", error: " + e.getMessage());
            return dateString;
        }
    }
    
    private void refreshPendingTable(Object[][] data) {
        pendingPanel.removeAll();
        pendingPanel.add(createPendingTable(data));
        pendingPanel.revalidate();
        pendingPanel.repaint();
    }
    
    private void refreshCompleteTable(Object[][] data) {
        completePanel.removeAll();
        completePanel.add(createCompleteTable(data));
        completePanel.revalidate();
        completePanel.repaint();
    }
    
    private JScrollPane createPendingTable(Object[][] data) {
        String[] columnNames = {"Name of file", "Designer Name", "Ex code", "Date", "Download", "Set to Complete", "Note"};
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5 || column == 6; // Download, Complete, and Note columns are editable
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
        
        // Set fixed column widths
        table.getColumnModel().getColumn(4).setPreferredWidth(maxWidth); // Download
        table.getColumnModel().getColumn(4).setMaxWidth(maxWidth);
        table.getColumnModel().getColumn(5).setPreferredWidth(maxWidth); // Complete
        table.getColumnModel().getColumn(5).setMaxWidth(maxWidth);
        table.getColumnModel().getColumn(6).setPreferredWidth(maxWidth); // Note
        table.getColumnModel().getColumn(6).setMaxWidth(maxWidth);
        
        configureDownloadColumn(table);
        configureCompleteColumn(table);
        configureNoteColumn(table);
        
        return new JScrollPane(table);
    }
    
    private JScrollPane createCompleteTable(Object[][] data) {
        String[] columnNames = {"Name of file", "Designer Name", "Ex code", "Date", "Note"};
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Note column is editable
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
        
        // Set fixed column width for Note column
        table.getColumnModel().getColumn(4).setPreferredWidth(maxWidth);
        table.getColumnModel().getColumn(4).setMaxWidth(maxWidth);
        
        configureNoteColumn(table);
        
        return new JScrollPane(table);
    }
    
    private void configureDownloadColumn(JTable table) {
        table.getColumnModel().getColumn(4).setCellRenderer(getButtonRenderer("src/main/resources/icons/download.png"));
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String actionCommand = e.getActionCommand();
                String[] parts = actionCommand.split(",(?=[^,]+$)");
                
                if (parts.length == 2) {
                    String filePath = parts[0].trim();
                    String fileId = parts[1].trim();
                    
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            App.sftpClient.pickAndDownloadFile(fileId, filePath);
                            return null;
                        }
                        
                        @Override
                        protected void done() {
                            System.out.println("File download completed for ID: " + fileId);
                        }
                    }.execute();
                } else {
                    System.err.println("Invalid action command format. Expected 'path,id'");
                }
            }
        }, "src/main/resources/icons/download.png"));
    }
    
    private void configureCompleteColumn(JTable table) {
        table.getColumnModel().getColumn(5).setCellRenderer(getButtonRenderer("src/main/resources/icons/complete.png"));
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
                            ApiCalls.markComplete(false, e.getActionCommand());
                            App.globalData.getRedesignData(currentPendingPage, startDate, endDate);
                            App.globalData.getRedesignCompleteData(currentCompletePage, startDate, endDate);
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
            }
        }, "src/main/resources/icons/complete.png"));
    }
    
    private void configureNoteColumn(JTable table) {
        int noteColumn = table.getColumnCount() - 1; // Note is always last column
        table.getColumnModel().getColumn(noteColumn).setCellRenderer(getButtonRenderer("src/main/resources/icons/note.png"));
        table.getColumnModel().getColumn(noteColumn).setCellEditor(new ButtonEditor(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String note = e.getActionCommand();
                if (note != null && !note.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        null, 
                        note, 
                        "Note", 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        }, "src/main/resources/icons/note.png"));
    }
    
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
    
    private void handlePageButtonClick(JButton button, int pageNumber) {
        for (Component comp : pagesPanel.getComponents()) {
            if (comp instanceof JButton) {
                comp.setBackground(Color.GRAY);
            }
        }
        
        button.setBackground(Color.BLUE);
        handlePageSelection(pageNumber);
    }
    
    private void handlePageSelection(int pageNumber) {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == 0) {
            currentPendingPage = pageNumber;
            loadPendingData(pageNumber);
        } else {
            currentCompletePage = pageNumber;
            loadCompleteData(pageNumber);
        }
        logPageSelection(pageNumber);
    }
    
    private void loadPendingData(int pageNumber) {
        setStatusPanel("Loading pending data...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    App.globalData.getRedesignData(pageNumber, startDate, endDate);
                } catch (Exception e) {
                    System.err.println("Error loading pending data: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    setStatusPanel("Idle");
                    fillPendingData();
                });
            }
        }.execute();
    }

    private void loadCompleteData(int pageNumber) {
        setStatusPanel("Loading complete data...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    App.globalData.getRedesignCompleteData(pageNumber, startDate, endDate);
                } catch (Exception e) {
                    System.err.println("Error loading complete data: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    setStatusPanel("Idle");
                    fillCompleteData();
                });
            }
        }.execute();
    }
    
    private void logPageSelection(int pageNumber) {
        statusLabel.setToolTipText("Selected Page: " + pageNumber);
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        System.out.println("[" + timestamp + "] Page selected: " + pageNumber);
    }
    
    private void updatePageButtonsForCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        int totalPages, currentPage;
        
        if (selectedIndex == 0) {
            totalPages = pending.pageCount();
            currentPage = currentPendingPage;
        } else {
            totalPages = complete.pageCount();
            currentPage = currentCompletePage;
        }
        
        updatePageButtons(totalPages, currentPage);
    }
    
    private void updatePageButtons(int totalPages, int currentPage) {
        pagesPanel.removeAll();
        
        // Add left arrow button if there are more than 10 pages
        if (totalPages > 10) {
            JButton leftArrow = createArrowButton("<", currentPage == 1);
            leftArrow.addActionListener(e -> {
                int newPage = Math.max(1, currentPage - 1);
                handlePageSelection(newPage);
            });
            pagesPanel.add(leftArrow);
        }

        // Calculate page range to display (show 10 pages at a time)
        int startPage = 1;
        int endPage = totalPages;
        
        if (totalPages > 10) {
            startPage = Math.max(1, currentPage - 5);
            endPage = Math.min(totalPages, currentPage + 4);
            
            // Adjust if we're near the start or end
            if (startPage == 1) {
                endPage = Math.min(10, totalPages);
            }
            if (endPage == totalPages) {
                startPage = Math.max(1, totalPages - 9);
            }
        }

        // Add page number buttons
        for (int i = startPage; i <= endPage; i++) {
            JButton button = createPageButton(i);
            if (i == currentPage) {
                button.setBackground(Color.BLUE);
            }
            pagesPanel.add(button);
        }

        // Add right arrow button if there are more than 10 pages
        if (totalPages > 10) {
            JButton rightArrow = createArrowButton(">", currentPage == totalPages);
            rightArrow.addActionListener(e -> {
                int newPage = Math.min(totalPages, currentPage + 1);
                handlePageSelection(newPage);
            });
            pagesPanel.add(rightArrow);
        }
        
        pagesPanel.revalidate();
        pagesPanel.repaint();
    }
    
    private JButton createArrowButton(String text, boolean isDisabled) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(25, 25));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(isDisabled ? Color.LIGHT_GRAY : Color.BLUE);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setEnabled(!isDisabled);
        return button;
    }
    
    private TableCellRenderer getButtonRenderer(String iconPath) {
        return (table, value, isSelected, hasFocus, row, column) -> createIconButton(iconPath);
    }
    
    private JButton createIconButton(String iconPath) {
        JButton button = new JButton(new ImageIcon(iconPath));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JPanel createPendingTablePanel(Object[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xC4D7E9));
        
        String[] columnNames = {"Name of file", "Designer Name", "Ex code", "Date", "Download", "Set to Complete", "Note"};
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5 || column == 6;
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
        
        // Set fixed column widths
        table.getColumnModel().getColumn(4).setPreferredWidth(maxWidth); // Download
        table.getColumnModel().getColumn(4).setMaxWidth(maxWidth);
        table.getColumnModel().getColumn(5).setPreferredWidth(maxWidth); // Complete
        table.getColumnModel().getColumn(5).setMaxWidth(maxWidth);
        table.getColumnModel().getColumn(6).setPreferredWidth(maxWidth); // Note
        table.getColumnModel().getColumn(6).setMaxWidth(maxWidth);
        
        configureDownloadColumn(table);
        configureCompleteColumn(table);
        configureNoteColumn(table);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCompleteTablePanel(Object[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xC4D7E9));
        
        String[] columnNames = {"Name of file", "Designer Name", "Ex code", "Date", "Note"};
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
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
        
        // Set fixed column width for Note column
        table.getColumnModel().getColumn(4).setPreferredWidth(maxWidth);
        table.getColumnModel().getColumn(4).setMaxWidth(maxWidth);
        
        configureNoteColumn(table);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
    
    public void updatePendingData(RedesignModel newData) {
        this.pending = newData;
        fillPendingData();
    }
    
    public void updateCompleteData(RedesignModel newData) {
        this.complete = newData;
        fillCompleteData();
    }
    
    public void setStartDate(LocalDate date) {
        this.startDate = date;
        startDateSpinner.setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        refreshDataForDateRange();
    }

    public void setEndDate(LocalDate date) {
        this.endDate = date;
        endDateSpinner.setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        refreshDataForDateRange();
    }

    private void refreshDataForDateRange() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == 0) {
            currentPendingPage = 1;
            loadPendingData(currentPendingPage);
        } else {
            currentCompletePage = 1;
            loadCompleteData(currentCompletePage);
        }
    }
    
    public JPanel getView() {
        return mainPanel;
    }
}