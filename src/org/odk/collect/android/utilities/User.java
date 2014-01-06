package org.odk.collect.android.utilities;

import org.odk.collect.android.activities.LoginActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;

import com.mpower.mintel.android.models.UserData;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class User {

	public static final String BROADCAST_ACTION_AUTHENTICATION_DONE = "com.mpower.Collect.android.broadcast.AUTHENTICATION_DONE";
	public static final String BROADCAST_ACTION_AUTHENTICATION_NEEDED = "com.mpower.Collect.android.broadcast.AUTHENTICATION_NEEDED";

	public static final String LOGOUT_MESSAGE_NETWORK = "Internet not available.";
	public static final String LOGOUT_MESSAGE_NETWORK_SERVER = "Internet not available or server caused an error.";
	public static final String LOGOUT_MESSAGE_ID_MISSMATCH = "ID/password mismatch.";
	public static final String LOGOUT_MESSAGE_SERVER_ERROR = "Server unreachable or caused an error.";
	public static final String LOGOUT_MESSAGE_SESSION_EXPIRED = "Session expired.";
	public static final String LOGOUT_MESSAGE_INTERNAL_ERROR = "Internal application error.";
	public static final String LOGOUT_MESSAGE_USER_REQUEST = "Logged out.";
	public static final String LOGOUT_MESSAGE_UNKNOWN = "Unknown error.";

	private static boolean OFFLINE_LOGIN_DEFAULT = false;

	private static volatile User instance = null;

	private volatile UserData mUserData = null;
	private volatile boolean mLoggedin = false;

	private User() {
		mUserData = new UserData();
	}

	private void loginFinalCheck(boolean initialLoginPassed, String logoutMessage) {
		if (initialLoginPassed) {
			if ((mUserData.getUsername().length() > 0) && (mUserData.getPassword().length() > 0)) {
				login();
			} else {
				logOff(LOGOUT_MESSAGE_INTERNAL_ERROR);
			}
		} else {
			logOff(logoutMessage);
		}
	}

	public boolean isLoggedin() {
		return mLoggedin;
	}

	public void setLoginResult(boolean succeed, UserData ld, String logoutMessage) {
		if (succeed && (ld != null)) {
			mUserData = ld;
			loginFinalCheck(true, null);
		} else {
			loginFinalCheck(false, logoutMessage);
		}
	}

	public boolean checkOfflineLogin(String username, String password) throws Exception {
		boolean retVal = false;

		if ("".equals(username.trim()) || "".equals(password.trim())) {
			return retVal;
		}

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getAppContext());
		String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
		String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);

		if (username.equals(storedUsername) && password.equals(storedPassword)) {
			retVal = true;
		}

		return retVal;
	}

	public UserData extractOfflineLoginData() {

		UserData lr = new UserData();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getAppContext());
		String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
		String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);

		lr.setUsername(storedUsername);
		lr.setPassword(storedPassword);

		return lr;
	}

	public boolean offlineUserDataAvailable() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getAppContext());
		String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
		String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);

		if ((storedUsername != null) && (storedPassword != null)) {
			if ((storedUsername.length() > 0) && (storedPassword.length() > 0)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void checkLogin(String u, String p, LoginActivity activityInstance) throws Exception {

		boolean offlineUserDataAvailable = offlineUserDataAvailable();

		if (OFFLINE_LOGIN_DEFAULT) {
			// Offline login default
			if (offlineUserDataAvailable) {
				// Login offline if data is available
				if (checkOfflineLogin(u, p)) {
					UserData ld = extractOfflineLoginData();
					setLoginResult(true, ld, null);
				} else {
					// Login doesn't match with offline data, check online
					if (WebUtils.isConnected(Collect.getAppContext())) {
						validateLoginFromServer(activityInstance);
					} else {
						loginFinalCheck(false, LOGOUT_MESSAGE_ID_MISSMATCH + "\n" + LOGOUT_MESSAGE_NETWORK);
					}
				}
			} else {
				// User data is not available, proceed with regular login
				if (WebUtils.isConnected(Collect.getAppContext())) {
					validateLoginFromServer(activityInstance);
				} else {
					loginFinalCheck(false, LOGOUT_MESSAGE_NETWORK);
				}
			}

		} else {
			// Online login default
			if (WebUtils.isConnected(Collect.getAppContext())) {
				validateLoginFromServer(activityInstance);
			} else {
				// Network not available
				if (offlineUserDataAvailable) {
					if (checkOfflineLogin(u, p)) {
						UserData ld = extractOfflineLoginData();
						setLoginResult(true, ld, null);
					} else {
						loginFinalCheck(false, LOGOUT_MESSAGE_ID_MISSMATCH + "\n" + LOGOUT_MESSAGE_NETWORK);
					}
				} else {
					loginFinalCheck(false, LOGOUT_MESSAGE_NETWORK);
				}
			}
		}

	}

	private void validateLoginFromServer(LoginActivity activityInstance) {
		activityInstance.checkLoginOnline();
	}

	public void logOff(String logoutMessage) {
		mUserData.resetAll();
		mLoggedin = false;

		WebUtils.clearAllCredentials();

		Intent broadcastIntent = new Intent();
		broadcastIntent.putExtra("message", logoutMessage);
		broadcastIntent.setAction(BROADCAST_ACTION_AUTHENTICATION_NEEDED);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		Collect.getAppContext().sendBroadcast(broadcastIntent);
	}

	public void logOffAndClearCache() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getAppContext());

		settings.edit().putString(PreferencesActivity.KEY_USERNAME, null).commit();
		settings.edit().putString(PreferencesActivity.KEY_PASSWORD, null).commit();

		logOff(LOGOUT_MESSAGE_SESSION_EXPIRED);
	}

	private void login() {

		mLoggedin = true;

		// Let all broadcast receivers know that we have logged in
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(BROADCAST_ACTION_AUTHENTICATION_DONE);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		Collect.getAppContext().sendBroadcast(broadcastIntent);

		WebUtils.clearAllCredentials();
		WebUtils.addCredentials(mUserData.getUsername(), mUserData.getPassword());

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getAppContext());

		settings.edit().putString(PreferencesActivity.KEY_USERNAME, mUserData.getUsername()).commit();
		settings.edit().putString(PreferencesActivity.KEY_PASSWORD, mUserData.getPassword()).commit();
	}

	public UserData getUserData() {
		return mUserData;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static User getInstance() {
		if (instance == null) {
			synchronized (User.class) {
				if (instance == null) {
					instance = new User();
				}
			}
		}
		return instance;
	}

}
