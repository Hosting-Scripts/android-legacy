package com.sufalam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.owncloud.common.BaseActivity;

public class WebdavMethodImpl extends BaseActivity {

	public String deleteFileOrFolder(String url, String where,
			String whichFile, HttpClient httpClient) {
		if (isOnline()) {
			try {
				// DeleteMethod deletefile = new DeleteMethod(url + "/" +
				// whichFile);
				DeleteMethod deletefile = new DeleteMethod(url);
				httpClient.executeMethod(deletefile);
				String test = deletefile.getResponseBodyAsString();
				return test;
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("DeleteFileOrFolder", e.toString());
				return null;
			}
		} else {
			WebNetworkAlert();
			return null;
		}
	}

	public String createDir(String url, String whichFile, HttpClient httpClient) {
		if (isOnline()) {
			try {
				MkColMethod createdir = new MkColMethod(url + "/"
						+ whichFile.replace(" ", "%20"));
				httpClient.executeMethod(createdir);
				String test = createdir.getResponseBodyAsString();
				return test;
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("Create dir : ", e.toString());
				return null;
			}
		} else {
			WebNetworkAlert();
			return null;
		}
	}

	// public void findDir(HttpClient httpClient) {
	// try {
	// String query = "//element(*, rep:root)";
	// SearchMethod search = new SearchMethod(
	// "http://owncloud-test.dev.hive01.com/owncloud/files/webdav.php",
	// query, "xpath");
	// httpClient.executeMethod(search);
	// MultiStatus resp;
	// try {
	// resp = search.getResponseBodyAsMultiStatus();
	// for (int i1=0; i1<=resp.getResponses().length-1; i1++) {
	// String respString = resp.getResponses()[i1].getHref();
	// Log.d("response", respString);
	// }
	// } catch (DavException e) {
	// e.printStackTrace();
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public String uploadFile(String url, String where, FileInputStream from,
			String fileName, HttpClient httpClient) {
		String statusMsg = null;
		if (isOnline()) {
			if (chkFile(url, fileName)) {
				return "FileExist";
			} else {

				fileName = URLEncoder.encode(fileName);

				PutMethod putMethod = new PutMethod(url + "/" + where
						+ fileName);
				try {
					RequestEntity re = new InputStreamRequestEntity(from);
					
					if (mUploadFlag) {
						mUploadCnt = true;
						Log.d("UploadFlag ","uploadFile to server =====> True ");
						putMethod.setRequestEntity(re);
						//===================
						putMethod.setUseExpectHeader(true);
						httpClient.getParams().setParameter(
								HttpMethodParams.USE_EXPECT_CONTINUE, true);
						putMethod.setContentChunked(true);
						httpClient.setTimeout(0);
						//===================
						int status = httpClient.executeMethod(putMethod);
						if (status == HttpStatus.SC_OK) {
							Log.e("Upload File Status",
									"======>  " + String.valueOf(status)
									+ "  <============");
						}
						statusMsg = putMethod.getResponseBodyAsString();
						mUploadCnt = false;
						Log.d("Upload Statis "," Upload Done");
					}else{
						Log.v("UploadFlag ","uploadFile =====> false");
						statusMsg = "false";
					}
					return statusMsg;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

			}
		} else {
			return "NetworkError";
		}
	}

	public MultiStatus listAll(String fromUrl, HttpClient httpClient) {
		if (isOnline()) {
			try {
				PropFindMethod find = new PropFindMethod(fromUrl);
				httpClient.executeMethod(find);
				MultiStatus resp = null;
				try {
					resp = find.getResponseBodyAsMultiStatus();
					// for (int i1=0; i1<=resp.getResponses().length-1; i1++) {
					// String respString = resp.getResponses()[i1].getHref();
					// Log.d("response", respString);
					// }
				} catch (DavException e) {
					e.printStackTrace();
				}
				return resp;
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
		} else {
			WebNetworkAlert();
			return null;
		}
	}

	public boolean login(String fromUrl, HttpClient httpClient) {
		if ((listAll(fromUrl, httpClient)) == null) {
			return false;
		} else {
			return true;
		}
	}

	public void downloadFile(String url) {
		if (isOnline()) {
			GetMethod gm = new GetMethod(url);
			try {
				int status = httpClient.executeMethod(gm);
				if (status == HttpStatus.SC_OK) {
					InputStream input = gm.getResponseBodyAsStream();

					String fileName = new StringBuffer(url).reverse()
							.toString();
					String[] fileNameArray = fileName.split("/");
					fileName = new StringBuffer(fileNameArray[0]).reverse()
							.toString();

					fileName = URLDecoder.decode(fileName);

					File folder = new File(
							Environment.getExternalStorageDirectory()
									+ "/ownCloud");
					boolean success = false;
					if (!folder.exists()) {
						success = folder.mkdir();
					}
					if (!success) {
						// Do something on success
						File file = new File(Environment
								.getExternalStorageDirectory().toString()
								+ "/ownCloud/", fileName);
						OutputStream fOut = new FileOutputStream(file);

						int byteCount = 0;
						byte[] buffer = new byte[4096];
						int bytesRead = -1;

						while ((bytesRead = input.read(buffer)) != -1) {
							fOut.write(buffer, 0, bytesRead);
							byteCount += bytesRead;
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
		} else {
			WebNetworkAlert();
		}
	}

	public boolean chkFile(String url, String name) {

		/*MultiStatus ms = null;
		try {
			ms = listAll(url, httpClient);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (ms != null) {

			if (ms.getResponses().length != 1) {
				for (int i = 1; i <= ms.getResponses().length - 1; i++) {

					File file = new File(ms.getResponses()[i].getHref());
					String fileName = file.getName();
					fileName = URLDecoder.decode(fileName);

					if (fileName.equals(name)) {
						Log.e("Check File ", "Server File : " + fileName
								+ " == Client File : " + name);
						return true;
					} else {
						Log.d("Check File ", "Server File : " + fileName
								+ " != Client File : " + name);
					}
				}
			}
		}*/
		if(mListFile.contains(name)){
			Log.i("Chk File","============== > File Exist : "+name);
			mListFile.clear();
			return true;
		}
		Log.i("Chk File","============= > File Not Exist : "+name);
		mListFile.clear();
		return false;
	}
}
