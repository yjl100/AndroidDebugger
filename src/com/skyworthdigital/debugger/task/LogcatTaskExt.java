package com.skyworthdigital.debugger.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.skyworthdigital.debugger.LogcatService.LogcatTaskListener;
import com.skyworthdigital.debugger.listener.IConsolListener;
import com.skyworthdigital.debugger.util.ProcessUtil;

import android.util.Log;
/**
 * 抓取logcat的task
 * @author yangjialin
 * 2017年10月18日下午3:21:59
 */
public class LogcatTaskExt extends Thread {

	public static final String TAG = "LogcatTaskExt";
	private boolean mRunning = false;
	private boolean mExit = false;
	public final static String CMD_CLEAR_LOG = "logcat -c";
	public final static String CMD_LOGCAT = "logcat";
	public final static String CMD_LOGCAT_WITH_TIME = "logcat -vtime";
	private int mTotalPage = 0;
	private int mTotalLines = 0;
	private int mOnePageNumLimite = 40;
	final String NEW_LINE = "\n";
	ArrayList<String> clearLog;
	String mLogPath = "";
	String mLogName = "";
	File logFile;
	int logCount = 0;
	private FileOutputStream mFos;
	private IConsolListener mListener;
	private LogcatType mType = LogcatType.LOGCAT_AFTER_CLRER;
	Process mProcess = null;

	public enum LogcatType {
		LOGCAT, LOGCAT_AFTER_CLRER
	};

	public LogcatTaskExt(LogcatType type) {
		// TODO Auto-generated constructor stub
		mType = type;
	}

	public synchronized void setLogcatListener(IConsolListener listener) {
		mListener = listener;
	}

	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		mRunning = true;
		super.start();
	}

	public synchronized void startGet() {
		mRunning = true;
		interrupt();
	}

	public synchronized void stopGet() {
		mRunning = false;
		try {
			Runtime.getRuntime().exec("sync");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProcessUtil.killProcess(mProcess);
		mProcess = null;
		// interrupt();
	}

	public synchronized void exit() {
		mRunning = false;
		mExit = true;
		interrupt();
		try {
			Runtime.getRuntime().exec("sync");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProcessUtil.killProcess(mProcess);
		mProcess = null;
	}

	public void setSavePath(String path) {
		mLogPath = path;
	}

	public String getSavingFile() {
		return mLogPath + File.separator + mLogName;
	}

	public boolean isRunning() {
		return mRunning;
	}

	private void initFileOutputStream() {
		mLogName = "logcat-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis()))
				+ ".txt";
		Log.d(TAG, "Save:" + mLogPath + File.separator + mLogName);
		File logFile = new File(mLogPath + File.separator + mLogName);// 打开文件
		mTotalPage = 0;
		try {
			mFos = new FileOutputStream(logFile, true);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.d(TAG, "LogTaskExt run thread id : " + this.getId() + " mType:" + mType);

		while (!mExit) {
			try {
				if (!mRunning) {
					try {
						Log.d(TAG, "runCmd sleep 1s");
						Thread.sleep(1000);
						continue;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
				}
				if (mListener != null) {
					mListener.onOneLinePrint(mListener, "runCmd initFileOutputStream!");
				}
				initFileOutputStream();
				// clear log if necessary
				if (mType == LogcatType.LOGCAT_AFTER_CLRER) {
					Runtime.getRuntime().exec(CMD_CLEAR_LOG);
					if (mListener != null) {
						mListener.onOneLinePrint(mListener, "---runCmd clear logcat!");
					}
				}
				// run logcat -vtime
				if (mListener != null) {
					mListener.onOneLinePrint(mListener,
							"---runCmd logcat save path : " + mLogPath + File.separator + mLogName);
				}
				mProcess = Runtime.getRuntime().exec(CMD_LOGCAT_WITH_TIME);
				String line;
				InputStreamReader isr = new InputStreamReader(mProcess.getInputStream());
				BufferedReader buffRead = new BufferedReader(isr);

				Log.d(TAG, "process : " + mProcess.toString() + " getInputStream:" + mProcess.getInputStream());
				int totalLines = 0;
				int totalPage = 0;

				if (mProcess.getInputStream() != null) {
					// read line and write line
					while ((line = buffRead.readLine()) != null && mRunning) {
						if (mListener != null) {
							mListener.onOneLinePrint(mListener, line);
						}

						// calculate page change
						totalLines++;
						totalPage = totalLines / mOnePageNumLimite + 1;
						if (totalPage > mTotalPage) {
							if (mListener != null) {
								mListener.onPageChangeNotify(totalPage);
							}
							mTotalPage = totalPage;
						}

						mFos.write((line).getBytes());
						mFos.write(NEW_LINE.getBytes());
					}
					isr.close();
					buffRead.close();
					Log.d(TAG, "---runCmd read InputStream ok----");
					if (mListener != null) {
						mListener.onOneLinePrint(mListener, "runCmd read InputStream ok!");
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG, "---runCmd close start");
			try {
				if (mFos != null) {
					mFos.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mFos = null;
			ProcessUtil.killProcess(mProcess);
			mProcess = null;
			Log.d(TAG, "---runCmd close end");
			if (mListener != null) {
				mListener.onOneLinePrint(mListener, "runCmd all process closed!");
			}
		}
		try {
			if (mFos != null) {
				mFos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mListener.onOneLinePrint(mListener, "---runCmd cmd error : " + e.getMessage());
		}
		mFos = null;
		ProcessUtil.killProcess(mProcess);
		mProcess = null;
		if (mListener != null) {
			mListener.onOneLinePrint(mListener, "runCmd all process closed, thread exit!");
		}
		Log.d(TAG, "LogTaskExt thread end...");
	}
}
