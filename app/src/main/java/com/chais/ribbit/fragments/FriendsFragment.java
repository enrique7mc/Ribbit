package com.chais.ribbit.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.chais.ribbit.activities.RecipientsActivity;
import com.chais.ribbit.adapter.UserAdapter;
import com.chais.ribbit.util.ParseConstants;
import com.chais.ribbit.R;
import com.chais.ribbit.util.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Enrique on 05/05/2015.
 */
public class FriendsFragment extends Fragment {
	private static final String TAG = FriendsFragment.class.getSimpleName();
	protected List<ParseUser> mFriends;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;
	protected @InjectView(R.id.friendsGrid) GridView mGridView;
	protected @InjectView(android.R.id.empty) TextView emptyTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
		ButterKnife.inject(this, rootView);

		mGridView.setEmptyView(emptyTextView);

		return rootView;
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

			if (mGridView.getAdapter() == null) {
				UserAdapter adapter = new UserAdapter(getActivity(), mFriends);
				mGridView.setAdapter(adapter);
			} else {
				((UserAdapter)mGridView.getAdapter()).refill(mFriends);
			}

		}
	};
}
