package com.example.bookreading;

import java.util.ArrayList;
import java.util.List;

public class BookHolder {
	
	enum item_type {FCOVER, BOOKMARK, TOC, CHAPTER, INDEX, BCOVER, END};
	int curItem=0;
	//
	List<BookItem> bookholder = new ArrayList<BookItem>();

	public int setBookHolder() {
		
		bookholder.add(new BookItem("toc.html",item_type.FCOVER));
		bookholder.add(new BookItem("toc.html",item_type.TOC));
		bookholder.add(new BookItem("toc.html",item_type.BOOKMARK));
		bookholder.add(new BookItem("about.txt"));
		bookholder.add(new BookItem("intro.html"));
		bookholder.add(new BookItem("ch1.html"));
		bookholder.add(new BookItem("ch2.html"));
		bookholder.add(new BookItem("ch3.html"));
		bookholder.add(new BookItem("ch4.html"));
		bookholder.add(new BookItem("toc.html",item_type.BCOVER));
		
		return 1;
	}
	
	public int getCurItem() {
		return curItem;
	}
	public int getStartItem() {
		return 0;
	}
	public int getLastItem() {
		return bookholder.size()-1;
	}
	public int getMaxItem() {
		return bookholder.size();
	}
	public BookItem getItem(int item) {
		return bookholder.get(item);
	}
	public boolean nextValid() {
		return (getCurItem()+1 < getMaxItem());
	}
	public boolean prevValid() {
		return (getCurItem()-1 >= getStartItem());
	}
	public boolean nextValid(int item) {
		return (item+1 < getMaxItem());
	}
	public boolean prevValid(int item) {
		return (item-1 >= getStartItem());
	}
	public String getItemSrc(int item) {
		return bookholder.get(item).getSrc();
	}
	public String getItemSrc() {
		return bookholder.get(getCurItem()).getSrc();
	}
	public item_type getItemType(int item) {
		return bookholder.get(item).getType();
	}
	public item_type getItemType() {
		return bookholder.get(getCurItem()).getType();
	}

	public String getNextItemSrc() {
		if (getCurItem()+1 < getMaxItem())
			return bookholder.get(getCurItem()+1).getSrc();
		return null;
	}
	public String getPrevItemSrc() {
		if (getCurItem() > 0)
			return bookholder.get(getCurItem()-1).getSrc();
		return null;
	}
	public void setCurItem(int item) {
		curItem=item;
	}

	public static class BookItem {
		String midref;
		item_type mtype;
		public BookItem(String title, item_type type) {
			midref=title;
			mtype=type;
		}
		public BookItem(String title) {
			midref=title;
			mtype=item_type.CHAPTER;
		}
		public void setMainCover(String title) {
			midref=title;
			mtype = item_type.FCOVER;
		}
		public void setBackCover(String title) {
			midref=title;
			mtype = item_type.BCOVER;
		}
		public void setTOC(String title) {
			midref=title;
			mtype = item_type.TOC;
		}
		public void setChapter(String title) {
			midref=title;
			mtype = item_type.CHAPTER;
		}
		public String getSrc() {
			return midref;
		}
		public item_type getType() {
			return mtype;
		}
	}
	
	
	
}
