package com.lunartok.logcollector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class LogCollector {

    private final Context context;

    public LogCollector(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void collectAndSendLogs(String serverUrl) {
        try {

            String androidVersion = "Android " + Build.VERSION.RELEASE + ", UI Version: " + Build.VERSION.SDK_INT;
            String ipv4Address = getIPAddress(true);
            String ipv6Address = getIPAddress(false);
            String wifiMacAddress = getWiFiMACAddress();
            String grantedPermissions = getGrantedPermissions();
            long appUsageDuration = getAppUsageDuration();
            String appVersion = getAppVersion();
            String appInstallDate = getAppInstallationDate();

            String logFileName = "log_" + UUID.randomUUID().toString() + ".txt";

            String log = "Log File: " + logFileName + "\n"
                    + "Android Version: " + androidVersion + "\n"
                    + "IPv4 Address: " + ipv4Address + "\n"
                    + "IPv6 Address: " + ipv6Address + "\n"
                    + "WiFi MAC Address: " + wifiMacAddress + "\n"
                    + "Granted Permissions: " + grantedPermissions + "\n"
                    + "App Usage Duration: " + appUsageDuration + " seconds\n"
                    + "App Version: " + appVersion + "\n"
                    + "App Installation Date: " + appInstallDate;

            sendLogToServer(serverUrl, log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (Network network : cm.getAllNetworks()) {
                
            }
            return "Unavailable";
        } catch (Exception e) {
            e.printStackTrace();
            return "Unavailable";
        }
    }

    private String getWiFiMACAddress() {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unavailable";
        }
    }

    private String getGrantedPermissions() {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            StringBuilder grantedPermissions = new StringBuilder();
            if (packageInfo.requestedPermissions != null) {
                for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
                    if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                        grantedPermissions.append(packageInfo.requestedPermissions[i]).append(", ");
                    }
                }
            }
            return grantedPermissions.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unavailable";
        }
    }

    private long getAppUsageDuration() {

        return 0;
    }

    private String getAppVersion() {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Unavailable";
        }
    }

    private String getAppInstallationDate() {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            long installTime = packageInfo.firstInstallTime;
            return android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", installTime).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unavailable";
        }
    }

    private void sendLogToServer(String serverUrl, String log) {
        new Thread(() -> {
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(log.getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                System.out.println("Response Code: " + responseCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}