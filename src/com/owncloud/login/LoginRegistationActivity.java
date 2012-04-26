package com.owncloud.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

import com.owncloud.R;
import com.owncloud.common.BaseActivity;

public class LoginRegistationActivity extends BaseActivity{
	
	EditText mFirstName;
	EditText mUserPass;
	EditText mLastName;
	EditText mEmail;
	ImageButton mLoginBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginregistation);
		
		mFirstName = (EditText) findViewById(R.id.firstName);
		mUserPass = (EditText) findViewById(R.id.password);
		mLastName = (EditText)findViewById(R.id.lastName);
		mEmail = (EditText)findViewById(R.id.email);
		
		mLoginBtn = (ImageButton)findViewById(R.id.loginBtn);
		mLoginBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				startActivity(new Intent(getApplicationContext(),
//						DashBoardActivity.class));
//				finish();
			}
		});
		
		
	
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			
			startActivity(new Intent(getApplicationContext(),LoginSelectionActivity.class));
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
