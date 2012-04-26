/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
	
  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
*/

package com.owncloud.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.owncloud.common.BaseActivity;

public class Downloader extends IntentService {
  public static final String EXTRA_MESSENGER="com.sufalamtech.downloadmanager.downloader.EXTRA_MESSENGER";
  private HttpClient client=null;

  Intent i;
  
  public Downloader() {
    super("Downloader");
  }
  
  @Override
  public void onCreate() {
    super.onCreate();
    
    client=new DefaultHttpClient();
  }
  
  @Override
  public void onDestroy() {
    super.onDestroy();
    
    client.getConnectionManager().shutdown();
  }

  @Override 
  public void onHandleIntent(Intent i) {
	  downloadFile(i);
  }
  
  
  Thread download = new Thread() {

      @Override
      public void run() {

          for (int i = 1; i < 100; i++) {
        	  DashBoardActivity.updateNotation();

              try {
                  Thread.sleep(100);
              } catch (InterruptedException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              }
          }

          Bundle extras=i.getExtras();
		  
		    if (extras!=null) {
		      Messenger messenger=(Messenger)extras.get(EXTRA_MESSENGER);
		      Message msg=Message.obtain();
		      
		      msg.arg1=Activity.RESULT_OK;
		      
		      try {
		        messenger.send(msg);
		      }
		      catch (android.os.RemoteException e1) {
		        Log.w(getClass().getName(), "Exception sending message", e1);
		      }
		    }
         

      }
  };

  
  public void downloadFile(Intent i) {
  	if(isOnline()) {
  		String url = i.getData().toString();
			GetMethod gm = new GetMethod(url);

			try {
				int status = BaseActivity.httpClient.executeMethod(gm);
//				Toast.makeText(getApplicationContext(), String.valueOf(status),2).show();
				Log.e("HttpStatus","==============>"+String.valueOf(status));
				if (status == HttpStatus.SC_OK) {
					InputStream input = gm.getResponseBodyAsStream();
					
					String fileName = new StringBuffer(url).reverse().toString();
					String[] fileNameArray = fileName.split("/");
					fileName = new StringBuffer(fileNameArray[0]).reverse()
							.toString();

					fileName = URLDecoder.decode(fileName);
					File folder = new File(
							BaseActivity.mDownloadDest);
					boolean success = false;
					if (!folder.exists()) {
						success = folder.mkdir();
					}
					if (!success) {
						// Do something on success
						File file = new File(BaseActivity.mDownloadDest, fileName);
						OutputStream fOut = new FileOutputStream(file);
	
						int byteCount = 0;
						byte[] buffer = new byte[4096];
						int bytesRead = -1;
	
						while ((bytesRead = input.read(buffer)) != -1) {
							fOut.write(buffer, 0, bytesRead);
							byteCount += bytesRead;
//							DashBoardActivity.updateNotation();
						}
	
						fOut.flush();
						fOut.close();
					} else {
						// Do something else on failure
						Log.d("Download Prob..", String.valueOf(success)
								+ ", Some problem in folder creating");
					}
					
				}
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gm.releaseConnection();

			Bundle extras=i.getExtras();
		  
		    if (extras!=null) {
		      Messenger messenger=(Messenger)extras.get(EXTRA_MESSENGER);
		      Message msg=Message.obtain();
		      
		      msg.arg1=Activity.RESULT_OK;
		      
		      try {
		        messenger.send(msg);
		      }
		      catch (android.os.RemoteException e1) {
		        Log.w(getClass().getName(), "Exception sending message", e1);
		      }
		    }
  	} else {
  		WebNetworkAlert();
  	}
  }
  public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			// Try This
			int i = netInfo.getType();
			System.out.println("Net Type =" + i);

			return true;
		}
		return false;

	}

	public void WebNetworkAlert() {
		new AlertDialog.Builder(this).setTitle("Network Error")
				.setMessage("Internet connection not available.")
				.setPositiveButton("Ok", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// do stuff onclick of YES
//						finish();
						return;
					}
				}).show();
	}
  
}
