package com.hnyer.bean;


public class SortModel extends PeopleStartBean {


	public SortModel(String name, String number, String sortKey) {
		super(name, number, sortKey);
	}

	public String sortLetters; //显示数据拼音的首字母
	public SortToken sortToken=new SortToken();
}
