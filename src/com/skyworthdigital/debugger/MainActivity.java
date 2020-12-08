package com.skyworthdigital.debugger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.skyworthdigital.debugger.aidl.ILogcatCallback;
import com.skyworthdigital.debugger.aidl.ILogcatService;
import com.skyworthdigital.debugger.listener.IConsolListener;
import com.skyworthdigital.debugger.listener.ILogcatReaderListener;
import com.skyworthdigital.debugger.listener.SaveLogListener;
import com.skyworthdigital.debugger.model.CmdType;
import com.skyworthdigital.debugger.model.ConsolType;
import com.skyworthdigital.debugger.model.SaveLog;
import com.skyworthdigital.debugger.task.LogcatTaskExt;
import com.skyworthdigital.debugger.task.LogcatTaskExt.LogcatType;
import com.skyworthdigital.debugger.task.ManualCmdTask;
import com.skyworthdigital.debugger.util.GetMountPoint;
import com.skyworthdigital.debugger.util.GetMountPoint.MountPoint;
import com.skyworthdigital.debugger.util.LogFileReader;
import com.skyworthdigital.debugger.util.LogcatFileReader;
import com.skyworthdigital.debugger.util.ManualCmdUtil;
import com.skyworthdigital.debugger.util.SkyworthBoxUtil;
import com.skyworthdigital.debugger.view.SaveConsolLogDialog;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * SkyworthDebugger
 * 
 * @author yangjialin 2017年10月18日下午2:03:19
 */
public class MainActivity extends Activity implements ViewSwitcher.ViewFactory {

	public static final String TAG = "SkyworthDebuggerActivity";
	public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final int MSG_APPEND_CONSOL = 0x03;
	public static final int MSG_UPDATE_TIME = 0x04;
	public static final int MSG_REFRESH_CONSOL = 0x05;
	public static final int MSG_REFRESH_PAGE = 0x06;
	public static final int MSG_SHOW_MANURAL_CONSOL_TIP = 0x07;
	public static final int MSG_PROCESS_COMPLETE = 0x08;

	private TextView mText;
	private TextView mPageText;
	private TextView mLogInfoText;
	private EditText mManualEditText;
	private Button mStart;
	private Button mStop;
	private Button mToTop;
	private Button mToBottom;
	private Button mToNextPage;
	private Button mToPrePage;
	private Button mClearScreen;
	private Button mSearchString;
	private Switch mConsolSwith;
	private Switch mLogcatScanConsolSwith;
	private Switch mWaitCmdCallbackSwith;

	LogcatTaskExt mLogcatTaskExt;
	ManualCmdTask mManualCmdTask;
	FrameLayout mConsolLayout;
	private Spinner mCmdSpinner;
	private TextSwitcher mTimeSwitcher;
	private View mCustomView;
	private AppStatus mAppStatus = AppStatus.None;
	private int mCurrentPage = 1;

	SaveConsolLogDialog mSaveConsolLogDialog;

	private enum AppStatus {
		None, Running, Paused
	};

	private ConsolListener mConsolListener = new ConsolListener();

	private class ConsolListener implements IConsolListener {

		@Override
		public void onOneLinePrint(IConsolListener lisener, String line) {
			// TODO Auto-generated method stub
			if (mConsolSwith.isChecked()) {
				mHandler.sendMessage(mHandler.obtainMessage(MSG_APPEND_CONSOL, line));
			}
		}

