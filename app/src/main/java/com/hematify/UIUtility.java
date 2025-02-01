package com.hematify;

import android.app.Activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Looper;
import android.view.Gravity;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;


public class UIUtility {
    public static void showCustomPopup(Activity activity, String message) {
        // Create a custom layout for the notification
        View customView = LayoutInflater.from(activity).inflate(R.layout.custom_popup, null);

        // Set up the TextView with the message
        TextView messageText = customView.findViewById(R.id.popup_message);
        messageText.setText(message);

        // Create a FrameLayout.LayoutParams object to position the notification at the top
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP;  // Position it at the top of the screen
        params.leftMargin = 16;  // Optional: add margin to the left
        params.rightMargin = 16; // Optional: add margin to the right

        // Create a new FrameLayout to overlay on top of the current activity's root view
        FrameLayout frameLayout = new FrameLayout(activity);
        frameLayout.addView(customView);
        frameLayout.setLayoutParams(params);

        // Add the FrameLayout to the activity's root view (this makes it visible on top)
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        rootView.addView(frameLayout);

        // Create a Handler on the main looper thread (UI thread) to handle the delayed task
        Handler handler = new Handler(Looper.getMainLooper());  // Use the concrete Handler subclass
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Remove the popup after the delay
                rootView.removeView(frameLayout);
            }
        }, 3000); // 3000 milliseconds = 3 seconds
    }

    public static Bitmap generateInitialsImage(String fullName) {
        // Extract the initials from the user's full name
        String[] nameParts = fullName.split(" ");
        String initials = "";

        // Take the first letter from each part of the name
        if (nameParts.length > 0) {
            initials += nameParts[0].substring(0, 1).toUpperCase();
        }
        if (nameParts.length > 1) {
            initials += nameParts[1].substring(0, 1).toUpperCase();
        }

        // Create a Bitmap with a fixed size (e.g., 200x200)
        int width = 200;
        int height = 200;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Paint object to draw the initials
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#4CAF50"));  // Set a default background color (green)
        canvas.drawCircle(width / 2, height / 2, width / 2, paint);  // Draw a circle

        // Set paint for the text (initials)
        paint.setColor(Color.WHITE);  // Set text color to white
        paint.setTextSize(80);  // Set text size
        paint.setTextAlign(Paint.Align.CENTER);

        // Draw the initials in the center of the circle
        canvas.drawText(initials, width / 2, height / 2 + (paint.getTextSize() / 3), paint);

        return bitmap;
    }
}
