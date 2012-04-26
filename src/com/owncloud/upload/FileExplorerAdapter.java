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
import android.widget.CheckBox;
import android.widget.TextView;

import com.owncloud.R;

public class FileExplorerAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<String> item = null;
	private List<String> path = null;
	
	private ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();

	private String root = "/";
	public static int mNoOfData;

	String dirPath;
	Context context;

	/**
	 * Constructor from a list of items
	 */
	public FileExplorerAdapter(Context context) {

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
		CheckBox mCheckBox;
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
			holder.mCheckBox = (CheckBox) vi.findViewById(R.id.chkBox);

			vi.setTag(holder);
		} else {
			holder = (ViewHolder) vi.getTag();
		}
		holder.mText1.setText(item.get(position));
		// holder.mCheckBox.setChecked(false);
		if (!item.get(position).endsWith("/")) {
			holder.mCheckBox.setVisibility(View.VISIBLE);
		} else
			holder.mCheckBox.setVisibility(View.INVISIBLE);

		holder.mCheckBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (holder.mCheckBox.isChecked()) {

					itemChecked.set(position, true);

					FileExplorer.uploadFile.add(path.get(position));
					Log.i("File Name:", path.get(position));
					Log.i("No. Of File Selected",
							String.valueOf(FileExplorer.uploadFile.size()));
				} else {
					itemChecked.set(position, false);

					if (FileExplorer.uploadFile.contains(path.get(position))) {
						FileExplorer.uploadFile.remove(path.get(position));
						Log.i("Remove File Name:", path.get(position));
						Log.i("No. Of File Selected",
								String.valueOf(FileExplorer.uploadFile.size()));
					}
				}
			}
		});

		try {
			holder.mCheckBox.setChecked(itemChecked.get(position));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		vi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				final File file;
				if(path.get(position).equals("/") ||path.get(position).equals("/mnt")){
					file = new File("/mnt/sdcard");
				}else{
					file = new File(path.get(position));
				}
				if (position <= 1 && FileExplorer.uploadFile.size() > 0) {
					new AlertDialog.Builder(context)
							.setTitle("your files will not be uploaded.")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											if (file.isDirectory()) {
												if (file.canRead())
													getDir(file.getPath());

												else {
													new AlertDialog.Builder(
															context)
															.setTitle(
																	file.getName()
																			+ "  folder can't be read!")
															.setPositiveButton(
																	"OK",null).show();
												}
											}

										}
									}).setNegativeButton("Back", null).show();

				} else {
					
					if (file.isDirectory()) {
						if (file.canRead())
							getDir(file.getPath());

						else {
							new AlertDialog.Builder(context)
									.setTitle(
											file.getName()
													+ "  folder can't be read!")
									.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													// TODO
													// Auto-generated
													// method
													// stub
												}
											}).show();
						}
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
		itemChecked.clear();
//		for (int i = 0; i < mNoOfData; i++) {
//			itemChecked.add(i, false); // initializes all items value with false
//		}
		
		FileExplorer.uploadFile.clear();

		File f = new File(dirPath);
		Log.e("File Explorer","Dir Path ===============================>" +dirPath);
		File[] files = f.listFiles();

		if (!dirPath.equals(root)) {

			item.add(root);
			path.add(root);
			Log.i("File Explorer","List Item 0 : "+root);
			itemChecked.add(0,false);
		
			item.add("../");
			path.add(f.getParent());
			Log.i("File Explorer","List Item 1 : ../");
			itemChecked.add(1,false);
		}
		int index = 2;
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			
			try {
				if (file.isDirectory()&& !file.getName().startsWith(".")){
					Log.i("File Explorer","List Item Directory <== "+ index +" <== : "+file.getName());
					item.add(file.getName() + "/");
					itemChecked.add(index++,false);
					path.add(file.getPath());
				}else if (!file.isHidden() && !file.getName().startsWith(".")){
					Log.d("File Explorer","List Item File <== "+ index +" <== : "+file.getName());
					item.add(file.getName());
					itemChecked.add(index++,false);
					path.add(file.getPath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		notifyDataSetChanged();
	}

}