package com.ssbm.floatingfps;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

public class FpsService extends Service implements Choreographer.FrameCallback {

    private WindowManager windowManager;
    private TextView fpsTextView;
    private long lastTimeNanos = 0;
    private int frameCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        fpsTextView = new TextView(this);
        fpsTextView.setText("FPS: --");
        fpsTextView.setTextColor(Color.GREEN);
        fpsTextView.setTextSize(16);
        fpsTextView.setPadding(16, 8, 16, 8);
        fpsTextView.setBackgroundColor(Color.argb(160, 0, 0, 0));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 30;
        params.y = 100;

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
