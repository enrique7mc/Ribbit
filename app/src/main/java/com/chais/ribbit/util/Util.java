package com.chais.ribbit.util;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Enrique on 10/05/2015.
 */
public class Util {
	public static void alertDialogShow(Context context, String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
				.setPositiveButton(android.R.string.ok, null)
				.create()
				.show();
	}

	public static void alertDialogShow(Context context, String title, String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
				.setPositiveButton(android.R.string.ok, null)
				.setTitle(title)
				.create()
				.show();
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager manager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();

		return info != null && info.isConnected();
	}
}
