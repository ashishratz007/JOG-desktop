package com.jogdesktopapp.Jog_Desktop_App.models;

import javax.swing.*;
import java.io.*;

public class EpsToPngConverter {

    private static final String GS_PATH = "C:\\Program Files\\gs\\gs10.05.0\\bin\\gswin64c.exe"; // Ghostscript path (Windows)
    private static final String INKSCAPE_PATH = "C:\\Program Files\\Inkscape\\bin\\inkscape.exe"; // Inkscape path (Windows)

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

            return pngFile.exists() && pngFile.length() > 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convert EPS to SVG by first converting to PDF (via Ghostscript), then to SVG (via Inkscape).
     */
    public static boolean convertEpsToSvg(File epsFile) {
        try {
            String fileNameWithoutExt = epsFile.getName().replaceFirst("[.][^.]+$", "");
            File pdfFile = new File(epsFile.getParent(), fileNameWithoutExt + ".pdf");
            File svgFile = new File(epsFile.getParent(), fileNameWithoutExt + ".svg");

            // Step 1: Convert EPS to PDF
            ProcessBuilder pb1 = new ProcessBuilder(GS_PATH,
                    "-dNOPAUSE", "-dBATCH",
                    "-sDEVICE=pdfwrite",
                    "-sOutputFile=" + pdfFile.getAbsolutePath(),
                    epsFile.getAbsolutePath());
            pb1.redirectErrorStream(true);
            Process p1 = pb1.start();
            printProcessOutput(p1);
            p1.waitFor();

            // Step 2: Convert PDF to SVG using Inkscape
            ProcessBuilder pb2 = new ProcessBuilder(INKSCAPE_PATH,
                    pdfFile.getAbsolutePath(),
                    "--export-type=svg",
                    "--export-filename=" + svgFile.getAbsolutePath());
            pb2.redirectErrorStream(true);
            Process p2 = pb2.start();
            printProcessOutput(p2);
            p2.waitFor();

            pdfFile.delete(); // Clean up temporary PDF file
            return svgFile.exists() && svgFile.length() > 0;
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
     * Test the EPS to PNG/SVG conversion using file picker.
     */
    public static void main(String[] args) {
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

        if (convertEpsToSvg(epsFile)) {
            System.out.println("EPS to SVG conversion successful.");
        } else {
            System.out.println("EPS to SVG conversion failed.");
        }
    }
}