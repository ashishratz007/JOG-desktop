package com.jogdesktopapp.Jog_Desktop_App;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class PrinterModel extends JFrame {

    public JPanel view() {
        JPanel framePanel = createFramePanel();
        JPanel mainPanel = createMainPanel();

        JPanel printerWrapper = createPrinterWrapper();
        JPanel processPanel = createProcessPanel();
        JPanel datePanel = createDatePanel();

        // Assemble main UI
        mainPanel.add(printerWrapper, BorderLayout.NORTH);
        mainPanel.add(new JSeparator(), BorderLayout.CENTER);
        mainPanel.add(processPanel, BorderLayout.CENTER);
        mainPanel.add(datePanel, BorderLayout.SOUTH);

        framePanel.add(mainPanel);
        return framePanel;
    }

    private JPanel createFramePanel() {
        JPanel framePanel = new JPanel(new BorderLayout(10, 10));
        framePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        framePanel.setBackground(AppColors.BACKGROUND_GREY);
        return framePanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(AppColors.WHITE);
        return mainPanel;
    }

    private JPanel createPrinterWrapper() {
        JPanel printerWrapper = new JPanel(new BorderLayout());
        printerWrapper.setBackground(AppColors.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppColors.WHITE);

        JLabel titleLabel = new JLabel("Printer Activity", JLabel.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel statusAware = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        statusAware.setBackground(AppColors.BlueBg);
        statusAware.add(statusChip("Online", Color.GREEN));
        statusAware.add(statusChip("Busy", Color.RED));
        statusAware.add(statusChip("Offline", Color.GRAY));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusAware, BorderLayout.EAST);

        JPanel printerStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        printerStatusPanel.setBackground(AppColors.BACKGROUND_GREY);
        printerStatusPanel.add(createPrinterStatus("Printer 1", Color.GREEN));
        printerStatusPanel.add(createPrinterStatus("Printer 2", Color.RED));
        printerStatusPanel.add(createPrinterStatus("Printer 3", Color.BLUE));
        printerStatusPanel.add(createPrinterStatus("Printer 4", Color.GREEN));
        printerStatusPanel.add(createPrinterStatus("Printer 5", Color.RED));

        printerWrapper.add(headerPanel, BorderLayout.NORTH);
        printerWrapper.add(Box.createVerticalStrut(10));
        printerWrapper.add(printerStatusPanel, BorderLayout.SOUTH);

        return printerWrapper;
    }

    private JPanel createProcessPanel() {
        JPanel processPanel = new JPanel(new BorderLayout());
        processPanel.setBackground(AppColors.WHITE);
        
        JLabel titleLabel = new JLabel("Process", JLabel.LEFT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding to title
        processPanel.add(titleLabel, BorderLayout.NORTH);
        
        processPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Panel padding

        // Define table data
        String[] columnNames = {"  Name", "  Status", "  File"};
        Object[][] data = {
            {"Printer 1", "Busy", "C:\\Program Files (x86) \\Aomet Partition..."},
            {"Printer 1", "Busy", "C:\\Program Files (x86) \\Aomet Partition..."},
            {"Printer 1", "Printing", "C:\\Program Files (x86) \\Aomet Partition..."},
            {"Printer 1", "Completed", "C:\\Program Files (x86) \\Aomet Partition..."},
            {"Printer 1", "Completed", "C:\\Program Files (x86) \\Aomet Partition..."},
            {"Printer 1", "Completed", "C:\\Program Files (x86) \\Aomet Partition..."},
            {"Printer 1", "Completed", "C:\\Program Files (x86) \\Aomet Partition..."}
        };

        JTable table = createStyledTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);

        processPanel.add(scrollPane, BorderLayout.CENTER);
        return processPanel;
    }

    private JTable createStyledTable(Object[][] data, String[] columnNames) {
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        JTable table = new JTable(model);

        table.setRowHeight(35); // Increase row height to fit padding
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);

        // Customize table header with left alignment and border
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBorder(BorderFactory.createLineBorder(Color.decode("#7A8A99"), 1)); // Add border around the header

        // Custom renderer for padding in both header and cells
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding
                label.setHorizontalAlignment(JLabel.LEFT); // Left align column values
                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : AppColors.BACKGROUND_GREY);
                }
                return label;
            }
        };

        // Apply padding to table header and align text to the start (left)
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#7A8A99"))); // Keep column dividers
        headerRenderer.setHorizontalAlignment(JLabel.LEFT); // Left align column names
        headerRenderer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Header padding
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        return table;
    }

    
    private JPanel createDatePanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        datePanel.add(new JLabel("Date: 18/02/2024"));
        return datePanel;
    }

    private JPanel createPrinterStatus(String printerName, Color color) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(198, 226, 255),
                    0, height, Color.WHITE,
                    true
                );

                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        };

        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel printerLabel = new JLabel(printerName);

        JPanel colorBoxPanel = new JPanel();
        colorBoxPanel.setOpaque(false);
        colorBoxPanel.add(createColorBox(color));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(printerLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(colorBoxPanel, gbc);

        return panel;
    }

    private JPanel statusChip(String status, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        panel.setBackground(AppColors.BlueBg);
        JPanel colorBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        colorBoxPanel.add(createColorBox(color));
        panel.add(colorBoxPanel);
        JLabel titleLabel = new JLabel(status);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 10));
        panel.add(titleLabel);
        return panel;
    }

    private JLabel createColorBox(Color color) {
        JLabel colorBox = new JLabel();
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(10, 5));
        return colorBox;
    }
}
