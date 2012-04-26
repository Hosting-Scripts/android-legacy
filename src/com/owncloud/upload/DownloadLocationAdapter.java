package com.owncloud.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.owncloud.R;
import com.owncloud.common.BaseActivity;

public class DownloadLocationAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<String> item = null;
	private List<String> path = null;
	public static int mNoOfData;
	private String root = "/";
	String dirPath;
	Context context;

	public DownloadLocationAdapter(Context context) {
		this.context = context;

		item = new ArrayList<String>();
		path = new ArrayList<String>();
		getDir("/mnt/sdcard");
		mNoOfData = this.getCount();

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public static class ViewHolder {
		public TextView mText1;
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {

		View vi = convertView;
		final ViewHolder holder;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.row, null);
			holder = new ViewHolder();
			holder.mText1 = (TextView) vi.findViewById(R.id.rowtext);
			vi.setTag(holder);
		} else {
			holder = (ViewHolder) vi.getTag();
		}
		holder.mText1.setText(item.get(position));
		// holder.mCheckBox.setChecked(false);

		vi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final File file;
				if (path.get(position).equals("/")
						|| path.get(position).equals("/mnt")) {
					file = new File("/mnt/sdcard");
				} else {
					file = new File(path.get(position));
				}
				if (file.isDirectory()) {
					if (file.canRead()){
						BaseActivity.mDownloadDest = file.getPath();
						Log.i("Download File destination","========= > " + file.getPath());
						getDir(file.getPath());
					}else {
						new AlertDialog.Builder(context)
								.setTitle(
										file.getName()
												+ "  folder can't be read!")
								.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
											}
										}).show();
					}
				}

			}
		});
		return vi;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return item.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private void getDir(String dirPath) {
		this.dirPath = dirPath;

		item.clear();
		path.clear();

		FileExplorer.uploadFile.clear();

		File f = new File(dirPath);
		Log.e("File Explorer", "Dir Path ===============================>"
				+ dirPath);
		File[] files = f.listFiles();

		if (!dirPath.equals(root)) {

			item.add(root);
			path.add(root);
			Log.i("File Explorer", "List Item 0 : " + root);

			item.add("../");
			path.add(f.getParent());
			Log.i("File Explorer", "List Item 1 : ../");
		}
		int index = 2;
		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			try {
				if (file.isDirectory() && !file.getName().startsWith(".")) {
					Log.i("File Explorer", "List Item Directory <== " + index
							+ " <== : " + file.getName());
					item.add(file.getName() + "/");
					path.add(file.getPath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		notifyDataSetChanged();
	}
}
