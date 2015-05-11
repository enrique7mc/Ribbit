package com.chais.ribbit.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.chais.ribbit.ParseConstants;
import com.chais.ribbit.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class EditFriendsActivity extends ListActivity {

	private static final String TAG = EditFriendsActivity.class.getSimpleName();
	protected List<ParseUser> mUsers;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_friends);

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

		setProgressBarIndeterminateVisibility(true);
		ParseQuery<ParseUser> query =
				ParseUser.getQuery()
						 .whereNotEqualTo(ParseConstants.KEY_OBJECT_ID, mCurrentUser.getObjectId())
						 .orderByAscending(ParseConstants.KEY_USERNAME)
						 .setLimit(1000);

		query.findInBackground(findCallBack);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (getListView().isItemChecked(position)) {
			mFriendsRelation.add(mUsers.get(position));
		} else {
			mFriendsRelation.remove(mUsers.get(position));
		}

		mCurrentUser.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e != null) {
					Log.e(TAG, e.getMessage());
				}
			}
		});
	}

	private FindCallback<ParseUser> findCallBack = new FindCallback<ParseUser>() {
		@Override
		public void done(List<ParseUser> parseUsers, ParseException e) {
			setProgressBarIndeterminateVisibility(false);
			if (e != null) {
				Log.e(TAG, e.getMessage());
				AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
				builder.setMessage(e.getMessage())
						.setTitle(getString(R.string.error_title))
						.setPositiveButton(android.R.string.ok, null)
						.create()
						.show();
				return;
			}

			mUsers = parseUsers;
			String[] usernames = new String[mUsers.size()];
			int i = 0;
			for (ParseUser user : mUsers) {
				usernames[i] = user.getUsername();
				i++;
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<>(
					EditFriendsActivity.this,
					android.R.layout.simple_list_item_checked,
					usernames);
			setListAdapter(adapter);

			addFriendCheckmarks();
		}
	};

	private void addFriendCheckmarks() {
		mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				if(e != null) {
					Log.e(TAG, e.getMessage());
					return;
				}

				for (int i = 0; i < mUsers.size(); i++) {
					ParseUser user = mUsers.get(i);
					for (ParseUser friend : friends) {
						if(friend.getObjectId().equals(user.getObjectId())) {
							getListView().setItemChecked(i, true);
						}
					}
				}
			}
		});
	}
}
