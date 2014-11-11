package com.example.bookreading;

import android.R.color;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BookControl extends BaseAdapter {
	
	private Context mContext;
	private int mView=0;
	// Constructor
	public BookControl(Context c) { mContext = c; } 
	public int getCount() { return 2; }
	public Object getItem(int position) { return null; }
	public long getItemId(int position) { return 0; }
	
	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) 
	{ 
				
		//https://github.com/mburman/Android-File-Explore/blob/master/FileExplore/src/com/mburman/fileexplore/FileExplore.java
		TextView file;
		if (convertView == null) {
		
			file = new TextView(mContext);
			if (position==0)
				file.setText("BookMark");
			else
				file.setText(Html.fromHtml("TOC"));
			
			//When we set the font size in the code it applies to all the device type.
			//file.setTextSize(file.getTextSize());
			file.setTextColor(Color.WHITE);
			file.setGravity(Gravity.CENTER);
			//file.setBackgroundColor(color.background_dark);
			
		}
		else { 
			file = (TextView) convertView;
		} 
		
		return file;
	}
}
