/*
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.utilities;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.opendatakit.httpclientandroidlib.auth.*;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.HttpClient;
import org.opendatakit.httpclientandroidlib.client.methods.HttpGet;
import org.opendatakit.httpclientandroidlib.client.methods.HttpHead;
import org.opendatakit.httpclientandroidlib.client.methods.HttpPost;
import org.opendatakit.httpclientandroidlib.client.params.AuthPolicy;
import org.opendatakit.httpclientandroidlib.client.params.ClientPNames;
import org.opendatakit.httpclientandroidlib.client.params.HttpClientParams;
import org.opendatakit.httpclientandroidlib.client.protocol.ClientContext;
import org.opendatakit.httpclientandroidlib.impl.client.DefaultHttpClient;
import org.opendatakit.httpclientandroidlib.params.BasicHttpParams;
import org.opendatakit.httpclientandroidlib.params.HttpConnectionParams;
import org.opendatakit.httpclientandroidlib.params.HttpParams;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;
import org.opendatakit.httpclientandroidlib.*;
import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

/**
 * Common utility methods for managing the credentials associated with the
 * request context and constructing http context, client and request with the
 * proper parameters and OpenRosa headers.
 * 
 * @author mitchellsundt@gmail.com
 */
public final class WebUtils {
	public static final String t = "WebUtils";

	public static final String OPEN_ROSA_VERSION_HEADER = "X-OpenRosa-Version";
	public static final String OPEN_ROSA_VERSION = "1.0";
	private static final String DATE_HEADER = "Date";

	public static final String HTTP_CONTENT_TYPE_TEXT_XML = "text/xml";
	public static final int CONNECTION_TIMEOUT = 30000;

	public static final String URL_PART_LOGIN = "/m/login";
	public static final String URL_PART_SUBMISSION = "/m/submission";
	public static final String URL_PART_FORMLIST = "/m/formList";

	private static final GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

	public static final AuthScope buildAuthScopes(String host) {

		AuthScope a;
		// allow digest auth on any port...
		a = new AuthScope(host, -1, null, AuthPolicy.DIGEST);

		return a;
	}

	public static void refreshCredential() {
		if (User.getInstance().isLoggedin()) {
			clearAllCredentials();
			addCredentials(User.getInstance().getUserData().getUsername(), User.getInstance().getUserData().getPassword());
		}
	}

	public static final void clearAllCredentials() {
		HttpContext localContext = Collect.getInstance().getHttpContext();
		CredentialsProvider credsProvider = (CredentialsProvider) localContext.getAttribute(ClientContext.CREDS_PROVIDER);
		credsProvider.clear();
	}

	public static final boolean hasCredentials(String userEmail, String host) {
		HttpContext localContext = Collect.getInstance().getHttpContext();
		CredentialsProvider credsProvider = (CredentialsProvider) localContext.getAttribute(ClientContext.CREDS_PROVIDER);

		AuthScope as = buildAuthScopes(host);
		boolean hasCreds = true;

		Credentials c = credsProvider.getCredentials(as);
		if (c == null) {
			hasCreds = false;
		}
		return hasCreds;
	}

	public static void addCredentials(String username, String password) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
		String scheduleUrl = prefs.getString(PreferencesActivity.KEY_SERVER_URL, null);

		String host = "";

		try {
			host = new URL(URLDecoder.decode(scheduleUrl, "utf-8")).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		addCredentials(username, password, host);
	}

	public static final void addCredentials(String userEmail, String password, String host) {
		HttpContext localContext = Collect.getInstance().getHttpContext();
		Credentials c = new UsernamePasswordCredentials(userEmail, password);
		addCredentials(localContext, c, host);
	}

	private static final void addCredentials(HttpContext localContext, Credentials c, String host) {
		CredentialsProvider credsProvider = (CredentialsProvider) localContext.getAttribute(ClientContext.CREDS_PROVIDER);

		AuthScope as = buildAuthScopes(host);
		credsProvider.setCredentials(as, c);
	}

	private static final void setOpenRosaHeaders(HttpRequest req) {
		req.setHeader(OPEN_ROSA_VERSION_HEADER, OPEN_ROSA_VERSION);
		g.setTime(new Date());
		req.setHeader(DATE_HEADER, DateFormat.format("E, dd MMM yyyy hh:mm:ss zz", g).toString());
	}

	public static final HttpHead createOpenRosaHttpHead(URI uri) {
		HttpHead req = new HttpHead(uri);
		setOpenRosaHeaders(req);
		return req;
	}

	public static final HttpGet createOpenRosaHttpGet(URI uri) {
		return createOpenRosaHttpGet(uri, "");
	}

	public static final HttpGet createOpenRosaHttpGet(URI uri, String auth) {
		HttpGet req = new HttpGet();
		setOpenRosaHeaders(req);
		setGoogleHeaders(req, auth);
		req.setURI(uri);
		return req;
	}

	public static final void setGoogleHeaders(HttpRequest req, String auth) {
		if ((auth != null) && (auth.length() > 0)) {
			req.setHeader("Authorization", "GoogleLogin auth=" + auth);
		}
	}

	public static final HttpPost createOpenRosaHttpPost(URI uri) {
		return createOpenRosaHttpPost(uri, "");
	}

	public static final HttpPost createOpenRosaHttpPost(URI uri, String auth) {
		HttpPost req = new HttpPost(uri);
		setOpenRosaHeaders(req);
		setGoogleHeaders(req, auth);
		return req;
	}

