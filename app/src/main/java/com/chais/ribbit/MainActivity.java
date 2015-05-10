package com.chais.ribbit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity {

	private static final String TAG = MainActivity.class.getSimpleName();
	public static final int TAKE_PHOTO_REQUEST = 0;
	public static final int TAKE_VIDEO_REQUEST = 1;
	public static final int PICK_PHOTO_REQUEST = 2;
	public static final int PICK_VIDEO_REQUEST = 3;

	public static final int MEDIA_TYPE_IMAGE = 4;
	public static final int MEDIA_TYPE_VIDEO = 5;

	public static final  int FILE_SIZE_LIMIT = 1024 * 1024 * 10;
	public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

	protected Uri mMediaUri;
	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser == null) {
			navigateToLogin();
		} else {
			Log.i(TAG, currentUser.getUsername());
		}


		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
				if (data == null) {
					Toast.makeText(this, getString(R.string.general_error),
							Toast.LENGTH_LONG).show();
					return;
				}

				mMediaUri = data.getData();
				if (requestCode == PICK_VIDEO_REQUEST) {
					checkVideoSize();
				}
			} else {
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				mediaScanIntent.setData(mMediaUri);
				sendBroadcast(mediaScanIntent);
			}

			Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
			recipientsIntent.setData(mMediaUri);
			String fileType;
			if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
				fileType = ParseConstants.TYPE_IMAGE;
			} else {
				fileType = ParseConstants.TYPE_VIDEO;
			}
			recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
			startActivity(recipientsIntent);
		} else if (resultCode != RESULT_CANCELED) {
			Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
		}
	}

	private void checkVideoSize() {
		int fileSize = 0;
		InputStream inputStream = null;
		try {
			inputStream = getContentResolver().openInputStream(mMediaUri);
			fileSize = inputStream.available();
			if (fileSize >= FILE_SIZE_LIMIT) {
				Toast.makeText(this, getString(R.string.error_file_size_too_large),
						Toast.LENGTH_LONG).show();
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(this, getString(R.string.error_opening_file),
					Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(this, getString(R.string.general_error),
					Toast.LENGTH_LONG).show();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
	}

	private void navigateToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id){
			case R.id.action_logout:
				ParseUser.logOut();
				navigateToLogin();
				break;
			case R.id.action_edit_friends:
				Intent intent = new Intent(this, EditFriendsActivity.class);
				startActivity(intent);
			case R.id.action_camera:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setItems(R.array.camera_choices, mDialogListener);
				builder.create().show();
		}

		return super.onOptionsItemSelected(item);
	}

	protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			switch (which) {
				case 0:
					Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
					if (mMediaUri == null) {
						Toast.makeText(MainActivity.this,
								getString(R.string.error_external_storage),
								Toast.LENGTH_LONG).show();
						return;
					}
					takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
					startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
					break;
				case 1:
					Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
					if (mMediaUri == null) {
						Toast.makeText(MainActivity.this,
								getString(R.string.error_external_storage),
								Toast.LENGTH_LONG).show();
						return;
					}
					videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
					videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
					videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
					startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
					break;
				case 2:
					Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
					choosePhotoIntent.setType("image/*");
					startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
					break;
				case 3:
					Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
					chooseVideoIntent.setType("video/*");
					Toast.makeText(MainActivity.this,
							getString(R.string.video_file_size_warning), Toast.LENGTH_LONG).show();
					startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
					break;
			}
		}

		private Uri getOutputMediaFileUri(int mediaType) {
			if (isExternalStorageAvailable()) {
				String appName = MainActivity.this.getString(R.string.app_name);
				File mediaStorageDir = new File(
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						appName);
				if (!mediaStorageDir.exists() ) {
					if (!mediaStorageDir.mkdirs()) {
						Log.e(TAG, "Failed to create directory");
						return null;
					}
				}

				File mediaFile;
				Date now = new Date();
				String timestamp = formatter.format(now);
				String path = mediaStorageDir.getPath() + File.separator;
				if (mediaType == MEDIA_TYPE_IMAGE) {
					mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
				} else if(mediaType == MEDIA_TYPE_VIDEO) {
					mediaFile = new File(path + "VID_" + timestamp + ".mp4");
				} else {
					return null;
				}

				Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
				return Uri.fromFile(mediaFile);
			}

			return null;
		}

		private boolean isExternalStorageAvailable() {
			String state = Environment.getExternalStorageState();
			return state.equals(Environment.MEDIA_MOUNTED);
		}
	};
}
