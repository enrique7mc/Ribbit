package com.chais.ribbit.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.chais.ribbit.R;
import com.chais.ribbit.activities.ViewImageActivity;
import com.chais.ribbit.adapter.MessageAdapter;
import com.chais.ribbit.util.ParseConstants;
import com.chais.ribbit.util.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Enrique on 05/05/2015.
 */
public class InboxFragment extends ListFragment {
	protected List<ParseObject> mMessages;
	protected @InjectView(R.id.swipeRefreshLayout) SwipeRefreshLayout mSwipeRefreshLayout;
	private static final String TAG = InboxFragment.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
		ButterKnife.inject(this, rootView);
		mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
		mSwipeRefreshLayout.setColorSchemeColors(R.color.swipeRefresh1, R.color.swipeRefresh2,
				R.color.swipeRefresh3, R.color.swipeRefresh4);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().setProgressBarIndeterminateVisibility(true);
		getUserMessages();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (!Util.isNetworkAvailable(getActivity())) {
			Util.alertDialogShow(getActivity(), getString(R.string.general_error),
					getString(R.string.no_network_available_message));
			return;
		}

		ParseObject message = mMessages.get(position);
		String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
		ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
		Uri fileUri = Uri.parse(file.getUrl());

		if (messageType.equals(ParseConstants.TYPE_IMAGE)) {
			Intent intent = new Intent(getActivity(), ViewImageActivity.class);
			intent.setData(fileUri);
			startActivity(intent);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
			intent.setDataAndType(fileUri, "video/*");
			startActivity(intent);
		}

		List<String> ids = message.getList(ParseConstants.KEY_RECIPIENTS_IDS);
		if (ids.size() == 1) {
			message.deleteInBackground();
		} else {
			String currentUserId = ParseUser.getCurrentUser().getObjectId();
			ids.remove(currentUserId);
			List<String> idsToRemove = Arrays.asList(currentUserId);
			message.removeAll(ParseConstants.KEY_RECIPIENTS_IDS, idsToRemove);
			message.saveInBackground();
		}
	}

	private void getUserMessages() {
		ParseQuery<ParseObject> query = new ParseQuery<>(ParseConstants.CLASS_MESSAGES);
		query.whereEqualTo(ParseConstants.KEY_RECIPIENTS_IDS,
				ParseUser.getCurrentUser().getObjectId());
		query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> messagesList, ParseException e) {
				getActivity().setProgressBarIndeterminateVisibility(false);

				if (mSwipeRefreshLayout.isRefreshing()) {
					mSwipeRefreshLayout.setRefreshing(false);
				}

				if (e != null) {
					Log.e(TAG, e.getMessage());
					Util.alertDialogShow(getActivity(), getString(R.string.error_title),
							e.getMessage());
					return;
				}

				mMessages = messagesList;

				if(getListAdapter() == null) {
					MessageAdapter adapter = new MessageAdapter(getActivity(), mMessages);
					setListAdapter(adapter);
				} else {
					((MessageAdapter)getListAdapter()).refill(mMessages);
				}
			}
		});
	}

	protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener =
			new SwipeRefreshLayout.OnRefreshListener() {
		@Override
		public void onRefresh() {
			getUserMessages();
		}
	};
}
