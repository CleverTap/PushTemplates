package com.clevertap.pushtemplates;


import android.annotation.SuppressLint;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.clevertap.android.sdk.CTPushNotificationReceiver;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class VideoActivity extends AppCompatActivity {
	Bundle extras;
	private SimpleExoPlayer player;
	PlayerView playerView;
	private int currentWindow = 0;
	FixedAspectRatioFrameLayout aspectRatioFrameLayout;
	FrameLayout fullscreenButton;
	private long playbackPosition = 0;
	ImageButton openapp_button,close_button;
	ImageView fullscreenicon;

	boolean fullscreen = false;
	private ArrayList<String> deepLinkList;
	private Context context;

	@SuppressWarnings("ConstantConditions")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video);
		context = VideoActivity.this;
		try
		{
			this.getSupportActionBar().hide();
		}
		catch (NullPointerException e){
			PTLog.debug("NPE during hiding action bar");
		}
		extras = getIntent().getExtras();
		deepLinkList = Utils.getDeepLinkListFromExtras(extras);
		prepareMedia();

		playMedia();
	openapp_button=findViewById(R.id.openapp2);
	close_button=findViewById(R.id.exo_close2);
	fullscreenButton=findViewById(R.id.exo_fullscreen_button2);
	fullscreenicon=findViewById(R.id.exo_fullscreen_icon);


		fullscreen_layout(false);

		setFinishOnTouchOutside(false);


		openapp_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
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
					context.sendBroadcast(launchIntent);
					finish();
				} else {
					Intent i = context.getPackageManager().getLaunchIntentForPackage(getPackageName());
					context.startActivity(i);
					finish();
				}

			}
		});


		close_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				releasePlayer();
				finish();
			}
		});


		fullscreenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(fullscreen) {
					fullscreenicon.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.pt_video_fullscreen_open));
					ViewGroup.LayoutParams params1 = openapp_button.getLayoutParams();
					ViewGroup.LayoutParams params2 = close_button.getLayoutParams();
					params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
					params1.height=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
					params2.width=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
					params2.height=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
					openapp_button.setLayoutParams(params1);
					close_button.setLayoutParams(params2);
					playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
					getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
					if(getSupportActionBar() != null){
						getSupportActionBar().hide();
					}
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					fullscreen_layout(false);

					fullscreen = false;
				}else{
				//	playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
				/*
					player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);*/
					//FixedAspectRatioFrameLayout.LayoutParams a = (FixedAspectRatioFrameLayout.LayoutParams) aspectRatioFrameLayout.getLayoutParams();
					//playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);




					fullscreenicon.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.pt_video_fullscreen_close));
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
					fullscreen_layout(true);
					fullscreen = true;
				}
			}
		});


	}

	private void prepareMedia(){
		aspectRatioFrameLayout=findViewById(R.id.video_layout);
		aspectRatioFrameLayout.setVisibility(View.VISIBLE);
		playerView = new PlayerView(context);
		PlayerControlView playerControlView = new PlayerControlView(context);
		playerControlView.setId(R.layout.exo_player_control_view);
		playerControlView.show();

		//PlayerView.switchTargetView();
		playerView.setUseController(true);
		playerView.setControllerAutoShow(true);
		playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
		initializePlayer(extras.getString(Constants.PT_VIDEO_URL));
		aspectRatioFrameLayout.addView(playerView);
		//


	/*	fullscreenButton = new ImageView(this);
		fullscreenButton.setImageDrawable(context.getResources().getDrawable(R.drawable.pt_video_fullscreen_open));
		setPositionOfView(fullscreenButton, Gravity.END|Gravity.BOTTOM, "bottomRight");

		openapp_button= new ImageView(this);
		openapp_button.setImageDrawable(context.getResources().getDrawable(R.drawable.pt_open_app));
		setPositionOfView(openapp_button, Gravity.START|Gravity.BOTTOM, "bottomLeft");

		close_button= new ImageView(this);
		close_button.setImageDrawable(context.getResources().getDrawable(R.drawable.pt_video_close));
		setPositionOfView(close_button, Gravity.END|Gravity.TOP, "topRight");



		aspectRatioFrameLayout.addView(fullscreenButton,1);
		aspectRatioFrameLayout.addView(openapp_button,2);
		aspectRatioFrameLayout.addView(close_button,3);*/
	}

	private void setPositionOfView(ImageView imageView, int gravity, String position){
		int iconWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
		int iconHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(iconWidth,iconHeight);
		layoutParams.gravity = gravity;
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
		switch (position){
			case "topRight" : layoutParams.setMargins(0,margin,margin,0);
			break;
			case "topLeft" : layoutParams.setMargins(margin,margin,0,0);
			break;
			case "bottomRight" : layoutParams.setMargins(0,0,margin,margin);
			break;
			case "bottomLeft" : layoutParams.setMargins(margin,0,0,0);
			break;
		}
		imageView.setLayoutParams(layoutParams);
	}

	private void playMedia(){
		playerView.requestFocus();
		playerView.setVisibility(View.VISIBLE);
		playerView.setPlayer(player);
		player.setPlayWhenReady(true);
	}

	private void initializePlayer(String url) {
		player = ExoPlayerFactory.newSimpleInstance(VideoActivity.this);

		Uri uri = Uri.parse(url);
		MediaSource mediaSource = buildMediaSource(uri);

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
		//initializePlayer(extras.getString(Constants.PT_VIDEO_URL));
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


	public void fullscreen_layout(boolean isFullScreen) {

		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) aspectRatioFrameLayout.getLayoutParams();
		float density=getApplicationContext().getResources().getDisplayMetrics().density;
		if(isFullScreen) {
			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			//playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
			if(density ==1) {
				params.width = (int) (2.7*Constants.PT_VIDEO_WIDTH * getApplicationContext().getResources().getDisplayMetrics().density);
			}
			else if(density >1 && density <=1.5) {
				params.width = (int) (1.3 * Constants.PT_VIDEO_WIDTH * getApplicationContext().getResources().getDisplayMetrics().density);
			}
			else if(density>1.5 && density<2.5) {
				params.width = (int) (1.5 * Constants.PT_VIDEO_WIDTH * getApplicationContext().getResources().getDisplayMetrics().density);
			}
			else if(density >=2.5) {
				params.width = (int) (1.7 * Constants.PT_VIDEO_WIDTH * getApplicationContext().getResources().getDisplayMetrics().density);
			}
		//	params.width=ViewGroup.LayoutParams.MATCH_PARENT;
			params.height = ViewGroup.LayoutParams.MATCH_PARENT;
			//params.width=ViewGroup.LayoutParams.MATCH_PARENT;

			playerView.setLayoutParams(params);

		} else {
			//FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) aspectRatioFrameLayout.getLayoutParams();
			params.height = ViewGroup.LayoutParams.MATCH_PARENT;
			params.width=ViewGroup.LayoutParams.MATCH_PARENT;

			playerView.setLayoutParams(params);
			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		/*	if(density ==1) {
				params.width = (int) (1.5*Constants.PT_VIDEO_WIDTH * getApplicationContext().getResources().getDisplayMetrics().density);
				params.height = (int) ( 300 * getApplicationContext().getResources().getDisplayMetrics().density);
			}
			else if(density >1 && density <=1.5) {
				params.width = ViewGroup.LayoutParams.MATCH_PARENT;
				params.height = (int) ( 200 * getApplicationContext().getResources().getDisplayMetrics().density);
			}
			else if(density>1.5 && density<2.5) {
				params.width = ViewGroup.LayoutParams.MATCH_PARENT;
				params.height = (int) ( 200 * getApplicationContext().getResources().getDisplayMetrics().density);
			}
			else if(density >=2.5) {
				params.width = (int) (Constants.PT_VIDEO_WIDTH * getApplicationContext().getResources().getDisplayMetrics().density);
				params.height = (int) ( 200 * getApplicationContext().getResources().getDisplayMetrics().density);
			}
//params.width=ViewGroup.LayoutParams.MATCH_PARENT;
		//	params.height=ViewGroup.LayoutParams.MATCH_PARENT;
			playerView.setLayoutParams(params);*/
		}
	}
}