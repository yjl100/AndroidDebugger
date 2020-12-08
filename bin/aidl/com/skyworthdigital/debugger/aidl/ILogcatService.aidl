package com.skyworthdigital.debugger.aidl;

import com.skyworthdigital.debugger.aidl.ILogcatCallback;

interface ILogcatService
{
	 void registerCallback(ILogcatCallback callback);
	 void unregisterCallback(ILogcatCallback callback);
}