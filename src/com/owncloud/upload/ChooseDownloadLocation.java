package com.owncloud.upload;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.owncloud.R;
import com.owncloud.common.BaseActivity;

public class ChooseDownloadLocation extends BaseActivity{

	DownloadLocationAdapter adapter;
	Button mCancel;
	Button mDownloadDestBtn;
	
	protected Thread t;
	ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileexplorer);
		
		mListView = (ListView) findViewById(R.id.list);
		mCancel = (Button) findViewById(R.id.cancel);
		mDownloadDestBtn = (Button) findViewById(R.id.upload);
		mOwnCloud = (Button) findViewById(R.id.BtnOwnCloud);
		mOwnCloud.setVisibility(View.GONE);
	
		mCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDownloadDest = Environment.getExternalStorageDirectory() + "/ownCloud";
//				startActivity(new Intent(getApplicationContext(),
//						DashBoardActivity.class).putExtra("From",
//						"DownloadLocation"));
				finish();
			}
		});
		mDownloadDestBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				startActivity(new Intent(getApplicationContext(),
//						DashBoardActivity.class).putExtra("From",
//						"DownloadLocation"));
				finish();
			}
		});
		
		adapter = new DownloadLocationAdapter(getApplicationContext());
		mListView.setAdapter(adapter);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		trimCache(getApplicationContext());
	}
}
