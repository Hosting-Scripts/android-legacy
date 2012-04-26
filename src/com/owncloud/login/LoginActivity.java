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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.owncloud.R;
import com.owncloud.activity.DashBoardActivity;
import com.sufalam.WebdavMethodImpl;

public class LoginActivity extends WebdavMethodImpl implements OnClickListener {
	/** Called when the activity is first created. */

	EditText mUserName;
	EditText mUserPass;
	EditText mServerUrl;
	EditText mBaseUrl;
	CheckBox mSSL;

	ImageButton mLoginBtn;
	ImageButton mCancelBtn;
	ImageView mProto;

	String mUrl;
	String mUrlSsl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mUserName = (EditText) findViewById(R.id.userName);
		mUserPass = (EditText) findViewById(R.id.password);
		mServerUrl = (EditText) findViewById(R.id.serverUrl);
		mBaseUrl = (EditText) findViewById(R.id.baseUrl);
		mProto = (ImageView) findViewById(R.id.proto);

		mSSL = (CheckBox) findViewById(R.id.sslBtn);
		mSSL.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub

				if (isChecked)
					mProto.setBackgroundResource(R.drawable.text_https);
				else
					mProto.setBackgroundResource(R.drawable.text_http);
			}
		});

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
					mProto.setBackgroundResource(R.drawable.text_http);
					url = url.replace("http://", "");
				} else if (url.contains("https://")) {
					mProto.setBackgroundResource(R.drawable.text_https);
					url = url.replace("https://", "");
					mSSL.setChecked(true);
				}
				mBaseUrl.setText(url);
			} else {
				mUserName.setText(pref.getString(PREF_USERNAME, null));
				mUserPass.setText(pref.getString(PREF_PASSWORD, null));

				String url = pref.getString(PREF_SERVERURL, null);
				if (url.contains("http://")) {
					mProto.setBackgroundResource(R.drawable.text_http);
					url = url.replace("http://", "");
				} else if (url.contains("https://")) {
					mProto.setBackgroundResource(R.drawable.text_https);
					url = url.replace("https://", "");
					mSSL.setChecked(true);
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
						if (mSSL.isChecked()) {
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
							// TODO Auto-generated catch block
							e.printStackTrace();
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
	public void onClick(View v) {
		// TODO Auto-generated method stub

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