package com.chais.ribbit.activities;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chais.ribbit.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ViewImageActivity extends ActionBarActivity {
	@InjectView(R.id.imageView) ImageView mImageView;
	@InjectView(R.id.progressBar) ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);

		ButterKnife.inject(this);

		mProgressBar.setVisibility(View.VISIBLE);

		Uri imageUri = getIntent().getData();
		Picasso.with(this).load(imageUri.toString())
						  .into(mImageView, new ImageLoadedCallback(mProgressBar));
	}

	private class ImageLoadedCallback implements Callback {
		ProgressBar progressBar;

		public  ImageLoadedCallback(ProgressBar progBar){
			progressBar = progBar;
		}

		@Override
		public void onSuccess() {
			if (this.progressBar != null) {
				this.progressBar.setVisibility(View.GONE);
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						finish();
					}
				}, 10_000);
			}
		}

		@Override
		public void onError() {
			// not used yet
		}
	}
}
