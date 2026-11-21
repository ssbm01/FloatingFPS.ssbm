package com.ssbm.floatingfps;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FpsService extends Service implements Choreographer.FrameCallback {

    private WindowManager windowManager;
    private TextView fpsTextView;
    private WindowManager.LayoutParams params;
    private long lastTimeNanos = 0;
    private int frameCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // সুন্দর রাউন্ডেড ব্যাকগ্রাউন্ড ডিজাইন
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.argb(180, 0, 0, 0));
        background.setCornerRadius(30f);

        fpsTextView = new TextView(this);
        fpsTextView.setText("FPS: --");
        fpsTextView.setTextColor(Color.GREEN);
        fpsTextView.setTextSize(15);
        fpsTextView.setPadding(24, 12, 24, 12);
        fpsTextView.setBackground(background);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 30;
        params.y = 100;

        // আঙুল দিয়ে টেনে সরানোর (Drag) কোড
        fpsTextView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(fpsTextView, params);
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(fpsTextView, params);
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (lastTimeNanos == 0) {
            lastTimeNanos = frameTimeNanos;
        }
        frameCount++;
        long elapsedNanos = frameTimeNanos - lastTimeNanos;

        if (elapsedNanos >= 1_000_000_000L) {
            int fps = (int) (frameCount * 1_000_000_000L / elapsedNanos);
            fpsTextView.setText("FPS: " + fps);
            frameCount = 0;
            lastTimeNanos = frameTimeNanos;
        }
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Choreographer.getInstance().removeFrameCallback(this);
        if (fpsTextView != null) {
            windowManager.removeView(fpsTextView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
