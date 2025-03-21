package com.jogdesktopapp.Jog_Desktop_App.models;

import javax.swing.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;

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
    public static boolean compressPng(File inputFile) {
        try {
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                System.err.println("Error: Could not read image file.");
                return false;
            }

            float compressionQuality = 1.0f; // Start with high quality
            long fileSize;
            do {
                compressionQuality -= 0.05f; // Reduce quality step by step
                if (compressionQuality < 0.1f) {
                    System.err.println("Cannot compress further while maintaining quality.");
                    return false;
                }

                // Create a temporary file for compression
                File tempFile = new File(inputFile.getParent(), "temp_compressed.png");

                // Compress and write to temp file
                fileSize = writeCompressedImage(image, tempFile, compressionQuality);
                System.out.println("Compression quality: " + compressionQuality + " | File size: " + fileSize + " bytes");

                // Replace the original file if the compression succeeded
                if (fileSize < 100 * 1024) {
                    if (inputFile.delete() && tempFile.renameTo(inputFile)) {
                        return true;
                    } else {
                        System.err.println("Failed to replace the original file.");
                        return false;
                    }
                }

            } while (fileSize > 100 * 1024); // Ensure file is less than 100 KB

            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Write the compressed image file.
     */
    private static long writeCompressedImage(BufferedImage image, File outputFile, float quality) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality); // Apply compression quality

        try (OutputStream os = new FileOutputStream(outputFile);
             ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        }

        writer.dispose();
        return outputFile.length(); // Return file size
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

        if (convertEpsToPng(epsFile)) {
            System.out.println("EPS to PNG conversion successful.");
        } else {
            System.out.println("EPS to PNG conversion failed.");
        }
    }
}