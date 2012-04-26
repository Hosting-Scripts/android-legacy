package com.owncloud.upload;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.owncloud.R;
import com.owncloud.activity.DashBoardActivity;
import com.owncloud.common.BaseActivity;
import com.sufalam.WebdavMethodImpl;

public class UploadImageActivity extends WebdavMethodImpl implements
		OnClickListener {

	/**
	 * Grid view holding the images.
	 */
	public GridView sdcardImages;
	public GridView videoSdcardImage;

	Button mImageBtn;
	Button mVideoBtn;
	/**
	 * Image adapter for the grid view.
	 */
	private ImageAdapter imageAdapter;
	private VideoAdapter videoAdapter;
	boolean imageFlag = true;

	/**
	 * Display used for getting the width of the screen.
	 */
	private Display display;

	Button mCancel;
	Button mUpload;
	// Button mOwnCloud;
	Cursor Vcursor;

	int counter = 0;

	int counterVideo = 0;
	int size;
	int min;
	int mImgCounter;

	int minVideo;
	int mVideoCounter;

	Bitmap bitmap = null;
	Bitmap newBitmap = null;
	Uri uri = null;
	Uri videoUri = null;

	Cursor cursor;
	int columnIndex;
	int videoColumnIndex;
	int videoSize;

	// Button btnNext;

	GestureDetector gd;

	// ProgressBar mProgress;
	List<String> uploadFile = new ArrayList<String>();
	List<Uri> listUri = new ArrayList<Uri>();
	List<String> videoUploadFile = new ArrayList<String>();
	List<Uri> videoListUri = new ArrayList<Uri>();

	Button mAbort;
	LinearLayout mMainLayout;
	LinearLayout mLoadingLayout;

	boolean startFlag = true;
	boolean startFlagV = true;

	/**
	 * Creates the content view, sets up the grid, the adapter, and the click
	 * listener.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Request progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.uploadimage);
		setTitle("SD Card Images");
		display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();

		mCancel = (Button) findViewById(R.id.cancel);
		mUpload = (Button) findViewById(R.id.upload);
		mOwnCloud = (Button) findViewById(R.id.BtnOwnCloud);
		mImageBtn = (Button) findViewById(R.id.imageBtn);
		mVideoBtn = (Button) findViewById(R.id.videoBtn);
		mAbort = (Button) findViewById(R.id.abort);

		mMainLayout = (LinearLayout) findViewById(R.id.mailLayout);
		mLoadingLayout = (LinearLayout) findViewById(R.id.lodingLayout);

		mMainLayout.setEnabled(false);

		mAbort.setOnClickListener(this);
		mImageBtn.setOnClickListener(this);
		mVideoBtn.setOnClickListener(this);

		// btnNext = (Button) findViewById(R.id.btnNext);

		// enableDisableView(mMainLayout, false);

		SpannableString content = new SpannableString("Load more images");
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		// btnNext.setText(content);

		gd = new GestureDetector(getApplicationContext(), sogl);

		min = 21;
		mImgCounter = 0;

		minVideo = 21;
		mVideoCounter = 0;
		mUploadFlag = true;

		mCancel.setOnClickListener(this);
		// mUpload.setOnClickListener(this);
		mUpload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLoadingLayout.setVisibility(View.VISIBLE);
				// mUpload.setText("Uploading...");
				// mUpload.setEnabled(false);
				// mCancel.setEnabled(false);
				// mOwnCloud.setEnabled(false);
				new UploadingData().execute();
			}
		});
		mOwnCloud.setOnClickListener(this);
		if (!getIntent().getExtras().getString("fileLocation").equals("")) {
			fileLocation = getIntent().getExtras().getString("fileLocation");
		} else {
			fileLocation = BaseActivity.url;
		}
		setupViews();
		setProgressBarIndeterminateVisibility(true);
		showDialog(0);
		loadImages();
		loadVideos();

	}

	GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener() {

		public boolean onDown(MotionEvent event) {

			return true;
		}

		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {

			if (event1.getRawY() > event2.getRawY()
					&& StrictMath.abs(event1.getRawX() - event2.getRawX()) < 100) {

				try {
					if (imageFlag && mImgCounter < size) {
						// min = min + 15;
						// if (min > size) {
						// min = size;
						// }
						setProgressBarIndeterminateVisibility(true);
						showDialog(0);
						new LoadImagesFromSDCard().execute();
					} else if (mVideoCounter < videoSize) {

						// if (minVideo > videoSize) {
						// minVideo = videoSize;
						// }
						setProgressBarIndeterminateVisibility(true);
						showDialog(0);
						new LoadVideoFromSDCard().execute();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return true;
		}
	};

	class UploadingData extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub

			// if (sdcardImages.getVisibility() != View.VISIBLE) {
			//
			// uploadFile = videoUploadFile;
			// }
			//

			System.gc();
			trimCache(getApplicationContext());
			try {
				for (int i = 0; i < videoUploadFile.size(); i++) {
					uploadFile.add(videoUploadFile.get(i));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Log.i("Total Files : ", String.valueOf(uploadFile.size()));

			if (uploadFile.size() > 0) {
				for (int i = 0; i < uploadFile.size(); i++) {
					if (mUploadFlag) {
						final String index = String.valueOf(i + 1);
						try {
							final File file = new File(uploadFile.get(i));

							Log.e("Upload file data", "==== > " + index + " / "
									+ String.valueOf(uploadFile.size())
									+ " Name : " + file.getName());

							FileInputStream fis = null;
							String uploadResult = null;
							try {
								String fileName = file.getName();
								fis = new FileInputStream(file);

								try {
									uploadResult = uploadFile(fileLocation, "",
											fis, fileName,
											BaseActivity.httpClient);
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
									Toast.makeText(getApplicationContext(),
											"Please try again", 1).show();
								}

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
												UploadImageActivity.this,
												"File with that name already exists on the server.",
												1).show();
									}
								});
							} else if (uploadResult.equals("NetworkError")) {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated
										// method stub
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
												UploadImageActivity.this,
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

								System.gc();
								trimCache(getApplicationContext());

								UploadImageActivity.this
										.startActivity(new Intent(
												UploadImageActivity.this,
												DashBoardActivity.class)
												.putExtra("From",
														"FileExplorer")
												.putExtra("newdir", "newDir"));
								finish();
							}

						} catch (Exception e) {
							// TODO Auto-generated catch
							// block
							Log.i("Invalid file format", e.toString());
							e.printStackTrace();
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method
									// stub
									Toast.makeText(UploadImageActivity.this,
											"Invalid file format",
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
						// TODO Auto-generated method stub

						Toast.makeText(UploadImageActivity.this,
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
			mUploadFlag=true;
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		super.dispatchTouchEvent(e);
		return gd.onTouchEvent(e);
	}

	/**
	 * Free up bitmap related resources.
	 */
	protected void onDestroy() {
		super.onDestroy();

		/*
		 * GridView grid = sdcardImages; int count = grid.getChildCount();
		 * ImageView v = null; for (int i = 0; i < count; i++) { v = (ImageView)
		 * grid.getChildAt(i); ((BitmapDrawable)
		 * v.getDrawable()).setCallback(null); } grid = videoSdcardImage; count
		 * = grid.getChildCount(); ImageView v1 = null; for (int i = 0; i <
		 * count; i++) { v1 = (ImageView) grid.getChildAt(i); ((BitmapDrawable)
		 * v1.getDrawable()).setCallback(null); }
		 */
		System.gc();
		trimCache(getApplicationContext());

		// Vcursor.close();
	}

	/**
	 * Setup the grid view.
	 */
	private void setupViews() {
		sdcardImages = (GridView) findViewById(R.id.sdcard);
		videoSdcardImage = (GridView) findViewById(R.id.gridVideo);
		// sdcardImages.setNumColumns(display.getWidth()/95);

		sdcardImages.setNumColumns(3);
		sdcardImages.setClipToPadding(false);

		videoSdcardImage.setNumColumns(3);
		videoSdcardImage.setClipToPadding(false);
		// sdcardImages.setOnItemClickListener(UploadImageActivity.this);

		imageAdapter = new ImageAdapter(getApplicationContext());
		videoAdapter = new VideoAdapter(getApplicationContext());
		sdcardImages.setAdapter(imageAdapter);
		videoSdcardImage.setAdapter(videoAdapter);
	}

	/**
	 * Load images.
	 */

	private void loadImages() {
		final Object data = getLastNonConfigurationInstance();
		if (data == null) {
			new LoadImagesFromSDCard().execute();
		} else {
			final LoadedImage[] photos = (LoadedImage[]) data;
			if (photos.length == 0) {
				new LoadImagesFromSDCard().execute();
			}
			for (LoadedImage photo : photos) {
				addImage(photo);
			}
		}
	}

	private void loadVideos() {
		final Object data = getLastNonConfigurationInstance();
		if (data == null) {
			new LoadVideoFromSDCard().execute();
		} else {
			final LoadedImage[] photos = (LoadedImage[]) data;
			if (photos.length == 0) {
				new LoadVideoFromSDCard().execute();
			}
			for (LoadedImage photo : photos) {
				addVideo(photo);
			}
		}
	}

	/**
	 * Add image(s) to the grid view adapter.
	 * 
	 * @param value
	 *            Array of LoadedImages references
	 */
	private void addImage(LoadedImage... value) {
		for (LoadedImage image : value) {
			imageAdapter.addPhoto(image);
			imageAdapter.notifyDataSetChanged();
		}
	}

	private void addVideo(LoadedImage... value) {
		for (LoadedImage image : value) {
			videoAdapter.addPhoto(image);
			videoAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Save bitmap images into a list and return that list.
	 * 
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		GridView grid;
		if (imageFlag) {
			grid = sdcardImages;
		} else {
			grid = videoSdcardImage;
		}
		final int count = grid.getChildCount();
		final LoadedImage[] list = new LoadedImage[count];

		for (int i = 0; i < count; i++) {
			final ImageView v = (ImageView) grid.getChildAt(i);
			list[i] = new LoadedImage(
					((BitmapDrawable) v.getDrawable()).getBitmap());
		}

		return list;
	}

	/**
	 * Async task for loading the images from the SD card.
	 * 
	 * @author Mihai Fonoage
	 * 
	 */
	class LoadVideoFromSDCard extends AsyncTask<Object, LoadedImage, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub

			System.gc();
			trimCache(getApplicationContext());
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (startFlagV) {

						String[] vProjection = {
								MediaStore.Video.Thumbnails._ID,
								MediaStore.Video.Media.DATA };
						Vcursor = managedQuery(
								MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
								vProjection, null, null,
								MediaStore.Video.Thumbnails._ID + " DESC");
						Log.i("Video Image Count ",
								String.valueOf(Vcursor.getCount()));
						videoColumnIndex = Vcursor
								.getColumnIndexOrThrow(MediaStore.Video.Thumbnails._ID);

						videoSize = Vcursor.getCount();
						Log.i("No of Video in SD",
								"==========> " + String.valueOf(videoSize));
						startFlagV = false;

					}

					int videoImageId = 0;
					int tmpCounter = counterVideo;
					while (counterVideo != tmpCounter + 18
							&& mVideoCounter < videoSize) {

						Log.i("Total Video " + String.valueOf(videoSize),
								"List Counter : "
										+ String.valueOf(counterVideo)
										+ " mVideoCounter : "
										+ String.valueOf(mVideoCounter));

						if (mVideoCounter < videoSize) {
							Vcursor.moveToPosition(mVideoCounter);
							videoImageId = Vcursor.getInt(videoColumnIndex);
							videoUri = Uri
									.parse(Vcursor.getString(Vcursor
											.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));

							mVideoCounter++;

							try {
								bitmap = MediaStore.Video.Thumbnails
										.getThumbnail(
												getContentResolver(),
												videoImageId,
												MediaStore.Video.Thumbnails.MINI_KIND,
												null);
								if (bitmap == null) {
									bitmap = BitmapFactory.decodeResource(
											getResources(), R.drawable.videos);
								}

								newBitmap = Bitmap.createScaledBitmap(bitmap,
										70, 70, true);
								bitmap.recycle();
								if (newBitmap != null) {
									publishProgress(new LoadedImage(newBitmap));
									videoListUri.add(videoUri);
									Log.i("Upload Video", "[ " + counterVideo++
											+ " ] ==> Video Url : " + videoUri);
								}

							} catch (Exception e) {
								// Error fetching image, try to recover
								Log.i("VideoLoad Image ===>", e.toString());
							}

						}
					}
				}
			});
			return null;
		}

		@Override
		public void onProgressUpdate(LoadedImage... value) {
			addVideo(value);
		}

		/**
		 * Set the visibility of the progress bar to false.
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Object result) {
			setProgressBarIndeterminateVisibility(false);
			mCancel.setEnabled(true);
			mUpload.setEnabled(true);
			mOwnCloud.setEnabled(true);
			removeDialog(0);
		}

	}

	class LoadImagesFromSDCard extends AsyncTask<Object, LoadedImage, Object> {

		/**
		 * Load images from SD Card in the background, and display each image on
		 * the screen.
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Object doInBackground(Object... params) {
			// setProgressBarIndeterminateVisibility(true);

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mCancel.setEnabled(false);
					mUpload.setEnabled(false);
					mOwnCloud.setEnabled(false);
				}
			});
			try {

				if (startFlag) {
					System.gc();
					trimCache(getApplicationContext());

					String[] projection = { MediaStore.Images.Thumbnails._ID };

					cursor = managedQuery(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							projection, null, null,
							MediaStore.Images.Thumbnails._ID + " DESC");

					try {
						columnIndex = cursor
								.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
					} catch (Exception e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					size = cursor.getCount();
					Log.i("No of Images in SD",
							"==========> " + String.valueOf(size));

					startFlag = false;
				}
				int imageID = 0;
				int tmpCounter = counter;
				while (counter != tmpCounter + 18 && mImgCounter < size) {

					Log.i("Total Images " + String.valueOf(size),
							"List Counter : " + String.valueOf(counter)
									+ " mImgCounter : "
									+ String.valueOf(mImgCounter));
					cursor.moveToPosition(mImgCounter);
					try {
						imageID = cursor.getInt(columnIndex);
						Log.d("imageId", String.valueOf(imageID));

						uri = Uri.withAppendedPath(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"" + imageID);

						mImgCounter++;

						// listUri.add(uri);
						try {

							// bitmap=
							// MediaStore.Images.Media.getBitmap(getContentResolver(),
							// getRealPathFromURI(uri));
							// bitmap =
							// BitmapFactory.decodeFile(getRealPathFromURI(uri));

							try {

								bitmap = MediaStore.Images.Thumbnails
										.getThumbnail(
												getContentResolver(),
												imageID,
												MediaStore.Images.Thumbnails.MINI_KIND,
												null);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								Log.e("Create Bitmap ", e1.toString());
							}
							if (bitmap != null) {

								uri = Uri
										.withAppendedPath(
												MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
												"" + imageID);

								newBitmap = Bitmap.createScaledBitmap(bitmap,
										80, 80, true);

								bitmap.recycle();

								if (newBitmap != null) {

									try {
										publishProgress(new LoadedImage(
												newBitmap));
										listUri.add(uri);
										Log.i("Upload Images", "[ " + counter++
												+ " ] ==> Image Url : "
												+ getRealPathFromURI(uri));
									} catch (Exception e) {
										// TODO Auto-generated catch block
										Log.e("PublichProgress", e.toString());
										e.printStackTrace();
									}

								}
							}
						} catch (Exception e) {
							Log.e("Upload Image Exception", e.toString());
							e.printStackTrace();
						}

					} catch (Exception e) {
						Log.i("Load Image Prob ===>", e.toString());
						e.printStackTrace();
					}

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("doInBackground exception ", e.toString());

			}

			return null;
		}

		/**
		 * Add a new LoadedImage in the images grid.
		 * 
		 * @param value
		 *            The image.
		 */
		@Override
		public void onProgressUpdate(LoadedImage... value) {
			addImage(value);
		}

		/**
		 * Set the visibility of the progress bar to false.
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Object result) {
			setProgressBarIndeterminateVisibility(false);
			mCancel.setEnabled(true);
			mUpload.setEnabled(true);
			mOwnCloud.setEnabled(true);
			removeDialog(0);
		}

		public void loadImg() {
		}

		public void loadVisddeo() {
		}

	}

	/**
	 * Adapter for our image files.
	 * 
	 * @author Mihai Fonoage
	 * 
	 */
	class ImageAdapter extends BaseAdapter {

		private Context mContext;
		private ArrayList<LoadedImage> photos = new ArrayList<LoadedImage>();
		String selectedFilePath = "";
		// String pos1 = "";
		private ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();

		public ImageAdapter(Context context) {
			mContext = context;
		}

		public void addPhoto(LoadedImage photo) {
			photos.add(photo);
			itemChecked.add(false);
		}

		public int getCount() {
			return photos.size();
		}

		public Object getItem(int position) {
			return photos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ImageView imageView;
			// if (convertView == null) {
			imageView = new ImageView(mContext);
			// } else {
			// imageView = (ImageView) convertView;
			// }
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setPadding(1, 1, 1, 1);
			imageView.setBackgroundColor(Color.WHITE);
			imageView.setImageBitmap(photos.get(position).getBitmap());

			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						Log.i("Position", "===>" + String.valueOf(position));
						Log.i("ListUri", "====>" + listUri.get(position));
						selectedFilePath = getRealPathFromURI(listUri
								.get(position));

						// ImageView imageView = (ImageView) v;
						if (uploadFile.contains(selectedFilePath)) {
							uploadFile.remove(selectedFilePath);
							itemChecked.set(position, false);
							Log.i("Image Remove", "==========>"
									+ selectedFilePath);
							imageView.setAlpha(255);

						} else {
							uploadFile.add(selectedFilePath);
							itemChecked.set(position, true);
							Log.i("Image Add", "==========>" + selectedFilePath);
							imageView.setAlpha(100);

						}

					} catch (Exception e) {
						// Error fetching image, try to recover
						Log.e("View Image Exception===>", e.toString());
						itemChecked.clear();

					}
				}
			});

			if (itemChecked.get(position)) {
				imageView.setAlpha(100);
			}

			return imageView;
		}
	}

	class VideoAdapter extends BaseAdapter {

		private Context mContext;
		private ArrayList<LoadedImage> photos = new ArrayList<LoadedImage>();
		String selectedFilePath = "";
		private ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();

		public VideoAdapter(Context context) {
			mContext = context;
		}

		public void addPhoto(LoadedImage photo) {
			photos.add(photo);
			itemChecked.add(false);
		}

		public int getCount() {
			return photos.size();
		}

		public Object getItem(int position) {
			return photos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ImageView imageView;
			imageView = new ImageView(mContext);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setPadding(1, 1, 1, 1);
			imageView.setBackgroundColor(Color.WHITE);
			imageView.setImageBitmap(photos.get(position).getBitmap());

			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						Log.i("Video Position",
								"===>" + String.valueOf(position));
						Log.i("Video ListUri",
								"====>" + videoListUri.get(position));

						int video_column_index = Vcursor
								.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
						Vcursor.moveToPosition((position));
						selectedFilePath = Vcursor
								.getString(video_column_index);

						// ImageView imageView = (ImageView) v;
						if (videoUploadFile.contains(selectedFilePath)) {
							videoUploadFile.remove(selectedFilePath);
							itemChecked.set(position, false);
							Log.i("Video Remove", "==========>"
									+ selectedFilePath);
							imageView.setAlpha(255);

						} else {
							videoUploadFile.add(selectedFilePath);
							itemChecked.set(position, true);
							Log.i("Video Add", "==========>" + selectedFilePath);
							imageView.setAlpha(100);

						}

					} catch (Exception e) {
						// Error fetching image, try to recover
						Log.e("View Video Exception===>", e.toString());
						itemChecked.clear();

					}
				}
			});
			if (itemChecked.get(position)) {
				imageView.setAlpha(100);
			}

			return imageView;
		}
	}

	/**
	 * A LoadedImage contains the Bitmap loaded for the image.
	 */
	private static class LoadedImage {
		Bitmap mBitmap;

		LoadedImage(Bitmap bitmap) {
			mBitmap = bitmap;
		}

		public Bitmap getBitmap() {
			return mBitmap;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mCancel) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					// Message toMain = handler.obtainMessage();
					// toMain.obj = "mCancel";
					// handler.sendMessage(toMain);
					startActivity(new Intent(getApplicationContext(),
							DashBoardActivity.class).putExtra("From",
							"FileExplorer").putExtra("newdir", "newDir"));
					removeDialog(0);
					overridePendingTransition(android.R.anim.fade_in,
							android.R.anim.fade_out);
					finish();
				}
			};
			t.start();
		} else if (v == mOwnCloud) {

			// Message toMain = handler.obtainMessage();
			// toMain.obj = "mOwnCloud";
			// handler.sendMessage(toMain);
			startActivity(new Intent(getApplicationContext(),
					ChooseUploadLocation.class).putExtra("Url", mainUrl));
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
		} else if (v == mImageBtn) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mImageBtn";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mVideoBtn) {
			showDialog(0);
			t = new Thread() {
				public void run() {
					Message toMain = handler.obtainMessage();
					toMain.obj = "mVideoBtn";
					handler.sendMessage(toMain);
				}
			};
			t.start();
		} else if (v == mAbort ) {
			
			if(mUploadCnt){
				Toast.makeText(UploadImageActivity.this, "Downloading in progress, you can't abort it.", 1).show();
				mUploadFlag = false;
			}else{
			mUploadFlag = false;
			// mUpload.setText("Upload");
			// mUpload.setEnabled(true);
			// mCancel.setEnabled(true);
			// mOwnCloud.setEnabled(true);
			mLoadingLayout.setVisibility(View.GONE);
			}
		}

	}

	private void enableDisableView(View view, boolean enabled) {
		view.setEnabled(enabled);

		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;

			for (int idx = 0; idx < group.getChildCount(); idx++) {
				enableDisableView(group.getChildAt(idx), enabled);
			}
		}
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
			} else if (msg.obj.toString().contentEquals("mLoad")) {
				new LoadImagesFromSDCard().execute();
			} else if (msg.obj.toString().contentEquals("mOwnCloud")) {

				startActivity(new Intent(getApplicationContext(),
						ChooseUploadLocation.class).putExtra("Url", mainUrl));
				// finish();

			} else if (msg.obj.toString().contentEquals("mVideoBtn")) {
				imageFlag = false;
				setTitle("SD Card Videos");
				videoSdcardImage.setVisibility(View.VISIBLE);
				sdcardImages.setVisibility(View.GONE);
			} else if (msg.obj.toString().contentEquals("mImageBtn")) {
				imageFlag = true;
				setTitle("SD Card Images");
				videoSdcardImage.setVisibility(View.GONE);
				sdcardImages.setVisibility(View.VISIBLE);
			}
			removeDialog(0);
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (mCancel.isEnabled()) {
				showDialog(0);
				t = new Thread() {
					public void run() {
						Message toMain = handler.obtainMessage();
						toMain.obj = "mCancel";
						handler.sendMessage(toMain);
					}
				};
				t.start();
			} else {

			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}