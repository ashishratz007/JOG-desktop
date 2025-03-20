package com.jogdesktopapp.Jog_Desktop_App;


import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class PrinterStatusChecker {

    public static void main(String[] args) {
        PrinterStatusChecker checker = new PrinterStatusChecker();
        
        System.out.println("Checking locally connected printers...");
        checker.checkLocalPrinters();
        
        System.out.println("\nScanning network for printers...");
        checker.scanNetworkPrinters();
    }

    // Method to check locally connected printers
    public void checkLocalPrinters() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        if (printServices.length == 0) {
            System.out.println("No local printers found.");
        } else {
            for (PrintService printer : printServices) {
                System.out.println("Local Printer: " + printer.getName());
            }
        }
    }

    // Method to scan for network printers
    public void scanNetworkPrinters() {
        try {
            String localIP = getLocalIPAddress();
            String subnet = localIP.substring(0, localIP.lastIndexOf(".")); // Extract subnet (e.g., 192.168.1)

            System.out.println("Scanning network for printers in: " + subnet + ".x");
            List<String> printers = scanNetworkForPrinters(subnet);

            if (printers.isEmpty()) {
                System.out.println("No network printers found.");
            } else {
                System.out.println("Found network printers:");
                for (String printer : printers) {
                    System.out.println(" - " + printer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get local IP address of the machine
    private String getLocalIPAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    // Scan the subnet for active network printers
    private List<String> scanNetworkForPrinters(String subnet) {
        List<String> foundPrinters = new ArrayList<>();

        for (int i = 1; i < 255; i++) { // Scan IPs from 1 to 254
            String ip = subnet + "." + i;
            if (isPrinterOnline(ip)) {
                foundPrinters.add(ip);
            }
        }
        return foundPrinters;
    }

    // Check if a device is a printer by connecting to common printer ports
    private boolean isPrinterOnline(String ip) {
        int[] printerPorts = {9100, 515, 631, 80}; // RAW printing, LPR, IPP, HTTP (for web interfaces)

        for (int port : printerPorts) {
            try (Socket socket = new Socket(ip, port)) {
                System.out.println("Printer found at: " + ip + " on port " + port);
                return true;
            } catch (IOException ignored) {
                // No response means it's not a printer or offline
            }
        }
        return false;
    }
}
