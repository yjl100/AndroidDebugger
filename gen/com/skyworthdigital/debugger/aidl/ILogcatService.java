/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\1-yangjialin\\Company\\Document\\CompanyFileAll\\Develop-Material\\ÆäËû\\SkyworthDebugger\\v1.1\\SkyworthDebugger\\src\\com\\skyworthdigital\\debugger\\aidl\\ILogcatService.aidl
 */
package com.skyworthdigital.debugger.aidl;
public interface ILogcatService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.skyworthdigital.debugger.aidl.ILogcatService
{
private static final java.lang.String DESCRIPTOR = "com.skyworthdigital.debugger.aidl.ILogcatService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.skyworthdigital.debugger.aidl.ILogcatService interface,
 * generating a proxy if needed.
 */
public static com.skyworthdigital.debugger.aidl.ILogcatService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.skyworthdigital.debugger.aidl.ILogcatService))) {
return ((com.skyworthdigital.debugger.aidl.ILogcatService)iin);
}
return new com.skyworthdigital.debugger.aidl.ILogcatService.Stub.Proxy(obj);
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
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
com.skyworthdigital.debugger.aidl.ILogcatCallback _arg0;
_arg0 = com.skyworthdigital.debugger.aidl.ILogcatCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
com.skyworthdigital.debugger.aidl.ILogcatCallback _arg0;
_arg0 = com.skyworthdigital.debugger.aidl.ILogcatCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.skyworthdigital.debugger.aidl.ILogcatService
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
@Override public void registerCallback(com.skyworthdigital.debugger.aidl.ILogcatCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterCallback(com.skyworthdigital.debugger.aidl.ILogcatCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void registerCallback(com.skyworthdigital.debugger.aidl.ILogcatCallback callback) throws android.os.RemoteException;
public void unregisterCallback(com.skyworthdigital.debugger.aidl.ILogcatCallback callback) throws android.os.RemoteException;
}
