package com.chais.ribbit.activities;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.chais.ribbit.R;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ViewImageActivity extends ActionBarActivity {
	@InjectView(R.id.imageView) ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);

		ButterKnife.inject(this);

		Uri imageUri = getIntent().getData();
		Picasso.with(this).load(imageUri.toString()).into(mImageView);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				finish();
			}
		}, 10_000);
	}
}
