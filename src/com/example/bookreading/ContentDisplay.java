package com.example.bookreading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookreading.BookHolder.item_type;
import com.example.bookreading.BookNavigator.OnNavigationChangeListener;
import com.example.bookreading.BookNavigator.navAction;
import com.example.bookreading.BookNavigator.navEvent;
import com.example.bookreading.util.PixelConverter;

//http://stackoverflow.com/questions/4951142/smooth-scrolling-in-android?rq=1
//http://developer.android.com/training/animation/crossfade.html
//https://github.com/grantland/android-autofittextview
public class ContentDisplay { // implements BookNavigator.OnNavigationChangeListener {
	ListView mlv, mbmv;
	GridView mgv;
	View mtv;
	TextView mtitlev;
	Context mContext;
	Activity mActivity;
	Lang mLang=Lang.DEFAULT;
	
	BookNavigator bn;
	int curViewId=0;
	item_type curContentType;
	String curContentSrc;
	
	boolean toc_loaded=false;
	int maxlines=0;
	float fontsz=0;
	int downX, downY;
	boolean theme_changed=false;
	theme themeid;
	
	enum change_type {THEME, FONT, FONTSZ, LINEHT};
	enum theme {DAY, NIGHT};
	enum Lang { ENGLISH, HINDI, DEFAULT };
	enum Content { TXT, HTML };
	
	public OnScreenChangeListener mOnScreenChangeListener=sDummyListener;
	
