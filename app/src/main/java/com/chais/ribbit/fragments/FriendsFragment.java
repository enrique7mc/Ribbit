package com.chais.ribbit.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.chais.ribbit.activities.RecipientsActivity;
import com.chais.ribbit.util.ParseConstants;
import com.chais.ribbit.R;
import com.chais.ribbit.util.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Enrique on 05/05/2015.
 */
public class FriendsFragment extends ListFragment {
	private static final String TAG = FriendsFragment.class.getSimpleName();
	protected List<ParseUser> mFriends;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

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

			ArrayAdapter<String> adapter = new ArrayAdapter<>(
					getActivity(),
					android.R.layout.simple_list_item_1,
					usernames);
			setListAdapter(adapter);
		}
	};
}
