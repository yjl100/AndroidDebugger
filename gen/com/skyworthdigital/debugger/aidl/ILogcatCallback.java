/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\1-yangjialin\\Company\\Document\\CompanyFileAll\\Develop-Material\\ÆäËû\\SkyworthDebugger\\v1.1\\SkyworthDebugger\\src\\com\\skyworthdigital\\debugger\\aidl\\ILogcatCallback.aidl
 */
package com.skyworthdigital.debugger.aidl;
public interface ILogcatCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.skyworthdigital.debugger.aidl.ILogcatCallback
{
private static final java.lang.String DESCRIPTOR = "com.skyworthdigital.debugger.aidl.ILogcatCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.skyworthdigital.debugger.aidl.ILogcatCallback interface,
 * generating a proxy if needed.
 */
public static com.skyworthdigital.debugger.aidl.ILogcatCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.skyworthdigital.debugger.aidl.ILogcatCallback))) {
return ((com.skyworthdigital.debugger.aidl.ILogcatCallback)iin);
}
return new com.skyworthdigital.debugger.aidl.ILogcatCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onOneLinePrint:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.onOneLinePrint(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.skyworthdigital.debugger.aidl.ILogcatCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onOneLinePrint(java.lang.String line) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(line);
mRemote.transact(Stub.TRANSACTION_onOneLinePrint, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onOneLinePrint = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onOneLinePrint(java.lang.String line) throws android.os.RemoteException;
}
