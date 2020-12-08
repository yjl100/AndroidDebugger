package com.skyworthdigital.debugger.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.skyworthdigital.debugger.listener.IConsolListener;
import com.skyworthdigital.debugger.model.ConsolType;
import com.skyworthdigital.debugger.util.ProcessUtil;

import android.util.Log;

public class ManualCmdTask extends Thread {
	public static final String TAG = "ManualCmdTask";
	private boolean mRunning = false;
	private boolean mExit = false;
	private IConsolListener mListener;
	private String mCmd;
	private boolean mReadInput;
	Process mProcess;
	private boolean mIsShellCmd = false;

	public ManualCmdTask(boolean readInput) {
		// TODO Auto-generated constructor stub
		Log.d(TAG, "new  ManualCmdTask readInput:" + readInput);
		mReadInput = readInput;
	}

	public synchronized void setLogcatListener(IConsolListener listener) {
		mListener = listener;
	}

	public synchronized void setCmd(String cmd, boolean isShell) {
		mCmd = cmd;
		mIsShellCmd = isShell;
	}

	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		if (mCmd == null || mCmd.isEmpty()) {
			Log.d(TAG, "Commond null, please input commond!");
			return;
		}
		mRunning = true;
		super.start();
	}

	public synchronized void exit() {
		mRunning = false;
		try {
			Runtime.getRuntime().exec("sync");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!mReadInput) {
			mListener.onProcessComplete(mListener, ConsolType.ManualCmd);
			ProcessUtil.killProcess(mProcess);
		}
		System.out.println("runCmd exit...");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		List<String> cmds = new ArrayList<String>();
		ProcessBuilder pb = null;
		if (mIsShellCmd) {
			cmds.add("sh");
			cmds.add("-c");
			cmds.add(mCmd);
			pb = new ProcessBuilder(cmds);
		}

		try {
			Log.d(TAG, "runCmd start.");
			String str;
			if (mListener != null) {
				mListener.onOneLinePrint(mListener, "---runCmd : " + (mIsShellCmd ? " sh -c " : "") + mCmd);
			}
			if (mIsShellCmd) {
				mProcess = pb.start();
			} else {
				mProcess = Runtime.getRuntime().exec(mCmd);
			}
			Log.d(TAG, "runCmd after..processId:" + ProcessUtil.getProcessId(mProcess.toString()) + " mCmd : "
					+ (mIsShellCmd ? "sh -c " : "") + mCmd);

			if (!mReadInput) {
				Log.d(TAG, "runCmd not ReadInput return!");
				if (mListener != null) {
					mListener.onOneLinePrint(mListener, "Wait press stop...");
				}
				return;
			}

			InputStreamReader isr = new InputStreamReader(mProcess.getInputStream());
			BufferedReader buffRead = new BufferedReader(isr);// 获取输入流
			Log.d(TAG, "runCmd after..getInputStream:" + mProcess.getErrorStream());

			if (mProcess.getInputStream() != null) {
				while ((str = buffRead.readLine()) != null && mRunning) {
					System.out.println("---line:" + str);
					if (mListener != null) {
						mListener.onOneLinePrint(mListener, str);
					}
				}
				isr.close();
				buffRead.close();
			} else {
				System.out.println("---runCmd getErrgetInputStream null");
			}
			Log.d(TAG, "runCmd end...");

			if (mProcess.getErrorStream() != null) {
				InputStreamReader isre = new InputStreamReader(mProcess.getErrorStream());
				BufferedReader buffe = new BufferedReader(isre);// 获取输入流
				while ((str = buffe.readLine()) != null && mRunning) {
					System.out.println("---error:" + str);
					if (mListener != null) {
						mListener.onOneLinePrint(mListener, str);
					}
				}
				isre.close();
				buffe.close();
				// InputStream error = process.getErrorStream();
				// for (int i = 0; i < error.available(); i++) {
				// System.out.println("---error:" + error.read());
				// }
			} else {
				System.out.println("---error getErrorStream null");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("---error getErrorStream Exception");
			e.printStackTrace();
			mListener.onOneLinePrint(mListener, "---runCmd cmd error : " + e.getMessage());
		} finally {
			if (mReadInput) {
				ProcessUtil.killProcess(mProcess);
				mRunning = false;
				mListener.onProcessComplete(mListener, ConsolType.ManualCmd);
				System.out.println("runCmd kill process end...");
			}
		}
		System.out.println("runCmd end...");
	}
}
