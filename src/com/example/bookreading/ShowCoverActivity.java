package com.example.bookreading;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class ShowCoverActivity extends Activity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_cover_page);
        new SplashActivity().execute();
	}
	
	@SuppressWarnings("unused")
	private class SplashActivity extends AsyncTask
	{
		public static final int SLEEP_TIME = 3000; // in Milli Seconds

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			Intent intent = new Intent(ShowCoverActivity.this, ReadingActivity.class);
			startActivity(intent);
			finish();
		}
    }

	
}
