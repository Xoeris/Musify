package com.xoeris.android.xesc.system.core.module.media.ux.audio.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.xoeris.android.musify.app.dialog.PermissionDialog;
import com.xoeris.android.musify.app.dialog.PermissionDialog.PermissionDialogListener;
import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    private static final String PREFS_NAME = "permission_prefs";
    private static final String DIALOG_SHOWN_KEY = "dialog_shown";

    // Check and request permissions
    public static void checkAndRequestPermissions(Context context, PermissionDialogListener listener) {
        // Check the shared preferences to see if the dialog has been shown already
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean dialogShown = prefs.getBoolean(DIALOG_SHOWN_KEY, false);

        // If dialog has been shown before, return early to prevent showing it again
        if (dialogShown) {
            listener.onAllPermissionsGranted();  // Permissions already granted, proceed with the logic
            return;
        }

        List<String> permissionsNeeded = new ArrayList<>();

        // Check for necessary permissions
        if (ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("android.permission.POST_NOTIFICATIONS");
        }
        if (ContextCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("android.permission.READ_EXTERNAL_STORAGE");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(context, "android.permission.READ_MEDIA_AUDIO") != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("android.permission.READ_MEDIA_AUDIO");
        }

        // If permissions are needed, show the dialog
        if (!permissionsNeeded.isEmpty()) {
            // Show custom permission dialog
            PermissionDialog.showCustomPermissionDialog(context, v -> {
                // User clicked "Grant" - Request permissions
                ActivityCompat.requestPermissions(
                        (Activity) context,
                        permissionsNeeded.toArray(new String[0]),
                        100
                );
            }, (dialog, which) -> {
                // User clicked "Cancel" - Handle permission denial
                listener.onPermissionsDenied();
            });
        } else {
            // All permissions are already granted
            listener.onAllPermissionsGranted();
            setDialogShown(context, true);  // Mark the dialog as shown in shared preferences
        }
    }

    // Handle permission results
    public static void onRequestPermissionsResult(Context context, int requestCode, String[] permissions, int[] grantResults, PermissionDialogListener listener) {
        if (requestCode == 100) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            // If permissions are granted, update shared preferences to avoid showing the dialog again
            if (allGranted) {
                listener.onAllPermissionsGranted();
                setDialogShown(context, true);  // Set the flag to avoid showing the dialog again
            } else {
                listener.onPermissionsDenied();
            }
        }
    }

    // Save to SharedPreferences that the dialog has been shown
    private static void setDialogShown(Context context, boolean shown) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(DIALOG_SHOWN_KEY, shown);
        editor.apply();
    }
}
