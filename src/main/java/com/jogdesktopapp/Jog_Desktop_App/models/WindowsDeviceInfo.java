package com.jogdesktopapp.Jog_Desktop_App.models;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class WindowsDeviceInfo {
    private static WindowsDeviceInfo instance;
    private final String deviceId;
    private final String deviceName;
    private final String productId;
    private final Map<String, String> deviceSpecs;

    private WindowsDeviceInfo() {
        this.deviceSpecs = gatherDeviceSpecs();
        this.deviceId = getWindowsDeviceId();
        this.deviceName = getWindowsDeviceName();
        this.productId = getWindowsProductId();
    }

    public static synchronized WindowsDeviceInfo getInstance() {
        if (instance == null) {
            instance = new WindowsDeviceInfo();
        }
        return instance;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getProductId() {
        return productId;
    }

    public Map<String, String> getDeviceSpecs() {
        return new HashMap<>(deviceSpecs);
    }

    private String getWindowsDeviceId() {
        try {
            // This gets the exact Device ID shown in Windows Settings
            Process process = Runtime.getRuntime().exec("powershell -command \"(Get-CimInstance -Class Win32_ComputerSystemProduct).UUID\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String id = reader.readLine();
            return id != null ? id.trim() : "UNKNOWN-DEVICE-ID";
        } catch (Exception e) {
            return "ERROR-GETTING-DEVICE-ID";
        }
    }

    private String getWindowsDeviceName() {
        try {
            // This gets the exact Device Name shown in Windows Settings
            String name = System.getenv("COMPUTERNAME");
            return name != null ? name : "UNKNOWN-DEVICE-NAME";
        } catch (Exception e) {
            return "ERROR-GETTING-DEVICE-NAME";
        }
    }

    private String getWindowsProductId() {
        try {
            // This gets the exact Product ID shown in Windows Settings
            Process process = Runtime.getRuntime().exec("powershell -command \"(Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion').ProductId\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String id = reader.readLine();
            return id != null ? id.trim() : "UNKNOWN-PRODUCT-ID";
        } catch (Exception e) {
            return "ERROR-GETTING-PRODUCT-ID";
        }
    }

    private Map<String, String> gatherDeviceSpecs() {
        Map<String, String> specs = new HashMap<>();
        
        // Basic Windows identification
        specs.put("computer.name", System.getenv("COMPUTERNAME"));
        specs.put("user.name", System.getProperty("user.name"));
        
        // Hardware specifications
        specs.put("cpu", execWMIC("cpu get name"));
        specs.put("cpu.cores", execWMIC("cpu get NumberOfCores"));
        specs.put("cpu.threads", execWMIC("cpu get NumberOfLogicalProcessors"));
        specs.put("memory.total", formatBytes(execWMIC("computersystem get TotalPhysicalMemory")));
        specs.put("gpu", execWMIC("path win32_VideoController get name"));
        
        // Disk information
        specs.put("disk.model", execWMIC("diskdrive get model"));
        specs.put("disk.size", formatBytes(execWMIC("diskdrive get size")));
        
        // System information
        specs.put("bios.version", execWMIC("bios get version"));
        specs.put("bios.manufacturer", execWMIC("bios get manufacturer"));
        specs.put("os.name", execWMIC("os get caption"));
        specs.put("os.version", execWMIC("os get version"));
        specs.put("os.architecture", execWMIC("os get osarchitecture"));
        
        return specs;
    }

    private String execWMIC(String query) {
        try {
            Process process = Runtime.getRuntime().exec("wmic " + query);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            
            StringBuilder output = new StringBuilder();
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (!firstLine) {
                        output.append("\n");
                    }
                    output.append(line);
                    firstLine = false;
                }
            }
            
            // Clean up the output
            String result = output.toString();
            if (result.contains("\n")) {
                String[] parts = result.split("\n");
                if (parts.length > 1) {
                    return parts[1].trim();
                }
            }
            return result.isEmpty() ? null : result;
        } catch (Exception e) {
            return null;
        }
    }

    private String formatBytes(String bytes) {
        try {
            if (bytes == null || bytes.isEmpty()) return "Unknown";
            long size = Long.parseLong(bytes.trim());
            if (size == 0) return "0 Bytes";
            
            String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
            return String.format("%,.1f %s", size/Math.pow(1024, digitGroups), 
                               units[digitGroups]);
        } catch (NumberFormatException e) {
            return bytes + " Bytes";
        }
    }
}