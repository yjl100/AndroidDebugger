package com.skyworthdigital.debugger.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Timer;
import java.util.TimerTask;

import com.skyworthdigital.debugger.listener.ILogcatReaderListener;

import android.util.Log;
import android.util.SparseArray;

/**
 * 对logcat日志进行浏览，采用内存映射浏览，增强对超大日志浏览能力
 * 
 * @author yangjialin 2017年10月18日下午3:21:59
 */
public class LogcatFileReader {

	private static final String TAG = "LogFileReader";

	private File mReadFile;
	private FileInputStream mFis;
	private int mOnePageNumLimite = 40;
	private int mCurrentPage = 0;
	private int mTotalPage = 0;
	private int mCurrentLine = 0;
	private int mTotalLines = 0;
	private ILogcatReaderListener mListener;
	private boolean mReading = false;

	private Timer mReadTimer;

	public LogcatFileReader() {
		// TODO Auto-generated constructor stub
	}

	public void setOnReadListener(ILogcatReaderListener listener) {
		mListener = listener;
	}

	public void setOnePageLineNumber(int num) {
		mOnePageNumLimite = num;
	}

	public int getCurrentReadPage() {
		return mCurrentPage;
	}

	private MappedByteBuffer mMapByteBuffer;
	String mLineFromByte;

	public String open(File file, int page) {
		String ret = null;
		mReadFile = file;
		int sLinePosition = 0;
		if (mRaf == null) {
			try {
				Log.d(TAG, "startRead : " + mReadFile);
				mRaf = new RandomAccessFile(file, "rw");
				FileChannel fc;
				fc = mRaf.getChannel();
				// 注意，文件通道的可读可写要建立在文件流本身可读写的基础之上
				long lendth = mRaf.length();
				int totalLineNum = 0;
				mMapByteBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, lendth);
				System.out.println("MapFile---------Start open...");

				for (int i = 0; i < lendth; i++) {
					if (mMapByteBuffer.get(i) == '\n') {
						totalLineNum++;
					}
				}

				if (lendth > 0 && totalLineNum == 0) { // if one line
					mTotalLines = 1;
				} else { // more than one line
					mTotalLines = totalLineNum;
				}
				mTotalPage = mTotalLines / mOnePageNumLimite + 1;
				System.out.println("MapFile---------Start end...totalLineNum:" + totalLineNum + " mTotalPage:"
						+ mTotalPage + " specify page:" + page);
				ret = readPage(page);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}

	public void close() {
		try {
			mRaf.getChannel().close();
			mRaf.close();
			mRaf = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getTotalLine() {
		return mTotalLines;
	}

	public int getTotalPage() {
		return mTotalPage;
	}

	public void stopRead() {
		mReading = false;
		if (mReadTimer != null) {
			mReadTimer.cancel();
			mReadTimer = null;
		}
	}

	/**
	 * Read one page with specify page number
	 * 
	 * @param pageNum
	 * @return 2017年10月24日下午3:01:34
	 */
	public String readPage(int pageNum) {
		String ret = null;
		Log.d(TAG, "readPage pageNum : " + pageNum + " mTotalLines:" + mTotalLines);
		if (pageNum > 0 && pageNum <= mTotalPage) {
			int start = mOnePageNumLimite * (pageNum - 1) + 1;
			if (start > mTotalLines) { // if page one set 0
				start = 0;
			}

			int end = start + mOnePageNumLimite - 1;
			if (end > mTotalLines) { // if page last set total
				end = mTotalLines;
			}

			ret = getSpecifyLines(start, end);
		} else {
			Log.d(TAG, "readPage pageNum illegal : " + pageNum);
		}
		return ret;

	}

	/**
	 * get specify lines string from start line to end line
	 * 
	 * @param startLine
	 * @param endLine
	 * @return 2017年10月24日下午3:02:30
	 */
	public String getSpecifyLines(int startLine, int endLine) {
		String lines = null;
		long lendth = 0;
		long startPoint = 0;
		long endPoint = 0;
		long tempPoint = 0;
		int currentLine = 0;
		boolean startPosConfirm = false;
		boolean endPosConfirm = false;
		if (startLine == 0) {
			Log.e(TAG, "getSpecifyLines startLine must bigger than 0!");
			return null;
		}
		if (startLine > endLine) {
			Log.e(TAG, "getSpecifyLines end must bigger than start!");
			return null;
		}
		try {
			lendth = mRaf.length();
			if (lendth > 0) {
				currentLine++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < lendth; i++) {
			byte getByte = mMapByteBuffer.get(i);
			if (getByte == '\n' || getByte == '\r') {
				tempPoint = i;
				currentLine++;
			}

			if (!startPosConfirm) {
				if (startLine == 1 && endLine == 1) {
					startPoint = 0;
					startPosConfirm = true;
				} else {
					if (startLine == currentLine) {
						if (startLine == 1) {
							startPoint = 0;
						} else {
							startPoint = tempPoint + 1;
						}
						startPosConfirm = true;
						Log.d(TAG, "startPoint:" + startPoint);
					}

				}
			}
			if (!endPosConfirm) {
				if (startLine == 1 && endLine == 1) {
					if (tempPoint == lendth - 1) {
						endPoint = lendth - 1;
						// endPosConfirm = true;
						break;
					} else if (currentLine == 2) {
						endPoint = tempPoint;
						// endPosConfirm = true;
						break;
					}
				} else {
					if (endLine + 1 == currentLine) {
						endPoint = tempPoint;
						Log.d(TAG, "endPoint:" + endPoint);
						// endPosConfirm = true;
						break;
					}
				}
			}
		}

		int bytelen = (int) (endPoint - startPoint + 1);
		byte[] lineByteArray = new byte[bytelen];
		Log.d(TAG, "getSpecifyLines bytelen:" + bytelen);
		for (int i = (int) startPoint; i <= (int) endPoint; i++) {
			byte b = mMapByteBuffer.get(i);
			lineByteArray[i - (int) startPoint] = b;
		}
		// Log.d(TAG, "lineByteArray : " +lineByteArray.length +"
		// toStirng:"+lineByteArray.toString());

		try {
			lines = new String(lineByteArray, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d(TAG, "getSpecifyLines start:" + startLine + " end:"
				+ endLine /* +"\n---lines---:\n"+lines */);
		return lines;
	}

	public int getTotalLines(File file) throws IOException {
		FileReader in = new FileReader(file);
		Log.d(TAG, "getTotalLines start..");
		LineNumberReader reader = new LineNumberReader(in);
		String s = reader.readLine();
		int lines = 0;
		while (s != null) {
			s = reader.readLine();
			// if (s != null) {
			lines++;
			// }
		}
		reader.close();
		in.close();
		Log.d(TAG, "getTotalLines end...");
		return lines;
	}

	RandomAccessFile mRaf = null;

	@SuppressWarnings("unchecked")
	public void closeMapFile() {
		// AccessController.doPrivileged(new PrivilegedAction() {
		// public Object run() {
		// try {
		// Method getCleanerMethod =
		// mMapByteBuffer.getClass().getMethod("cleaner", new Class[0]);
		// getCleanerMethod.setAccessible(true);
		// sun.misc.Cleaner cleaner = (sun.misc.Cleaner)
		// getCleanerMethod.invoke(mMapByteBuffer, new Object[0]);
		// cleaner.clean();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// return null;
		// }
		// });
	}

}
