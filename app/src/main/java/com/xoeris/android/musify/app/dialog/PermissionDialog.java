package com.xoeris.android.musify.app.dialog;  // Your package

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.xoeris.android.musify.R;

@SuppressWarnings("all")
public class PermissionDialog {

    public static void showCustomPermissionDialog(Context context,
                                                  View.OnClickListener onGrantClickListener,
                                                  DialogInterface.OnClickListener onCancelClickListener) {

        final Dialog dialog = getDialog(context, onGrantClickListener, onCancelClickListener);

        // Adjust window size (scale the dialog)
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();

            // Adjust the size of the dialog
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;  // Set width to match parent
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;  // Set height to wrap content

            // Optionally, set margins or adjust the size further
            // layoutParams.x = 50; // example offset for positioning

            window.setAttributes(layoutParams);
        }

        // Show the dialog
        dialog.show();
    }

    @NonNull
    private static Dialog getDialog(Context context, View.OnClickListener onGrantClickListener, DialogInterface.OnClickListener onCancelClickListener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_media_files_permissions);  // Your custom dialog layout
        dialog.setCancelable(false);

        // Find UI elements in your layout
        TextView title = dialog.findViewById(R.id.dialog_title);
        TextView message = dialog.findViewById(R.id.dialog_message);
        Button btnGrant = dialog.findViewById(R.id.btn_grant);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Set button actions
        btnGrant.setOnClickListener(v -> {
            dialog.dismiss();
            onGrantClickListener.onClick(v);  // Request permissions
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            onCancelClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);  // Handle cancel
        });
        return dialog;
    }

    // Interface for dialog actions
    public interface PermissionDialogListener {
        void onAllPermissionsGranted();
        void onPermissionsDenied();
    }
}
