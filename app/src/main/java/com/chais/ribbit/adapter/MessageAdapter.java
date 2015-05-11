package com.chais.ribbit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chais.ribbit.util.ParseConstants;
import com.chais.ribbit.R;
import com.parse.ParseObject;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Enrique on 10/05/2015.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {
	protected Context mContext;
	protected List<ParseObject> mMessages;

	public MessageAdapter(Context context, List<ParseObject> messages) {
		super(context, R.layout.message_item, messages);
		mContext = context;
		mMessages = messages;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		if (view != null) {
			holder = (ViewHolder) view.getTag();
		} else {
			view = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}

		ParseObject message = mMessages.get(position);

		String fileType = message.getString(ParseConstants.KEY_FILE_TYPE);
		if (fileType.equals(ParseConstants.TYPE_IMAGE)) {
			holder.iconImageView.setImageResource(R.drawable.ic_action_picture);
		} else if(fileType.equals(ParseConstants.TYPE_VIDEO)) {
			holder.iconImageView.setImageResource(R.drawable.ic_action_play_over_video);
		}

		holder.nameLabel.setText(message.getString(ParseConstants.KEY_SENDER_NAME));

		return view;
	}

	public void refill(List<ParseObject> messages) {
		mMessages.clear();
		mMessages.addAll(messages);
		notifyDataSetChanged();
	}

	static class ViewHolder {
		@InjectView(R.id.messageIcon) ImageView iconImageView;
		@InjectView(R.id.senderLabel) TextView nameLabel;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}
}
