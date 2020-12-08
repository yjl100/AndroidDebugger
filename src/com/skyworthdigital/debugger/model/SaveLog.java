package com.skyworthdigital.debugger.model;

public class SaveLog implements Cloneable {
	public String path;
	public String pathDescribe;
	public int index;

	@Override
	protected Object clone() {
		// TODO Auto-generated method stub
		SaveLog sl = null;
		try {
			sl = (SaveLog) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sl;
	}
}