		@Override
		public void onPageChangeNotify(int page) {
			// TODO Auto-generated method stub
			if (mConsolSwith.isChecked()) {
				mCurrentPage = page;
				mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_PAGE, page, page));
			}
		}

		@Override
		public void onProcessComplete(IConsolListener lisener, ConsolType type) {
			// TODO Auto-generated method stub
			if (type == ConsolType.ManualCmd) {
				mHandler.sendEmptyMessage(MSG_PROCESS_COMPLETE);
			}
		}

	};

	LogcatFileReader mLogcatFileReader;

	public void onStart(View view) {
		Log.d(TAG, "onStart...");
		// Intent intent = new
		// Intent("com.skyworthdigital.debugger.LOGCAT_SERVICE");
		// intent.setPackage("com.skyworthdigital.debugger");
		// bindService(intent, mLogcatServiceConnection,
		// Service.BIND_AUTO_CREATE);
		// runCmd(mEditText.getText().toString());

		final int select = mCmdSpinner.getSelectedItemPosition();
		if (getConsolType() == ConsolType.Logcat) {
			SaveLogListener lisener = new SaveLogListener() {

				@Override
				public void onSetComplete(SaveLog saveLog) {
					// TODO Auto-generated method stub
					if (mLogcatTaskExt == null) {
						LogcatType type = select == CmdType.LogcatWithClear.ordinal() ? LogcatType.LOGCAT_AFTER_CLRER
								: select == (CmdType.Logcat.ordinal()) ? LogcatType.LOGCAT
										: LogcatType.LOGCAT_AFTER_CLRER;
						mLogcatTaskExt = new LogcatTaskExt(type);
						Log.d(TAG, "mLogcatTaskExt new : " + mLogcatTaskExt);
						mLogcatTaskExt.setLogcatListener(mConsolListener);
						if (saveLog != null) {
							mCurrentPage = 1;
							if (mConsolSwith.isChecked()) {
								mHandler.sendMessage(
										mHandler.obtainMessage(MSG_REFRESH_PAGE, mCurrentPage, mCurrentPage, "")); // clear
																													// screen
								scrollConsolToStart();
							}
							mLogcatTaskExt.setSavePath(saveLog.path);
							mLogcatTaskExt.start();
							mStart.setEnabled(false);
							mCmdSpinner.setEnabled(false);
							mConsolSwith.setEnabled(false);
							mStop.requestFocus();
						}
					} else if (mLogcatTaskExt.isAlive()) {
						Log.d(TAG, "mLogcatTaskExt isAlive reStartGet mLogcatTaskExt : " + mLogcatTaskExt);
						if (mLogcatTaskExt.isRunning()) {
							mLogcatTaskExt.stopGet();
						}

						mCurrentPage = 1;
						if (mConsolSwith.isChecked()) {
							mHandler.sendMessage(
									mHandler.obtainMessage(MSG_REFRESH_PAGE, mCurrentPage, mCurrentPage, "")); // clear
																												// screen
							scrollConsolToStart();
						}
						mLogcatTaskExt.setSavePath(saveLog.path);
						mLogcatTaskExt.startGet();
						mStart.setEnabled(false);
						mStop.requestFocus();
						mCmdSpinner.setEnabled(false);
						mConsolSwith.setEnabled(false);
					} else {
						Log.d(TAG, "mLogcatTaskExt is not Alive");
					}
				}
			};
			Log.d(TAG, "showSaveLogDialog");
			showSaveLogDialog(lisener);

		} else if (getConsolType() == ConsolType.ManualCmd) {
			// ManualCmdUtil.run(mManualEditText.getText().toString(),
			// mConsolListener);
			if (mManualCmdTask == null) {
				mManualCmdTask = new ManualCmdTask(mWaitCmdCallbackSwith.isChecked());
				mManualCmdTask.setCmd(mManualEditText.getText().toString(),
						(select == CmdType.ManuralCmdWithShell.ordinal()));
				mManualCmdTask.setLogcatListener(mConsolListener);
				mManualCmdTask.start();
				mStart.setEnabled(false);
				mCmdSpinner.setEnabled(false);
				mConsolSwith.setEnabled(false);
				mStop.requestFocus();
			} else {
				Log.d(TAG, "mManualCmdTask is running!");
			}
		}

	}

	public void onStop(View view) {
		Log.d(TAG, "onStop...");
		if (getConsolType() == ConsolType.Logcat) {
			if (mLogcatTaskExt != null && mLogcatTaskExt.isRunning()) {
				mLogcatTaskExt.stopGet();

				if (mLogcatFileReader != null) {
					mLogcatFileReader.close();
					mLogcatFileReader = null;
				}
				if (mConsolSwith.isChecked()) {
					if (mLogcatScanConsolSwith.isChecked()) {
						mLogcatFileReader = new LogcatFileReader();
						if (mLogcatFileReader != null) {
							String pageString = mLogcatFileReader.open(new File(mLogcatTaskExt.getSavingFile()),
									mCurrentPage);
							if (pageString != null) {
								mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_PAGE, mCurrentPage,
										mLogcatFileReader.getTotalPage()));
								mHandler.sendMessage(
										mHandler.obtainMessage(MSG_REFRESH_CONSOL, mCurrentPage, -1, pageString));
							}
						}
					}
				}
			} else {
				Log.w(TAG, "mLogcatTaskExt is already stop : " + mLogcatTaskExt);
			}
		} else if (getConsolType() == ConsolType.ManualCmd) {
			if (mManualCmdTask != null) {
				mManualCmdTask.exit();
				mManualCmdTask = null;
			}
		}
		mStart.setEnabled(true);
		mCmdSpinner.setEnabled(true);
		mConsolSwith.setEnabled(true);
	}

	public void toPageTop(View view) {
		if (!mConsolSwith.isChecked()) {
			return;
		}
		if (!mLogcatScanConsolSwith.isChecked()) {
			return;
		}
		if (getConsolType() == ConsolType.Logcat) {
			if (mLogcatFileReader != null) {
				if (mCurrentPage == 1) {
					return;
				}
				String pageString = mLogcatFileReader.readPage(1);
				if (pageString != null) {
					mCurrentPage = 1;
					mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_PAGE, 1, mLogcatFileReader.getTotalPage()));
					mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_CONSOL, 1, -1, pageString));
				}
			}
		} else if (getConsolType() == ConsolType.ManualCmd) {
			scrollConsolToStart();
		}
	}

	public void toPageBottom(View view) {
		if (!mConsolSwith.isChecked()) {
			return;
		}
		if (!mLogcatScanConsolSwith.isChecked()) {
			return;
		}
		if (getConsolType() == ConsolType.Logcat) {
			if (mLogcatFileReader != null) {
				if (mCurrentPage == mLogcatFileReader.getTotalPage()) {
					return;
				}
				String pageString = mLogcatFileReader.readPage(mLogcatFileReader.getTotalPage());
				if (pageString != null) {
					mCurrentPage = mLogcatFileReader.getTotalPage();
					mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_PAGE, mLogcatFileReader.getTotalPage(),
							mLogcatFileReader.getTotalPage()));
					mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_CONSOL, mLogcatFileReader.getTotalPage(),
							-1, pageString));
				}
			}
		} else if (getConsolType() == ConsolType.ManualCmd) {
			scrollConsolToEnd();
		}
	}

	public void toNextPage(View view) {
		if (!mConsolSwith.isChecked()) {
			return;
		}
		if (!mLogcatScanConsolSwith.isChecked()) {
			return;
		}
		if (getConsolType() == ConsolType.Logcat) {
			if (mLogcatFileReader != null) {
				if (mCurrentPage == mLogcatFileReader.getTotalPage()) {
					return;
				}
				String pageString = mLogcatFileReader.readPage(++mCurrentPage);
				if (pageString != null) {
					mHandler.sendMessage(
							mHandler.obtainMessage(MSG_REFRESH_PAGE, mCurrentPage, mLogcatFileReader.getTotalPage()));
					mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_CONSOL, mCurrentPage, -1, pageString));
				}
			}
		} else if (getConsolType() == ConsolType.ManualCmd) {
			scrollConsolToNextPage();
		}
	}

	public void toPrePage(View view) {
		if (!mConsolSwith.isChecked()) {
			return;
		}
		if (!mLogcatScanConsolSwith.isChecked()) {
			return;
		}
		if (getConsolType() == ConsolType.Logcat) {
			if (mLogcatFileReader != null) {
				if (mCurrentPage == 1) {
					return;
				}
				String pageString = mLogcatFileReader.readPage(--mCurrentPage);
				if (pageString != null) {
					mHandler.sendMessage(
							mHandler.obtainMessage(MSG_REFRESH_PAGE, mCurrentPage, mLogcatFileReader.getTotalPage()));
					mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_CONSOL, mCurrentPage, -1, pageString));
				}
			}
		} else if (getConsolType() == ConsolType.ManualCmd) {
			scrollConsolToPrePage();
		}
	}

	public void toClearPage(View view) {
		if (!mConsolSwith.isChecked()) {
			return;
		}
		mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_PAGE, mCurrentPage, -1, ""));
	}

	public void toSearchFile(View view) {

	}

	public void toHome(View view) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.addCategory("android.intent.category.LAUNCHER");
		startActivity(intent);
	}

	public void onHelp(View view) {
		showHelpDialog();
	}

	private ConsolType getConsolType() {
		ConsolType ret = ConsolType.Logcat;
		int position = mCmdSpinner.getSelectedItemPosition();
		if (position < CmdType.Logcat.ordinal()) {
			ret = ConsolType.Logcat;
		} else if (position >= CmdType.ManuralCmd.ordinal() && position <= CmdType.ManuralCmdWithShell.ordinal()) {
			ret = ConsolType.ManualCmd;
		}
		return ret;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
		mAppStatus = AppStatus.Running;
		mCmdSpinner.requestFocus();
		super.onResume();
		SkyworthBoxUtil.getSkyworthBoxCardNumByReflect(this);
		SkyworthBoxUtil.getSkyworthBoxStbIdByReflect();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mAppStatus = AppStatus.Paused;
		mHandler.removeMessages(MSG_UPDATE_TIME);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// unbindService(mLogcatServiceConnection);
		super.onDestroy();
		if (mLogcatTaskExt != null) {
			mLogcatTaskExt.exit();
			mLogcatTaskExt = null;
		}
		if (mManualCmdTask != null) {
			mManualCmdTask.interrupt();
			mManualCmdTask = null;
		}

		if (mLogcatFileReader != null) {
			mLogcatFileReader.close();
			mLogcatFileReader = null;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mConsolLayout = (FrameLayout) findViewById(R.id.consol_lyt);
		mStart = (Button) findViewById(R.id.start);
		mStop = (Button) findViewById(R.id.stop);

		mToTop = (Button) findViewById(R.id.btn_to_page_top);
		mToBottom = (Button) findViewById(R.id.btn_to_page_bottom);
		mToNextPage = (Button) findViewById(R.id.btn_to_page_next);
		mToPrePage = (Button) findViewById(R.id.btn_to_page_pre);

		mPageText = (TextView) findViewById(R.id.page_info_txt);
		mLogInfoText = (TextView) findViewById(R.id.log_info_txt);

		mConsolSwith = (Switch) findViewById(R.id.consol_swith);
		mLogcatScanConsolSwith = (Switch) findViewById(R.id.logcat_consol_scan_switch);

		mWaitCmdCallbackSwith = (Switch) findViewById(R.id.wait_cmd_callback);
		mLogcatScanConsolSwith.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				mToTop.setEnabled(isChecked);
				mToBottom.setEnabled(isChecked);
				mToNextPage.setEnabled(isChecked);
				mToPrePage.setEnabled(isChecked);
			}
		});
		mText = (TextView) findViewById(R.id.test_content);
		mText.setMovementMethod(ScrollingMovementMethod.getInstance());
		mText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					if (getConsolType() == ConsolType.ManualCmd) {
						scrollConsolToEnd();
					}
					mConsolLayout.setForeground(getResources().getDrawable(R.drawable.consol_focus_bg));
				} else {
					mConsolLayout.setForeground(null);
				}
			}
		});
		mManualEditText = (EditText) findViewById(R.id.edit);
		mCmdSpinner = (Spinner) findViewById(R.id.cmd_spin);
		mCmdSpinner.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_ENTER:
					case KeyEvent.KEYCODE_DPAD_CENTER:
						if (mLogcatFileReader != null && mCmdSpinner.isFocused()) {
							showCloseLogcatReaderDialog();
							return true;
						}
						break;
					default:
						break;
					}
				}
				return false;
			}
		});
		mCmdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				// int cmd = CmdType.ManuralCmd.ordinal();
				Log.d(TAG, "onItemSelected position:" + position);

				if (getConsolType() == ConsolType.ManualCmd) {
					if (mManualEditText.getVisibility() != View.VISIBLE) {
						mManualEditText.setVisibility(View.VISIBLE);
						mCurrentPage = 1;
						mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH_PAGE, mCurrentPage, mCurrentPage, "")); // clear
																														// screen
					}
				} else if (getConsolType() == ConsolType.Logcat) {
					mToTop.setEnabled(true);
					mToBottom.setEnabled(true);
					mToNextPage.setEnabled(true);
					mToPrePage.setEnabled(true);
					if (mManualEditText.getVisibility() == View.VISIBLE) {
						mManualEditText.setVisibility(View.GONE);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}

		});

		mCustomView = getLayoutInflater().inflate(R.layout.switch_text_time, null);
		mTimeSwitcher = (TextSwitcher) mCustomView.findViewById(R.id.time_switcher);
		mTimeSwitcher.setFactory(this);
		Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
		Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
		mTimeSwitcher.setInAnimation(in);
		mTimeSwitcher.setOutAnimation(out);
		final ActionBar bar = getActionBar();
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
		lp.rightMargin = 50;
		bar.setCustomView(mCustomView, lp);
		int flags = ActionBar.DISPLAY_SHOW_CUSTOM;
		Log.d(TAG, "bar.getDisplayOptions():" + bar.getDisplayOptions());
		int change = bar.getDisplayOptions() ^ flags;
		bar.setDisplayOptions(change, flags);
		bar.setBackgroundDrawable(new ColorDrawable(0xff1f97f3));

	}

	private List<MountPoint> getSavePath() {
		String[] ret = null;
		List<MountPoint> list = GetMountPoint.GetMountPointInstance(this).getMountedPoint();
		for (int i = 0; i < list.size(); i++) {
			if (ret == null) {
				ret = new String[list.size()];
			}
			MountPoint mp = list.get(i);
			Log.d(TAG, ">>>mp:" + mp.getFile().getAbsolutePath());
			ret[i] = mp.getFile().getAbsolutePath();
		}
		// Log.d(TAG, "getSavePath ret:"+ret);
		return list;
	}

	private void showSaveLogDialog(final SaveLogListener listener) {
		final SaveLog selectSaveLog = new SaveLog();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.save_icon);
		builder.setTitle(R.string.logcat_save_dialog_message);
		int lastRemovable = 0;
		final List<MountPoint> pathItems = getSavePath();// {"/mnt/sda/sda1","/mnt/sda/sdb1","/mnt/sdcard"};
		final String[] pathDescribe = new String[pathItems.size()];
		for (int i = 0; i < pathItems.size(); i++) {
			MountPoint p = pathItems.get(i);
			Log.d(TAG,
					"MountPoint i:" + i + " isRemovable:" + p.isRemovable() + " path:" + p.getFile().getAbsolutePath());
			if (p.getFile().getAbsolutePath().startsWith("/mnt/sd")) {
				pathDescribe[i] = p.getFile().getAbsolutePath() + getString(R.string.removable_storage);
				lastRemovable = i;
			} else /*
					 * if (!p.isRemovable() ||
					 * p.getFile().getAbsolutePath().contains("emulated"))
					 */ {
				pathDescribe[i] = p.getFile().getAbsolutePath();
				if (!p.getFile().getAbsolutePath().contains("emulated")) {
					lastRemovable = i;
				}
			}
		}

		selectSaveLog.index = lastRemovable;
		selectSaveLog.path = pathItems.get(lastRemovable).getFile().getAbsolutePath();
		selectSaveLog.pathDescribe = pathDescribe[lastRemovable];
		builder.setSingleChoiceItems(pathDescribe, lastRemovable, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				// Toast.makeText(getApplicationContext(), "You clicked "+
				// pathDescribe[i], Toast.LENGTH_SHORT).show();
				selectSaveLog.index = i;
				selectSaveLog.path = pathItems.get(i).getFile().getAbsolutePath();
				selectSaveLog.pathDescribe = pathDescribe[i];
			}
		});
		// 监听下方button点击事件
		builder.setPositiveButton(R.string.postive_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				// Toast.makeText(getApplicationContext(),getString(R.string.postive_button)
				// +" i:"+i + " path:"+ selectSaveLog.path,
				// Toast.LENGTH_SHORT).show();
				Log.d(TAG, " path:" + selectSaveLog.path);
				listener.onSetComplete(selectSaveLog);
			}
		});
		builder.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				// Toast.makeText(getApplicationContext(),
				// getString(R.string.negative_button) +" i:"+i,
				// Toast.LENGTH_SHORT).show();
			}
		});
		builder.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void showHelpDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.help);
		builder.setTitle(R.string.help_dialog_title);
		builder.setMessage(R.string.help_dialog_content);
		builder.setPositiveButton(R.string.postive_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {

			}
		});
		builder.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void showCloseLogcatReaderDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.help);
		builder.setTitle(R.string.tip);
		builder.setMessage(R.string.close_logcat_reader);
		builder.setPositiveButton(R.string.postive_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (mLogcatFileReader != null) {
					mLogcatFileReader.close();
					mLogcatFileReader = null;
					mLogInfoText.setText("");
				}
			}
		});
		builder.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		builder.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void updateTimerCounter() {
		// Log.d(TAG, "updateTimerCounter
		// consol.length:"+mText.getText().length());
		Date current = new Date(System.currentTimeMillis());
		DateFormat df = new SimpleDateFormat(TIME_FORMAT);
		String time = df.format(current);
		mTimeSwitcher.setText(time);
	}

	private void updateLogFileInfo() {
		String fileSize = "";
		long fileLength = new File(mLogcatTaskExt.getSavingFile()).length();
		if (fileLength < 1024) { // 0-1024Byte
			fileSize = fileLength + "Byte";
		} else if (fileLength >= 1024 && fileLength < 1024 * 1024) { // 1-1023Kb
			fileSize = String.format("%.2f", fileLength / 1024f) + "KByte";
		} else if (fileLength >= 1024 * 1024 && fileLength < 1024 * 1024 * 1024) { // 1-1023Mb
			fileSize = String.format("%.3f", fileLength / 1024f / 1024f) + "MByte";
		} else { // bigger than 1GB
			fileSize = "> 1GB";
		}
		Log.d(TAG, "fileSize:" + fileSize + " fileLength:" + fileLength);
		String info = getString(R.string.logfile_info, mLogcatTaskExt.getSavingFile(), fileSize);
		mLogInfoText.setText(info);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onKeyDown getAction:" + event.getAction() + " keyCode:" + keyCode);
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_DOWN:
				break;
			default:
				break;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_APPEND_CONSOL) {
				int count = mText.getText().length();
				String newLine = msg.obj.toString() + "\n";
				mText.append(newLine);
				scrollConsolToEnd();
			} else if (MSG_UPDATE_TIME == msg.what) {
				updateTimerCounter();
				mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
				if (mLogcatTaskExt != null && mLogcatTaskExt.isRunning()) {
					updateLogFileInfo();
				}
			} else if (msg.what == MSG_REFRESH_CONSOL) {
				if (msg.obj.toString() != null) {
					mText.append(msg.obj.toString());
				}
			} else if (msg.what == MSG_REFRESH_PAGE) {
				mText.setText("");
				scrollConsolToStart();
				if (getConsolType() == ConsolType.Logcat) {
					mPageText.setText(getString(R.string.page_tip) + msg.arg1 + "/" + msg.arg2);
				} else if (getConsolType() == ConsolType.ManualCmd) {
					mPageText.setText(getString(R.string.page_manural_consol_tip));
				}
			} else if (msg.what == MSG_PROCESS_COMPLETE) {
				Log.d(TAG, "MSG_PROCESS_COMPLETE");
				if (mManualCmdTask != null) {
					mManualCmdTask.interrupt();
					mManualCmdTask = null;
				}
				mStart.setEnabled(true);
				mCmdSpinner.setEnabled(true);
				mConsolSwith.setEnabled(true);
			} else if (msg.what == MSG_SHOW_MANURAL_CONSOL_TIP) {
				mPageText.setText(getString(R.string.page_manural_consol_tip));
			}
		}
	};

	private void scrollConsolToStart() {
		if (mText != null) {
			mText.scrollTo(0, 0);
		}
	}

	private void scrollConsolToEnd() {
		if (mText != null) {
			int offset = mText.getLineCount() * mText.getLineHeight();
			if (offset > (mText.getHeight() - mText.getLineHeight() - 10)) {
				mText.scrollTo(0, offset - mText.getHeight() + mText.getLineHeight() + 10);
			}
		}
	}

	private void scrollConsolToNextPage() {
		int textContentHeight = mText.getLineCount() * mText.getLineHeight();
		int textViewHeight = mText.getHeight();
		int scrollY = mText.getScrollY();
		Log.d(TAG, "scrollConsolToNextPage scrollY:" + scrollY + " consolHeight:" + textViewHeight
				+ " textContentHeight:" + textContentHeight);
		if (scrollY + textViewHeight > textContentHeight) {
			mText.scrollTo(0, textContentHeight);
		} else {
			mText.scrollTo(0, scrollY + textViewHeight);
		}
	}

	private void scrollConsolToPrePage() {
		int textContentHeight = mText.getLineCount() * mText.getLineHeight();
		int textViewHeight = mText.getHeight();
		int scrollY = mText.getScrollY();
		Log.d(TAG, "scrollConsolToPrePage scrollY:" + scrollY + " consolHeight:" + textViewHeight
				+ " textContentHeight:" + textContentHeight);
		if (scrollY - textViewHeight < 0) {
			mText.scrollTo(0, 0);
		} else {
			mText.scrollTo(0, scrollY - textViewHeight);
		}
	}

	private ILogcatService mLogcatService;

	private ServiceConnection mLogcatServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onServiceDisconnected name:" + name);
			try {
				mLogcatService.unregisterCallback(mILogcatCallback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			mLogcatService = ILogcatService.Stub.asInterface(service);
			Log.d(TAG, "onServiceConnected service:" + service + " mLogcatService:" + mLogcatService);
			try {
				mLogcatService.registerCallback(mILogcatCallback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private ILogcatCallback mILogcatCallback = new ILogcatCallback.Stub() {

		@Override
		public void onOneLinePrint(String line) throws RemoteException {
			// TODO Auto-generated method stub
			if (!mConsolSwith.isChecked()) {
				return;
			}
			mHandler.sendMessage(mHandler.obtainMessage(MSG_APPEND_CONSOL, line));
		}

	};

	@Override
	public View makeView() {
		// TODO Auto-generated method stub
		TextView t = new TextView(this);
		t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		// float size = getResources().getDimension(R.dimen.title_size);
		// Log.d(TAG, "makeView size:"+size);
		t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		t.setTextColor(0xa0000000);// (0xffeeedf0);
		return t;
	}

}
