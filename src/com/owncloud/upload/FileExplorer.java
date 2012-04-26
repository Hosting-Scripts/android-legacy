package com.owncloud.upload;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.owncloud.R;
import com.owncloud.activity.DashBoardActivity;
import com.owncloud.common.BaseActivity;
import com.sufalam.WebdavMethodImpl;

public class FileExplorer extends WebdavMethodImpl implements OnClickListener {

	String dirPath;
	FileExplorerAdapter adapter;
	Button mCancel;
	Button mUpload;
	// Button mOwnCloud;

	// ProgressBar mProgress;
	public static List<String> uploadFile = new ArrayList<String>();

	protected Thread t;
	ListView mListView;

	Button mAbort;
	LinearLayout mMainLayout;
	LinearLayout mLoadingLayout;

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		trimCache(getApplicationContext());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileexplorer);
		setTitle("File Explorer");
		// getDir("/mnt/sdcard");
		mListView = (ListView) findViewById(R.id.list);

		mCancel = (Button) findViewById(R.id.cancel);
		mUpload = (Button) findViewById(R.id.upload);
		mOwnCloud = (Button) findViewById(R.id.BtnOwnCloud);
		mAbort = (Button) findViewById(R.id.abort);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mUpload.setText("Upload");
				mUpload.setEnabled(true);
				mCancel.setEnabled(true);
				mOwnCloud.setEnabled(true);
			}
		});

		mMainLayout = (LinearLayout) findViewById(R.id.mailLayout);
		mLoadingLayout = (LinearLayout) findViewById(R.id.lodingLayout);

		mAbort.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		// mUpload.setOnClickListener(this);
		mOwnCloud.setOnClickListener(this);

		mUpload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLoadingLayout.setVisibility(View.VISIBLE);
				mUpload.setText("Uploading..");
				mUpload.setEnabled(true);
				mCancel.setEnabled(true);
				mOwnCloud.setEnabled(true);
				new UploadingData().execute();

			}
		});

		// mProgress = (ProgressBar) findViewById(R.id.progress);
		adapter = new FileExplorerAdapter(this);
		mListView.setAdapter(adapter);
		if (!getIntent().getExtras().getString("fileLocation").equals("")) {
			fileLocation = getIntent().getExtras().getString("fileLocation");
		} else {
			fileLocation = BaseActivity.url;
		}
		// Log.i("FileLocation ", fileLocation);
	}

	class UploadingData extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub

			// TODO Auto-generated method stub

			Log.i("Total Files : ", String.valueOf(uploadFile.size()));

			if (uploadFile.size() > 0) {
				for (int i = 0; i < uploadFile.size(); i++) {
					if (mUploadFlag) {
						try {
							final File file = new File(uploadFile.get(i));
							final String index = String.valueOf(i + 1);

							Log.e("Upload file data", "==== > " + index + " / "
									+ String.valueOf(uploadFile.size())
									+ " Name : " + file.getName());

							FileInputStream fis = null;
							String uploadResult = null;
							try {
								String fileName = file.getName();
								fis = new FileInputStream(file);

								uploadResult = uploadFile(fileLocation, "",
										fis, fileName, BaseActivity.httpClient);

								Log.i("Upload File==>", "Loc :" + fileLocation
										+ "File :" + file.getName());
							} catch (Exception e) {
								// TODO Auto-generated catch
								// block
								Log.i("Upload Exception :", e.toString());
								e.printStackTrace();
							}

							if (uploadResult == null
									|| uploadResult.equals("FileExist")) {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated
										// method stub
										Toast.makeText(
												FileExplorer.this,
												"File with that name already exists on the server.",
												1).show();
									}
								});
							} else if (uploadResult.equals("NetworkError")) {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method
										// stub
										WebNetworkAlert();
									}
								});
							} else if (uploadResult.equals("false")) {

							} else {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated
										// method stub
										Toast.makeText(
												FileExplorer.this,
												index
														+ " / "
														+ String.valueOf(uploadFile
																.size())
														+ " File uploaded successfully",
												2).show();
										Log.i("File Upload Msg",
												index
														+ " / "
														+ String.valueOf(uploadFile
																.size())
														+ " File uploaded successfully");
									}
								});
							}

							if (i == uploadFile.size() - 1 && mUploadFlag) {
								FileExplorer.this.startActivity(new Intent(
										FileExplorer.this,
										DashBoardActivity.class).putExtra(
										"From", "FileExplorer").putExtra(
										"newdir", "newDir"));
								finish();
							}

						} catch (final Exception e) {
							// TODO Auto-generated catch
							// block
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									Log.i("Invalid file format", e.toString());
									e.printStackTrace();
									Toast.makeText(
											FileExplorer.this,
											"File already exists on server or Invalid file format.",
											Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				}
			} else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(FileExplorer.this,
								"Please select file first", 1).show();
					}
				});
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mLoadingLayout.setVisibility(View.GONE);
			mUpload.setText("Upload");
			mUpload.setEnabled(true);
			mCancel.setEnabled(true);
			mOwnCloud.setEnabled(true);
			mUploadFlag = true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			startActivity(new Intent(getApplicationContext(),
					DashBoardActivity.class).putExtra("From", "FileExplorer")
					.putExtra("newdir", "newDir"));
			finish();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// mProgress.setVisibility(ProgressBar.VISIBLE);
			if (msg.obj.toString().contentEquals("mCancel")) {

				startActivity(new Intent(getApplicationContext(),
						DashBoardActivity.class).putExtra("From",
						"FileExplorer").putExtra("newdir", "newDir"));
				finish();
				removeDialog(0);
			} else if (msg.obj.toString().contentEquals("mOwnCloud")) {

				startActivity(new Intent(getApplicationContext(),
						ChooseUploadLocation.class).putExtra("Url", mainUrl));
				// finish();
				removeDialog(0);
			}

			// mProgress.setVisibility(ProgressBar.GONE);
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v == mCancel) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mCancel";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mOwnCloud) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					// Message toMain = handler.obtainMessage();
					// toMain.obj = "mOwnCloud";
					// handler.sendMessage(toMain);
					startActivity(new Intent(getApplicationContext(),
							ChooseUploadLocation.class)
							.putExtra("Url", mainUrl));
					// finish();
					removeDialog(0);
				}
			};
			t.start();
		} else if (v == mAbort) {
			mUploadFlag = false;
			mUpload.setText("Upload");
			mUpload.setEnabled(true);
			mCancel.setEnabled(true);
			mOwnCloud.setEnabled(true);
			mLoadingLayout.setVisibility(View.GONE);

		}
	}

}