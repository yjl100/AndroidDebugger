package com.skyworthdigital.debugger.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.skyworthdigital.debugger.listener.IConsolListener;
import com.skyworthdigital.debugger.model.ConsolType;
import com.skyworthdigital.debugger.util.ProcessUtil;

import android.util.Log;

public class ManualCmdUtil {
	public static final String TAG = "ManualCmdUtil";
	
	public static void run(String cmd, IConsolListener listener) {
		try {
			List<String> cmds = new ArrayList<String>();
			cmds.add("sh");
			cmds.add("-c");
			cmds.add(cmd);
			ProcessBuilder pb =new ProcessBuilder(cmds);
			Log.e(TAG, "exec command start");
			Process process = pb.start();
			Log.e(TAG, "exec command start after process:" + process.toString());
//			Process process = Runtime.getRuntime().exec(new String[] {"dumpsys", "activity", "|", "grep", "top"});
			if (0 != process.waitFor()) {
				Log.e(TAG, "exec command error");
			} else {
				Log.e(TAG, "exec command waitFor ok");
				BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = "";
				while ((line = input.readLine()) != null) {
					Log.e(TAG, "*****line****:" + line);
//					if (line.endsWith("(top-activity)")) {
//						String[] tmpStrings = line.split(":|/");
//						topPackageName = tmpStrings[tmpStrings.length - 2];
//						Log.e(TAG, "*****what i want line: " + line + " package name: " + topPackageName);
//						break;
					listener.onOneLinePrint(listener, line);
//					}
				}
				input.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e(TAG, "*****run**** end!!!!");
	}
	
	
	
	
}
