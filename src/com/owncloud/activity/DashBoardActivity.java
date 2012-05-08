package com.owncloud.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.webdav.MultiStatus;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.owncloud.R;
import com.owncloud.login.LoginSelectionActivity;
import com.owncloud.upload.ChooseDownloadLocation;
import com.owncloud.upload.FileExplorer;
import com.owncloud.upload.UploadImageActivity;
import com.sufalam.WebdavMethodImpl;
import com.sufalam.actionbar.ActionItem;
import com.sufalam.actionbar.NewQAAdapter;
import com.sufalam.actionbar.QuickAction;

public class DashBoardActivity extends WebdavMethodImpl implements
		OnClickListener {

	private int mSelectedRow = 0;
	private ImageView mMoreIv = null;

	ImageButton mButtonFile;
	ImageButton mButtonUpload;
	ImageButton mButtonSetting;
	ImageButton mButtonUploadPhoto;
	ImageButton mButtonUploadOther;
	ImageButton mButtonUnlink;

	LinearLayout mLayoutUpload;
	LinearLayout mLayoutSetting;

	LinearLayout mLayoutSettHelp;
	LinearLayout mLayoutSettPass;
	LinearLayout mLayoutSettRecommend;
	LinearLayout mLayoutSettFeedback;

	Button mListBack;
	TextView mNoText;
	TextView mLoginURL;
	TextView mCurrentDir;
	
	TextView mPassOnOff;

	String mLastUrl = "";
	String mFileUrl = "";

	private static final int ID_DOWNLOAD = 1;
	private static final int ID_DELETE = 2;
	// private static final int ID_UPLOAD = 3;

	ListView mListView;

	String[] listUrl;
	NewQAAdapter adapter;

	String[] listName;

	List<String> mListName = new ArrayList<String>();
	int id;

	public static String fileName = "";

	static int NOTFICATION_ID = 198990;
	static NotificationManager notificationManager = null;
	static Notification notification;
	private static int progress = 10;
	String titleText;
	String notiText;
	boolean isDownload = false;
//	ProgressBar mProgress;
	LinearLayout mProgress;

	boolean mTextFlag = false;
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
		setContentView(R.layout.dashboard);
		Log.i("Dashboard onCreate","===> OnCreate");
		
		mListView = (ListView) findViewById(R.id.listview);
		mListBack = (Button) findViewById(R.id.backList);
		mProgress = (LinearLayout) findViewById(R.id.lodingLayout);
		
		mCurrentDir = (TextView)findViewById(R.id.currentDir);
		mPassOnOff = (TextView) findViewById(R.id.passOnOff);
		mNoText = (TextView)findViewById(R.id.noFile);
		mLoginURL = (TextView)findViewById(R.id.loginURL);
		mButtonFile = (ImageButton) findViewById(R.id.dash_file);
		mButtonUpload = (ImageButton) findViewById(R.id.dash_upload);
		mButtonSetting = (ImageButton) findViewById(R.id.dash_setting);
		mButtonUploadPhoto = (ImageButton) findViewById(R.id.dash_photo_video);
		mButtonUploadOther = (ImageButton) findViewById(R.id.dash_other_file);
		mButtonUnlink = (ImageButton) findViewById(R.id.dash_unlink);

		mLayoutSetting = (LinearLayout) findViewById(R.id.layout_setting);
		mLayoutUpload = (LinearLayout) findViewById(R.id.layout_upload);
		mLayoutSettHelp = (LinearLayout) findViewById(R.id.dash_sett_help);
		mLayoutSettPass = (LinearLayout) findViewById(R.id.dash_sett_pass);
		mLayoutSettFeedback = (LinearLayout) findViewById(R.id.dash_sett_feedback);
		mLayoutSettRecommend = (LinearLayout) findViewById(R.id.dash_sett_recomm);

		mButtonFile.setOnClickListener(this);
		mButtonUpload.setOnClickListener(this);
		mButtonSetting.setOnClickListener(this);
		mButtonUploadOther.setOnClickListener(this);
		mButtonUploadPhoto.setOnClickListener(this);
		mButtonUnlink.setOnClickListener(this);
		mListBack.setOnClickListener(this);

		// mLayoutSetting.setOnClickListener(this);
		// mLayoutUpload.setOnClickListener(this);
		mLayoutSettFeedback.setOnClickListener(this);
		mLayoutSettHelp.setOnClickListener(this);
		mLayoutSettPass.setOnClickListener(this);
		mLayoutSettRecommend.setOnClickListener(this);

		if (getIntent().getExtras().getString("From").equals("PassCodeOn")) {
			mNoText.setVisibility(View.GONE);
			mCurrentDir.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			mLayoutSetting.setVisibility(View.VISIBLE);
			mLayoutUpload.setVisibility(View.GONE);
			mButtonFile.setImageResource(R.drawable.mn_files);
			mButtonUpload.setImageResource(R.drawable.mn_upload);
			mButtonSetting.setImageResource(R.drawable.mn_setting_active);
			mListBack.setVisibility(View.GONE);
		} else if (getIntent().getExtras().getString("From")
				.equals("FileExplorer")) {
			mNoText.setVisibility(View.GONE);
			mCurrentDir.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			mLayoutSetting.setVisibility(View.GONE);
			mLayoutUpload.setVisibility(View.VISIBLE);
			mButtonFile.setImageResource(R.drawable.mn_files);
			mButtonUpload.setImageResource(R.drawable.mn_upload_active);
			mButtonSetting.setImageResource(R.drawable.mn_setting);
			mListBack.setVisibility(View.GONE);
		}

		try {
			if (pref.getString(PREF_PASSCODE, null).equals("true")
					&& pref.getString(PREF_PASSCODE, null) != null) {
				mPassOnOff.setText("turn off");
			} else {
				mPassOnOff.setText("turn on");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			mPassOnOff.setText("turn on");
			e.printStackTrace();
		}
		try {
			mLoginURL.setText(pref.getString(PREF_SERVERURL, ""));
			String test = getIntent().getExtras().getString("newdir")
					.toString();
			if (url.equals(mainUrl)) {
				mListBack.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			mainUrl = url;
		}

		adapter = new NewQAAdapter(this);
		try {
			// ListAllFiles();
			mListView.setEnabled(false);
			mProgress.setVisibility(View.VISIBLE);
			new ListAllFiles().execute();
//			showDialog(0);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		

		ActionItem downloadItem = new ActionItem(ID_DOWNLOAD, "Download",
				getResources().getDrawable(R.drawable.ic_add));
		ActionItem deleteItem = new ActionItem(ID_DELETE, "Delete",
				getResources().getDrawable(R.drawable.ic_accept));
		// ActionItem showItem = new ActionItem(3, "Show", getResources()
		// .getDrawable(R.drawable.ic_add));

		final QuickAction mQuickAction = new QuickAction(this);

		mQuickAction.addActionItem(downloadItem);
		mQuickAction.addActionItem(deleteItem);
		// mQuickAction.addActionItem(showItem);

		mQuickAction
				.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
					@Override
					public void onItemClick(QuickAction quickAction, int pos,
							int actionId) {
						ActionItem actionItem = quickAction.getActionItem(pos);

						if (actionId == ID_DOWNLOAD) { // Add item selected

							fileName = new StringBuffer(mFileUrl).reverse()
									.toString();
							String[] name = fileName.split("/");
							fileName = new StringBuffer(name[0]).reverse()
									.toString();

							isDownload = true;

						} else if (actionId == ID_DELETE) {
							showDialog(0);
							t = new Thread() {
								public void run() {
									try {
										String deleteFile = deleteFileOrFolder(
												mFileUrl, "", "", httpClient);
										if (deleteFile == null) {
											Toast.makeText(
													getApplicationContext(),
													"File deleted Successfully.",
													1).show();
										} else {
											Toast.makeText(
													getApplicationContext(),
													"There is some problem while deleting file.",
													1).show();
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										Log.i("Delete File Exception ",
												e.toString());
									}
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											mListView.setEnabled(false);
											new ListAllFiles().execute();
											
										}
									});
									
								}
							};
							t.start();
						}
					}
				});

		// setup on dismiss listener, set the icon back to normal
		mQuickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				// mMoreIv.setImageResource(R.drawable.ic_list_more);
				if (isDownload)
