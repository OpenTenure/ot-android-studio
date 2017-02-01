package org.fao.sola.clients.android.opentenure.network;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
	public static final int _NO_CONNECTION = 460;
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private View questo;
	private TextView mLoginStatusMessageView;
	private OpenTenureApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.

		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.setText(mUsername);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		questo = this.getCurrentFocus();
		findViewById(R.id.log_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});

		findViewById(R.id.log_in_cancel_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						finish();

					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login_activity, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mUsernameView.setError(getString(R.string.error_invalid_username));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					mUsername, mPassword);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, Integer> {

		protected Integer doInBackground(String... params) {
			// TODO: attempt authentication against a network service.

			try {

				return CommunityServerAPI.login(params[0], params[1]);

			} catch (Throwable ex) {

				Log.d("LoginActivity",
						"Ok, An error has occurred during login:"
								+ ex.getMessage());
				ex.printStackTrace();
				return 0;
			}

		}

		@Override
		protected void onPostExecute(final Integer status) {
			mAuthTask = null;
			showProgress(false);

			Toast toast;

			switch (status) {
			case 200:
				OpenTenureApplication.setLoggedin(true);
				OpenTenureApplication.setUsername(mUsername);

				FragmentActivity fa = (FragmentActivity) OpenTenureApplication
						.getActivity();
				fa.invalidateOptionsMenu();

				toast = Toast.makeText(OpenTenureApplication.getContext(),
						R.string.message_login_ok, Toast.LENGTH_LONG);
				toast.show();

				finish();

				break;
			case 401:
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();

				break;

			case 404:
				mPasswordView
						.setError(getString(R.string.message_service_not_available));
				mPasswordView.requestFocus();

				break;

			case _NO_CONNECTION:
				mPasswordView.setError(getString(R.string.error_connection));
				mPasswordView.requestFocus();

				break;

			case 80:
				mPasswordView
						.setError(getString(R.string.error_generic_conection));
				mPasswordView.requestFocus();

				break;

			case 0:
				mPasswordView.setError(getString(R.string.error_generic_login));
				mPasswordView.requestFocus();

				break;

			default:
				break;
			}

			String serverProtoVersion = CommunityServerAPI.getServerProtoVersion();
			String expectedProtoVersion = Configuration.getConfigurationValue(Configuration.PROTOVERSION_NAME);

			if(expectedProtoVersion != null && serverProtoVersion != null){

				if(expectedProtoVersion.compareTo(serverProtoVersion) > 0){
					toast = Toast.makeText(OpenTenureApplication.getContext(),
							R.string.message_update_server, Toast.LENGTH_LONG);
					toast.show();
				}else if(expectedProtoVersion.compareTo(serverProtoVersion) < 0){
					toast = Toast.makeText(OpenTenureApplication.getContext(),
							R.string.message_update_client, Toast.LENGTH_LONG);
					toast.show();
				}
			}

		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}

	}
}
