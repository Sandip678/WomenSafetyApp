package com.womensefty.womensafetyapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingWindowService extends Service {
    private WindowManager windowManager;
    private View floatingView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create a floating view
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_sos_button, null);

        // Set layout parameters
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // Set position of the floating view to center
        params.gravity = Gravity.CENTER; // Center the floating view
        params.x = 0; // No horizontal offset
        params.y = 0; // No vertical offset

        // Add the view to the window
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        // Handle SOS button click
        Button sosButton = floatingView.findViewById(R.id.floatingSosButton);
        sosButton.setOnClickListener(v -> {
            Toast.makeText(FloatingWindowService.this, "SOS Sent!", Toast.LENGTH_SHORT).show();
        });

        // Handle Move to Screen Text click
        TextView moveToScreenText = floatingView.findViewById(R.id.moveToScreenText);
        moveToScreenText.setOnClickListener(v -> {
            // You can add functionality here if needed
            Toast.makeText(FloatingWindowService.this, "Move to Screen Clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }
}