//					notificationDownload();
					new AlertDialog.Builder(DashBoardActivity.this)
							.setTitle("Download File")
							.setMessage("Default downloading to : /ownCloud")
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface arg0, int arg1) {
											mDownloadDest=Environment.getExternalStorageDirectory() + "/ownCloud";
											notificationDownload();
										}
									})
							.setNegativeButton("Select",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface arg0, int arg1) {
											startActivity(new Intent(getApplicationContext(),ChooseDownloadLocation.class));
//											finish();
										}
									}).show();


			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (isOnline()) {
					mSelectedRow = position; // set the selected row
					if (!((listUrl[position].substring(listUrl[position]
							.length() - 1)).equals("/"))) {

						Log.d("new url", baseUrl);

						mFileUrl = baseUrl + listUrl[position];
						mQuickAction.show(view);

					} else {
						url = baseUrl
								+ listUrl[position].substring(0,
										listUrl[position].length() - 1);

						Log.d("full Url", url);

						mListView.setEnabled(false);
						mProgress.setVisibility(View.VISIBLE);
						new ListAllFiles().execute();
					
					}
					if (url.equals(mainUrl)) {
						mListBack.setVisibility(View.GONE);
					} else {
						// mListBack.setVisibility(View.VISIBLE);
					}
				} else {
					WebNetworkAlert();
				}
			}
		});

	}

	class ListAllFiles extends AsyncTask<Object, Object, Object> {

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mListView.setEnabled(true);
			mProgress.setVisibility(View.GONE);
			removeDialog(0);
			Log.i("DashBord", "doInBack============End");
		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub

			Log.i("DashBord", "doInBack============Start");

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					if (isOnline()) {
						String textList = null;
						MultiStatus ms = null;
						try {
							ms = listAll(url, httpClient);
							String[] name = url.split("webdav.php/");
							mCurrentDir.setText(" ownCloud/"+URLDecoder.decode(name[1]));
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
							listUrl = new String[ms.getResponses().length - 1];
							listName = new String[ms.getResponses().length - 1];
							Log.e("length",
									String.valueOf(ms.getResponses().length));
							if (ms.getResponses().length == 1) {
								mTextFlag=true;
								Toast.makeText(
										getApplicationContext(),
										"No files in this direcory. Please press back button.",
										1).show();
								mNoText.setVisibility(View.VISIBLE);
							} else {
								mTextFlag=false;
								mNoText.setVisibility(View.GONE);
								for (int i = 1; i <= ms.getResponses().length - 1; i++) {
									String respString = ms.getResponses()[i]
											.getHref();
									listUrl[i - 1] = ms.getResponses()[i]
											.getHref();

									File file = new File(URLDecoder.decode(ms
											.getResponses()[i].getHref()));
									mListFile.add(file.getName());
									Log.i("ListFile", String.valueOf(i) + "  "
											+ file.getName());

									String fileName = new StringBuffer(
											listUrl[i - 1]).reverse()
											.toString();
									String[] fileNameArray = fileName
											.split("/");
									if ((fileName.substring(0, 1)).equals("/")) {
										// fileName = fileName.substring(1);
										listName[i - 1] = new StringBuffer(
												fileNameArray[1] + "/")
												.reverse().toString();

									} else {

										listName[i - 1] = new StringBuffer(
												fileNameArray[0]).reverse()
												.toString();
									}
									if (textList == null) {
										textList = listName[i - 1] + "\n";
									} else {
										textList = textList + listName[i - 1]
												+ "\n";
									}

									Log.d("response", respString);
								}
							}

							adapter.setData(listName);
							mListView.setAdapter(adapter);
						}

					} else {
						WebNetworkAlert();
					}
				}
			});
			return null;
		}

	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			Toast.makeText(this, "Search", Toast.LENGTH_LONG).show();
			break;
		case R.id.newFolder:

			if (!(url.contains("/Shared/") || url.endsWith("/Shared"))) {

				Intent intent = new Intent(getApplicationContext(),
						CreateNewDirectory.class).putExtra("From", "DashBoard");
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				startActivity(intent);
			} else {
				new AlertDialog.Builder(DashBoardActivity.this)
						.setMessage(
								"Sorry, You don't have permission to create folder in Shared directory.")
						.setTitle("ownCloud Alert")
						.setPositiveButton("OK", null).show();
			}
			break;
		case R.id.setting:
			Toast.makeText(this, "Setting", Toast.LENGTH_LONG).show();
			break;
		case R.id.refresh:

			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "refresh";
					handler.sendMessage(toMain);
				}
			};
			t.start();

			break;
		case R.id.upload:

			startActivity(new Intent(getApplicationContext(),
					UploadImageActivity.class));
			break;
		case R.id.finish:
			finish();
		}
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {

		case 0:

			if (resultCode == RESULT_OK) {
				Uri selectedImage = imageReturnedIntent.getData();
				// newImg.setImageURI(selectedImage);

				FileInputStream fis = null;
				String fileName = null;
				String uploadResult = null;
				try {
					fis = new FileInputStream(getRealPathFromURI(selectedImage));
					fileName = "test.jpg";

					uploadResult = uploadFile(url, "", fis, fileName,
							httpClient);

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (uploadResult == null || uploadResult.equals("FileExist")) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated
							// method stub
							Toast.makeText(
									DashBoardActivity.this,
									"File with that name already exists on the server.",
									1).show();
						}
					});
				} else if (uploadResult.equals("NetworkError")) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							WebNetworkAlert();
						}
					});
				} else {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated
							// method stub
							Toast.makeText(DashBoardActivity.this,
									"File uploaded successfully", 1).show();
							Log.i("File Upload Msg",
									"File uploaded successfully");
						}
					});
				}

				// ListAllFiles();
				mListView.setEnabled(false);
				new ListAllFiles().execute();
				mProgress.setVisibility(View.VISIBLE);
