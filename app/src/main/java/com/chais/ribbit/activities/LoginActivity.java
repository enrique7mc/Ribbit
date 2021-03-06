package com.chais.ribbit.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chais.ribbit.R;
import com.chais.ribbit.util.Util;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class LoginActivity extends Activity {
	private static final String TAG = LoginActivity.class.getSimpleName();
	@InjectView(R.id.usernameField) EditText mUsername;
	@InjectView(R.id.passwordField) EditText mPassword;
	@InjectView(R.id.loginButton) Button mLoginButton;
	@InjectView(R.id.signUpText) TextView mSignUpTextView;
	ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		ButterKnife.inject(this);
	}

	@OnClick(R.id.signUpText)
	public void SignUpTextViewClick(View view) {
		Intent intent = new Intent(this, SignUpActivity.class);
		startActivity(intent);
	}

	@OnClick(R.id.loginButton)
	public void SignUpButtonClick(View view) {
		if (!Util.isNetworkAvailable(this)) {
			Util.alertDialogShow(this, getString(R.string.general_error),
					getString(R.string.no_network_available_message));
			return;
		}
		String username = mUsername.getText().toString();
		String password = mPassword.getText().toString();

		username = username.trim();
		password = password.trim();

		if (username.isEmpty() || password.isEmpty()) {
			Util.alertDialogShow(this, getString(R.string.login_error_title),
					getString(R.string.login_error_message));
			return;
		}

		progress = ProgressDialog.show(this, "Logging in",
				"Please wait a moment", true);
		ParseUser.logInInBackground(username, password, logInCallback);
	}

	private LogInCallback logInCallback = new LogInCallback() {
		@Override
		public void done(ParseUser parseUser, ParseException e) {
			progress.dismiss();
			if (e != null) {
				Log.e(TAG, e.getMessage());
				String message = e.getMessage().equals("invalid login parameters") ?
						getString(R.string.login_invalid_credentials) :
						getString(R.string.login_error_title);

				Util.alertDialogShow(LoginActivity.this, getString(R.string.login_error_parse),
						message);
				return;
			}

			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}
	};
}
