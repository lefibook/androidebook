package com.example.bookreading;

import com.example.bookreading.BookHolder.item_type;


public class BookNavigator {

	protected BookHolder book=null;
	
	enum navEvent {UP, DOWN, LEFT, RIGHT, SCROLL, SWIPE, MIDDLE, TOCSELECT, BMSELECT, LONGPRESS, CLICK};
	enum navAction {PREVPAGE, LASTPAGE, NEXTPAGE, NEXTCONTENT, PREVCONTENT, TOCTARGET, BMTARGET, TOC, BM, SETTINGS, NONE};
	
	public OnNavigationChangeListener mOnNavigationChangeListener = sDummyListener;
	
	public BookNavigator() {
		book = new BookHolder();
		book.setBookHolder();
	}
	public int getCurPos() { return book.getCurItem(); }
	public void loadStart() { loadItem(); }
	public void loadStart(int id) { loadItem(getTOCPos(), 0); }
	public void loadItem() {
		loadItem(0);
	}
	public void loadItem(int vpos) {
		loadContent( book.getItemSrc(), book.getItemType(), vpos);
	}
	public int getTOCPos() {
		for(int i=book.getStartItem(); i <= book.getLastItem(); i++) {
			if (book.getItemType(i) == item_type.TOC) return i;
		}
		return -1;
	}
	public int getBMPos() {
		for(int i=book.getStartItem(); i <= book.getLastItem(); i++) {
			if (book.getItemType(i) == item_type.BOOKMARK) return i;
		}
		return -1;
	}
	public int getChapPos() {
		for(int i=book.getStartItem(); i <= book.getLastItem(); i++) {
			if (book.getItemType(i) == item_type.CHAPTER) return i;
		}
		return -1;
	}
	public void loadRelItem(int pos) {
		if (book.getCurItem()+pos < book.getMaxItem()) {
			book.setCurItem(book.getCurItem()+pos);
			loadItem();
		}
	}
	public void loadItem(int pos, int vpos) {
		if (pos < book.getMaxItem()) {
			book.setCurItem(pos);
			loadItem(vpos);
		}
	}
	/* Target TOC selected: selected index starts from 1 */
	public void loadTOCItem(int pos) {
		//pos += getTOCPos();
		if (pos==1) pos=0;
		if (book.getItemType(pos)==item_type.TOC) pos++;
		if (pos < book.getMaxItem()) {
			book.setCurItem(pos);
			loadItem();
		}
	}
	/* Target Bookmark selected selected index starts from 1 */
	public void loadBMItem(int pos, int scroll) {
		pos += getChapPos();
		if (pos < book.getMaxItem() && pos >= 0) {
			book.setCurItem(pos);
			loadItem(scroll);
		}
	}
	/* TOC Table Page itself */
	public void loadTOCItem() {
		int pos=getTOCPos();
		if (pos < book.getMaxItem() && pos >= 0) {
			book.setCurItem(pos);
			loadItem();
		}
	}
	/* BookMark Table itself */
	public void loadBMItem() {
		int pos=getBMPos();
		if (pos < book.getMaxItem() && pos >= 0) {
			book.setCurItem(pos);
			loadItem();
		}
	}

	public boolean loadNextItem() {
		if (book.nextValid()) {
			book.setCurItem(book.getCurItem()+1);
			loadItem();
			return true;
		}
		return false;
	}
	public boolean loadPrevItem() {
		if (book.prevValid()) {
			book.setCurItem(book.getCurItem()-1);
			loadItem();
			return true;
		}
		return false;
	}
	public void loadContent(String sfile, item_type type, int pos) {

		switch(type) {
		case FCOVER:
			
			break;
		case BCOVER:
			
			break;
		case TOC:

			break;
		default:

		}
		mOnNavigationChangeListener.loadDisplayContent(sfile, type, pos);
	}
	public static navAction resolveEvent(navEvent nEvent) {
		switch(nEvent) {
		
		case UP:
			return navAction.PREVPAGE;
		case DOWN:
			return navAction.NEXTPAGE;
		case LEFT:
			return navAction.PREVCONTENT;
		case RIGHT:
			return navAction.NEXTCONTENT;
		case MIDDLE:
			return navAction.SETTINGS;
		case LONGPRESS:
			return navAction.SETTINGS;
		case TOCSELECT:
			return navAction.TOCTARGET;
		case BMSELECT:
			return navAction.BMTARGET;
		default:
			return navAction.NONE;
		}
		
	}
	public void handleNavigation(navAction nAction, int info, int srcId, Object data) {
	
		switch(nAction) {
		
		case PREVPAGE:
			if (mOnNavigationChangeListener.changeDisplayContent(navAction.PREVPAGE, srcId, data)) {
			}
			else {
				if (loadPrevItem()) {
					mOnNavigationChangeListener.changeDisplayContent(navAction.LASTPAGE, srcId, data);
				}
			}
			break;
		case NEXTPAGE:
			if (mOnNavigationChangeListener.changeDisplayContent(navAction.NEXTPAGE, srcId, data)) {
			}
			else {
				loadNextItem();
			}
			break;
		case PREVCONTENT:
			loadPrevItem();
			break;
		case NEXTCONTENT:
			loadNextItem();
			break;
		case TOCTARGET:
			loadTOCItem(info);
			break;
		case BMTARGET:
			loadBMItem(info, srcId);
			break;
		case BM:
			loadBMItem();
			break;
		case TOC:
			loadTOCItem();
			break;
		default:
			
		}
	}
	public void handleNavigation(navEvent nEvent, int info, int srcId, Object data) {
		navAction nAction;
		nAction = resolveEvent(nEvent);
		handleNavigation(nAction, info, srcId, data);
	}
	public void setOnNavigationChangeListener(
			OnNavigationChangeListener listener) {
		if (listener == null) {
			listener = sDummyListener;
		}

		mOnNavigationChangeListener = listener;
	}
	private static OnNavigationChangeListener sDummyListener = new OnNavigationChangeListener() {
		@Override
		public boolean changeDisplayContent(navAction na, int srcId, Object data) {
			return true;
		}
		@Override
		public void loadDisplayContent(String sfile, item_type type, int pos) {}
	};	
	public interface OnNavigationChangeListener {
		boolean changeDisplayContent(navAction na, int srcId, Object data);
		void loadDisplayContent(String sfile, item_type type, int pos);
	}
}
