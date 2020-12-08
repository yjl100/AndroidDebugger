package com.skyworthdigital.debugger.util;

import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class ProcessUtil {
	/** 
	   * ͨ��Android�ײ�ʵ�ֽ��̹ر� 
	   */  
	  public static void killProcess(Process process) {  
		  if (process == null) {
			  return;
		  }
	      int pid = getProcessId(process.toString()); 
	      Log.d("ProcessUtil", "killProcess pid:"+pid);
	      if (pid != 0) {  
	          try {  
	              closeAllStream(process);  
	              android.os.Process.killProcess(pid);
	          } catch (Exception e) {  
	        	  e.printStackTrace();
	              try {  
	                  process.destroy();  
	              } catch (Exception ex) {  
	                  ex.printStackTrace();  
	              }  
	          }  
	      }  
	  }  
	  
	  /** 
	   * ��ȡ��ǰ���̵�ID 
	   */  
	  public static int getProcessId(String str) {  
	      try {  
	          int i = str.indexOf("=") + 1;  
	          int j = str.indexOf("]");  
	          String cStr = str.substring(i, j).trim();  
	          return Integer.parseInt(cStr);  
	      } catch (Exception e) {  
	          return 0;  
	      }  
	  }  
	  
	  /** 
	   * �رս��̵������� 
	   * 
	   * @param process 
	   */  
	  public static void closeAllStream(Process process) {  
	      try {  
	          InputStream in = process.getInputStream();  
	          if (in != null)  
	              in.close();  
	      } catch (Exception e) {  
	          e.printStackTrace();  
	      }  
	      try {  
	          InputStream in = process.getErrorStream();  
	          if (in != null)  
	              in.close();  
	      } catch (Exception e) {  
	          e.printStackTrace();  
	      }  
	      try {  
	          OutputStream out = process.getOutputStream();  
	          if (out != null)  
	              out.close();  
	      } catch (Exception e) {  
	          e.printStackTrace();  
	      }  
	  }  
}
