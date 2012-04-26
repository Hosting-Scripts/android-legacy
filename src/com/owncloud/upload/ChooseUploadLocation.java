package com.owncloud.upload;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.webdav.MultiStatus;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.owncloud.R;
import com.owncloud.activity.CreateNewDirectory;
import com.sufalam.WebdavMethodImpl;
import com.sufalam.actionbar.NewQAAdapter;

public class ChooseUploadLocation extends WebdavMethodImpl implements
		OnClickListener {

	Button mCancel;
	Button mChoose;
	Button mListBack;
	ProgressBar progressBar;

	ImageButton mCreateFolder;

	TextView mDirText;
	TextView mCurrentDir;
	List<String> listName = new ArrayList<String>();

	ListView mListView;
	String mUrl;
	List<String> listUrl = new ArrayList<String>();
	NewQAAdapter adapter;

	boolean flag = true;

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		trimCache(getApplicationContext());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.uploadlocation);

		progressBar = (ProgressBar) findViewById(R.id.progress);

		mListView = (ListView) findViewById(R.id.listview);
		mListBack = (Button) findViewById(R.id.backList);
		mDirText = (TextView) findViewById(R.id.dirText);
		mCreateFolder = (ImageButton) findViewById(R.id.createFolder);
		mCurrentDir = (TextView) findViewById(R.id.currentDir);
		mCancel = (Button) findViewById(R.id.cancel);
		mChoose = (Button) findViewById(R.id.choose);
		mListBack.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mChoose.setOnClickListener(this);
		mCreateFolder.setOnClickListener(this);

		try {
			mUrl = getIntent().getExtras().getString("Url");
			if (mUrl.equals(mainUrl)) {
				mListBack.setVisibility(View.GONE);
			} else {
				mListBack.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adapter = new NewQAAdapter(this, "Upload");
		// ListAllFiles();
		progressBar.setVisibility(View.VISIBLE);
		mListView.setEnabled(false);
		new ListAllDir().execute();

		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				try {

					if (!listUrl.get(position).endsWith("Shared/")) {
						if (!((listUrl.get(position).substring(listUrl.get(
								position).length() - 1)).equals("/"))) {

							view.setClickable(false);
							id = position;

						} else {
							// mLastUrl = url;
							mUrl = baseUrl
									+ listUrl.get(position).substring(0,
											listUrl.get(position).length() - 1);
							// ListAllFiles();
							mListView.setEnabled(false);
							progressBar.setVisibility(View.VISIBLE);
							new ListAllDir().execute();
						}
					} else {
						new AlertDialog.Builder(ChooseUploadLocation.this)
								.setMessage(
										"Sorry, You don't have permission to upload file in Shared directory.")
								.setTitle("ownCloud Alert")
								.setPositiveButton("OK", null).show();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	class ListAllDir extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (isOnline()) {
						listName.clear();
						listUrl.clear();
						mListFile.clear();
						System.gc();

						MultiStatus ms = null;

						try {
							ms = listAll(mUrl, httpClient);
							String[] name = mUrl.split("webdav.php/");
							mCurrentDir.setText(" ownCloud/"
									+ URLDecoder.decode(name[1]));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							mCurrentDir.setText(" ownCloud/");
						}
						// if (ms.equals(null)) {
						if (ms == null) {
							// Toast.makeText(getApplicationContext(),
							// "There is some problem in listing files",
							// 1).show();
						} else {

							int j = 0;
							Log.e("Total Files ",
									"===============> "
											+ String.valueOf(ms.getResponses().length - 1));

							if (flag) {
								for (int i = 1; i <= ms.getResponses().length - 1; i++) {

									String respString = ms.getResponses()[i]
											.getHref();

									File file = new File(
											URLDecoder.decode(respString));
									mListFile.add(file.getName());

									String fileName = new StringBuffer(
											respString).reverse().toString();
									Log.i("File Name",
											"====> [ " + String.valueOf(i)
													+ " ]  " + file.getName());
									if ((fileName.substring(0, 1)).equals("/")) {

										fileName = fileName.substring(1);

										listUrl.add(respString);
										Log.i("ListUrl ", listUrl.get(j));
										String[] fileNameArray = fileName
												.split("/");

										listName.add(new StringBuffer(
												fileNameArray[0] + "/")
												.reverse().toString());

										Log.i("ListName ", listName.get(j++));

									}

								}
							}
						}
					} else {
						WebNetworkAlert();
					}
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			adapter.setData(listName.toArray(new String[listName.size()]));
			mListView.setAdapter(adapter);
			try {
				if (mUrl.equals(mainUrl)) {
					Log.i("URL Compair", "mUrl : " + mUrl + " == " + mainUrl);
					mListBack.setVisibility(View.GONE);
				} else {
					mListBack.setVisibility(View.VISIBLE);
					Log.d("URL Compair", "mUrl : " + mUrl + " != " + mainUrl);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.e("Process End", "===========================================");
			progressBar.setVisibility(View.GONE);
			mListView.setEnabled(true);

			System.gc();
			trimCache(getApplicationContext());

		}

		@Override
		protected void onProgressUpdate(Object... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			Log.e("Process return",
					"===========================================");
			progressBar.setVisibility(View.GONE);
			mListView.setEnabled(true);
		}

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.i("ChooseUploadLocation", "==========> Restart <========");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i("ChooseUploadLocation", "==========> Resume <========");

		if (!(CreateNewDirectory.mUrl.equals("") || CreateNewDirectory.mUrl == null)) {
			// mUrl = CreateNewDirectory.mUrl;
			progressBar.setVisibility(View.VISIBLE);
			mListView.setEnabled(false);
			new ListAllDir().execute();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mCancel) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message msg = handler.obtainMessage();
					msg.obj = "mCancel";
					handler.sendMessage(msg);
				}
			};
			t.start();
		} else if (v == mChoose) {

			showDialog(0);
			t = new Thread() {
				public void run() {
					Message msg = handler.obtainMessage();
					msg.obj = "mChoose";
					handler.sendMessage(msg);
				}
			};
			t.start();
		} else if (v == mListBack) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message msg = handler.obtainMessage();
					msg.obj = "back";
					handler.sendMessage(msg);
				}
			};
			t.start();
		} else if (v == mCreateFolder) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message msg = handler.obtainMessage();
					msg.obj = "CreateFolder";
					handler.sendMessage(msg);
				}
			};
			t.start();
		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			showDialog(0);
			if (msg.obj.toString().contentEquals("mCancel")) {
				// startActivity(new Intent(getApplicationContext(),
				// FileExplorer.class).putExtra("fileLocation", ""));
				finish();
			} else if (msg.obj.toString().contentEquals("mChoose")) {
				// startActivity(new Intent(getApplicationContext(),
				// FileExplorer.class).putExtra("fileLocation", mUrl));
				fileLocation = mUrl;
				String[] name = mUrl.split("webdav.php/");
				try {
					mOwnCloud.setText("ownCloud/" + URLDecoder.decode(name[1]));
				} catch (Exception e) {
					mOwnCloud.setText("Destination Folder : ownCloud/");
				}
				flag = false;
				finish();
				Log.i("Location : ", mUrl);
			} else if (msg.obj.toString().contentEquals("back")) {

				try {
					String fileName = new StringBuffer(mUrl).reverse()
							.toString();
					if ((fileName.substring(0, 1)).equals("/")) {
						fileName = fileName.substring(1);
					}
					fileName = new StringBuffer(fileName).reverse().toString();
					String[] fileNameArray = fileName.split("/");
					for (int i = 0; i < fileNameArray.length - 1; i++) {
						if (i == 0) {
							mUrl = fileNameArray[i];
						} else {
							mUrl = mUrl + "/" + fileNameArray[i];
						}
					}
					if (mUrl.equals(mainUrl)) {
						mListBack.setVisibility(View.GONE);
					}
					Log.d("mUrl", mUrl);
					progressBar.setVisibility(View.VISIBLE);
					mListView.setEnabled(false);
					new ListAllDir().execute();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (msg.obj.toString().contentEquals("CreateFolder")) {

				if (!(url.contains("/Shared/") || url.endsWith("/Shared"))) {

					startActivity(new Intent(getApplicationContext(),
							CreateNewDirectory.class)
							.putExtra("From", "Upload").putExtra("Url", mUrl));
					// finish();

				} else {
					new AlertDialog.Builder(ChooseUploadLocation.this)
							.setMessage(
									"Sorry, You don't have permission to create folder in Shared directory.")
							.setTitle("ownCloud Alert")
							.setPositiveButton("OK", null).show();
				}

			} else if (msg.obj.toString().contentEquals("ListAllFiles")) {

			}
			removeDialog(0);
		}

	};

}
