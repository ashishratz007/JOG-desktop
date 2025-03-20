package com.jogdesktopapp.Jog_Desktop_App;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PrinterStatusChecker {

    public static void main(String[] args) {
        PrinterStatusChecker checker = new PrinterStatusChecker();
        checker.scanNetworkForPrinters();
    }

    // Scan local network for printers
    public void scanNetworkForPrinters() {
        try {
            String localIP = getLocalIPAddress();
            String subnet = localIP.substring(0, localIP.lastIndexOf(".")); // Extract subnet (e.g., 192.168.1)

            System.out.println("Scanning network for Mimaki, Oric, and Grando printers in: " + subnet + ".x");
            List<String> printers = scanSubnet(subnet);

            if (printers.isEmpty()) {
                System.out.println("No Mimaki, Oric, or Grando printers found.");
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

    // Get local IP address
    private String getLocalIPAddress() throws IOException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    // Scan the entire subnet for active printers
    private List<String> scanSubnet(String subnet) {
        List<String> foundPrinters = new ArrayList<>();

        for (int i = 1; i < 255; i++) { // Scan IPs from 1 to 254
            String ip = subnet + "." + i;
            if (isPrinterOnline(ip)) {
                foundPrinters.add(ip);
            }
        }
        return foundPrinters;
    }

    // Check if an IP is a printer (Mimaki, Oric, Grando) by testing printer ports
    private boolean isPrinterOnline(String ip) {
        int[] printerPorts = {9100, 515, 631}; // Common printing ports (RAW, LPD, IPP)

        for (int port : printerPorts) {
            try (Socket socket = new Socket(ip, port)) {
                System.out.println("Printer found at: " + ip + " on port " + port);
                return true;
            } catch (IOException ignored) {
                // No response means it's not a printer or it's offline
            }
        }
        return false;
    }
}