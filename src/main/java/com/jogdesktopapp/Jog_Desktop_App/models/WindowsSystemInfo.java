package com.jogdesktopapp.Jog_Desktop_App.models;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WindowsSystemInfo {
    private static WindowsSystemInfo instance;
    private String deviceId;
    private String deviceName;
    private Map<String, String> systemInfo;

    private WindowsSystemInfo() {
        this.systemInfo = gatherSystemInfo();
        this.deviceId = generateDeviceId();
        this.deviceName = getWindowsDeviceName();
    }

    public static synchronized WindowsSystemInfo getInstance() {
        if (instance == null) {
            instance = new WindowsSystemInfo();
        }
        return instance;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public Map<String, String> getSystemInfo() {
        return new HashMap<>(systemInfo);
    }

    private Map<String, String> gatherSystemInfo() {
        Map<String, String> info = new HashMap<>();
        
        // Basic system properties
        info.put("os.name", System.getProperty("os.name"));
        info.put("os.version", System.getProperty("os.version"));
        info.put("os.arch", System.getProperty("os.arch"));
        info.put("java.version", System.getProperty("java.version"));
        info.put("user.name", System.getProperty("user.name"));
        info.put("computer.name", System.getenv("COMPUTERNAME"));
        
        // Windows-specific info
        try {
            info.put("windows.product.name", execCommand("wmic os get caption /value"));
            info.put("windows.product.id", execCommand("wmic os get serialnumber /value"));
            info.put("windows.build.number", execCommand("wmic os get buildnumber /value"));
            info.put("cpu.name", execCommand("wmic cpu get name /value"));
            info.put("total.physical.memory", execCommand("wmic computersystem get totalphysicalmemory /value"));
        } catch (Exception e) {
            // Ignore if commands fail
        }
        
        // Runtime info
        Runtime runtime = Runtime.getRuntime();
        info.put("available.processors", String.valueOf(runtime.availableProcessors()));
        info.put("total.memory", String.valueOf(runtime.totalMemory()));
        info.put("free.memory", String.valueOf(runtime.freeMemory()));
        
        return info;
    }

    private String generateDeviceId() {
        try {
            // Get Windows Product ID (more reliable than MAC address which can change)
            String productId = execCommand("wmic csproduct get uuid");
            if (productId != null && !productId.trim().isEmpty()) {
                return "WIN-" + productId.trim().replace(" ", "");
            }
            
            // Fallback to BIOS serial number
            String biosSerial = execCommand("wmic bios get serialnumber");
            if (biosSerial != null && !biosSerial.trim().isEmpty()) {
                return "WIN-BIOS-" + biosSerial.trim().replace(" ", "");
            }
            
            // Final fallback to volume serial and computer name
            String volumeSerial = getVolumeSerial();
            String computerName = System.getenv("COMPUTERNAME");
            return "WIN-FB-" + (computerName != null ? computerName.hashCode() : "") + 
                   (volumeSerial != null ? volumeSerial.hashCode() : "");
        } catch (Exception e) {
            return "WIN-RAND-" + UUID.randomUUID().toString();
        }
    }

    private String getWindowsDeviceName() {
        try {
            // First try computer name
            String computerName = System.getenv("COMPUTERNAME");
            if (computerName != null && !computerName.isEmpty()) {
                return computerName;
            }
            
            // Fallback to host name
            return execCommand("hostname");
        } catch (Exception e) {
            return "Unknown-Windows-Device";
        }
    }

    private String getVolumeSerial() {
        try {
            Process process = Runtime.getRuntime().exec(
                new String[]{"cmd.exe", "/c", "vol c:"});
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

    private String execCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.contains("=")) {
                    output.append(line.trim());
                }
            }
            return output.toString().replaceAll("\n", "").replaceAll("\r", "");
        } catch (Exception e) {
            return null;
        }
    }
}