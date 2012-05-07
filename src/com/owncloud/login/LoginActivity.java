package com.owncloud.login;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jackrabbit.webdav.MultiStatus;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.owncloud.R;
import com.owncloud.activity.DashBoardActivity;
import com.sufalam.WebdavMethodImpl;

public class LoginActivity extends WebdavMethodImpl implements OnClickListener, OnFocusChangeListener {
	EditText mUserName;
	EditText mUserPass;
	EditText mServerUrl;
	EditText mBaseUrl;

	ImageButton mLoginBtn;
	ImageButton mCancelBtn;
	ImageView mProto;
	TextView mConnectionHint;

	String mUrl;
	String mUrlSsl;
	
	private HttpConnectionTester mHttpConnectionTester = null;
	private boolean useSSL = true;
	
	/**
	 * Used for connection testing - enable / disable HTTPs automatically
	 */
	private Handler connectionTestHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				// SSL Test Started -> Provide user feedback
				mProto.setBackgroundResource(R.drawable.login_spinner);
				mProto.setVisibility(View.VISIBLE);
				AnimationDrawable spinner = (AnimationDrawable) mProto.getBackground();
				spinner.start();
				mConnectionHint.setText(getResources().getString(R.string.login_testing_connection));
				break;
			case 1:
				// Test result received
				if(!msg.getData().getBoolean(HttpConnectionTester.MSG_KEY_URL_VALID)){
					// Invalid URL
					onInvalidURL();
				} else {
					if(msg.getData().getBoolean(HttpConnectionTester.MSG_KEY_CONNECTED)){
						// We have contact! SSL enabled?
						if(msg.getData().getBoolean(HttpConnectionTester.MSG_KEY_SSL_SUCCESS)){
							onSSLSucess();
						} else {
							onSSLFailed();
						}
					} else {
						// Connection failed. Hostname might be wrong or there is no connection
						onNetworkError();
					}
				}
				break;
			default:
				throw new IllegalArgumentException("Invalid what specified for HTTP connection test message");
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mUserName = (EditText) findViewById(R.id.userName);
		mUserPass = (EditText) findViewById(R.id.password);
		mServerUrl = (EditText) findViewById(R.id.serverUrl);
		mBaseUrl = (EditText) findViewById(R.id.baseUrl);
		mProto = (ImageView) findViewById(R.id.proto);
		mConnectionHint = (TextView) findViewById(R.id.connectionHint);

		// Check HTTPS availability on focus lost
		mBaseUrl.setOnFocusChangeListener(this);

		mLoginBtn = (ImageButton) findViewById(R.id.loginBtn);
		mCancelBtn = (ImageButton) findViewById(R.id.cancelBtn);

		mLoginBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);

		if ((!(pref.getString(PREF_USERNAME, null) == null))
				&& (!(pref.getString(PREF_PASSWORD, null) == null))
				&& (!(pref.getString(PREF_SERVERURL, null) == null))
				&& (!(pref.getString(PREF_BASEURL, null) == null))
				&& (!(pref.getString(PREF_UNLINK, null) == null))) {

			if (pref.getString(PREF_UNLINK, null).equals("true")) {
				mUserName.setText(pref.getString(PREF_USERNAME, null));
				// mUserPass.setText(pref.getString(PREF_PASSWORD, null));

				String url = pref.getString(PREF_SERVERURL, null);
				if (url.contains("http://")) {
					url = url.replace("http://", "");
				} else if (url.contains("https://")) {
					url = url.replace("https://", "");
				}
				mBaseUrl.setText(url);
			} else {
				mUserName.setText(pref.getString(PREF_USERNAME, null));
				mUserPass.setText(pref.getString(PREF_PASSWORD, null));

				String url = pref.getString(PREF_SERVERURL, null);
				if (url.contains("http://")) {
					url = url.replace("http://", "");
				} else if (url.contains("https://")) {
					url = url.replace("https://", "");
				}
				mBaseUrl.setText(url);
			}
		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.obj.toString().equalsIgnoreCase("Login")) {
				if (isOnline()) {

					if (!mBaseUrl.getText().toString()
							.endsWith("/files/webdav.php")) {
						if ((mBaseUrl.getText().toString()).endsWith("/")) {
							mUrl = mBaseUrl.getText().toString()
									+ "files/webdav.php";
						} else {
							mUrl = mBaseUrl.getText().toString()
									+ "/files/webdav.php";
						}
					} else {
						mUrl = mBaseUrl.getText().toString();
					}
					if (!(mUrl.startsWith("https://") || mUrl
							.startsWith("http://"))) {
						if (useSSL) {
							mUrl = "https://" + mUrl;
							mUrlSsl = "https://";
						} else {
							mUrl = "http://" + mUrl;
							mUrlSsl = "http://";
						}
					}

					HostConfiguration config = new HostConfiguration();
					// Allow access even though certificate is self signed

					@SuppressWarnings("deprecation")
					Protocol lEasyHttps = new Protocol("https",
							new EasySslProtocolSocketFactory(), 443);
					Protocol.registerProtocol("https", lEasyHttps);

					config.setHost(mUrl, 443, lEasyHttps);

					Log.i("URL : ", mUrl);

					MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

					HttpConnectionManagerParams params = new HttpConnectionManagerParams();

					params.setMaxConnectionsPerHost(config, 5);

					httpClient = new HttpClient(manager);

					httpClient.setHostConfiguration(config);

					Credentials cred = new UsernamePasswordCredentials(
							mUserName.getText().toString(), mUserPass.getText()
									.toString());

					httpClient.getState().setCredentials(AuthScope.ANY, cred);

					MultiStatus ms = null;
					try {
						ms = listAll(mUrl, httpClient);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (ms == null) {
						Toast.makeText(getApplicationContext(),
								"Please enter valid details", 1).show();
					} else {

						try {
							String[] tempUrlNew = (mUrl.split("//"));
							String[] tempUrl = (tempUrlNew[1]).split("/");

							getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
									.edit()
									.putString(PREF_SERVERURL, mUrl)
									.putString(PREF_BASEURL,
											mUrlSsl + tempUrl[0])
									.putString(PREF_USERNAME,
											mUserName.getText().toString())
									.putString(PREF_PASSWORD,
											mUserPass.getText().toString())
									.putString(PREF_UNLINK, "false").commit();

							url = mUrl;

							baseUrl = mUrlSsl + tempUrl[0];

							startActivity(new Intent(getApplicationContext(),
									DashBoardActivity.class).putExtra("From",
									"PassCodeOff"));
							finish();
						} catch (Exception e) {
							Toast.makeText(getApplicationContext(),
									"Please enter valid details", 1).show();
						}
					}

				} else {
					WebNetworkAlert();
				}
			} else if (msg.obj.toString().equalsIgnoreCase("cancel")) {
				startActivity(new Intent(getApplicationContext(),
						LoginSelectionActivity.class));
				finish();
			}
			// finish();
			removeDialog(0);
		}

	};

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		
		if(!hasFocus){
			if(mHttpConnectionTester == null){
				mHttpConnectionTester = new HttpConnectionTester(connectionTestHandler);
				mHttpConnectionTester.start();
			}
			
			// Prepare message to test connection in a separate thread
			Message message = new Message();
			Bundle data = new Bundle();
			String url = mBaseUrl.getText().toString();
			data.putString(HttpConnectionTester.MSG_KEY_URL, url);
			message.setData(data);
			
			// Send message and test
			mHttpConnectionTester.getHandler().sendMessage(message);
		}
	}
	
	
	
	/**
	 * Called, when there is no SSL connection.
	 */
	private void onSSLFailed(){
		mProto.setBackgroundDrawable(getResources().getDrawable(R.drawable.lock_http));
		mConnectionHint.setText(getResources().getString(R.string.login_ssl_disabled));
		useSSL = false;
	}
	
	/**
	 * Called, when the SSL connection could be established
	 */
	private void onSSLSucess(){
		mProto.setBackgroundDrawable(getResources().getDrawable(R.drawable.lock_https));
		mConnectionHint.setText(getResources().getString(R.string.login_ssl_enabled));
		useSSL = true;
	}
	
	/**
	 * Called on total connection failure
	 */
	private void onNetworkError(){
		mProto.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_error));
		mConnectionHint.setText(getResources().getString(R.string.login_network_error));
	}
	
	/**
	 * Called when the URL is invalid
	 */
	private void onInvalidURL(){
		mProto.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_error));
		mConnectionHint.setText(getResources().getString(R.string.login_invalid_url));
	}
	
	@Override
	public void onClick(View v) {

		if (v == mLoginBtn) {

			showDialog(0);
			t = new Thread() {
				public void run() {
					Message msg = handler.obtainMessage();
					msg.obj = "Login";
					handler.sendMessage(msg);
				}
			};
			t.start();

		} else if (v == mCancelBtn) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message msg = handler.obtainMessage();
					msg.obj = "cancel";
					handler.sendMessage(msg);
				}
			};
			t.start();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			startActivity(new Intent(getApplicationContext(),
					LoginSelectionActivity.class));
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		trimCache(getApplicationContext());
	}

}