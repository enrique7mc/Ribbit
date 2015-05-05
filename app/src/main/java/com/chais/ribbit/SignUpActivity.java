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

	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		ButterKnife.inject(this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_signup, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@OnClick(R.id.signupButton)
	public void SignUpButtonClick(View view) {
		String username = mUsername.getText().toString();
		String password = mPassword.getText().toString();
		String email = mEmail.getText().toString();

		username = username.trim();
		password = password.trim();
		email = email.trim();

		if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.signup_error_message))
				   .setTitle(getString(R.string.signup_error_title))
				   .setPositiveButton(android.R.string.ok, null)
				   .create()
				   .show();
			return;
		}

		// create the new user
		ParseUser newUser = new ParseUser();
		newUser.setUsername(username);
		newUser.setPassword(password);
		newUser.setEmail(email);

		setProgressBarIndeterminateVisibility(true);
		newUser.signUpInBackground(new SignUpCallback() {
			@Override
			public void done(ParseException e) {
				setProgressBarIndeterminateVisibility(false);
				if (e != null) {
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
					builder.setMessage(getString(R.string.signup_error_parse))
							.setTitle(getString(R.string.signup_error_title))
							.setPositiveButton(android.R.string.ok, null)
							.create()
							.show();
					return;
				}

				Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
			}
		});

	}
}