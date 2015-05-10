package com.chais.ribbit;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class LoginActivity extends ActionBarActivity {
	private static final String TAG = LoginActivity.class.getSimpleName();
	@InjectView(R.id.usernameField) EditText mUsername;
	@InjectView(R.id.passwordField) EditText mPassword;
	@InjectView(R.id.loginButton) Button mLoginButton;
	@InjectView(R.id.signUpText) TextView mSignUpTextView;

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
		String username = mUsername.getText().toString();
		String password = mPassword.getText().toString();

		username = username.trim();
		password = password.trim();

		if (username.isEmpty() || password.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.login_error_message))
					.setTitle(getString(R.string.login_error_title))
					.setPositiveButton(android.R.string.ok, null)
					.create()
					.show();
			return;
		}

		setSupportProgressBarIndeterminateVisibility(true);
		ParseUser.logInInBackground(username, password, logInCallback);
	}

	private LogInCallback logInCallback = new LogInCallback() {
		@Override
		public void done(ParseUser parseUser, ParseException e) {
			setSupportProgressBarIndeterminateVisibility(false);
			if (e != null) {
				Log.e(TAG, e.getMessage());
				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
				builder.setMessage(getString(R.string.login_error_parse))
						.setTitle(getString(R.string.login_error_title))
						.setPositiveButton(android.R.string.ok, null)
						.create()
						.show();
				return;
			}

			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}
	};
}
