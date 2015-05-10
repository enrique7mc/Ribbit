package com.chais.ribbit;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class RecipientsActivity extends ListActivity {
	private static final String TAG = RecipientsActivity.class.getSimpleName();
	protected List<ParseUser> mFriends;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;
	protected MenuItem mSendMenuItem;
	protected Uri mMediaUri;
	protected String mFileType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipients);

		Intent intent = getIntent();
		mMediaUri = intent.getData();
		mFileType = intent.getExtras().getString(ParseConstants.KEY_FILE_TYPE);

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

		setProgressBarIndeterminateVisibility(true);

		ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
		query.addAscendingOrder(ParseConstants.KEY_USERNAME);
		query.findInBackground(findCallback);
	}

	private FindCallback<ParseUser> findCallback = new FindCallback<ParseUser>() {
		@Override
		public void done(List<ParseUser> friends, ParseException e) {
			setProgressBarIndeterminateVisibility(false);
			if (e != null) {
				Log.e(TAG, e.getMessage());
				AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
				builder.setMessage(e.getMessage())
						.setTitle(getString(R.string.error_title))
						.setPositiveButton(android.R.string.ok, null)
						.create()
						.show();
				return;
			}

			mFriends = friends;

			String[] usernames = new String[mFriends.size()];
			int i = 0;
			for (ParseUser user : mFriends) {
				usernames[i] = user.getUsername();
				i++;
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<>(
					RecipientsActivity.this,
					android.R.layout.simple_list_item_checked,
					usernames);
			setListAdapter(adapter);
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (l.getCheckedItemCount() > 0) {
			mSendMenuItem.setVisible(true);
		} else {
			mSendMenuItem.setVisible(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_recipients, menu);
		mSendMenuItem = menu.getItem(0);
		mSendMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_send) {
			ParseObject message = createMessage();
			if (message == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
				builder.setMessage(getString(R.string.error_selecting_file))
						.setTitle(getString(R.string.error_title))
						.setPositiveButton(android.R.string.ok, null)
						.create()
						.show();

			} else {
				send(message);
				finish();
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void send(ParseObject message) {
		message.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e != null) {
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
					builder.setMessage(e.getMessage())
							.setTitle(getString(R.string.error_title))
							.setPositiveButton(android.R.string.ok, null)
							.create()
							.show();
					return;
				}

				Toast.makeText(RecipientsActivity.this,
						getString(R.string.success_message), Toast.LENGTH_LONG).show();
			}
		});
	}

	private ParseObject createMessage() {
		ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
		message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
		message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
		message.put(ParseConstants.KEY_RECIPIENTS_IDS, getRecipientsIds());
		message.put(ParseConstants.KEY_FILE_TYPE, mFileType);
		ParseFile file = getParseFile();
		if (file == null) {
			return null;
		}
		message.put(ParseConstants.KEY_FILE, file);

		return message;
	}

	private ParseFile getParseFile() {
		byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
		if (fileBytes == null) {
			return null;
		}

		if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
			fileBytes = FileHelper.reduceImageForUpload(fileBytes);
		}

		String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
		ParseFile file = new ParseFile(fileName, fileBytes);

		return file;
	}

	private ArrayList<String> getRecipientsIds() {
		ArrayList<String> recipientIds = new ArrayList<>();

		for (int i = 0; i < getListView().getCount(); i++) {
			if (getListView().isItemChecked(i)) {
				recipientIds.add(mFriends.get(i).getObjectId());
			}
		}

		return recipientIds;
	}
}
