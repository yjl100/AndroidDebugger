package com.skyworthdigital.debugger.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.skyworthdigital.debugger.LogcatService.LogcatTaskListener;

public class LogcatTask extends Thread {

	private boolean mRunning = false;
	private boolean mExit = false;
	ArrayList<String> getLog;
	ArrayList<String> clearLog;
	String logPath = "/data/testlog.txt";
	File logFile;
	int logCount = 0;
	private FileOutputStream fos;
	private LogcatTaskListener mListener;

	public LogcatTask() {
		// TODO Auto-generated constructor stub
		getLog = new ArrayList<String>();
		clearLog = new ArrayList<String>();
		getLog.add("logcat");
		getLog.add("-d");
		getLog.add("-v");
		getLog.add("time");
		clearLog.add("logcat");
		clearLog.add("-c");
	}
	
	public synchronized void setLogcatListener(LogcatTaskListener listener) {
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
		interrupt();
	}
	
	public synchronized void exit() {
		mRunning = false;
		mExit = true;
		interrupt();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("LogTask run thread id :"+this.getId());
		while (!mExit) {
			if (mRunning) {
				try {
					Process process = Runtime.getRuntime().exec(getLog.toArray(new String[getLog.size()]));
					BufferedReader buffRead = new BufferedReader(new InputStreamReader(process.getInputStream()));// ��ȡ������
					Runtime.getRuntime().exec(clearLog.toArray(new String[clearLog.size()]));// �����Ϊ���´�ץȡ�����ͷץȡ
					String str = null;
					logFile = new File(logPath);// ���ļ�
					fos = new FileOutputStream(logFile, true);// true��ʾ��д��ʱ�����ļ�ĩβ׷��
					String newline = "\n";// System.getProperty("line.separator");//���е��ַ���
					while ((str = buffRead.readLine()) != null) {// ѭ����ȡÿһ��
						SimpleDateFormat format = new SimpleDateFormat("yyyy-");
						Date date = new Date(System.currentTimeMillis());
						String time = format.format(date);
						fos.write((time + str).getBytes());// ������
						fos.write(newline.getBytes());// ����
						logCount++;
						mListener.onOneLinePrint(mListener, time + str + newline);
						if (logCount > 10000) {// ����10000�о��˳�
							mRunning = false;
							// captureLogThread = null;
							fos.close();
							break;
						}
					}
					fos.close();
					fos = null;
					Runtime.getRuntime().exec(clearLog.toArray(new String[clearLog.size()]));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // ץȡ��ǰ�Ļ�����־
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		System.out.println("LogTask thread end...");
	}
}
