package com.jogdesktopapp.Jog_Desktop_App.models;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

import javax.swing.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;

public class EpsToPngConverter {

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
     */
    public static boolean convertEpsToPng(File epsFile, File pngFile) {
        try {
        	ProcessBuilder pb = new ProcessBuilder(GS_PATH,
        	        "-dNOPAUSE", "-dBATCH",
        	        "-sDEVICE=png16m", // Use png16m instead of pngalpha
        	        "-r600", // Increase resolution for better output
        	        "-dEPSCrop", // Ensure content is correctly cropped
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
    public static boolean convertEpsToSvg(File epsFile, File svgFile) {
        try {
            File pdfFile = new File(epsFile.getParent(), "temp.pdf");

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
    public static void main() {
        File epsFile = pickEpsFile();
        if (epsFile == null) {
            System.out.println("No file selected.");
            return;
        }

        File pngFile = new File(epsFile.getParent(), "output.png");
        File svgFile = new File(epsFile.getParent(), "output.svg");

        // Convert EPS to PNG
        if (convertEpsToPng(epsFile, pngFile)) {
            System.out.println("EPS to PNG conversion successful: " + pngFile.getAbsolutePath());
        } else {
            System.out.println("EPS to PNG conversion failed.");
        }

        // Convert EPS to SVG
        if (convertEpsToSvg(epsFile, svgFile)) {
            System.out.println("EPS to SVG conversion successful: " + svgFile.getAbsolutePath());
        } else {
            System.out.println("EPS to SVG conversion failed.");
        }
    }
}