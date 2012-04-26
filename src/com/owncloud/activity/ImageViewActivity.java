package com.owncloud.activity;

import android.os.Bundle;
import android.webkit.WebView;

import com.owncloud.R;
import com.owncloud.common.BaseActivity;

public class ImageViewActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageview);

//		ImageView img = (ImageView) findViewById(R.id.img);
//		img.setImageBitmap(bitmap);
		String urls = getIntent().getExtras().getString("URL");
		WebView ads_content = (WebView) findViewById(R.id.ads_content);
		ads_content.loadUrl(urls);
	}
}