//				showDialog(0);

			}

		}

	}

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public void notificationDownload() {

		isDownload = false;
		notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
		String MyText = "Downloading";
		titleText = URLDecoder.decode(fileName);
		notiText = "Downloading...";

		Log.d("Download file name ",fileName + " <=======");
		notification = new Notification(R.drawable.icon, MyText,
				System.currentTimeMillis());

		Intent MyIntent = new Intent(Intent.ACTION_VIEW);

		MyIntent.putExtra("extendedTitle", titleText);

		PendingIntent StartIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, MyIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), titleText,
				notiText, StartIntent);

		notificationManager.notify(NOTFICATION_ID, notification);

		Intent i = new Intent(getApplicationContext(), Downloader.class);
		i.setData(Uri.parse(mFileUrl));
		i.putExtra(Downloader.EXTRA_MESSENGER, new Messenger(notiHandler));

		startService(i);
	}

	public static void updateNotation() {
		progress++;
		notification.contentView.setProgressBar(R.id.status_progress, 100,
				progress, false);

		// inform the progress bar of updates in progress
		notificationManager.notify(NOTFICATION_ID, notification);
	}

	private Handler notiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			notificationManager.cancel(NOTFICATION_ID);

			notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
			String myText = "Downloaded";
			titleText = URLDecoder.decode(fileName);
			notiText = "Downloaded";
			notification = new Notification(R.drawable.icon, myText,
					System.currentTimeMillis());

			Intent myIntent = new Intent(Intent.ACTION_VIEW);

			myIntent.putExtra("extendedTitle", titleText);
			
			myIntent.setData(Uri.parse(mDownloadDest));

			PendingIntent startIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, myIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), titleText,
					notiText, startIntent);

			notificationManager.notify(NOTFICATION_ID, notification);
			File file = new File(mDownloadDest,fileName);
			fileName="";
			
			openFile(file);
			
			Log.i("DashBoard Download Done","FileName : "+fileName);
			
		}
	};

		private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			try {
				if (!mLastUrl.equals("") && mLastUrl.equals(url)
						|| url.equals(mainUrl)) {
					mListBack.setVisibility(View.GONE);
				} else {
					// mListBack.setVisibility(View.VISIBLE);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (msg.obj.toString().contentEquals("refresh")) {
				// ListAllFiles();
				mListView.setEnabled(false);
				new ListAllFiles().execute();
				mProgress.setVisibility(View.VISIBLE);
//				showDialog(0);
				

			} else if (msg.obj.toString().contentEquals("mLayoutUpload")) {

			} else if (msg.obj.toString().contentEquals("mButtonFile")) {
				if (mTextFlag) {
					mNoText.setVisibility(View.VISIBLE);
				}
				mCurrentDir.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.VISIBLE);
				mLayoutSetting.setVisibility(View.GONE);
				mLayoutUpload.setVisibility(View.INVISIBLE);
				mButtonFile.setImageResource(R.drawable.mn_files_active);
				mButtonUpload.setImageResource(R.drawable.mn_upload);
				mButtonSetting.setImageResource(R.drawable.mn_setting);
			} else if (msg.obj.toString().contentEquals("mButtonUpload")) {
				if (mTextFlag) {
					mNoText.setVisibility(View.GONE);
				}
				mCurrentDir.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
				mLayoutSetting.setVisibility(View.GONE);
				mLayoutUpload.setVisibility(View.VISIBLE);
				mButtonFile.setImageResource(R.drawable.mn_files);
				mButtonUpload.setImageResource(R.drawable.mn_upload_active);
				mButtonSetting.setImageResource(R.drawable.mn_setting);
				mListBack.setVisibility(View.GONE);
			} else if (msg.obj.toString().contentEquals("mButtonSetting")) {
				if (mTextFlag) {
					mNoText.setVisibility(View.GONE);
				}
				mCurrentDir.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
				mLayoutSetting.setVisibility(View.VISIBLE);
				mLayoutUpload.setVisibility(View.GONE);
				mButtonFile.setImageResource(R.drawable.mn_files);
				mButtonUpload.setImageResource(R.drawable.mn_upload);
				mButtonSetting.setImageResource(R.drawable.mn_setting_active);
				mListBack.setVisibility(View.GONE);
			} else if (msg.obj.toString().contentEquals("mButtonUploadPhoto")) {
				startActivity(new Intent(getApplicationContext(),
						UploadImageActivity.class).putExtra("fileLocation", ""));
				finish();

			} else if (msg.obj.toString().contentEquals("mButtonUploadOther")) {
				startActivity(new Intent(getApplicationContext(),
						FileExplorer.class).putExtra("fileLocation", ""));
				finish();

			} else if (msg.obj.toString().contentEquals("LayoutsettFeedback")) {
				if (isOnline()) {
					startActivity(new Intent(Intent.ACTION_VIEW,
							Uri.parse("http://owncloud.com/mobile/feedback")));
				} else {
					WebNetworkAlert();
				}

			} else if (msg.obj.toString().contentEquals("LayoutSettHelp")) {
				// Toast.makeText(getApplicationContext(), "LayoutSettHelp", 1)
				// .show();

				if (isOnline()) {
					startActivity(new Intent(Intent.ACTION_VIEW,
							Uri.parse("http://owncloud.com/mobile/help")));
				} else {
					WebNetworkAlert();
				}

			} else if (msg.obj.toString().contentEquals("LayoutSettPass")) {

				try {
					if (pref.getString(PREF_PASSCODE, null).equals("true")) {

						new AlertDialog.Builder(DashBoardActivity.this)
								.setTitle("ownCloud Alert")
								.setMessage(
										"Are you sure you want to remove Passcode Lock?")
								.setPositiveButton("Ok",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface arg0,
													int arg1) {

												startActivity(new Intent(getApplicationContext(),
														PassCodeActivity.class).putExtra("From",
														"passOff"));
												finish();
											/*	getSharedPreferences(
														PREFS_NAME,
														MODE_PRIVATE)
														.edit()
														.putString(
																PREF_PASSCODE,
																"false")
														.putString(
																PREF_PASS_TXT_FIRST,
																"")
														.putString(
																PREF_PASS_TXT_SECOND,
																"")
														.putString(
																PREF_PASS_TXT_THIRD,
																"")
														.putString(
																PREF_PASS_TXT_THIRD,
																"").commit();
												mPassOnOff.setText("turn on");*/
											}
										})
								.setNegativeButton("Not now",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												// do stuff onclick of CANCEL
												return;
											}
										}).show();

					} else {

						startActivity(new Intent(getApplicationContext(),
								PassCodeActivity.class).putExtra("From",
								"dashboard"));
						finish();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("DashBoard: PassCode", "");
					startActivity(new Intent(getApplicationContext(),
							PassCodeActivity.class).putExtra("From",
							"dashboard"));
					finish();
				}
			} else if (msg.obj.toString().contentEquals("LayoutSettRecommend")) {

				// mailToFriend("hello Friend");

				if (isOnline()) {
					startActivity(new Intent(Intent.ACTION_VIEW,
							Uri.parse("http://owncloud.com/mobile/recommend")));
				} else {
					WebNetworkAlert();
				}

			} else if (msg.obj.toString().contentEquals("mButtonUnlink")) {

				new AlertDialog.Builder(DashBoardActivity.this)
						.setTitle("ownCloud Alert")
						.setMessage("Unlink android from ownCloud.")
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface arg0,
											int arg1) {

//										getSharedPreferences(PREFS_NAME,
//												MODE_PRIVATE)
//												.edit()
//												.putString(PREF_USERNAME, null)
//												.putString(PREF_PASSWORD, null)
//												.putString(PREF_SERVERURL, null)
//												.putString(PREF_BASEURL, null)
//												.commit();
										getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_UNLINK, "true").commit();

										startActivity(new Intent(
												getApplicationContext(),
												LoginSelectionActivity.class));
										finish();

									}
								})
						.setNegativeButton("Not now",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface arg0,
											int arg1) {
										// do stuff onclick of CANCEL
										return;
									}
								}).show();

			} else if (msg.obj.toString().contentEquals("mListBack")) {
				// url = mLastUrl;

				String fileName = new StringBuffer(url).reverse().toString();
				if ((fileName.substring(0, 1)).equals("/")) {
					fileName = fileName.substring(1);
				}
				fileName = new StringBuffer(fileName).reverse().toString();
				String[] fileNameArray = fileName.split("/");
				for (int i = 0; i < fileNameArray.length - 1; i++) {
					if (i == 0) {
						url = fileNameArray[i];
					} else {
						url = url + "/" + fileNameArray[i];
					}
				}
				if (url.equals(mainUrl)) {
					mListBack.setVisibility(View.GONE);
				}
				// Toast.makeText(getApplicationContext(), url, 1).show();
				Log.d("url", url);
				// ListAllFiles();
				mListView.setEnabled(false);
				new ListAllFiles().execute();
				mProgress.setVisibility(View.VISIBLE);
//				showDialog(0);
			}

			removeDialog(0);
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if (v == mLayoutSetting) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mLayoutSetting";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mLayoutUpload) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mLayoutUpload";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mButtonFile) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mButtonFile";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mButtonUpload) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mButtonUpload";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mButtonSetting) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mButtonSetting";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mButtonUploadPhoto) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mButtonUploadPhoto";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mButtonUploadOther) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mButtonUploadOther";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mLayoutSettFeedback) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "LayoutsettFeedback";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mLayoutSettHelp) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "LayoutSettHelp";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mLayoutSettPass) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "LayoutSettPass";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mLayoutSettRecommend) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "LayoutSettRecommend";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mButtonUnlink) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mButtonUnlink";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mListBack) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mListBack";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (mainUrl == null) {
				mainUrl = url;
			}
			try {
				if (!(url.equals(mainUrl))) {

					// url = mLastUrl;

					String fileName = new StringBuffer(url).reverse()
							.toString();
					if ((fileName.substring(0, 1)).equals("/")) {
						fileName = fileName.substring(1);
					}
					fileName = new StringBuffer(fileName).reverse().toString();
					String[] fileNameArray = fileName.split("/");
					for (int i = 0; i < fileNameArray.length - 1; i++) {
						if (i == 0) {
							url = fileNameArray[i];
						} else {
							url = url + "/" + fileNameArray[i];
						}
					}

					try {
						if (url.equals(mainUrl)) {
							mListBack.setVisibility(View.GONE);
						}
					} catch (Exception e) {
						mainUrl = url;
					}
					// Toast.makeText(getApplicationContext(), url, 1).show();
					Log.d("url", url);
					// ListAllFiles();
					mListView.setEnabled(false);
					new ListAllFiles().execute();
					mProgress.setVisibility(View.VISIBLE);
//					showDialog(0);

				} else {

          finish();
          moveTaskToBack(true);

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.i("Dashboard onRestart ","Download Destination : "+mDownloadDest + " File Name : " + fileName);
		if(!fileName.equals(""))
			notificationDownload();
	}

	public void openFile(File file){
		try {
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			
			Log.i("Downloaded file ", "===========> " + file.getName());
			String extension = android.webkit.MimeTypeMap
					.getFileExtensionFromUrl(Uri.fromFile(file).toString());
			String mimetype = android.webkit.MimeTypeMap.getSingleton()
					.getMimeTypeFromExtension(extension);
			intent.setDataAndType(Uri.fromFile(file), mimetype);
			startActivity(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

}

