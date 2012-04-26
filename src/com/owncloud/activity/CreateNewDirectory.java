package com.owncloud.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.owncloud.R;
import com.owncloud.upload.ChooseUploadLocation;
import com.sufalam.WebdavMethodImpl;

public class CreateNewDirectory extends WebdavMethodImpl {

	EditText txtName;
	Button btnDone;
	public static String mUrl = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.getfoldername);

		txtName = (EditText) findViewById(R.id.folderName);
		btnDone = (Button) findViewById(R.id.folderDone);

		if (getIntent().getExtras().getString("From")
				.equalsIgnoreCase("DashBoard")) {
			mUrl = DashBoardActivity.url;
		} else {
			mUrl = getIntent().getExtras().getString("Url");
		}

		Log.i("New Folder Url", mUrl);

		btnDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!(txtName.getText().toString()).equals("")) {
					String createFolder = createDir(mUrl, txtName.getText()
							.toString(), DashBoardActivity.httpClient);
					if (createFolder.equals(null)) {
						Toast.makeText(getApplicationContext(),
								"There is some problem in creating folder.", 1)
								.show();
					} else {
						mUrl = mUrl+"/"+txtName.getText().toString();
						Toast.makeText(getApplicationContext(),
								"Folder Create Successfully.", 1).show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"Please enter folder name.", 1).show();
				}
				if (getIntent().getExtras().getString("From")
						.equalsIgnoreCase("DashBoard")) {
					Intent intent = new Intent(getApplicationContext(),
							DashBoardActivity.class).putExtra("From",
							"PassCode").putExtra("newdir", "newDir");
					startActivity(intent);
					finish();
				}else{
//					startActivity(new Intent(getApplicationContext(),ChooseUploadLocation.class).putExtra("Url", mUrl));
					finish();
				}
			}
		});

	}

}
