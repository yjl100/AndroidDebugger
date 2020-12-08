package com.skyworthdigital.debugger;

import com.skyworthdigital.debugger.aidl.ILogcatCallback;
import com.skyworthdigital.debugger.listener.IConsolListener;
import com.skyworthdigital.debugger.model.ConsolType;
import com.skyworthdigital.debugger.task.LogcatTask;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class LogcatService extends Service {

	public static final String TAG = "LogcatService";
	private LogcatTask mTask;
	private ILogcatCallStub mLogcatCall;
	public final RemoteCallbackList <ILogcatCallback> mRCallbacks = new RemoteCallbackList <ILogcatCallback>();
	private LogcatTaskListener mLogcatListener;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "LogcatService onBind intent:"+intent);
		mLogcatListener = new LogcatTaskListener();
		mHandler.sendEmptyMessageDelayed(1, 3000);
		return mLogcatCall;
	}
	
	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onRebind intent:"+intent);
		mHandler.sendEmptyMessageDelayed(1, 3000);
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
//		mLogcatListener = null;
		Log.d(TAG, "onUnbind intent"+intent);
		if (mTask != null) {
			mTask.stopGet();
		}
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(TAG, "LogcatService onCreate...");
//		mHandler.sendEmptyMessageDelayed(1, 3000);
		mLogcatCall = new ILogcatCallStub();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "LogcatService onDestroy...");
		if (mTask != null) {
			mTask.exit();
			mTask = null;
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Log.d(TAG, "---handleMessage msg.what:" + msg.what);
			if (msg.what == 1) {
				if (mTask == null) {
					mTask = new LogcatTask();
					mTask.setLogcatListener(mLogcatListener);
					mTask.start();
				} else {
					mTask.startGet();
				}
//				mHandler.sendEmptyMessageDelayed(2, 30 * 1000);
			} else if (msg.what == 2) {
				if (mTask!=null) {
					mTask.stopGet();
				}
			}
		}
	};

	public class LogcatTaskListener implements IConsolListener {

		@Override
		public void onOneLinePrint(IConsolListener lisener, String line) {
			// TODO Auto-generated method stub
			final int N = mRCallbacks.beginBroadcast();  
//			System.out.println("onOneLinePrint N:"+N+" line:"+line);
			for (int i = 0; i < N; i++) {
	            try {  
	            	mRCallbacks.getBroadcastItem(i).onOneLinePrint(line);   
	            }  
	            catch (RemoteException e) {      
	            }  
	        }  
	        mRCallbacks.finishBroadcast();  
		}

		@Override
		public void onPageChangeNotify(int page) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProcessComplete(IConsolListener lisener, ConsolType type) {
			// TODO Auto-generated method stub
			
		}

	}

	public class ILogcatCallStub extends com.skyworthdigital.debugger.aidl.ILogcatService.Stub {

		@Override
		public void registerCallback(ILogcatCallback callback) throws RemoteException {
			// TODO Auto-generated method stub
			System.out.println("registerCallback callback:"+callback);
			mRCallbacks.register(callback);
		}

		@Override
		public void unregisterCallback(ILogcatCallback callback) throws RemoteException {
			// TODO Auto-generated method stub
			mRCallbacks.unregister(callback);
		}

	}

}