	public ContentDisplay(Activity activity, Context context) {
		mActivity = activity;
		mContext = context;
		
		setupActivity();
		
		bn = new BookNavigator(); 
		
		bn.setOnNavigationChangeListener(new OnNavigationChangeListener() {
			@Override
			public boolean changeDisplayContent(navAction na, int srcId, Object data) {
				//Toast.makeText(mContext, "addViews-changeDisplayContent ", Toast.LENGTH_SHORT).show();
				return mchangeDisplayContent(na, srcId, data);
			}
			@Override
			public void loadDisplayContent(String sfile, item_type type, int pos) {
				mloadDisplayContent(sfile, type, pos);
				//Toast.makeText(mContext, "addViews-loadDisplayContent ", Toast.LENGTH_SHORT).show();		
			}
		});
		
	}
	@SuppressLint("NewApi") public void setupActivity() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mActivity.getActionBar().hide();
			mActivity.getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	public void setLanguage(Lang lg) { mLang = lg; }
	public void addViews(ListView lv, ListView bmv, View tv, GridView gv, TextView titlev) { 
		mlv=lv; mtv=tv; mgv=gv;
		mbmv = bmv; mtitlev = titlev;
		
		if (TextView.class.isInstance(tv)) {
			((TextView)mtv).setPadding(10,  0,  10,  0);
			if (mLang==Lang.DEFAULT)
				((TextView)mtv).setLineSpacing(0, 1.2f);
			else if (mLang!=Lang.ENGLISH) {
				((TextView)mtv).setLineSpacing(0, 0.9f);
				((TextView)mtv).setTextSize(((TextView)mtv).getTextSize()+2);
			}
    		((TextView)mtv).setMovementMethod(ScrollingMovementMethod.getInstance());
    		if (mLang==Lang.HINDI) {
    			Typeface face=Typeface.createFromAsset(mActivity.getAssets(), "Mangal-Regular.ttf");
    			((TextView)mtv).setTypeface(face, Typeface.NORMAL);
    			mtitlev.setTypeface(face, Typeface.NORMAL);
    		}
    		fontsz = ((TextView)mtv).getTextSize();
    	}
		bn.loadStart(1);
		
		if (gv!=null) {
			
			gv.setAdapter(new BookControl(mContext)); 
			gv.setOnItemClickListener(new OnItemClickListener() { 
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) { 
					//Toast.makeText(mContext, "addViews-Grid ITem ", Toast.LENGTH_SHORT).show();
					if (position==1 && bn.getTOCPos()>=0) {
						bn.loadStart(bn.getTOCPos());
					}
				}
			});
		}
    	tv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		tv.setVerticalScrollBarEnabled(true);
		
		
		final Scroller mScroller = new Scroller(mContext, null, true);
		final GestureDetector mGD = new GestureDetector(mContext,
                new SimpleOnGestureListener() {

@Override
public boolean onScroll(MotionEvent e1, MotionEvent e2,
        float distanceX, float distanceY) {
// beware, it can scroll to infinity
	if (mtv.getScrollY()+(int)distanceY < getLayoutHeight() && mtv.getScrollY()+(int)distanceY >0)
		mtv.scrollBy((int)0, (int)distanceY);
return true;
}

@Override
public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
	//Toast.makeText(mtv.getContext(), "Fling ...", Toast.LENGTH_LONG).show();
mScroller.fling(mtv.getScrollX(), mtv.getScrollY(),
0, -(int)vY, 0, 0, 0, getLayoutHeight());
//mtv.postInvalidate(); // may not be required
return true;
}

@Override
public boolean onDown(MotionEvent e) {
if(!mScroller.isFinished() ) { // is flinging
	//Toast.makeText(mtv.getContext(), "Fling End", Toast.LENGTH_LONG).show();
mScroller.forceFinished(true); // to stop flinging on touch
}
return true; // else won't work
}
});
		
		
		tv.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
                case MotionEvent.ACTION_DOWN: {
                    downX = (int)event.getX();
                    downY = (int)event.getY();
                	}
                	break;
                case MotionEvent.ACTION_UP:
                	if(Math.abs(event.getX()-downX) < 2 && Math.abs(event.getY()-downY) < 2) {	// Click Event
                		return handleEvent(navEvent.CLICK, downX, downY, (int)event.getX(), (int)event.getY());
                	}
                	break;
                default:
				}
				//return v.performClick();
				boolean result = mGD.onTouchEvent(event);
				/*   if (!result) {
				       if (event.getAction() == MotionEvent.ACTION_UP) {
				           stopScrolling();
				           result = true;
				       }
				   }*/
				return result;
				//return false;
			}
		});
		//setupComplete
	}
	public int getVisibleWidth() {
		return mtv.getMeasuredWidth();
	}
	public int getTopVPos() { 
		if (mtv instanceof TextView) {
			try { return ((TextView)mtv).getLayout().getTopPadding(); }
			catch (Exception e) {}
		}
		return 2;
	}
	public int getLineHeight() { if (mtv instanceof TextView) return ((TextView)mtv).getLineHeight(); return 0;}
	public int getContentHeight() {
		if (mtv instanceof TextView)
			return (computeMaxLines()*((TextView)mtv).getLineHeight());
		return mtv.getMeasuredHeight();
	}
	public int computeMaxLines() { return ((mtv.getMeasuredHeight()-((TextView)mtv).getPaddingTop()-((TextView)mtv).getPaddingBottom())/((TextView)mtv).getLineHeight()); }
	public int getVisibleHeight() {	return mtv.getMeasuredHeight(); }
	public int getLayoutWidth() {
		if (mtv instanceof TextView)
			return ((TextView)mtv).getLayout().getWidth();
		else if (mtv instanceof WebView)
			return ((WebView)mtv).getWidth();
		return mtv.getWidth();
	}
	public int getLayoutHeight() {
		if (mtv instanceof TextView)
			return ((TextView)mtv).getLayout().getHeight();
		else if (mtv instanceof WebView)
			return ((WebView)mtv).getHeight();
		return mtv.getHeight();
	}
	public int getLayoutEnd() {
		if (mtv instanceof TextView)
			return ((TextView)mtv).getLayout().getHeight()-((TextView)mtv).getLayout().getBottomPadding();
		return mtv.getHeight()-mtv.getPaddingBottom();
	}
	public int getVPosition() {
		return mtv.getScrollY();
	}
	public void moveVPosition(int dx, int dy) {
		mtv.scrollBy(dx,dy);
	}
	public void setTOCContent(String sfile) {
		String content = readContent(sfile);
		if (content != null) {
			
			mlv.setVisibility(View.VISIBLE);
			if (mbmv.getVisibility()==View.VISIBLE)
				mbmv.setVisibility(View.INVISIBLE);
			//mlv.bringToFront();
			if (mtv instanceof TextView) {
				((TextView)mtv).setText("");
				((TextView)mtv).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				
			}	
			if (toc_loaded==true && theme_changed==false) {
				curViewId = 1;
				return;
			}
			
			//mlv.setAdapter(new ArrayAdapter<String>(mlv.getContext(),
        	//		android.R.layout.simple_list_item_1, content.split("\n")));
			//mlv.setClickable(true);
			mlv.setContentDescription("Table Of Contents");
			
			TextView mtemptv = new TextView(mlv.getContext());
			//mtemptv.setTextAppearance(mContext, android.R.id.text1);
			if (mLang!=Lang.DEFAULT)
				mtemptv.setText(R.string.toc_title);
			else
				mtemptv.setText("Table Of Contents");
			
			if (mlv.getHeaderViewsCount()==0)
				mlv.addHeaderView(mtemptv, "TOC", false);
			
			if (themeid == theme.NIGHT) {
				mlv.setDivider(new ColorDrawable(Color.WHITE));
				mlv.setDividerHeight(2);
				/*if (mLang==Lang.HINDI) {
					
				mlv.setAdapter(new ArrayAdapter<String>(mlv.getContext(),R.layout.list_item2,
        			android.R.id.text1, content.split("\n")) {
					
			        @Override
			        public View getView(int position, View convertView, ViewGroup parent) {
			        	LayoutInflater inflater=LayoutInflater.from(mContext);;
			            if(convertView == null)
			            {
			                 inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			            }
			            convertView = inflater.inflate(R.layout.list_item2, parent, false);
			            TextView entry = (TextView)convertView.findViewById(android.R.id.text1);
			            Typeface mFont=Typeface.createFromAsset(mActivity.getAssets(), "Mangal-Regular.ttf");
			            entry.setTypeface(mFont);
			            return convertView;
			        }
			    }
        			); 
				}
				else*/ {
					mlv.setAdapter(new ArrayAdapter<String>(mlv.getContext(),R.layout.list_item2,
		        			android.R.id.text1, content.split("\n")));
				}
			}
			else {
				mlv.setDivider(new ColorDrawable(Color.GRAY));
				mlv.setDividerHeight(2);
				/*if (mLang==Lang.HINDI) {
					mlv.setAdapter(new ArrayAdapter<String>(mlv.getContext(),R.layout.list_item,
		        			android.R.id.text1, content.split("\n")) {
							
					        @Override
					        public View getView(int position, View convertView, ViewGroup parent) {
					        	LayoutInflater inflater=null;
					            if(convertView == null)
					            {
					                 inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					                 convertView = inflater.inflate(R.layout.list_item, parent, false);
					            }
					            
					            TextView entry = (TextView)convertView.findViewById(android.R.id.text1);
					            Typeface mFont=Typeface.createFromAsset(mActivity.getAssets(), "Mangal-Regular.ttf");
					            entry.setTypeface(mFont);
					            return convertView;
					        }
					    }
		        			);
				}
				else*/
				mlv.setAdapter(new ArrayAdapter<String>(mlv.getContext(),R.layout.list_item,
	        			android.R.id.text1, content.split("\n")));
			}
			
    		mlv.setOnItemClickListener(new OnItemClickListener() { 
    			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    				//Toast.makeText(v.getContext(), "List Item "+position, Toast.LENGTH_SHORT).show();
    				handleEvent(navEvent.TOCSELECT,position,0,0,0);
    			}
    		});
    		curViewId = 1;
			toc_loaded = true;
		}
		theme_changed = false;
	}
	public void setImgContent(String sfile, int id) {
		
		if (mlv.getVisibility()==View.VISIBLE)
			mlv.setVisibility(View.INVISIBLE);
		if (mbmv.getVisibility()==View.VISIBLE)
			mbmv.setVisibility(View.INVISIBLE);
		if (mtv instanceof TextView) {
			((TextView)mtv).setText("");
			//((TextView)mtv).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, id);
			((TextView)mtv).setBackgroundResource(id);
		}
		else if (mtv instanceof WebView) {
			String content = readContent(sfile);
			((WebView)mtv).loadDataWithBaseURL("file:///android_asset/"+sfile, content, "text/html", "UTF-8", "");
		}
		if (mtv.getVisibility()!=View.VISIBLE)
			mtv.setVisibility(View.VISIBLE);
		//mtv.bringToFront();
		mtv.scrollTo(0, 0);
		
		curViewId = 2;
	}
	
	public void setBMContent(String sfile) {
		
		String content = "";
		SharedPreferences prefs = mActivity.getSharedPreferences("BookMark", Context.MODE_PRIVATE);
		Map<String, ?> allEntries = prefs.getAll();
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
		    //Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
			content += entry.getKey()+"\n";
		}
		if (content=="") {
			//addBookMark("Test mark", 3, 200);
		}
		if (content!=null) {
			if (mlv.getVisibility()==View.VISIBLE)
				mlv.setVisibility(View.INVISIBLE);
		mbmv.setVisibility(View.VISIBLE);
		mbmv.setContentDescription("Table Of Bookmarks");
		TextView mtemptv = new TextView(mbmv.getContext());
		//mtemptv.setTextAppearance(mContext, android.R.id.text1);
		if (mLang!=Lang.DEFAULT)
			mtemptv.setText(R.string.bookmark_title);
		else
			mtemptv.setText("Table Of Bookmarks");
		
		if (mbmv.getHeaderViewsCount()==0)
			mbmv.addHeaderView(mtemptv, "BOOKMARK", false);
		
		if (mtv instanceof TextView) {
			((TextView)mtv).setText("");
			((TextView)mtv).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);	
		}
		if (themeid == theme.NIGHT) {
			mbmv.setDivider(new ColorDrawable(Color.WHITE));
			mbmv.setDividerHeight(2);
			mbmv.setAdapter(new ArrayAdapter<String>(mlv.getContext(),R.layout.list_item2,
    			android.R.id.text1, content.split("\n")));
		}
		else {
			mbmv.setDivider(new ColorDrawable(Color.GRAY));
			mbmv.setDividerHeight(2);
			mbmv.setAdapter(new ArrayAdapter<String>(mlv.getContext(),R.layout.list_item,
        			android.R.id.text1, content.split("\n")));
		}
		
			

		mbmv.setOnItemClickListener(new OnItemClickListener() { 
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				String[] value=null;
				//Toast.makeText(v.getContext(), "List Item "+position, Toast.LENGTH_SHORT).show();
				SharedPreferences prefs = v.getContext().getSharedPreferences("BookMark", Context.MODE_PRIVATE);
				Map<String, ?> allEntries = prefs.getAll();
				int i=0;
				for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
				    i++;
					//Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
				    if (i==position) {
				    	value = entry.getValue().toString().split(":");
				    	break;
				    }
					
				}
				if (value!=null && value.length >= 2) {
					int chap = 0;
					int vpos = 0;

					try {
					    chap = Integer.parseInt(value[0]);
					    vpos = Integer.parseInt(value[1]);
					} catch(NumberFormatException nfe) {
					  // Handle parse error.
					}
					handleEvent(navEvent.BMSELECT, chap, vpos, 0, 0);
				}
			}
		});
		}
		curViewId = 2;
		
	}
	public void setMaxLines()
	{
		if (mtv instanceof TextView) {
			int lines=0;
			
		try {
			lines = computeMaxLines();
			
			//Toast.makeText(mtv.getContext(), "Lines: "+lines+" "+((TextView)mtv).getLineHeight()+"-"+((TextView)mtv).getTotalPaddingBottom()+":"+((TextView)mtv).getPaddingBottom(), Toast.LENGTH_SHORT).show();
			if (lines > 0) {
				((TextView)mtv).setMaxLines(lines);
				maxlines = lines;
			}
			int bline = ((TextView)mtv).getLayout().getLineForVertical(mtv.getScrollY()+getContentHeight());
			
			/*if ( ((TextView)mtv).getLayout().getLineBottom(bline) > mtv.getScrollY()+getContentHeight() ) {
				//((TextView)mtv).setMaxLines(lines-1);
				maxlines = lines-1;
			}
			else*/ if ((((TextView)mtv).getTotalPaddingBottom()-((TextView)mtv).getPaddingBottom()) > ((TextView)mtv).getLineHeight()) {
				lines += ((((TextView)mtv).getTotalPaddingBottom()-((TextView)mtv).getPaddingBottom()-((TextView)mtv).getLineHeight()-1) / ((TextView)mtv).getLineHeight());
				//Toast.makeText(mtv.getContext(), "Lines: "+lines+" "+((TextView)mtv).getLineHeight()+"-"+((TextView)mtv).getTotalPaddingBottom()+":"+((TextView)mtv).getPaddingBottom(), Toast.LENGTH_SHORT).show();
				((TextView)mtv).setMaxLines(lines);
				maxlines = lines;
			}
		} catch (Exception e) {	}
		}
	}
	public void setContent(String sfile, int vpos) {
		String content = readContent(sfile);
		Content cType = getContentType(sfile);
		int chapn = bn.getCurPos()-bn.getChapPos();
		if (chapn > 0) {
			mtitlev.setText(R.string.chapter);
			mtitlev.setText(mtitlev.getText().toString()+chapn);
		}
		if (content != null) {
			if (mlv.getVisibility()==View.VISIBLE)
				mlv.setVisibility(View.INVISIBLE);
			if (mbmv.getVisibility()==View.VISIBLE)
				mbmv.setVisibility(View.INVISIBLE);
			if (mtv instanceof TextView) {
				
				//((TextView)mtv).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				if (cType==Content.HTML)
					((TextView)mtv).setText(Html.fromHtml( content ));
				else
					((TextView)mtv).setText(content);
				
			}
			else if (mtv instanceof WebView) {
				//((WebView)mtv).loadUrl("file:///android_asset/"+sfile);
            	//((WebView)mtv).bringToFront();
            	//((WebView)mtv).loadData(content, "text/html", "utf-8");
				((WebView)mtv).loadDataWithBaseURL("file:///android_asset/"+sfile, content, "text/html", "UTF-8", "");
			}
			if (vpos==0) vpos += getTopVPos();
			mtv.scrollTo(0, vpos);
			if (mtv.getVisibility()!=View.VISIBLE)
				mtv.setVisibility(View.VISIBLE);
			//mtv.bringToFront();
			curViewId = 3;
			setMaxLines();
		}
	}

	public boolean mchangeDisplayContent(navAction na, int srcId, Object data) {
		
		switch (na) {
		case NEXTPAGE:
			if (getVPosition()+getVisibleHeight() < getLayoutEnd()) {
				//Toast.makeText(mtv.getContext(), "VPos "+getVPosition()+"Ht "+getContentHeight(), Toast.LENGTH_SHORT).show();
				moveVPosition(0, getContentHeight());
				return true;
			}
			break;
		case PREVPAGE:
			if (getVPosition() > getContentHeight()) {
        		moveVPosition(0, -getContentHeight());
        		return true;
			}
        	else if (getVPosition() > 0) {
        		moveVPosition(0, -getVPosition()+getTopVPos());
        		return true;
        	}
        	else {}
			break;
		case LASTPAGE:
			if (getVPosition()+getVisibleHeight() < getLayoutEnd()) {
				
				int pages = ((getLayoutEnd()-getTopVPos())/getContentHeight());
				if ((getLayoutEnd()-getTopVPos())%getContentHeight() > getLineHeight()/2) pages++;
				//Toast.makeText(mtv.getContext(), "TVPos "+getLayoutEnd()+"Ht "+getContentHeight()+"pages:"+pages, Toast.LENGTH_SHORT).show();
				if (pages > 0)
					moveVPosition(0,  (pages-1)*getContentHeight());
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	public void mloadDisplayContent(String sfile, item_type type, int pos) {
		curContentType = type;
		curContentSrc = sfile;
		//Toast.makeText(mtv.getContext(), "loadDisplayContent ", Toast.LENGTH_SHORT).show();
		((TextView)mtv).setBackgroundResource(0);
		mtitlev.setText(R.string.title_name);
		if (type == item_type.FCOVER) {
			setImgContent(sfile, R.drawable.fcover);
			//Toast.makeText(mtv.getContext(), "loadDisplayCover ", Toast.LENGTH_LONG).show();
		}
		else if (type == item_type.BCOVER) {
			setImgContent(sfile, R.drawable.bcover);
		}
		else if (type == item_type.TOC) {
			setTOCContent(sfile);
		}
		else if (type == item_type.BOOKMARK) {
			setBMContent(sfile);
		}
		else {
			setContent(sfile, pos);
		}
	}
	public boolean handleEvent(navEvent nEvent, int x1, int y1, int x2, int y2) {
		
		if (nEvent==navEvent.TOCSELECT) {
			bn.handleNavigation(nEvent, x1, curViewId, null);
			return true;
		}
		else if (nEvent==navEvent.BMSELECT) {
			bn.handleNavigation(nEvent, x1, y1, null);
			return true;
		}
		else 
		{
			int dx, dy;
			//Toast.makeText(mtv.getContext(), "M "+curContentType+"/"+getLayoutWidth()+":"+getLayoutHeight(), Toast.LENGTH_LONG).show();
			dx = x2-x1;
			dy = y2-y1;
			if(Math.abs(dx) < 2 && Math.abs(dy) < 2) { 	// Click Event
				if (x2 <= getVisibleWidth()*0.2) { 		//Left
					nEvent = navEvent.LEFT;
				}
				else if (x2 >= getVisibleWidth()*0.8) { //Right
					nEvent = navEvent.RIGHT;
				}
				else if (y1 <= getVisibleHeight()*0.2) { //Up
					nEvent = navEvent.UP;
				}
				else if (y2 >= getVisibleHeight()*0.8) { //Down
					nEvent = navEvent.DOWN;
				} else { 
					nEvent = navEvent.MIDDLE;
					mOnScreenChangeListener.applyScreenEvent();
					//Toast.makeText(mtv.getContext(), "M "+getVisibleWidth()+":"+getVisibleHeight()+"/"+getLayoutWidth()+":"+getLayoutHeight(), Toast.LENGTH_LONG).show();
				}
				bn.handleNavigation(nEvent, x1, curViewId, null);
				if (nEvent != navEvent.DOWN) return true;
			}
		}
		return false;
	}
	public Content getContentType(String sfile) {
		if (sfile.endsWith(".html") || sfile.endsWith(".HTML")) return Content.HTML;
		else /*if (sfile.endsWith(".txt") || sfile.endsWith(".text"))*/ return Content.TXT;
	}
	public String readContent(String sfile) {
   		if (sfile==null) return null;
		BufferedReader reader=null;
		try {
			//InputStream in = mContext.getAssets().open(sfile);
			//reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open(sfile), "UTF-8"));
		} catch (IOException ex) {
			return null;
		}
		String content="";
        try {
        	String line;
        	line = reader.readLine();
        	if (line!=null) {	
        		content=line;
        		while ((line = reader.readLine()) != null){
        			content+= "\n"+line;
        		}
        	}
        } catch (IOException e) {
    		//e.printStackTrace();
    	} finally {
    		if (reader != null) {
    			try {
    				reader.close();
    			} catch (IOException e) {
    				
    			}
    		}
    	}
        return content;
	}
	
	public void changeTheme(change_type change, theme mode) {
		theme_changed = true;
		themeid = mode;
		if (mtv instanceof TextView) {
			if (mode == theme.DAY) {
				((TextView)mtv).setTextColor(Color.BLACK);
				//((TextView)mtv).setBackgroundColor(Color.TRANSPARENT);
			}
			else {
				((TextView)mtv).setTextColor(Color.WHITE);
				//((TextView)mtv).setBackgroundColor(Color.BLACK);
			}
			((TextView)mtv).refreshDrawableState();
			//mlv.refreshDrawableState();
		}
	}
	
	/**Changes the font size of the content.
	 * @param fontSize Intended font size.
	 */
	public void changeFontSizeInPx(Float fontSize){
		if (mtv instanceof TextView) {
			int lines=0;
			
			if (maxlines > 0 && fontsz > 0) lines = (int) (maxlines*fontsz/fontSize);
			Toast.makeText(mtv.getContext(), "oSZ "+maxlines+"nSZ "+lines, Toast.LENGTH_SHORT).show();
			if (lines > 0) ((TextView)mtv).setMaxLines(lines);
			((TextView)mtv).setTextSize(fontSize);
			//if (lines > 0) ((TextView)mtv).setMaxLines(lines);
			
			setMaxLines();
			fontsz = fontSize;
		}
	}

	/**Gives content font size.
	 * @param fontSize Content font size.
	 */
	public float getContentFontSize(){
		return PixelConverter.getScaleIndependentPixelFromPixelValue(mContext, ((TextView)mtv).getTextSize());
	}
	
	
	public void setOnScreenChangeListener(
			OnScreenChangeListener listener) {
		if (listener == null) {
			listener = sDummyListener;
		}

		mOnScreenChangeListener = listener;
	}
	private static OnScreenChangeListener sDummyListener = new OnScreenChangeListener() {
		@Override
		public boolean applyScreenEvent() {
			return true;
		}
	};
	public interface OnScreenChangeListener {
		boolean applyScreenEvent();
	}
	
	public void addBookMark(String name, int chap, int vpos) {
		SharedPreferences prefs = mActivity.getSharedPreferences("BookMark", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		String value = String.valueOf(chap)+":"+String.valueOf(vpos);
		editor.putString(name, value);
		editor.commit();
	}
	public void addBookMark(String name)
	{
		if (bn.getCurPos() >= bn.getChapPos())
			addBookMark(name, bn.getCurPos()-bn.getChapPos(), getVPosition());
	}
}
