package com.example.bookreading;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bookreading.BookNavigator.navAction;
import com.example.bookreading.ContentDisplay.Lang;
import com.example.bookreading.ContentDisplay.OnScreenChangeListener;
import com.example.bookreading.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ReadingActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    //private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_FULLSCREEN;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

	protected ContentDisplay mBookDisplay;
	
	protected FrameLayout frame;
	protected View contentView;
	
	enum Zoom {IN, OUT};
	private Float initailContentFontSize = null;
	private static final int SUPPORTED_ZOOM_RANGE = 5; 
	
	enum theme {DAY, NIGHT};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //this.setTheme(R.style.dFullscreenTheme);
        setContentView(R.layout.activity_reading);
        
        frame = (FrameLayout) findViewById(R.id.fullscreen_frame);
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        contentView = findViewById(R.id.fullscreen_content);
        //final View contentView = findViewById(R.id.pagebody1);
        final View tocView = findViewById(R.id.toc_content);
        final View bmView = findViewById(R.id.bm_content);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setCustomView(R.layout.actionbar_icons);
        
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);
        frame.setBackgroundColor(Color.WHITE);
		((TextView)contentView).setTextColor(Color.BLACK);
		

		mBookDisplay = new ContentDisplay(this, this.getBaseContext());
	
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            //getActionBar().hide();
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                            
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        	//controlsView.setVisibility(View.INVISIBLE);
                        	//getActionBar().hide();
                        	
                        }
                        if (visible) {
                        	//controlsView.bringToFront();
                        	//findViewById(R.id.setting_button).bringToFront();
                        }
                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        /*
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });
        */
        
		
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.setting_button).setOnTouchListener(mDelayHideTouchListener);
        
        //controlsView.setVisibility(View.INVISIBLE);
        TextView subtitle = (TextView) getActionBar().getCustomView().findViewById(R.id.sub_title);
        mBookDisplay.setLanguage(Lang.HINDI);
        mBookDisplay.addViews((ListView)tocView, (ListView)bmView, contentView, (GridView)findViewById(R.id.setting_button), subtitle);
        Float fz = getSavedFontSize();
        if (fz!=0f) {
			mBookDisplay.changeFontSizeInPx(fz);
		}	
        mBookDisplay.setOnScreenChangeListener(new OnScreenChangeListener() {
			@Override
			public boolean applyScreenEvent() {
				
			    if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
				return true;
			}
        });
        //this.setTheme(R.style.dFullscreenTheme);
        ImageView chprev = (ImageView) getActionBar().getCustomView().findViewById(R.id.action_chprev);
        chprev.setOnClickListener(new OnClickListener() {
        	    @Override
        	    public void onClick(View view) {
        	    	//Toast.makeText(ReadingActivity.this, "PrevChap Clicked", Toast.LENGTH_SHORT).show();
        	    	mBookDisplay.bn.handleNavigation(navAction.PREVCONTENT, 0, 0, null);
        	    }
        });
        ImageView pgprev = (ImageView) getActionBar().getCustomView().findViewById(R.id.action_pgprev);
        pgprev.setOnClickListener(new OnClickListener() {
        	    @Override
        	    public void onClick(View view) {
        	    	//Toast.makeText(ReadingActivity.this, "PrevPage Clicked", Toast.LENGTH_SHORT).show();
        	    	mBookDisplay.bn.handleNavigation(navAction.PREVPAGE, 0, 0, null);
        	    }
        });
        ImageView pgnext = (ImageView) getActionBar().getCustomView().findViewById(R.id.action_pgnext);
        pgnext.setOnClickListener(new OnClickListener() {
        	    @Override
        	    public void onClick(View view) {
        	    	//Toast.makeText(ReadingActivity.this, "NextPage Clicked", Toast.LENGTH_SHORT).show();
        	    	mBookDisplay.bn.handleNavigation(navAction.NEXTPAGE, 0, 0, null);
        	    }
        });
        ImageView chnext = (ImageView) getActionBar().getCustomView().findViewById(R.id.action_chnext);
        chnext.setOnClickListener(new OnClickListener() {
        	    @Override
        	    public void onClick(View view) {
        	    	//Toast.makeText(ReadingActivity.this, "NextChap Clicked", Toast.LENGTH_SHORT).show();
        	    	mBookDisplay.bn.handleNavigation(navAction.NEXTCONTENT, 0, 0, null);
        	    }
        });
        getWindow().setBackgroundDrawable(null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            //Toast.makeText(view.getContext(), "mDelayHideTouchListener ", Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
        if (AUTO_HIDE) {
            delayedHide(10*AUTO_HIDE_DELAY_MILLIS);
        }
		return super.onMenuOpened(featureId, menu);
	}
	@Override
	public void onOptionsMenuClosed (Menu menu) {
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
		super.onOptionsMenuClosed(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		else if (id == R.id.action_day_theme) {
			changeTheme(theme.DAY);
		}
		else if (id == R.id.action_night_theme) {
			changeTheme(theme.NIGHT);
		}
		else if (id == R.id.action_zoom_in){
			changeFontSize(Zoom.IN);
		}
		else if (id == R.id.action_zoom_out) {
			changeFontSize(Zoom.OUT);
		}
		return super.onOptionsItemSelected(item);
	}
	public void saveFontSize(Float sz)
	{
		SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat("FONT_SIZE", sz);
		editor.commit();
	}
	public Float getSavedFontSize()
	{
		Float size=0f;
		SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
		prefs.getFloat("FONT_SIZE", size);
		return size;
	}
	/**
	 * Changes font size of the main content.
	 */
	public void changeFontSize(Zoom zoomAction) {
		Float currentFontSize  = mBookDisplay.getContentFontSize() ;
		if (initailContentFontSize == null ){
			initailContentFontSize = currentFontSize;
		}
		if (zoomAction == Zoom.IN ) {
			currentFontSize  += 1; //TODO: this value  should should be taken from configuration
		} else if (zoomAction == Zoom.OUT ) {
			currentFontSize  -= 1;
		}
		mBookDisplay.changeFontSizeInPx(currentFontSize);
		saveFontSize(currentFontSize);
	}
	
	public void changeTheme(theme id)
	{
		if (id == theme.DAY) {
			//this.setTheme(R.style.dFullscreenTheme);
			contentView.setBackgroundColor(Color.WHITE);
			//frame.setBackgroundColor(Color.WHITE);
			mBookDisplay.changeTheme(ContentDisplay.change_type.THEME, ContentDisplay.theme.DAY);
		}
		else {
			//this.setTheme(R.style.nFullscreenTheme);
			contentView.setBackgroundColor(Color.BLACK);
			//frame.setBackgroundColor(Color.BLACK);
			mBookDisplay.changeTheme(ContentDisplay.change_type.THEME, ContentDisplay.theme.NIGHT);
		}
		//frame.refreshDrawableState();
	}
    
}
