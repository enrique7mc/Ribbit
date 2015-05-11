package com.chais.ribbit;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.chais.ribbit.activities.ViewImageActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Enrique on 05/05/2015.
 */
public class InboxFragment extends ListFragment {
	protected List<ParseObject> mMessages;
	private static final String TAG = InboxFragment.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
		//ButterKnife.inject(this, rootView);
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
			List<String> idsToRemove = Arrays.asList(new String[] {currentUserId});
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
				if (e != null) {
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage(e.getMessage())
							.setTitle(getString(R.string.error_title))
							.setPositiveButton(android.R.string.ok, null)
							.create()
							.show();
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
}
