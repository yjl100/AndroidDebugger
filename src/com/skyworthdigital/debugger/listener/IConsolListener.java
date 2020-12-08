package com.skyworthdigital.debugger.listener;

import com.skyworthdigital.debugger.model.ConsolType;

public interface IConsolListener {

	void onOneLinePrint(IConsolListener lisener, String line);
	void onPageChangeNotify(int page);
	void onProcessComplete(IConsolListener lisener, ConsolType type);
}
