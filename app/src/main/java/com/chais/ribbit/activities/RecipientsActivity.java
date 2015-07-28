package com.chais.ribbit.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.chais.ribbit.R;
import com.chais.ribbit.util.FileHelper;
import com.chais.ribbit.util.ParseConstants;
import com.chais.ribbit.util.Util;
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


public class RecipientsActivity extends ActionBarActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		Uri mediaUri = intent.getData();
		String fileType = intent.getExtras().getString(ParseConstants.KEY_FILE_TYPE);

		RecipientsFragment fragment = RecipientsFragment.newInstance(mediaUri, fileType);
		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
	}

	public static class RecipientsFragment extends ListFragment {
		private static final String TAG = RecipientsActivity.class.getSimpleName();
		protected List<ParseUser> mFriends;
		protected ParseRelation<ParseUser> mFriendsRelation;
		protected ParseUser mCurrentUser;
		protected MenuItem mSendMenuItem;
		protected Uri mMediaUri;
		protected String mFileType;
		protected ProgressDialog mProgressDialog;

		public static RecipientsFragment newInstance(Uri mediaUri, String fileType) {
			RecipientsFragment f = new RecipientsFragment();

			Bundle args = new Bundle();
			args.putParcelable(ParseConstants.KEY_MEDIA_URI, mediaUri);
			args.putString(ParseConstants.KEY_FILE_TYPE, fileType);
			f.setArguments(args);

			return f;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			mMediaUri = getArguments().getParcelable(ParseConstants.KEY_MEDIA_URI);
			mFileType = getArguments().getString(ParseConstants.KEY_FILE_TYPE);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			setHasOptionsMenu(true);
			return inflater.inflate(R.layout.activity_recipients, container, false);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}

		@Override
		public void onResume() {
			super.onResume();

			mCurrentUser = ParseUser.getCurrentUser();
			mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

			getActivity().setProgressBarIndeterminateVisibility(true);

			ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
			query.addAscendingOrder(ParseConstants.KEY_USERNAME);
			query.findInBackground(findCallback);
		}

		private FindCallback<ParseUser> findCallback = new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				getActivity().setProgressBarIndeterminateVisibility(false);
				if (e != null) {
					Log.e(TAG, e.getMessage());
					Util.alertDialogShow(getActivity(), getString(R.string.error_title),
							e.getMessage());
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
						getActivity(),
						android.R.layout.simple_list_item_checked,
						usernames);
				setListAdapter(adapter);
			}
		};

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);

			if (l.getCheckedItemCount() > 0) {
				mSendMenuItem.setVisible(true);
			} else {
				mSendMenuItem.setVisible(false);
			}
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			// Inflate the menu; this adds items to the action bar if it is present.
			inflater.inflate(R.menu.menu_recipients, menu);
			mSendMenuItem = menu.getItem(0);
			mSendMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			int id = item.getItemId();
			if (id == R.id.action_send) {
				ParseObject message = createMessage();
				if (message == null) {
					Util.alertDialogShow(getActivity(), getString(R.string.error_title),
							getString(R.string.error_selecting_file));
				} else {
					mProgressDialog = ProgressDialog.show(getActivity(),
							getString(R.string.sending_message), getString(R.string.please_wait));
					send(message);
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
						Util.alertDialogShow(getActivity(), getString(R.string.error_title),
								e.getMessage());

						return;
					}

					Toast.makeText(getActivity(),
							getString(R.string.success_message), Toast.LENGTH_LONG).show();
					mProgressDialog.dismiss();
					getActivity().finish();
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
			byte[] fileBytes = FileHelper.getByteArrayFromFile(getActivity(), mMediaUri);
			if (fileBytes == null) {
				return null;
			}

			if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
				fileBytes = FileHelper.reduceImageForUpload(fileBytes);
			}

			String fileName = FileHelper.getFileName(getActivity(), mMediaUri, mFileType);
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
}
