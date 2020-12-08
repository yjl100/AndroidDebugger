package com.skyworthdigital.debugger.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

import com.skyworthdigital.debugger.listener.ILogcatReaderListener;

import android.util.Log;

public class LogFileReader {
	
	private static final String TAG = "LogFileReader";
	
	private File mReadFile;
	private FileInputStream mFis;
	private int mOnePageNumLimite = 30;
	private int mCurrentPage = 0;
	private int mTotalPage = 0;
	private int mCurrentLine = 0;
	private int mTotalLines = 0;
	private ILogcatReaderListener mListener;
	private boolean mReading = false;
	
	private Timer mReadTimer;
	
	public LogFileReader(File file) {
		// TODO Auto-generated constructor stub
		mReadFile = file;
//		try {
//			mFis = new FileInputStream(mReadFile);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
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
	
	public void startRead() {
//		try {
//			mLineReader =  new LineNumberReader(new FileReader(mReadFile));
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		if (mRaf == null) {
			try {
				Log.d(TAG, "startRead : " + mReadFile);
				mRaf = new RandomAccessFile(mReadFile, "rw");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		mReadTimer = new Timer();
		mReading = true;
//		mReadTimer.scheduleAtFixedRate(new TimerTask() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				int totalLines = 0;
//				try {
//					totalLines = getTotalLines(mReadFile);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				int totalPage = 0;
//				int needStartRead = 0;
//				
//				if (totalLines > 0 && mCurrentLine < totalLines) {
//					// calculate total page
//					totalPage = totalLines / mOnePageNumLimite + 1; 
//					
//					boolean pageChange = false;
//					
//					// if total page change, calculate needStartRead
//					if (totalPage > mTotalPage) {
//						if (totalLines <= mOnePageNumLimite) {
//							needStartRead = 0;
//						} else { // 39 - 5
//							needStartRead = totalLines - totalLines % mOnePageNumLimite;
//							Log.d(TAG, "needStartRead:"+needStartRead +" totalLines:"+totalLines+" mOnePageNumLimite:"+mOnePageNumLimite);
//						}
//						if (mListener != null) {
//							mListener.onPageNotify(totalPage);
//							
//						}
//						pageChange = true;
//						mTotalPage = totalPage;
//					}
//					
//					String readLines = getShowLines(needStartRead); // content from needStartRead to end
//					if (mListener != null) {
//						mListener.notifyRefresh(mListener, readLines, totalPage);
//					}
//					mCurrentLine = totalLines;
//					
//					Log.d(TAG, "*******mReadTimer****** totalLines:"+totalLines+" totalPage:"+totalPage+" needStartRead:"+needStartRead+" totalLines % mOnePageNumLimite:"+totalLines % mOnePageNumLimite);
////					if (totalLines - mCurrentLine > mOnePageNumLimite) { // full one page
////						
////					}
//				} else {
//					Log.d(TAG, "*******no new line****** totalLines:"+totalLines+" mCurrentLine:"+mCurrentLine);
//				}
//				
//			}
//		}, 0, 200);
		mReadTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int totalLines = 0;
				try {
					totalLines = getTotalLines(mReadFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
				int totalPage = 0;
				int needStartRead = 0;
				
				if (totalLines > 0 && mTotalLines < totalLines) { // line change 
					Log.d(TAG, "#######line change######  totalLines:"+totalLines);
					boolean pageChange = false;
					totalPage = totalLines / mOnePageNumLimite + 1; // calculate total page
					if (totalPage > mTotalPage) { // total page change
						mCurrentLine = totalLines - totalLines % mOnePageNumLimite;
						if (mListener != null) {
							Log.d(TAG, "#######line change######  onPageNotify...");
							mListener.onPageNotify(totalPage);
						}
						pageChange = true;
						mTotalPage = totalPage;
					} else { // total page not change
//						needStartRead = mCurrentLine;
						mCurrentLine += totalLines - mTotalLines;
					}
					// get lines to show
					String readLines = getShowLinesII(mCurrentLine, totalLines);
					if (mListener != null) {
						mListener.notifyRefresh(mListener, readLines, totalPage);
					}
					Log.d(TAG, "*******mReadTimer****** totalLines:"+totalLines+" totalPage:"+totalPage+" needStartRead:"+needStartRead
							+" mCurrentLine:"+mCurrentLine);
					mCurrentLine = totalLines;
					mTotalLines = totalLines;
					
				}
				
//				if (totalLines > 0 && mCurrentLine < totalLines) {
//					// calculate total page
//					totalPage = totalLines / mOnePageNumLimite + 1; 
//					
//					boolean pageChange = false;
//					
//					
//					// if total page change, calculate needStartRead
//					if (totalPage > mTotalPage) {
//						if (totalLines <= mOnePageNumLimite) {
//							needStartRead = 0;
//						} else { // 39 - 5
//							needStartRead = totalLines - totalLines % mOnePageNumLimite;
//							Log.d(TAG, "needStartRead:"+needStartRead +" totalLines:"+totalLines+" mOnePageNumLimite:"+mOnePageNumLimite);
//						}
//						if (mListener != null) {
//							mListener.onPageNotify(totalPage);
//							
//						}
//						pageChange = true;
//						mTotalPage = totalPage;
//					}
//					
//					String readLines = getShowLines(needStartRead); // content from needStartRead to end
//					if (mListener != null) {
//						mListener.notifyRefresh(mListener, readLines, totalPage);
//					}
//					mCurrentLine = totalLines;
					
//					Log.d(TAG, "*******mReadTimer****** totalLines:"+totalLines+" totalPage:"+totalPage+" needStartRead:"+needStartRead+" totalLines % mOnePageNumLimite:"+totalLines % mOnePageNumLimite);
//					if (totalLines - mCurrentLine > mOnePageNumLimite) { // full one page
//						
//					}
				 else {
					Log.d(TAG, "*******no new line****** totalLines:"+totalLines+" mCurrentLine:"+mCurrentLine);
				}
				
			}
		}, 0, 500);
	}
	
	public void stopRead() {
		mReading = false;
		if (mReadTimer != null) {
			mReadTimer.cancel();
			mReadTimer = null;
		}
	}
	
	public String readPage(int pageNum) {
		
		return null;
		
	}
	
	public int getTotalLines(File file) throws IOException {
		FileReader in = new FileReader(file);
		Log.d(TAG, "getTotalLines start..");
		LineNumberReader reader = new LineNumberReader(in);
		String s = reader.readLine();
		int lines = 0;
		while (s != null) {
			s = reader.readLine();
//			if (s != null) {
				lines++;
//			}
		}
		reader.close();
		in.close();
		Log.d(TAG, "getTotalLines end...");
		return lines;
	}  
	
	public long getSpecifyLinePointer(int lineNum) throws IOException {
		long ret = 0;
		long point = 0;
//		Log.d(TAG, "getSpecifyLinePointer lineNum:"+lineNum+" current point:"+mRaf.getFilePointer()+" readline:"+mRaf.readLine());
		if (mRaf != null) {
//			long len = mRaf.length();
			
			mRaf.seek(0);
			Log.d(TAG, "getSpecifyLinePointer seek 0 current point:"+mRaf.getFilePointer()+" mRaf.len:"+mRaf.length());
			int readLineNum = 0;
			long filePointer = 0;
			long addedLendth = 0;
			String line = null;
			while (readLineNum < lineNum) {
				filePointer = mRaf.getFilePointer();
				line = mRaf.readLine();
				addedLendth = mRaf.getFilePointer() - filePointer;
				point += addedLendth;
				readLineNum++;
//				Log.d(TAG, "getSpecifyLinePointer point:"+mRaf.getFilePointer()+" readLineNum:"+readLineNum
//						+" point:"+point+" addedLendth:"+addedLendth);
			}
			if (line != null) {
				point -=  addedLendth;
				Log.d(TAG, "getSpecifyLinePointer point:"+point+" line.length():"+line.length());
			}
		}
		ret = point;
		return ret;
	}  
	
	public long getSpecifyLinePointerFromBack(int lineNum) throws IOException {
		long ret = 0;
		long point = 0;
//		Log.d(TAG, "getSpecifyLinePointer lineNum:"+lineNum+" current point:"+mRaf.getFilePointer()+" readline:"+mRaf.readLine());
		if (mRaf != null) {
//			long len = mRaf.length();
			
			mRaf.seek(0);
			Log.d(TAG, "getSpecifyLinePointer seek 0 current point:"+mRaf.getFilePointer()+" mRaf.len:"+mRaf.length());
			int readLineNum = 0;
			long filePointer = 0;
			long addedLendth = 0;
			String line = null;
			while (readLineNum < lineNum) {
				filePointer = mRaf.getFilePointer();
				line = mRaf.readLine();
				addedLendth = mRaf.getFilePointer() - filePointer;
				point += addedLendth;
				readLineNum++;
//				Log.d(TAG, "getSpecifyLinePointer point:"+mRaf.getFilePointer()+" readLineNum:"+readLineNum
//						+" point:"+point+" addedLendth:"+addedLendth);
			}
			if (line != null) {
				point -=  addedLendth;
				Log.d(TAG, "getSpecifyLinePointer point:"+point+" line.length():"+line.length());
			}
		}
		ret = point;
		return ret;
	}  
	
	private LineNumberReader mLineReader;

	public String getShowLines(int startLine){
		StringBuffer sf = new StringBuffer();
		String line;
		try {
//			mLineReader.reset();
			mLineReader =  new LineNumberReader(new FileReader(mReadFile));
			Log.d(TAG, "getShowLines getLineNumber():"+mLineReader.getLineNumber()+" startLine:"+startLine);
//			mLineReader.setLineNumber(startLine);
			while ((line = mLineReader.readLine()) != null) {
				if (mLineReader.getLineNumber() >= startLine) {
					sf.append(line);
					sf.append("\n");
				}
			}
 			
//			line = mLineReader.readLine();
			
//			while (line != null) {
//				if (mLineReader.getLineNumber() >= startLine) {
//					sf.append(line);
//					sf.append("\r\n");
//				}
//				line = mLineReader.readLine();
//			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mLineReader.close();
			mLineReader = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sf.toString();
	}
	
	RandomAccessFile mRaf = null;
	
	/**
	 * 内存映射文件
	 * @param startLine
	 * @return
	 *  2017年10月13日下午6:28:02
	 */
	public String getShowLinesII(int startLine, int endLine) {
		
		StringBuffer sf = new StringBuffer();
		int lineNum = 0;
		int totalLength = 0;
		try {
//			if (mRaf == null) {
//				mRaf = new RandomAccessFile("/data/123.txt", "rw");
//			}
//			raf.seek(0);
			System.out.println("getShowLinesII mRaf.len:"+mRaf.length()+" point:"+mRaf.getFilePointer());
			long startLinePointer = getSpecifyLinePointer(startLine);
			mRaf.seek(startLinePointer);
			String line;
//			System.out.println("RandomAccessFile start read seek : " + startLinePointer);
			line = mRaf.readLine();
//			System.out.println("RandomAccessFile line:"+line);
			while(line != null) {
//				System.out.println("before read line ");
				if (lineNum == endLine) {
					break;
				}
				sf.append(line);
				sf.append("\n");
				lineNum++;
				line = mRaf.readLine();
				
//				System.out.println("RandomAccessFile arter read line:"+line+" lineNum:"+lineNum+" pointer:" + mRaf.getFilePointer());
			}
//			System.out.println("RandomAccessFile lineNum:"+lineNum);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		final int length = 0x8000000; // 128 Mb  
//		// 为了以可读可写的方式打开文件，这里使用RandomAccessFile来创建文件。  
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				FileChannel fc;
//				try {
//					System.out.println("MapFile---------Start writing");  
//					fc = new RandomAccessFile("/data/mapfile.txt", "rw").getChannel();
//			        //注意，文件通道的可读可写要建立在文件流本身可读写的基础之上  
//			        MappedByteBuffer out = fc.map(FileChannel.MapMode.READ_WRITE, 0, length);  
//			        //写128M的内容  
//			        for (int i = 0; i < length; i++) {  
//			            out.put((byte) 'x');  
//			        }  
//			        System.out.println("MapFile---------Finished writing");  out.l
//			        //读取文件中间6个字节内容  
//			        for (int i = length / 2; i < length / 2 + 6; i++) {  
//			            System.out.print((char) out.get(i));  
//			        }  
//			        fc.close(); 
//			        System.out.println("MapFile---------end...");  
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}  
//			}
//		}).start();
		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				int i = 1000;
//				RandomAccessFile raf = null;
//				int lineNum = 0;
//				try {
//					raf = new RandomAccessFile("/data/123.txt", "rw");
//					raf.seek(0);
//					String line;
//					System.out.println("RandomAccessFile start read");
//					System.out.println("RandomAccessFile 111:");
//					line = raf.readLine();
//					System.out.println("RandomAccessFile line:"+line);
//					while(line != null) {
//						System.out.println("before read line ");
//						line = raf.readLine();
//						System.out.println("RandomAccessFile arter read line:"+line+" lineNum:"+lineNum);
//						lineNum++;
//					}
//					System.out.println("RandomAccessFile lineNum:"+lineNum);
//					
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				while(i-- > 0) {
//					try {
//						System.out.println("RandomAccessFile---------Start length:"+raf.length()+" getFilePointer:"+raf.getFilePointer()
//						+" raf:"+raf.readLine()); 
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
				
				
//				FileChannel fc;
//				try {
//					System.out.println("MapFile---------Start writing");  
//					fc = new RandomAccessFile("/data/123.txt", "rw").getChannel();
//					System.out.println("MapFile---------  fc.size:"+fc.size()); 
//			        //注意，文件通道的可读可写要建立在文件流本身可读写的基础之上  
//			        MappedByteBuffer out = fc.map(FileChannel.MapMode.READ_WRITE, 0, length);  
////			        //写128M的内容  
////			        for (int i = 0; i < length; i++) {  
////			            out.put((byte) 'x');  
////			        }  
////			        System.out.println("MapFile---------Finished writing");
//			        //读取文件中间6个字节内容  
//			        System.out.println("MapFile--------- out.capacity():"+ out.capacity()+" fc.size:"+fc.size()+" "); 
//			        for (int i = 0; i < length; i++) { 
////			        	if((char) out.get(i)) {
//			        		System.out.println("read i:" + i + " c:" + (char) out.get(i));  
////			        	}
//			        }  
//			        fc.close(); 
//			        System.out.println("MapFile---------end...");  
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}  
//			}
//		}).start();
        
		return sf.toString();
		
	}

}
