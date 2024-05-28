package com.smart.st8050b_demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.topwise.cloudpos.aidl.AidlDeviceService;

import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
	public static final int SHOW_MSG = 0;
    private static final String TAG = "TPW-BaseTestActivity";

	public static final String TOPWISE_SERVICE_ACTION = "topwise_cloudpos_device_service";

	private int showLineNum = 0;

	private LinearLayout linearLayout;
	private ScrollView scrollView;
	private TextView textView1;
	private TextView textView2;
	private long oldTime = -1;
	public static final long DELAY_TIME = 200;
	public LinearLayout rightButArea = null;

	//设别服务连接桥
	private ServiceConnection conn = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            Log.d(TAG,"aidlService服务连接成功");
			if(serviceBinder != null){	//绑定成功
				AidlDeviceService serviceManager = AidlDeviceService.Stub.asInterface(serviceBinder);
				onDeviceConnected(serviceManager);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"AidlService服务断开了");
		}
	};
	public EditText et_money;
	public LinearLayout ll_input_edits;
	public EditText et_order;
	public EditText et_psw;
	public EditText et_name;

	//绑定服务
	public void bindService(){
		Intent intent = new Intent();
		intent.setAction(TOPWISE_SERVICE_ACTION);
		final Intent eintent = new Intent(createExplicitFromImplicitIntent(this,intent));
		boolean flag = bindService(eintent, conn, Context.BIND_AUTO_CREATE);
		if(flag){
            Log.d(TAG,"服务绑定成功");
		}else{
            Log.d(TAG,"服务绑定失败");
		}
	}


	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);


		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		bindService();
	}








	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		oldTime = -1;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unbindService(conn);
	}
	/**
	 * 服务连接成功时回调
	 * @param serviceManager
	 * @createtor：Administrator
	 * @date:2015-8-4 上午7:38:08
	 */
	public abstract void onDeviceConnected(AidlDeviceService serviceManager);

	public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }



	synchronized boolean isNormalVelocityClick(long time) {
		long newTime = System.currentTimeMillis();
		if (oldTime == -1) {
			oldTime = newTime;
			return true;
		} else {
			android.util.Log.v("asewang","newTime : " + newTime + " , oldTime : " + oldTime);
			if ((newTime - oldTime) <= time) {
				oldTime = newTime;
				return false;
			}
			oldTime = newTime;
		}
		return true;
	}



	public String getStringByid(int id)
	{
		return this.getString(id).toString();
	}
}
