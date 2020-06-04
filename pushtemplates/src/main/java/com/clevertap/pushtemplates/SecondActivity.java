package com.clevertap.pushtemplates;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SecondActivity extends AppCompatActivity {
	//VideoDialogFragment fragment1;
	Bundle bundle;
	private SimpleExoPlayer player;
	PlayerView videoView;
	private boolean playWhenReady = true;
	private int currentWindow = 0;
	AspectRatioFrameLayout aspectRatioFrameLayout;
	private long playbackPosition = 0;
	Button button;
//	private final PictureInPictureParams.Builder pictureInPictureParamsBuilder=new PictureInPictureParams.Builder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_second);
		Log.d("yaha_hore","cool");
		try
		{
			this.getSupportActionBar().hide();
		}
		catch (NullPointerException e){}
		videoView = findViewById(R.id.videoView);
		button=findViewById(R.id.button);
		aspectRatioFrameLayout=findViewById(R.id.activity_second);
		videoView.showController();
		aspectRatioFrameLayout.setAspectRatio(16f/9f);
		bundle = getIntent().getBundleExtra("bundle");
		if (bundle != null) {
			for (String key : bundle.keySet()) {
				Object value = bundle.get(key);

			}
		}
		setFinishOnTouchOutside(false);
		initializePlayer(bundle.getString("videourl"));
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Context ctx= SecondActivity.this; // or you can replace **'this'** with your **ActivityName.this**
				Intent i = ctx.getPackageManager().getLaunchIntentForPackage(getPackageName());
				ctx.startActivity(i);
			}
		});

	//	initializePlayer("https://claykart17091994.000webhostapp.com/videoplayback.mp4");

	}



	private void initializePlayer(String url) {
		player = ExoPlayerFactory.newSimpleInstance(SecondActivity.this);
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
				new DefaultDataSourceFactory(SecondActivity.this, "exoplayer-codelab");
		ProgressiveMediaSource.Factory mediaSourceFactory =
				new ProgressiveMediaSource.Factory(dataSourceFactory);

		// Create a media source using the supplied URI
		MediaSource mediaSource1 = mediaSourceFactory.createMediaSource(uri);

		// Additionally create a media source using an MP3
		// Uri audioUri = Uri.parse(getString(uri);
		// MediaSource mediaSource2 = mediaSourceFactory.createMediaSource(audioUri);

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
		if (Util.SDK_INT > 23) {
			//   initializePlayer();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		hideSystemUi();
		if ((Util.SDK_INT <= 23 || player == null)) {
			//   initializePlayer();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (Util.SDK_INT <= 23) {
			releasePlayer();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (Util.SDK_INT > 23) {
			releasePlayer();
		}
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