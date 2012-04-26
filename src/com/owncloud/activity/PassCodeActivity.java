package com.owncloud.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.owncloud.R;
import com.owncloud.common.BaseActivity;

public class PassCodeActivity extends BaseActivity {

	String mTextFirst;
	String mTextSec;
	String mTextThird;
	String mTextFour;
	EditText mText1;
	EditText mText2;
	EditText mText3;
	EditText mText4;
	TextView mPassHdr;
	Button mCancel;
	
	boolean flag = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.passcodelock);

		mText1 = (EditText) findViewById(R.id.txt1);
		mText1.requestFocus();
		mText2 = (EditText) findViewById(R.id.txt2);
		mText3 = (EditText) findViewById(R.id.txt3);
		mText4 = (EditText) findViewById(R.id.txt4);
		mPassHdr = (TextView) findViewById(R.id.passHdr);
		mCancel = (Button) findViewById(R.id.cancel);
		mCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				startActivity(new Intent(getApplicationContext(),
						DashBoardActivity.class).putExtra("From", "PassCodeOn"));
				finish();
			}
		});
		
		String mFrom = getIntent().getExtras().getString("From");
		
		if(mFrom.equals("splash")){
			mCancel.setVisibility(View.GONE);
		}
		mText1.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (s.length() > 0) {
					if (mPassHdr.getText().toString().equals("Enter pass code")) {
						mTextFirst = mText1.getText().toString();
					}

					mText2.requestFocus();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		mText2.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (s.length() > 0) {
					if (mPassHdr.getText().toString().equals("Enter pass code")) {
						mTextSec = mText2.getText().toString();
					}

					mText3.requestFocus();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		mText2.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub

				if (keyCode == KeyEvent.KEYCODE_DEL && flag) {

					mText1.requestFocus();
					mText1.setText("");
					mTextSec = "";
					flag=false;

				}else if(!flag){
					flag=true;
				}
				return false;
			}
		});
		mText3.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub

				if (keyCode == KeyEvent.KEYCODE_DEL && flag) {

					mText2.requestFocus();
					mText2.setText("");
					mTextThird = "";
					flag= false;
					
				}else if(!flag){
					flag=true;
				}
				return false;
			}
		});
		mText4.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub

				if (keyCode == KeyEvent.KEYCODE_DEL) {

					mText3.requestFocus();
					mText3.setText("");
					mTextFour="";
					flag= false;
					
				}else if(!flag){
					flag=true;
				}
				return false;
			}
		});
		mText3.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (s.length() > 0) {
					if (mPassHdr.getText().toString().equals("Enter pass code")) {
						mTextThird = mText3.getText().toString();
					}
					mText4.requestFocus();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		mText4.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if (s.length() > 0) {
					if (mPassHdr.getText().toString().equals("Enter pass code")) {
						mTextFour = mText4.getText().toString();
					}

					if (pref.getString(PREF_PASSCODE, null) != null
							&& pref.getString(PREF_PASSCODE, null).equals(
									"true")) {

						if (mTextFirst.equals(pref.getString(
								PREF_PASS_TXT_FIRST, null))) {
							if (mTextSec.equals(pref.getString(
									PREF_PASS_TXT_SECOND, null))) {
								if (mTextThird.equals(pref.getString(
										PREF_PASS_TXT_THIRD, null))) {
									if (mTextFour.equals(pref.getString(
											PREF_PASS_TXT_FOUR, null))) {
									
										if(getIntent().getExtras().getString("From").equals("passOff")){
											
											getSharedPreferences(
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
//											mPassOnOff.setText("turn on");
											
										}
										startActivity(new Intent(
												getApplicationContext(),
												DashBoardActivity.class)
												.putExtra("From", "PassCodeOff"));
										finish();
									} else {
										wrongPW();
									}
								} else {
									wrongPW();
								}
							} else {
								wrongPW();
							}
						} else {
							wrongPW();
						}

					} else if (mPassHdr.getText().toString()
							.equals("Reenter pass code")) {

						if (mTextFirst.equals(mText1.getText().toString())) {
							if (mTextSec.equals(mText2.getText().toString())) {
								if (mTextThird.equals(mText3.getText()
										.toString())) {
									if (mTextFour.equals(mText4.getText()
											.toString())) {
										getSharedPreferences(PREFS_NAME,
												MODE_PRIVATE)
												.edit()
												.putString(PREF_PASS_TXT_FIRST,
														mTextFirst)
												.putString(
														PREF_PASS_TXT_SECOND,
														mTextSec)
												.putString(PREF_PASS_TXT_THIRD,
														mTextThird)
												.putString(PREF_PASS_TXT_FOUR,
														mTextFour)
												.putString(PREF_PASSCODE,
														"true").commit();
										startActivity(new Intent(
												getApplicationContext(),
												DashBoardActivity.class)
												.putExtra("From", "PassCodeOn"));
										finish();
									} else {
										wrongPW();
									}
								} else {
									wrongPW();
								}
							} else {
								wrongPW();
							}

						} else {
							wrongPW();
						}

					} else {
						mPassHdr.setText("Reenter pass code");
						mText1.setText("");
						mText2.setText("");
						mText3.setText("");
						mText4.setText("");
						mText1.requestFocus();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

	}

	public void wrongPW() {
		Toast.makeText(getApplicationContext(), "Pass Code not match", 1)
				.show();
		mPassHdr.setText("Enter pass code");
		mText1.setText("");
		mText2.setText("");
		mText3.setText("");
		mText4.setText("");
		mText1.requestFocus();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (getIntent().getExtras().getString("From").equals("dashboard")) {
				// if ((!(pref.getString(PREF_PASSCODE, null) == null))
				// && (!pref.getString(PREF_PASSCODE, null).equals("true"))) {
				startActivity(new Intent(getApplicationContext(),
						DashBoardActivity.class).putExtra("From", "PassCodeOn"));
				finish();
			} else if (getIntent().getExtras().getString("From")
					.equals("splash")) {
				finish();
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
