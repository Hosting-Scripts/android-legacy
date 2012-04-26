package com.owncloud.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.owncloud.R;
import com.sufalam.WebdavMethodImpl;

public class LoginSelectionActivity extends WebdavMethodImpl implements
		OnClickListener {

	ImageButton mAlreadyUser;
	ImageButton mNewUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginselection);

		mAlreadyUser = (ImageButton) findViewById(R.id.alreadyUser);
		mNewUser = (ImageButton) findViewById(R.id.newUser);

		mAlreadyUser.setOnClickListener(this);
		mNewUser.setOnClickListener(this);

	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.obj.toString().equalsIgnoreCase("AlreadyUser")) {
				startActivity(new Intent(getApplicationContext(),
						LoginActivity.class));
				finish();
			} else if (msg.obj.toString().equalsIgnoreCase("NewUser")) {
				// startActivity(new
				// Intent(getApplicationContext(),LoginRegistationActivity.class));
				// finish();

				if (isOnline()) {
					startActivity(new Intent(Intent.ACTION_VIEW,
							Uri.parse("http://owncloud.com/mobile/new")));
				} else {
					WebNetworkAlert();
				}

			}

			removeDialog(0);
		}

	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v == mAlreadyUser) {
			showDialog(0);
			t = new Thread() {
				public void run() {

					Message msg = handler.obtainMessage();
					msg.obj = "AlreadyUser";
					handler.sendMessage(msg);
				}
			};
			t.start();

		} else if (v == mNewUser) {

			showDialog(0);

			t = new Thread() {
				public void run() {

					Message msg = handler.obtainMessage();
					msg.obj = "NewUser";
					handler.sendMessage(msg);
				}
			};
			t.start();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			alertbox.setTitle("Warning");
			alertbox.setMessage("Are you sure you want to exit from application.?");
			alertbox.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
							moveTaskToBack(true);

						}
					});
			alertbox.setNegativeButton("No", null);
			alertbox.show();
			
			return true;
		}else{
			
			Toast.makeText(getApplicationContext(), "Home", 1).show();
		}
		return super.onKeyDown(keyCode, event);
	}
}
