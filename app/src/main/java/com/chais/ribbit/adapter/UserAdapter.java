package com.chais.ribbit.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chais.ribbit.R;
import com.chais.ribbit.util.ParseConstants;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Enrique on 10/05/2015.
 */
public class UserAdapter extends ArrayAdapter<ParseUser> {
	protected Context mContext;
	protected List<ParseUser> mUsers;

	public UserAdapter(Context context, List<ParseUser> users) {
		super(context, R.layout.user_item, users);
		mContext = context;
		mUsers = users;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		if (view != null) {
			holder = (ViewHolder) view.getTag();
		} else {
			view = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}

		ParseUser user = mUsers.get(position);

		/*String fileType = message.getString(ParseConstants.KEY_FILE_TYPE);
		if (fileType.equals(ParseConstants.TYPE_IMAGE)) {
			holder.iconImageView.setImageResource(R.drawable.ic_picture);
		} else if(fileType.equals(ParseConstants.TYPE_VIDEO)) {
			holder.iconImageView.setImageResource(R.drawable.ic_video);
		}*/

		holder.nameLabel.setText(user.getUsername());

		return view;
	}

	public void refill(List<ParseUser> users) {
		mUsers.clear();
		mUsers.addAll(users);
		notifyDataSetChanged();
	}

	static class ViewHolder {
		//@InjectView(R.id.messageIcon) ImageView iconImageView;
		@InjectView(R.id.senderLabel) TextView nameLabel;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}
}