	public static final HttpClient createHttpClient(int timeout) {
		// configure connection
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		// support redirecting to handle http: => https: transition
		HttpClientParams.setRedirecting(params, true);
		// support authenticating
		HttpClientParams.setAuthenticating(params, true);
		// if possible, bias toward digest auth (may not be in 4.0 beta 2)
		List<String> authPref = new ArrayList<String>();
		authPref.add(AuthPolicy.DIGEST);
		authPref.add(AuthPolicy.BASIC);
		// does this work in Google's 4.0 beta 2 snapshot?
		params.setParameter("http.auth-target.scheme-pref", authPref);

		// setup client
		HttpClient httpclient = new DefaultHttpClient(params);
		httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 1);
		httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

		return httpclient;
	}

	/**
	 * Common method for returning a parsed xml document given a url and the
	 * http context and client objects involved in the web connection.
	 * 
	 * @param urlString
	 * @param localContext
	 * @param httpclient
	 * @return
	 */
	public static DocumentFetchResult getXmlDocument(String urlString, HttpContext localContext, HttpClient httpclient, String auth) {
		URI u = null;
		try {
			URL url = new URL(URLDecoder.decode(urlString, "utf-8"));
			u = url.toURI();
		} catch (Exception e) {
			e.printStackTrace();
			return new DocumentFetchResult(e.getLocalizedMessage()
			// + app.getString(R.string.while_accessing) + urlString);
					+ ("while accessing") + urlString, 0);
		}

		// set up request...
		HttpGet req = WebUtils.createOpenRosaHttpGet(u, auth);

		HttpResponse response = null;
		try {
			response = httpclient.execute(req, localContext);
			int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity entity = response.getEntity();

			if (entity != null && (statusCode != 200 || !entity.getContentType().getValue().toLowerCase().contains(WebUtils.HTTP_CONTENT_TYPE_TEXT_XML))) {
				try {
					// don't really care about the stream...
					InputStream is = response.getEntity().getContent();
					// read to end of stream...
					final long count = 1024L;
					while (is.skip(count) == count)
						;
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (statusCode != 200) {
				String webError = response.getStatusLine().getReasonPhrase() + " (" + statusCode + ")";

				return new DocumentFetchResult(u.toString() + " responded with: " + webError, statusCode);
			}

			if (entity == null) {
				String error = "No entity body returned from: " + u.toString();
				Log.e(t, error);
				return new DocumentFetchResult(error, 0);
			}

			if (!entity.getContentType().getValue().toLowerCase().contains(WebUtils.HTTP_CONTENT_TYPE_TEXT_XML)) {
				String error = "ContentType: " + entity.getContentType().getValue() + " returned from: " + u.toString()
						+ " is not text/xml.  This is often caused a network proxy.  Do you need to login to your network?";
				Log.e(t, error);
				return new DocumentFetchResult(error, 0);
			}

			// parse response
			Document doc = null;
			try {
				InputStream is = null;
				InputStreamReader isr = null;
				try {
					is = entity.getContent();
					isr = new InputStreamReader(is, "UTF-8");
					doc = new Document();
					KXmlParser parser = new KXmlParser();
					parser.setInput(isr);
					parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
					doc.parse(parser);
					isr.close();
				} finally {
					if (isr != null) {
						try {
							isr.close();
						} catch (Exception e) {
							// no-op
						}
					}
					if (is != null) {
						try {
							is.close();
						} catch (Exception e) {
							// no-op
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				String error = "Parsing failed with " + e.getMessage() + "while accessing " + u.toString();
				Log.e(t, error);
				return new DocumentFetchResult(error, 0);
			}

			boolean isOR = false;
			Header[] fields = response.getHeaders(WebUtils.OPEN_ROSA_VERSION_HEADER);
			if (fields != null && fields.length >= 1) {
				isOR = true;
				boolean versionMatch = false;
				boolean first = true;
				StringBuilder b = new StringBuilder();
				for (Header h : fields) {
					if (WebUtils.OPEN_ROSA_VERSION.equals(h.getValue())) {
						versionMatch = true;
						break;
					}
					if (!first) {
						b.append("; ");
					}
					first = false;
					b.append(h.getValue());
				}
				if (!versionMatch) {
					Log.w(t, WebUtils.OPEN_ROSA_VERSION_HEADER + " unrecognized version(s): " + b.toString());
				}
			}
			return new DocumentFetchResult(doc, isOR);
		} catch (Exception e) {
			e.printStackTrace();
			String cause;
			if (e.getCause() != null) {
				cause = e.getCause().getMessage();
			} else {
				cause = e.getMessage();
			}
			String error = "Error: " + cause + " while accessing " + u.toString();

			Log.w(t, error);
			return new DocumentFetchResult(error, 0);
		}
	}

	public static HttpResponse stringResponseGet(String urlString, HttpContext localContext, HttpClient httpclient) throws Exception {

		URL url = new URL(URLDecoder.decode(urlString, "utf-8"));
		URI u = url.toURI();

		HttpGet req = new HttpGet();
		req.setURI(u);

		HttpResponse response = null;
		response = httpclient.execute(req, localContext);

		return response;
	}

	public static boolean isConnected(Context context) {
		final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.getState() == NetworkInfo.State.CONNECTED) {
			return true;
		} else {
			return false;
		}
	}

	public static String getSHA512(String input) {
		String retval = "";
		try {
			MessageDigest m = MessageDigest.getInstance("SHA-512");
			byte[] out = m.digest(input.getBytes());
			retval = Base64.encodeToString(out, Base64.NO_WRAP);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return retval;
	}

}
