package com.clevertap.pushtemplates;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.clevertap.android.sdk.CTPushNotificationReceiver;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class VideoActivity extends AppCompatActivity {
	Bundle extras;
	private SimpleExoPlayer player;
	PlayerView videoView;
	private boolean playWhenReady = true;
	private int currentWindow = 0;
	FrameLayout aspectRatioFrameLayout;
	private long playbackPosition = 0;
	ImageButton openapp_button,close_button;
	ImageView fullscreenButton;
	boolean fullscreen = false;
	private ArrayList<String> deepLinkList;
	@SuppressWarnings("ConstantConditions")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video);
		try
		{
			this.getSupportActionBar().hide();
		}
		catch (NullPointerException e){
			PTLog.debug("NPE during hiding action bar");
		}
		videoView = findViewById(R.id.videoView);


		aspectRatioFrameLayout=findViewById(R.id.video_activity);
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)  videoView.getLayoutParams();
		params.width = (int) (Constants.PT_VIDEO_WIDTH * getApplicationContext().getResources().getDisplayMetrics().density);
		params.height = (int) ( Constants.PT_VIDEO_HEIGHT * getApplicationContext().getResources().getDisplayMetrics().density);


		extras = getIntent().getExtras();
		deepLinkList = Utils.getDeepLinkListFromExtras(extras);
		setFinishOnTouchOutside(false);
		openapp_button=videoView.findViewById(R.id.openapp);
		openapp_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Context ctx= VideoActivity.this;
				Intent launchIntent = new Intent(ctx, CTPushNotificationReceiver.class);
				if (deepLinkList != null) {
					launchIntent.putExtras(extras);
					launchIntent.putExtra(Constants.PT_NOTIF_ID, Constants.EMPTY_NOTIFICATION_ID);
					if (deepLinkList.get(0) != null) {
						launchIntent.putExtra("default_dl", true);
						launchIntent.putExtra(Constants.WZRK_DL, deepLinkList.get(0));
					}
					launchIntent.removeExtra(Constants.WZRK_ACTIONS);
					launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);
					launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					ctx.sendBroadcast(launchIntent);
					finish();
				} else {
					Intent i = ctx.getPackageManager().getLaunchIntentForPackage(getPackageName());
					ctx.startActivity(i);
					finish();
				}

			}
		});
		close_button=videoView.findViewById(R.id.exo_close);
		close_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				releasePlayer();
				finish();
			}
		});
		fullscreenButton = videoView.findViewById(R.id.exo_fullscreen_icon);//fullScreenButton is an ImageView
		fullscreenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(fullscreen) {
					fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.pt_video_fullscreen_open));
					ViewGroup.LayoutParams params1 = openapp_button.getLayoutParams();
					ViewGroup.LayoutParams params2 = close_button.getLayoutParams();
					params1.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
					params1.height=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
					params2.width=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
					params2.height=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
					openapp_button.setLayoutParams(params1);
					close_button.setLayoutParams(params2);

					getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
					if(getSupportActionBar() != null){
						getSupportActionBar().show();
					}
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) aspectRatioFrameLayout.getLayoutParams();
					params.width = (int) (350*getApplicationContext().getResources().getDisplayMetrics().density);
					params.height = (int) ( 210 * getApplicationContext().getResources().getDisplayMetrics().density);
					videoView.setLayoutParams(params);
					fullscreen = false;
				}else{
					fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.pt_video_fullscreen_close));
					ViewGroup.LayoutParams params1 = openapp_button.getLayoutParams();
					ViewGroup.LayoutParams params2 = close_button.getLayoutParams();
					params1.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
					params1.height=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
					params2.width=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
					params2.height=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
					openapp_button.setLayoutParams(params1);
					close_button.setLayoutParams(params2);
					getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
							|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
							|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
					if(getSupportActionBar() != null){
						getSupportActionBar().hide();
					}
					DisplayMetrics metrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(metrics);
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) aspectRatioFrameLayout.getLayoutParams();
					params.width = (int) (2 * Constants.PT_VIDEO_WIDTH * getApplicationContext().getResources().getDisplayMetrics().density);
					params.height = ViewGroup.LayoutParams.MATCH_PARENT;

					videoView.setLayoutParams(params);



					fullscreen = true;
				}
			}
		});
		initializePlayer(extras.getString(Constants.PT_VIDEO_URL));
	}

	private void initializePlayer(String url) {
		player = ExoPlayerFactory.newSimpleInstance(VideoActivity.this);
		videoView.setPlayer(player);

		Uri uri = Uri.parse(url);
		MediaSource mediaSource = buildMediaSource(uri);

		player.setPlayWhenReady(playWhenReady);
		player.seekTo(currentWindow, playbackPosition);
		player.prepare(mediaSource, false, false);
	}

	private MediaSource buildMediaSource(Uri uri) {
		// These factories are used to construct two media sources below
		DataSource.Factory dataSourceFactory =
				new DefaultDataSourceFactory(VideoActivity.this, Util.getUserAgent(this,this.getApplication().getPackageName()));
		ProgressiveMediaSource.Factory mediaSourceFactory =
				new ProgressiveMediaSource.Factory(dataSourceFactory);

		// Create a media source using the supplied URI
		MediaSource mediaSource1 = mediaSourceFactory.createMediaSource(uri);

		return new ConcatenatingMediaSource(mediaSource1);
	}

	private void releasePlayer() {
		if (player != null) {
			playbackPosition = player.getCurrentPosition();
			currentWindow = player.getCurrentWindowIndex();
			playWhenReady = player.getPlayWhenReady();
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
		initializePlayer(extras.getString(Constants.PT_VIDEO_URL));
	}

	@Override
	public void onPause() {
		super.onPause();
		releasePlayer();
	}

	@Override
	public void onStop() {
		super.onStop();
	}


	@SuppressLint("InlinedApi")
	private void hideSystemUi() {
		videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

}