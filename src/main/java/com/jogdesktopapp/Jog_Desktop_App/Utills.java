package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class Utills {
	static Component space(int value) {
		 // Spacer
        return (Box.createVerticalStrut(value)); // Space between

	}

}

class TableView {
	 String[] titles;
	 Object[][]  children;
	
	public TableView(  String[] titles,Object[][]  children) {
		this.titles = titles;
		this.children = children;
	}
	
     JTable createStyledTable() {
        DefaultTableModel model = new DefaultTableModel(children, titles);
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

	
	
}


