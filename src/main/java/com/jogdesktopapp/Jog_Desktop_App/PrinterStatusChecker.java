package com.jogdesktopapp.Jog_Desktop_App;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class PrinterStatusChecker {

    public static void main(String[] args) {
        PrinterStatusChecker checker = new PrinterStatusChecker();
        checker.scanNetworkForPrinters();
    }

    public void scanNetworkForPrinters() {
        try {
            String localIP = getLocalIPAddress();
            String subnet = localIP.substring(0, localIP.lastIndexOf(".")); // Extract subnet (e.g., 192.168.1)

            System.out.println("Scanning network for printers in: " + subnet + ".x");
            List<String> printers = scanSubnet(subnet);

            if (printers.isEmpty()) {
                System.out.println("No printers found.");
            } else {
                System.out.println("Found network printers:");
                for (String printerIP : printers) {
                    System.out.println(" - " + printerIP);
                    getPrinterStatus(printerIP);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLocalIPAddress() throws IOException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private List<String> scanSubnet(String subnet) {
        List<String> foundPrinters = new ArrayList<>();

        for (int i = 1; i < 255; i++) { // Scan IPs from 1 to 254
            String ip = subnet + "." + i;
            if (pingDevice(ip)) {
                foundPrinters.add(ip);
            }
        }
        return foundPrinters;
    }

    private boolean pingDevice(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (address.isReachable(500)) { // Ping with 500ms timeout
                System.out.println("Device found at: " + ip);
                return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    // Use SNMP to check printer status
    private void getPrinterStatus(String printerIP) {
        try {
            String oid = ".1.3.6.1.2.1.25.3.5.1.2.1"; // Printer status OID
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            Address targetAddress = new UdpAddress(printerIP + "/161");
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public")); // Default SNMP community
            target.setAddress(targetAddress);
            target.setRetries(2);
            target.setTimeout(1000);
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            ResponseEvent response = snmp.get(pdu, target);
            if (response.getResponse() != null) {
                String status = response.getResponse().get(0).toValueString();
                System.out.println("Printer at " + printerIP + " is: " + parseStatus(status));
            } else {
                System.out.println("No SNMP response from: " + printerIP);
            }

            snmp.close();
        } catch (Exception e) {
            System.out.println("Error getting status for: " + printerIP);
        }
    }

    private String parseStatus(String statusCode) {
        switch (statusCode) {
            case "3": return "Idle";
            case "4": return "Printing";
            case "5": return "Offline";
            default: return "Unknown";
        }
    }
}
