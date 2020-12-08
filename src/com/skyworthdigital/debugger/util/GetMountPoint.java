package com.skyworthdigital.debugger.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

public class GetMountPoint {
	private Context context;
	private final static String tag = "GetMountPoint";

	/** ���췽�� */
	private GetMountPoint(Context context)
	{
	    this.context = context;
	}
	
	/** ֮���������ַ�����ʼ��ʱΪ�˷�ֹʹ�õ�ʱ��û�м��SDK�汾���³��� */
	public static GetMountPoint GetMountPointInstance(Context context)
	{
	    if (14 <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT <= 23)
	    {
	        return new GetMountPoint(context);
	    }
	    else
	    {
	        Log.e(tag, "���಻֧�ֵ�ǰSDK�汾");
	        return null;
	    }
	}
	
	/** ���Ĳ���-��ȡ���й��ص���Ϣ�� */
	public List<MountPoint> getMountPoint()
	{
	    try
	    {
	        Class class_StorageManager = StorageManager.class;
	        Method method_getVolumeList = class_StorageManager.getMethod("getVolumeList");
	        Method method_getVolumeState = class_StorageManager.getMethod("getVolumeState", String.class);
	        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
	        Class class_StorageVolume = Class.forName("android.os.storage.StorageVolume");
	        Method method_isRemovable = class_StorageVolume.getMethod("isRemovable");
	        Method method_getPath = class_StorageVolume.getMethod("getPath");
	        Method method_getPathFile = null;
	        if (Build.VERSION.SDK_INT >= 17)
	        {// ��api16һ�µİ汾��StorageVolume������û��getPathFile
	            method_getPathFile = class_StorageVolume.getMethod("getPathFile");
	        }
	        Object[] objArray = (Object[]) method_getVolumeList.invoke(sm);

	        //region ���й��ص�File---���������ô洢���������ô洢���ı�־
	        List<MountPoint> result = new ArrayList<MountPoint>();
	        for (Object value : objArray)
	        {
	            String path = (String) method_getPath.invoke(value);
	            File file;
	            if (Build.VERSION.SDK_INT >= 17)
	            {
	                file = (File) method_getPathFile.invoke(value);
	            }
	            else
	            {
	                file = new File(path);
	            }

	            Boolean isRemovable = (Boolean) method_isRemovable.invoke(value);
	            boolean isMounted;
	            String getVolumeState = (String) method_getVolumeState.invoke(sm, path);//��ȡ����״̬��
	            if (getVolumeState.equals(Environment.MEDIA_MOUNTED))
	            {
	                isMounted = true;
	            }
	            else
	            {
	                isMounted = false;
	            }

	            result.add(new MountPoint(file, isRemovable, isMounted));
	        }
	        return result;
	        //endregion
	    }
	    catch (NoSuchMethodException e)
	    {
	        e.printStackTrace();
	    }
	    catch (InvocationTargetException e)
	    {
	        e.printStackTrace();
	    }
	    catch (IllegalAccessException e)
	    {
	        e.printStackTrace();
	    }
	    catch (ClassNotFoundException e)
	    {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	/** ��ȡ���ڹ���״̬�Ĺ��ص����Ϣ */
	public List<MountPoint> getMountedPoint()
	{
	    List<MountPoint> result = this.getMountPoint();
	    for (MountPoint value : result)
	    {
	        if (!value.isMounted)
	        {
	            result.remove(value);
	        }
	    }
	    return result;
	}
	
	public class MountPoint
	{
	    private File file;
	    /** �����ж��Ƿ�Ϊ���ô洢�������Ϊtrue���Ǵ������ص�����Ƴ����������ô洢��������֮ */
	    private boolean isRemovable;
	    /** ���ڱ�ʾ����δ���ִ�е�ʱ������������Ƿ��ڹ���״̬�������Ϊtrue������֮ */
	    private boolean isMounted;

	    public MountPoint(File file, boolean isRemovable, boolean isMounted)
	    {
	        this.file = file;
	        this.isMounted = isMounted;
	        this.isRemovable = isRemovable;
	    }

	    public File getFile()
	    {
	        return file;
	    }

	    public boolean isRemovable()
	    {
	        return isRemovable;
	    }

	    public boolean isMounted()
	    {
	        return isMounted;
	    }
	}
}
