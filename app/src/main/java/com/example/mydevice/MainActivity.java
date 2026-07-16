package com.example.mydevice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    // UI Components
    TextView tvInfo;
    Button btnSend;
    Button btnRefresh;
    
    // Firebase Components
    DatabaseReference databaseRef;
    FirebaseAuth firebaseAuth;
    
    // Permission request code
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    // Required permissions from manifest
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.QUERY_ALL_PACKAGES
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI Components
        tvInfo = findViewById(R.id.tvInfo);
        btnSend = findViewById(R.id.btnSend);
        btnRefresh = findViewById(R.id.btnRefresh);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("device_logs");

        // Request permissions for Android 6.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestRequiredPermissions();
        } else {
            initializeApp();
        }

        // Button Click Listeners
        btnSend.setOnClickListener(v -> sendDataToFirebase());
        
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> displayDeviceInfo());
        }
    }

    /**
     * Check and request required permissions
     */
    private void requestRequiredPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            initializeApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;

            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                Toast.makeText(this, "All permissions granted! ✅", Toast.LENGTH_SHORT).show();
                initializeApp();
            } else {
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show();
                initializeApp(); // Initialize anyway with limited features
            }
        }
    }

    /**
     * Initialize app after permissions are handled
     */
    private void initializeApp() {
        // Auto Guest Login
        firebaseAuth.signInAnonymously()
            .addOnSuccessListener(authResult -> {
                Toast.makeText(this, "Anonymous login successful ✅", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        // Display device information
        displayDeviceInfo();
    }

    /**
     * Display device information on screen
     */

   // IP বের করার জন্য
  private String getLocalIpAddress() {
    try {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    return inetAddress.getHostAddress();
                }
            }
        }
    } catch (SocketException ex) {
        ex.printStackTrace();
    }
    return "IP Not Found";
}

// Network Type বের করার জন্য
private String getNetworkType() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = cm.getActiveNetworkInfo();
    return info != null ? info.getTypeName() : "No Internet";
}


    
    private void displayDeviceInfo() {
        String info = "════════════════════════════\n" +
                      "📱 DEVICE INFORMATION 📱\n" +
                      "════════════════════════════\n\n" +
                      "Device Model: " + Build.MODEL + "\n" +
                      "Android Version: " + Build.VERSION.RELEASE + "\n" +
                      "Brand: " + Build.BRAND + "\n" +
                      "SDK Level: " + Build.VERSION.SDK_INT + "\n" +
                      "Device: " + Build.DEVICE + "\n" +
                      "Hardware: " + Build.HARDWARE + "\n" +
                      "Product: " + Build.PRODUCT + "\n" +
                      "Manufacturer: " + Build.MANUFACTURER + "\n" +
                      "Network Type: " + getNetworkType() + "\n\n" +
                      "════════════════════════════";
        
        tvInfo.setText(info);
    }

    /**
     * Send device data to Firebase Realtime Database
     */
    private void sendDataToFirebase() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // Create data HashMap
        HashMap<String, String> deviceData = new HashMap<>();
        deviceData.put("event", "send_button_click");
        deviceData.put("device_model", Build.MODEL);
        deviceData.put("android_version", Build.VERSION.RELEASE);
        deviceData.put("brand", Build.BRAND);
        deviceData.put("sdk_level", String.valueOf(Build.VERSION.SDK_INT));
        deviceData.put("event", "ip_check");
        deviceData.put("brand", Build.BRAND);
        deviceData.put("ip_address", getLocalIpAddress()); // IP এখানে
        deviceData.put("time", time);
        deviceData.put("device", Build.DEVICE);
        deviceData.put("hardware", Build.HARDWARE);
        deviceData.put("manufacturer", Build.MANUFACTURER);
        deviceData.put("network_type", getNetworkType());
        deviceData.put("timestamp", timestamp);
        deviceData.put("user_id", firebaseAuth.getCurrentUser() != null ? 
                firebaseAuth.getCurrentUser().getUid() : "anonymous");

        // Send to Firebase
        databaseRef.push().setValue(deviceData)
            .addOnSuccessListener(unused -> {
                Toast.makeText(MainActivity.this, 
                    "Data saved to Firebase ✅", 
                    Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, 
                    "Failed: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Get network type (WiFi, Mobile, or No Internet)
     */
    private String getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null ? info.getTypeName() : "No Internet";
        }
        return "No Internet";
    }
}

