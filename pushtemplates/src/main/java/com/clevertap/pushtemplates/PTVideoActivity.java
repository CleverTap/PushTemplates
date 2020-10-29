package com.clevertap.pushtemplates;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import java.util.ArrayList;

public class PTVideoActivity extends Activity {
    private Bundle extras;
    private SimpleExoPlayer player;
    private PlayerView playerView;
    private int currentWindow = 0;
    private FixedAspectRatioFrameLayout aspectRatioFrameLayout;
    private long playbackPosition = 0;
    private ImageButton openAppButton, closeVideoButton;
    private ImageView fullscreenIcon;
    private int portraitWidth;

    private boolean fullscreen = false;
    private ArrayList<String> deepLinkList;
    private Context context;

    @SuppressWarnings({"ConstantConditions", "SuspiciousNameCombination"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video);
        context = PTVideoActivity.this;
        extras = getIntent().getExtras();
        deepLinkList = Utils.getDeepLinkListFromExtras(extras);

        prepareMedia();
        playMedia();
        int orientation = getResources().getConfiguration().orientation;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        openAppButton = findViewById(R.id.pt_open_app_btn);
        closeVideoButton = findViewById(R.id.pt_video_close);
        FrameLayout fullscreenButton = findViewById(R.id.pt_video_fullscreen_btn);
        fullscreenIcon = findViewById(R.id.pt_video_fullscreen_icon);

        if (orientation == 1){
            //ORIENTATION_PORTRAIT
            portraitWidth = dm.widthPixels;
            setFullScreenLayout(false);
        } else if (orientation == 2){
            //ORIENTATION_LANDSCAPE
            fullscreen = true;
            fullscreenIcon.setImageDrawable(ContextCompat.getDrawable(PTVideoActivity.this, R.drawable.pt_video_fullscreen_close));
            ViewGroup.LayoutParams params1 = openAppButton.getLayoutParams();
            ViewGroup.LayoutParams params2 = closeVideoButton.getLayoutParams();
            params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
            params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
            params2.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
            params2.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
            openAppButton.setLayoutParams(params1);
            closeVideoButton.setLayoutParams(params2);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            portraitWidth = dm.heightPixels;
            setFullScreenLayout(true);
        }

        setFinishOnTouchOutside(true);

        openAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = new Intent(context, PTPushNotificationReceiver.class);
                if (deepLinkList != null) {
                    launchIntent.putExtras(extras);
                    launchIntent.putExtra(Constants.PT_NOTIF_ID, Constants.EMPTY_NOTIFICATION_ID);
                    if (deepLinkList.get(0) != null) {
                        launchIntent.putExtra("default_dl", true);
                        launchIntent.putExtra(Constants.WZRK_DL, deepLinkList.get(0));
                    }
                    launchIntent.removeExtra(Constants.WZRK_ACTIONS);
                    launchIntent.putExtra(Constants.WZRK_C2A, Constants.PT_VIDEO_C2A_KEY + "app_open");
                    launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(launchIntent);
                } else {
                    Intent i = context.getPackageManager().getLaunchIntentForPackage(getPackageName());
                    context.startActivity(i);
                }
                finish();
            }
        });

        closeVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releasePlayer();
                finish();
            }
        });

        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fullscreen) {
                    fullscreenIcon.setImageDrawable(ContextCompat.getDrawable(PTVideoActivity.this, R.drawable.pt_video_fullscreen_open));
                    ViewGroup.LayoutParams params1 = openAppButton.getLayoutParams();
                    ViewGroup.LayoutParams params2 = closeVideoButton.getLayoutParams();
                    params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
                    params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
                    params2.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
                    params2.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
                    openAppButton.setLayoutParams(params1);
                    closeVideoButton.setLayoutParams(params2);
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    setFullScreenLayout(false);
                    fullscreen = false;
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    fullscreenIcon.setImageDrawable(ContextCompat.getDrawable(PTVideoActivity.this, R.drawable.pt_video_fullscreen_close));
                    ViewGroup.LayoutParams params1 = openAppButton.getLayoutParams();
                    ViewGroup.LayoutParams params2 = closeVideoButton.getLayoutParams();
                    params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
                    params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
                    params2.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
                    params2.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
                    openAppButton.setLayoutParams(params1);
                    closeVideoButton.setLayoutParams(params2);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    setFullScreenLayout(true);
                    fullscreen = true;
                }
            }
        });
    }

    @SuppressLint("ResourceType")
    private void prepareMedia() {
        aspectRatioFrameLayout = findViewById(R.id.video_layout);
        aspectRatioFrameLayout.setVisibility(View.VISIBLE);
        playerView = findViewById(R.id.pt_player_view);
        initializePlayer(extras.getString(Constants.PT_VIDEO_URL));
    }

    private void playMedia() {
        playerView.requestFocus();
        playerView.setVisibility(View.VISIBLE);
        playerView.setPlayer(player);
        player.setPlayWhenReady(true);
    }

    private void initializePlayer(String url) {
        player = ExoPlayerFactory.newSimpleInstance(PTVideoActivity.this);

        Uri uri = Uri.parse(url);
        try {
            MediaSource mediaSource = buildMediaSource(uri, Util.inferContentType(uri));
            player.seekTo(currentWindow, playbackPosition);
            player.prepare(mediaSource, false, false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private MediaSource buildMediaSource(Uri uri, int type) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(PTVideoActivity.this,
                Util.getUserAgent(this, this.getApplication().getPackageName()));
        switch (type){
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory).
                        createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    public void setFullScreenLayout(boolean isFullScreen) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) aspectRatioFrameLayout.getLayoutParams();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (isFullScreen) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = (int) (1.3 * portraitWidth);
        } else {
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = (int) (0.9 * portraitWidth);
        }
        playerView.setLayoutParams(params);
    }
}