package com.chais.ribbit.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.chais.ribbit.R;
import com.chais.ribbit.util.Util;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class SignUpActivity extends ActionBarActivity {
	private static final String TAG = SignUpActivity.class.getSimpleName();
	@InjectView(R.id.usernameField) EditText mUsername;
	@InjectView(R.id.passwordField) EditText mPassword;
	@InjectView(R.id.emailField) EditText mEmail;
	@InjectView(R.id.signupButton) Button mSignUpButton;
	ProgressDialog progress;

	protected void onCreate(Bundle savedInstanceState) {
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		getSupportActionBar().hide();
		ButterKnife.inject(this);
	}

	@OnClick(R.id.signupButton)
	public void SignUpButtonClick(View view) {
		if (!Util.isNetworkAvailable(this)) {
			Util.alertDialogShow(this, getString(R.string.general_error),
					getString(R.string.no_network_available_message));
			return;
		}
		String username = mUsername.getText().toString();
		String password = mPassword.getText().toString();
		String email = mEmail.getText().toString();

		username = username.trim();
		password = password.trim();
		email = email.trim();

		if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
			Util.alertDialogShow(this, getString(R.string.signup_error_title),
					getString(R.string.signup_error_message));
			return;
		}

		ParseUser newUser = new ParseUser();
		newUser.setUsername(username);
		newUser.setPassword(password);
		newUser.setEmail(email);

		progress = ProgressDialog.show(this, "Sign up", "Please wait a moment", true);
		newUser.signUpInBackground(signUpCallback);
	}

	private SignUpCallback signUpCallback = new SignUpCallback() {
		@Override
		public void done(ParseException e) {
			progress.dismiss();
			if (e != null) {
				Log.e(TAG, e.getMessage());
				Util.alertDialogShow(SignUpActivity.this, getString(R.string.signup_error_title),
						e.getMessage());
				return;
			}

			Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}
	};
}
