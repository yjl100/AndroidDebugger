package com.skyworthdigital.debugger.listener;

public interface ILogcatReaderListener {
	void notifyRefresh(ILogcatReaderListener lisener, String str, int page);
	void onPageNotify(int page);
}
