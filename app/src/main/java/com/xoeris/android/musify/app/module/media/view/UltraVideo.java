package com.xoeris.android.musify.app.module.media.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.VideoView;

@SuppressWarnings("all")
public class UltraVideo extends FrameLayout {
    private LinearLayout overlayLayout;
    private VideoView videoView;

    public UltraVideo(Context context) {
        super(context);
        init(context);
    }

    public UltraVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UltraVideo(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        // Initialize VideoView with proper layout parameters
        this.videoView = new VideoView(context);
        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        videoParams.gravity = android.view.Gravity.CENTER;
        this.videoView.setLayoutParams(videoParams);
        
        // Set video scaling mode
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            this.videoView.setAudioFocusRequest(android.media.AudioManager.AUDIOFOCUS_GAIN);
        }
        
        addView(this.videoView);

        // Initialize overlay layout with proper parameters
        this.overlayLayout = new LinearLayout(context);
        FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        overlayParams.gravity = android.view.Gravity.CENTER;
        this.overlayLayout.setLayoutParams(overlayParams);
        this.overlayLayout.setOrientation(LinearLayout.VERTICAL);
        this.overlayLayout.setGravity(android.view.Gravity.CENTER);
        
        addView(this.overlayLayout);
    }

    public VideoView getVideoView() {
        return this.videoView;
    }

    public LinearLayout getOverlayLayout() {
        return this.overlayLayout;
    }
}