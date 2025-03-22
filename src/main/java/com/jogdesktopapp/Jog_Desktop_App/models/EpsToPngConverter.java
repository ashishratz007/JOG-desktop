package com.jogdesktopapp.Jog_Desktop_App.models;

import javax.swing.*;


import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;

import java.io.*;
import java.util.Iterator;
import ar.com.hjg.pngj.*;

import java.awt.Color;
import java.awt.Graphics2D;

public class EpsToPngConverter {

//    private static final String GS_PATH = "C:\\Program Files\\gs\\gs10.05.0\\bin\\gswin64c.exe"; // Ghostscript path (Windows)
//    private static final String INKSCAPE_PATH = "C:\\Program Files\\Inkscape\\bin\\inkscape.exe"; // Inkscape path (Windows)
	 private static final String GS_PATH = "/opt/homebrew/bin/gs"; // Path to Ghostscript (Mac)
	    private static final String INKSCAPE_PATH = "/opt/homebrew/bin/inkscape"; // Inkscape for PDF to SVG

    /**
     * Open file picker to select an EPS file.
     */
    public static File pickEpsFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an EPS File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("EPS Files", "eps"));

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null; // No file selected
        }
    }

    /**
     * Convert EPS to PNG using Ghostscript.
     * 
     */
    public static boolean convertEpsToPng(File epsFile) {
        try {
            String fileNameWithoutExt = epsFile.getName().replaceFirst("[.][^.]+$", "");
            File pngFile = new File(epsFile.getParent(), fileNameWithoutExt + ".png");

            ProcessBuilder pb = new ProcessBuilder(GS_PATH,
                    "-dNOPAUSE", "-dBATCH",
                    "-sDEVICE=png16m",
                    "-r600", // High resolution
                    "-dEPSCrop",
                    "-sOutputFile=" + pngFile.getAbsolutePath(),
                    epsFile.getAbsolutePath());

            pb.redirectErrorStream(true);
            Process process = pb.start();
            printProcessOutput(process);
            process.waitFor();
            compressPng(pngFile); 
            return pngFile.exists() && pngFile.length() > 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Print output from a process (for debugging).
     */
    private static void printProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    
    /**
     * Compress a PNG file to be less than 100 KB.
     * 
     * @param inputFile The PNG file to be compressed.
     * @return True if compression is successful, false otherwise.
     */
    public static void compressPng(File inputFile) throws IOException {
        // Define output file name
        String outputFilePath = inputFile.getParent() + File.separator +
                inputFile.getName().replace(".png", "_compressed.png");
        File compressedFile = new File(outputFilePath);

        // Read the PNG image
        PngReader pngr = new PngReader(inputFile);
        PngWriter pngw = new PngWriter(compressedFile, pngr.imgInfo, true);

        // Apply max compression
        pngw.setCompLevel(9);
        pngw.setFilterType(FilterType.FILTER_PAETH); // Best filter for small size
        pngw.setFilterPreserve(false); // Allow optimization
//        pngw.setFilterPreserve(false); // Remove metadata

        // Process rows
        for (int row = 0; row < pngr.imgInfo.rows; row++) {
            pngw.writeRow(pngr.readRow());
        }

        // Close the streams
        pngr.end();
        pngw.end();

        // Check and retry compression if needed
        long fileSize = compressedFile.length();
        System.out.println("Compressed size: " + fileSize / 1024 + " KB");

        if (fileSize > 100 * 1024) {
            System.out.println("Recompressing to ensure < 100KB...");
            reCompressPng(compressedFile);
        }
    }

    private static void reCompressPng(File file) throws IOException {
        PngReader pngr = new PngReader(file);
        File smallerFile = new File(file.getParent(), file.getName().replace(".png", "_below100kb.png"));
        PngWriter pngw = new PngWriter(smallerFile, pngr.imgInfo, true);

        pngw.setCompLevel(9);
        pngw.setFilterType(FilterType.FILTER_SUB); // Alternative filter for size reduction
        pngw.setFilterPreserve(false); // Strip metadata

        for (int row = 0; row < pngr.imgInfo.rows; row++) {
            pngw.writeRow(pngr.readRow());
        }

        pngr.end();
        pngw.end();

        System.out.println("Final compressed size: " + smallerFile.length() / 1024 + " KB");
    }
 

     
    /* Test the EPS to PNG/SVG conversion using file picker.
     */
    public static void main() {
        File epsFile = pickEpsFile();
        if (epsFile == null) {
            System.out.println("No file selected.");
            return;
        }

        if (convertEpsToPng(epsFile)) {
            System.out.println("EPS to PNG conversion successful.");
        } else {
            System.out.println("EPS to PNG conversion failed.");
        }
    }
}