package com.chais.ribbit.activities;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.chais.ribbit.R;
import com.chais.ribbit.util.ParseConstants;
import com.chais.ribbit.util.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class EditFriendsActivity extends ActionBarActivity {

    public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditFriendsFragment fragment = new EditFriendsFragment();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    public static class EditFriendsFragment extends ListFragment {
        private static final String TAG = EditFriendsActivity.class.getSimpleName();
        protected List<ParseUser> mUsers;
        protected ParseRelation<ParseUser> mFriendsRelation;
        protected ParseUser mCurrentUser;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_edit_friends, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
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

        @Override
        public void onResume() {
            super.onResume();

            mCurrentUser = ParseUser.getCurrentUser();
            mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

            getActivity().setProgressBarIndeterminateVisibility(true);
            ParseQuery<ParseUser> query =
                    ParseUser.getQuery()
                            .whereNotEqualTo(ParseConstants.KEY_OBJECT_ID, mCurrentUser.getObjectId())
                            .orderByAscending(ParseConstants.KEY_USERNAME)
                            .setLimit(1000);

            query.findInBackground(findCallBack);
        }



        private FindCallback<ParseUser> findCallBack = new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                    Util.alertDialogShow(getActivity(), getString(R.string.error_title),
                            e.getMessage());
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
                        getActivity(),
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
                    if (e != null) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    for (int i = 0; i < mUsers.size(); i++) {
                        ParseUser user = mUsers.get(i);
                        for (ParseUser friend : friends) {
                            if (friend.getObjectId().equals(user.getObjectId())) {
                                getListView().setItemChecked(i, true);
                            }
                        }
                    }
                }
            });
        }
    }



}
