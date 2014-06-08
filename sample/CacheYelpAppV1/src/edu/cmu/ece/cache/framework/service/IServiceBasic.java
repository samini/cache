/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/shahriyar/Documents/repos/android/projects/CacheServiceTestV2/src/edu/cmu/ece/cache/framework/service/IServiceBasic.aidl
 */
package edu.cmu.ece.cache.framework.service;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
public interface IServiceBasic extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements edu.cmu.ece.cache.framework.service.IServiceBasic
{
private static final java.lang.String DESCRIPTOR = "edu.cmu.ece.cache.framework.service.IServiceBasic";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IServiceBasic interface,
 * generating a proxy if needed.
 */
public static edu.cmu.ece.cache.framework.service.IServiceBasic asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof edu.cmu.ece.cache.framework.service.IServiceBasic))) {
return ((edu.cmu.ece.cache.framework.service.IServiceBasic)iin);
}
return new edu.cmu.ece.cache.framework.service.IServiceBasic.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_remoteRegisterApplication:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
double _arg3;
_arg3 = data.readDouble();
double _arg4;
_arg4 = data.readDouble();
int _arg5;
_arg5 = data.readInt();
int _arg6;
_arg6 = data.readInt();
boolean _arg7;
_arg7 = (0!=data.readInt());
boolean _arg8;
_arg8 = (0!=data.readInt());
int _result = this.remoteRegisterApplication(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_remoteStopSelf:
{
data.enforceInterface(DESCRIPTOR);
this.remoteStopSelf();
reply.writeNoException();
return true;
}
case TRANSACTION_remoteRequestContent:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _result = this.remoteRequestContent(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_remoteHelloWorld:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.remoteHelloWorld(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements edu.cmu.ece.cache.framework.service.IServiceBasic
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public int remoteRegisterApplication(java.lang.String appName, java.lang.String url, java.lang.String api, double cellWidth, double cellHeight, int priority, int rate, boolean multiLevel, boolean overlay) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appName);
_data.writeString(url);
_data.writeString(api);
_data.writeDouble(cellWidth);
_data.writeDouble(cellHeight);
_data.writeInt(priority);
_data.writeInt(rate);
_data.writeInt(((multiLevel)?(1):(0)));
_data.writeInt(((overlay)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_remoteRegisterApplication, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void remoteStopSelf() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_remoteStopSelf, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.lang.String remoteRequestContent(java.lang.String appName, java.lang.String schema, java.lang.String request) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appName);
_data.writeString(schema);
_data.writeString(request);
mRemote.transact(Stub.TRANSACTION_remoteRequestContent, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String remoteHelloWorld(java.lang.String name) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(name);
mRemote.transact(Stub.TRANSACTION_remoteHelloWorld, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_remoteRegisterApplication = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_remoteStopSelf = (IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_remoteRequestContent = (IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_remoteHelloWorld = (IBinder.FIRST_CALL_TRANSACTION + 3);
}
public int remoteRegisterApplication(java.lang.String appName, java.lang.String url, java.lang.String api, double cellWidth, double cellHeight, int priority, int rate, boolean multiLevel, boolean overlay) throws android.os.RemoteException;
public void remoteStopSelf() throws android.os.RemoteException;
public java.lang.String remoteRequestContent(java.lang.String appName, java.lang.String schema, java.lang.String request) throws android.os.RemoteException;
public java.lang.String remoteHelloWorld(java.lang.String name) throws android.os.RemoteException;
}
