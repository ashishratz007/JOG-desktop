package com.jogdesktopapp.Jog_Desktop_App.models;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class SystemInfoProvider {
    private static SystemInfoProvider instance;
    private String deviceId;
    private Map<String, String> systemInfo;

    private SystemInfoProvider() {
        this.systemInfo = gatherSystemInfo();
        this.deviceId = generateDeviceId();
    }

    public static synchronized SystemInfoProvider getInstance() {
        if (instance == null) {
            instance = new SystemInfoProvider();
        }
        return instance;
    }

    public String getDeviceId() {
        return deviceId;
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
        
        // Runtime info
        Runtime runtime = Runtime.getRuntime();
        info.put("available.processors", String.valueOf(runtime.availableProcessors()));
        info.put("total.memory", String.valueOf(runtime.totalMemory()));
        info.put("free.memory", String.valueOf(runtime.freeMemory()));
        
        return info;
    }

    private String generateDeviceId() {
        try {
            // Combine MAC address and other unique identifiers
            StringBuilder sb = new StringBuilder();
            
            // Get MAC address
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                    }
                    break; // Just use the first valid MAC
                }
            }
            
            // Get volume serial number (Windows) or disk UUID (Linux/Mac)
            String volumeSerial = getVolumeSerial();
            if (volumeSerial != null) {
                sb.append(volumeSerial);
            }
            
            if (sb.length() == 0) {
                // Fallback to a combination of user name and OS info
                sb.append(System.getProperty("user.name"))
                  .append(System.getProperty("os.name"))
                  .append(System.getProperty("os.arch"));
            }
            
            return sb.toString().hashCode() + "";
        } catch (Exception e) {
            // Fallback to a random UUID if all else fails
            return java.util.UUID.randomUUID().toString();
        }
    }

    private String getVolumeSerial() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows - get volume serial number
                Process process = Runtime.getRuntime().exec(
                    new String[]{"cmd.exe", "/c", "vol"});
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Volume Serial Number is")) {
                        return line.split("is")[1].trim();
                    }
                }
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Unix/Linux/Mac - get disk UUID
                Process process = Runtime.getRuntime().exec(
                    new String[]{"sh", "-c", "blkid | awk '{print $5}' | head -n 1"});
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                return reader.readLine();
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return null;
    }
}