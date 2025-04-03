package com.jogdesktopapp.Jog_Desktop_App.models;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WindowsDeviceInfo {
    private static WindowsDeviceInfo instance;
    private final String deviceId;
    private final String deviceName;
    private final String productId;
    private final Map<String, String> deviceSpecs;

    private WindowsDeviceInfo() {
        this.deviceSpecs = gatherDeviceSpecs();
        this.deviceId = getExactDeviceId();
        this.deviceName = getDeviceName();
        this.productId = getProductId();
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

    private String getExactDeviceId() {
        // Try multiple methods to get the Device ID
        String[] methods = {
            getRegistryDeviceId(),       // First try registry
            getWmiDeviceId(),            // Then try WMI
            getPowershellDeviceId(),     // Then try PowerShell
            generateFallbackDeviceId()   // Final fallback
        };

        // Return the first non-null, non-empty result
        for (String id : methods) {
            if (id != null && !id.isEmpty() && !id.startsWith("ERROR")) {
                return id;
            }
        }
        return "UNKNOWN-DEVICE-ID";
    }

    private String getRegistryDeviceId() {
        try {
            Process process = Runtime.getRuntime().exec(
                "reg query HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Diagnostics\\DiagTrack /v SettingsDeviceId");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("SettingsDeviceId")) {
                    String[] parts = line.split("REG_SZ");
                    if (parts.length > 1) {
                        return parts[1].trim();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return null;
    }

    private String getWmiDeviceId() {
        try {
            Process process = Runtime.getRuntime().exec(
                "wmic csproduct get uuid");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            
            // Skip the header line
            reader.readLine();
            String id = reader.readLine();
            return id != null ? id.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getPowershellDeviceId() {
        try {
            Process process = Runtime.getRuntime().exec(
                "powershell -command \"(Get-CimInstance -Class Win32_ComputerSystemProduct).UUID\"");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String id = reader.readLine();
            return id != null ? id.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String generateFallbackDeviceId() {
        try {
            String computerName = System.getenv("COMPUTERNAME");
            String volumeSerial = getVolumeSerial();
            return "GEN-" + 
                   (computerName != null ? computerName.hashCode() : "") + "-" +
                   (volumeSerial != null ? volumeSerial.hashCode() : "") + "-" +
                   UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            return "ERR-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    private String getVolumeSerial() {
        try {
            Process process = Runtime.getRuntime().exec("cmd /c vol c:");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Volume Serial Number is")) {
                    return line.split("is")[1].trim();
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return null;
    }

    private String getDeviceName1() {
        String name = System.getenv("COMPUTERNAME");
        return name != null ? name : "UNKNOWN-DEVICE-NAME";
    }

    private String getProductId1() {
        try {
            Process process = Runtime.getRuntime().exec(
                "reg query HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion /v ProductId");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("ProductId")) {
                    String[] parts = line.split("REG_SZ");
                    if (parts.length > 1) {
                        return parts[1].trim();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return "UNKNOWN-PRODUCT-ID";
    }

    private Map<String, String> gatherDeviceSpecs() {
        Map<String, String> specs = new HashMap<>();
        
        // Basic system info
        specs.put("computer.name", System.getenv("COMPUTERNAME"));
        specs.put("user.name", System.getProperty("user.name"));
        
        // Hardware specs
        specs.put("cpu", execWMIC("cpu get name"));
        specs.put("cpu.cores", execWMIC("cpu get NumberOfCores"));
        specs.put("cpu.threads", execWMIC("cpu get NumberOfLogicalProcessors"));
        specs.put("memory.total", formatBytes(execWMIC("computersystem get TotalPhysicalMemory")));
        specs.put("gpu", execWMIC("path win32_VideoController get name"));
        specs.put("disk.model", execWMIC("diskdrive get model"));
        specs.put("disk.size", formatBytes(execWMIC("diskdrive get size")));
        
        // System info
        specs.put("os.name", execWMIC("os get caption"));
        specs.put("os.version", execWMIC("os get version"));
        specs.put("os.architecture", execWMIC("os get osarchitecture"));
        specs.put("bios.version", execWMIC("bios get version"));
        specs.put("bios.manufacturer", execWMIC("bios get manufacturer"));
        
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
                    if (!firstLine) output.append("\n");
                    output.append(line);
                    firstLine = false;
                }
            }
            
            String result = output.toString();
            if (result.contains("\n")) {
                String[] parts = result.split("\n");
                if (parts.length > 1) return parts[1].trim();
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
            
            String[] units = {"Bytes", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
            return String.format("%,.1f %s", size/Math.pow(1024, digitGroups), units[digitGroups]);
        } catch (NumberFormatException e) {
            return bytes + " Bytes";
        }
    }
}