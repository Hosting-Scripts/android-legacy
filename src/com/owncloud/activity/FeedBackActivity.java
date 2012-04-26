package com.owncloud.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.owncloud.R;
import com.owncloud.common.BaseActivity;

public class FeedBackActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		
		final EditText mText = (EditText)findViewById(R.id.feedText);
		Button mButton = (Button)findViewById(R.id.doneBtn);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");
				 emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						    new String[] { "chirag.patel@sufalamtech.com" });
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"ownCloud App ");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,mText.getText().toString());

//				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
				startActivity(emailIntent);
				finish();
			}
		});
		
	
	}

}
