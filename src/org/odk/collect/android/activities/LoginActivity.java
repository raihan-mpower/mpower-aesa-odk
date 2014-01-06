package org.odk.collect.android.activities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.User;
import org.odk.collect.android.utilities.WebUtils;
import org.opendatakit.httpclientandroidlib.HttpEntity;
import org.opendatakit.httpclientandroidlib.HttpResponse;

import com.mpower.mintel.android.models.UserData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * LoginActivity - Login Activity
 * 
 * @author Mehdi Hasan <mhasan@mpower-health.com>
 * 
 */
public class LoginActivity extends Activity {

	/**
	 * Simple Dialog used to show the splash screen
	 */
	protected Dialog mSplashDialog;

	private EditText usernameEditText, passwordEditText;
	private Button loginButton;

	private String username = "", password = "";

	private User user;

	// menu options
	private static final int MENU_PREFERENCES = Menu.FIRST;

	private BroadcastReceiver authenticationDoneReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateData(null);
		}
	};

	private BroadcastReceiver authenticationNeededReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String logoutMessage = intent.getStringExtra("message");
			updateData(logoutMessage);
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(authenticationDoneReceiver);
		unregisterReceiver(authenticationNeededReceiver);
	}

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// FIX: Initial back button from homescreen causes problem logging in
		User.getInstance().logOff("");

		setContentView(R.layout.login_screen);

		IntentFilter authenticationDoneFilter = new IntentFilter(User.BROADCAST_ACTION_AUTHENTICATION_DONE);
		authenticationDoneFilter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(authenticationDoneReceiver, authenticationDoneFilter);

		IntentFilter authenticationNeededFilter = new IntentFilter(User.BROADCAST_ACTION_AUTHENTICATION_NEEDED);
		authenticationNeededFilter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(authenticationNeededReceiver, authenticationNeededFilter);

		user = User.getInstance();

		usernameEditText = (EditText) findViewById(R.id.login_username);
		passwordEditText = (EditText) findViewById(R.id.login_password);
		usernameEditText.setFilters(new InputFilter[] { getReturnFilter(), getWhitespaceFilter() });
		passwordEditText.setFilters(new InputFilter[] { getReturnFilter(), getWhitespaceFilter() });

		loginButton = (Button) findViewById(R.id.login_button);

		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkLogin();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_PREFERENCES, 0, getString(R.string.general_preferences)).setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			Intent ig = new Intent(this, PreferencesActivity.class);
			startActivity(ig);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateData(String logoutMessage) {

		if (user.isLoggedin()) {
			processValidLogin();
		} else {
			processInvalidLogin(logoutMessage);
		}
	}

	private void checkLogin() {
		username = usernameEditText.getText().toString().trim();
		password = WebUtils.getSHA512(passwordEditText.getText().toString().trim());

		if (!(username.length() > 0)) {
			Toast.makeText(this, "Please enter User ID", Toast.LENGTH_LONG).show();
			return;
		}

		if (!(password.length() > 0)) {
			Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
			return;
		}

		try {
			user.checkLogin(username, password, this);
		} catch (Exception e) {
			showAlertDialog("Login failed!", e.getMessage());
			e.printStackTrace();
		}

	}

	private void processInvalidLogin(String logoutMessage) {
		if (logoutMessage == null || "".equals(logoutMessage)) {
			logoutMessage = User.LOGOUT_MESSAGE_UNKNOWN;
		}
		showAlertDialog("Login failed!", "Possible causes:" + "\n\n" + logoutMessage);
		passwordEditText.setText("");
	}

	private void processValidLogin() {
		startNextActivity();
	}

	private void startNextActivity() {
		Intent i = new Intent(this, MainMenuActivity.class);
		startActivity(i);
		finish();
	}

	private void showAlertDialog(String title, String message) {

		AlertDialog.Builder adb = new AlertDialog.Builder(this);

		adb.setTitle(title);
		adb.setMessage(message);

		adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});

		adb.show();
	}

	private InputFilter getReturnFilter() {
		InputFilter returnFilter = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (Character.getType((source.charAt(i))) == Character.CONTROL) {
						return "";
					}
				}
				return null;
			}
		};
		return returnFilter;
	}

	private InputFilter getWhitespaceFilter() {
		InputFilter whitespaceFilter = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (Character.isWhitespace(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};
		return whitespaceFilter;
	}

	public void checkLoginOnline() {
		new LoginTask().execute();
	}

	protected void removeSplashScreen() {
		if (mSplashDialog != null) {
			mSplashDialog.dismiss();
			mSplashDialog = null;
		}
	}

	/**
	 * LoginTask - AsyncTask for logging in to server
	 * 
	 * @author Mehdi Hasan <mhasan@mpower-health.com>
	 * 
	 */
	class LoginTask extends AsyncTask<Void, Void, Void> {

		private String loginUrl;
		private int timeOut;
		private int loginStatus = 0;
		@SuppressWarnings("unused")
		private Exception loginE = null;
		private String loginResponse = "";
		private UserData onlineLd = null;

		private ProgressDialog pbarDialog;

		private void initPrefs() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
			loginUrl = prefs.getString(PreferencesActivity.KEY_SERVER_URL, null) + WebUtils.URL_PART_LOGIN;
			timeOut = WebUtils.CONNECTION_TIMEOUT;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pbarDialog = new ProgressDialog(LoginActivity.this);
			pbarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pbarDialog.setTitle(getString(R.string.please_wait));
			pbarDialog.setMessage("Logging in...");
			pbarDialog.setCancelable(false);
			pbarDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			if ("".equals(username) || "".equals(password)) {
				return null;
			}

			WebUtils.clearAllCredentials();
			WebUtils.addCredentials(username, password);

			initPrefs();
			login();

			return null;
		}

		private void login() {
			HttpResponse response;
			try {
				response = WebUtils.stringResponseGet(loginUrl, Collect.getInstance().getHttpContext(), WebUtils.createHttpClient(timeOut));
				loginStatus = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();

				Log.d("Login Status", "" + loginStatus);
				if ((entity != null) && (loginStatus == 200)) {
					onlineLd = new UserData();
				}

			} catch (Exception e) {
				loginE = e;
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute(Void result) {

			if (pbarDialog != null && pbarDialog.isShowing()) {
				pbarDialog.dismiss();
			}

			if ((loginStatus == 200) && (onlineLd != null)) {

				onlineLd.setUsername(username);
				onlineLd.setPassword(password);

				User.getInstance().setLoginResult(true, onlineLd, null);
			} else {
				if (loginStatus != 401) {
					// There was an error checking login online, but we are not
					// explicitly denied, let's proceed with offline login if
					// possible

					try {
						boolean offlineUserDataAvailable = User.getInstance().offlineUserDataAvailable();

						if (offlineUserDataAvailable) {
							if (User.getInstance().checkOfflineLogin(username, password)) {
								UserData offlineLd = User.getInstance().extractOfflineLoginData();
								User.getInstance().setLoginResult(true, offlineLd, null);
							} else {
								// Offline login username/password mismatch
								User.getInstance().setLoginResult(false, null, User.LOGOUT_MESSAGE_ID_MISSMATCH);
							}
						} else {
							// Offline login data not available
							User.getInstance().setLoginResult(false, null, User.LOGOUT_MESSAGE_NETWORK_SERVER);
						}
					} catch (Exception e) {
						User.getInstance().setLoginResult(false, null, User.LOGOUT_MESSAGE_INTERNAL_ERROR);
					}

				} else {
					// Login failed for sure, server returned 401
					User.getInstance().setLoginResult(false, null, User.LOGOUT_MESSAGE_ID_MISSMATCH);
				}
			}
		}
	}
}
