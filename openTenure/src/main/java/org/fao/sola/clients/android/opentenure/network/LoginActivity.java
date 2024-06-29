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

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

	public static final int _NO_CONNECTION = 460;
	public static final int REQUEST_CODE = 100;
	public static final int RESULT_CODE_SUCCESS = 200;

	public static final int RESULT_CODE_SUCCESS_INITIALIZE = 250;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// UI references.
	private EditText txtUsernameView;
	private EditText txtPasswordView;
	private EditText txtServerUrl;
	private TextView txtError;
	private View loginFormView;
	private View loginStatusView;
	private TextView txtLoginStatusMessage;
	private boolean showSuccessMessage = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		txtServerUrl = (EditText) findViewById(R.id.server_url);
		txtServerUrl.setText(csUrl);
		showSuccessMessage = getIntent().getBooleanExtra("showSuccessMessage", true);

		if(!csUrl.equalsIgnoreCase("")){
			txtServerUrl.setVisibility(View.GONE);
		}

		txtUsernameView = (EditText) findViewById(R.id.username);
		txtError = (TextView) findViewById(R.id.txtError);
		txtPasswordView = (EditText) findViewById(R.id.password);
		txtPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		loginFormView = findViewById(R.id.login_form);
		loginStatusView = findViewById(R.id.login_status);
		txtLoginStatusMessage = (TextView) findViewById(R.id.login_status_message);
		this.getCurrentFocus();

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
		//getMenuInflater().inflate(R.menu.login_activity, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		txtError.setText("");
		txtError.setVisibility(View.GONE);

		String serverProtoVersion = CommunityServerAPI.getServerProtoVersion();
		String expectedProtoVersion = Configuration.getConfigurationValue(Configuration.PROTOVERSION_NAME);
		Toast toast;

		if(expectedProtoVersion != null && serverProtoVersion != null){
			if(expectedProtoVersion.compareTo(serverProtoVersion) > 0){
				toast = Toast.makeText(OpenTenureApplication.getContext(),
						R.string.message_update_server, Toast.LENGTH_LONG);
				toast.show();
				return;
			} else if (expectedProtoVersion.compareTo(serverProtoVersion) < 0){
				toast = Toast.makeText(OpenTenureApplication.getContext(),
						R.string.message_update_client, Toast.LENGTH_LONG);
				toast.show();
				return;
			}
		}

		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		txtUsernameView.setError(null);
		txtPasswordView.setError(null);
		boolean cancel = false;
		View focusView = null;

		// Store values at the time of the login attempt.
		String userName = txtUsernameView.getText().toString();
		String pass = txtPasswordView.getText().toString();

		// Check for a valid password.
		if (TextUtils.isEmpty(pass)) {
			txtPasswordView.setError(getString(R.string.error_field_required));
			focusView = txtPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(userName)) {
			txtUsernameView.setError(getString(R.string.error_field_required));
			focusView = txtUsernameView;
			cancel = true;
		}

		if(txtServerUrl.getVisibility() == View.VISIBLE){
			String url = txtServerUrl.getText().toString();

			// Check if server URL is provided
			if (TextUtils.isEmpty(url)) {
				txtServerUrl.setError(getString(R.string.error_server_url_required));
				focusView = txtServerUrl;
				cancel = true;
			} else {
				// If server URL is different from the settings, save settings
				if(!OpenTenureApplication.getInstance().getServerUrl().equals(url)){
					OpenTenureApplication.getInstance().setServerUrl(url);
				}
			}
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			txtLoginStatusMessage.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userName, pass);
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
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			loginStatusView.setVisibility(View.VISIBLE);
			loginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							loginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			loginFormView.setVisibility(View.VISIBLE);
			loginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							loginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, Integer> {
		private String userName;

		protected Integer doInBackground(String... params) {
			// TODO: attempt authentication against a network service.
			try {
				userName = params[0];
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
					if(showSuccessMessage) {
						toast = Toast.makeText(OpenTenureApplication.getContext(),
								R.string.message_login_ok, Toast.LENGTH_SHORT);
						toast.show();
					}

					OpenTenureApplication.setLoggedin(true);
					OpenTenureApplication.setUsername(userName);

					((FragmentActivity) OpenTenureApplication.getActivity()).invalidateOptionsMenu();
					setResult(RESULT_CODE_SUCCESS);
					finish();

					break;
				case 401:
					txtError.setText(getString(R.string.error_incorrect_password));
					txtError.setVisibility(View.VISIBLE);
					break;
				case 404:
					txtError.setText(getString(R.string.message_service_not_available));
					txtError.setVisibility(View.VISIBLE);
					break;
				case _NO_CONNECTION:
					txtError.setText(getString(R.string.error_connection));
					txtError.setVisibility(View.VISIBLE);
					break;
				case 80:
					txtError.setText(getString(R.string.error_generic_conection));
					txtError.setVisibility(View.VISIBLE);
					break;
				case 0:
					txtError.setText(getString(R.string.error_generic_login));
					txtError.setVisibility(View.VISIBLE);
					break;
				default:
					break;
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
