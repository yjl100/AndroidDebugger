package com.skyworthdigital.debugger.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

/**
 * We can get skyworth box stb id and ca card number without import skyworthdigital.jar by java reflect function
 * it may be used by third-party company software
 * @author yangjialin
 * 2018-9-3 16:01:45
 */

public class SkyworthBoxUtil {
	public static final String TAG = "SkyworthBoxUtil";
	
	public static String getSkyworthBoxStbIdByReflect() {
		String stbId = null;
		try {
			// get class
			Class<?> classStbConfig = Class.forName("com.skyworthdigital.stb.StbConfig");
			// get method from StbConfig
			Method method_getConfigData = classStbConfig.getMethod("getConfigData");
			// invoke getConfigData return a SysConfigData object
			Object configDataObj = method_getConfigData.invoke(classStbConfig);
			// get declared fields
			Field[] fields = configDataObj.getClass().getDeclaredFields();
			// find field from fields where name is "mStbId"
			for (Field fd : fields) {
				fd.setAccessible(true);
				if (fd.getName().equals("mStbId")) {
					// get stbIdByte
					byte[] stbIdByte = (byte[]) fd.get(configDataObj);
					// convert byte to string and get substring
					stbId = new String(stbIdByte).substring(0, 17);
					Log.d(TAG, "getStbIdByReflect is :" + stbId);
					break;
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stbId;
	}
	
	public static String getSkyworthBoxCardNumByReflect(Context context) {
		String cardNum = null;
		try {
			// get class
			Class<?> classCommon = Class.forName("com.skyworthdigital.stb.ca.common.Common");
			// get getInstance(Context context) method from StbConfig
			Method method_getInstance = classCommon.getDeclaredMethod("getInstance", Context.class);
			// invoke getInstance return a Common object
			Object configDataObj = method_getInstance.invoke(classCommon, context);
			
			// get getCardNo(int cardID) method from Common
			Method getCardNo = configDataObj.getClass().getDeclaredMethod("getCardNo", int.class);
			// invoke getCardNo return a card number string
			String cardNumGet = ((String) getCardNo.invoke(configDataObj, 0));
			if (cardNumGet != null && !cardNumGet.isEmpty()) {
				// get substring
				cardNum = (cardNumGet).substring(0, 16).toLowerCase();
			}
			Log.d(TAG, "getCardNumByReflect is :" + cardNum);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cardNum;
	}
}
