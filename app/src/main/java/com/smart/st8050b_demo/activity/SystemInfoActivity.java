package com.smart.st8050b_demo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.smart.st8050b_demo.R;
import com.smart.st8050b_demo.adapter.OperatorInfoAdapter;
import com.smart.st8050b_demo.bean.OperatorInfo;
import com.topwise.cloudpos.aidl.AidlDeviceService;
import com.topwise.cloudpos.aidl.system.AidlSystem;

import java.util.ArrayList;
import java.util.List;

public class SystemInfoActivity extends BaseActivity {

    private ListView listView;
    private List<OperatorInfo> operatorInfoList = new ArrayList<>();
    private OperatorInfoAdapter adapter = null;
    private AidlSystem systemInf = null;
    private TextView txt_log = null;

    private final int SHOW_MESSAGE = 0;
    private String TAG = "SystemInfoActivity";
    private String strmess = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info);
        initView();
        initData();
    }

    @Override
    public void onDeviceConnected(AidlDeviceService serviceManager) {
        try {
            systemInf = AidlSystem.Stub.asInterface(serviceManager
                    .getSystemService());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void initView()
    {
        String title = "";
        Intent intent = this.getIntent();
        if(intent!=null)
        {
            title = intent.getExtras().getString("Name");
        }
        listView = (ListView)findViewById(R.id.list);
        listView.setOnItemClickListener(onItemClickListener);
        txt_log = (TextView)findViewById(R.id.txt_log);
        txt_log.setMovementMethod(ScrollingMovementMethod.getInstance());
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView)findViewById(R.id.header_title)).setText(title);
    }

    private void initData()
    {
        operatorInfoList.clear();
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getSerialNo),"getSerialNo".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getKsn),"getKsn".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getDriverVersion),"getDriverVersion".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getSDKVersion),"getSDKVersion".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.updateSysTime),"updateSysTime".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getStoragePath),"getStoragePath".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.updateDriver),"updateDriver".toLowerCase()));
        //operatorInfoList.add(new OperatorInfo(this.getString(R.string.updateSysTime),"updateSysTime".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getIMSI),"getIMSI".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getModel),"getModel".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getICCID),"getICCID".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getIMEI),"getIMEI".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getHardwareInfo),"getHardwareInfo".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getSecurityDriverInfo),"getSecurityDriverInfo".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getAndroidOsVersion),"getAndroidOsVersion".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getAndroidRomVersion),"getAndroidRomVersion".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getKernelVersion),"getKernelVersionInfo".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.reboot),"reboot".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.updateFirmware),"updateFirmware".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.getUpdateFirmwareState),"getUpdateFirmwareState".toLowerCase()));
        operatorInfoList.add(new OperatorInfo(this.getString(R.string.setAPN),"setAPN".toLowerCase()));


        adapter = new OperatorInfoAdapter(this,operatorInfoList);
        adapter.setData(operatorInfoList);
        listView.setAdapter(adapter);
    }

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position>operatorInfoList.size()) return;
            String operatorname = operatorInfoList.get(position).get_OperatorName();
            String operatorvalue = operatorInfoList.get(position).get_OperatorValue();
            if(operatorvalue.toLowerCase().equals("getSerialNo".toLowerCase()))
            {
                getTerminalSn(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getKsn".toLowerCase()))
            {
                getKSN(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getDriverVersion".toLowerCase()))
            {
                getDriverVersion(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getSDKVersion".toLowerCase()))
            {
                getCurrentSdkVersion(operatorname);
            }else if(operatorvalue.toLowerCase().equals("updateSysTime".toLowerCase()))
            {
                updateSysTime(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getStoragePath".toLowerCase()))
            {
                getStoragePath(operatorname);
            }else if(operatorvalue.toLowerCase().equals("updateDriver".toLowerCase()))
            {
                updateDriver(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getIMSI".toLowerCase()))
            {
                getIMSI(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getModel".toLowerCase()))
            {
                getModel(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getICCID".toLowerCase()))
            {
                getICCID(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getIMEI".toLowerCase()))
            {
                getIMEI(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getHardwareInfo".toLowerCase()))
            {
                getHardwareInfo(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getSecurityDriverInfo".toLowerCase()))
            {
                getSecurityDriverInfo(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getAndroidOsVersion".toLowerCase()))
            {
                getAndroidOsVersion(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getAndroidRomVersion".toLowerCase()))
            {
                getAndroidRomVersion(operatorname);
            }else if(operatorvalue.toLowerCase().equals("getKernelVersionInfo".toLowerCase()))
            {
                getKernelVersionInfo(operatorname);
            }else if(operatorvalue.toLowerCase().equals("reboot".toLowerCase()))
            {
                reboot(operatorname);
            }else if(operatorvalue.toLowerCase().equals("updateFirmware".toLowerCase()))
            {
                //reboot(operatorname);
            }

        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case SHOW_MESSAGE:
                    String mess = msg.obj.toString();
                    txt_log.append(mess + "\n");
                    int offset=txt_log.getLineCount()*txt_log.getLineHeight();
                    if(offset>txt_log.getHeight()){
                        txt_log.scrollTo(0,offset-txt_log.getHeight());
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };


    private void sendmessage(String mess)
    {
        Message msg = mHandler.obtainMessage();
        msg.what =SHOW_MESSAGE;
        msg.obj=mess;
        mHandler.sendMessage(msg);
    }

    /**
     * 读取终端序列号
     *
     */
    public void getTerminalSn(String val) {
        try {
            String terminalSn = systemInf.getSerialNo();
            strmess = val + ":"  + (TextUtils.isEmpty(terminalSn) ? "null" : terminalSn);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }


    /**
     * 读取KSN号
     *

     */
    public void getKSN(String val) {
        try {
            String ksn = systemInf.getKsn();
            if (TextUtils.isEmpty(ksn)) {
                strmess = val + " is null";
            }
            else {
                Log.d(TAG,"KSN:" + ksn);
                strmess = val + ksn;
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = "read error";
        }
        sendmessage(strmess);
    }

    public void getDriverVersion(String val) {
        try {
            String driverVesion = systemInf.getDriverVersion();
            strmess = val + ":" +  (driverVesion == null ? "null"
                    : driverVesion);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void getCurrentSdkVersion(String val) {
        try {
            String curSdkVesion = systemInf.getCurSdkVersion();
            strmess = val + ":" + (curSdkVesion == null ? "null"
                    : curSdkVesion);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void updateSysTime(String val) {
        try {
            boolean flag = systemInf.updateSysTime("2018-07-01 12:34:12");
            if (flag) {
                strmess = val +  ":success";
            } else {
                strmess = val +  ":faile";
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val +  ":error";
        }
        sendmessage(strmess);
    }

    public void getStoragePath(String val) {
        try {
            String filePath = systemInf.getStoragePath();
            strmess = val + ":" +filePath;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sendmessage(strmess);
    }

    public void updateDriver(String val) {
        try {
            systemInf.update(0x00);
            strmess = val + ":success";
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void getIMSI(String val) {
        try {
            String imsi = systemInf.getIMSI();
            strmess = val + ":" + imsi;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }
    public void getModel(String val) {
        try {
            String model = systemInf.getModel();
            strmess = val + ":" + model;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void getICCID(String val) {
        try {
            String model = systemInf.getICCID();
            strmess = val + ":" + model;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void getIMEI(String val) {
        try {
            String imsi = systemInf.getIMEI();
            strmess = val + ":" + imsi;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void getHardwareInfo(String val) {
        try {
            String hardWareInfo = systemInf.getHardWireVersion();
            strmess = val + ":" + hardWareInfo;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void getSecurityDriverInfo(String val) {
        try {
            String securityDriverInfo = systemInf.getSecurityDriverVersion();
            strmess = val + ":" + securityDriverInfo;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }



    public void getAndroidOsVersion(String val) {
        try {
            String androidOsVersion = systemInf.getAndroidOsVersion();
            strmess = val + ":" + androidOsVersion;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void getAndroidRomVersion(String val) {
        try {
            String romVersion = systemInf.getRomVersion();
            strmess = val + ":" + romVersion;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void getKernelVersionInfo(String val){
        try {
            String kernelVersion = systemInf.getAndroidKernelVersion();
            strmess = val + ":" + kernelVersion;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            strmess = val + ":error";
        }
        sendmessage(strmess);
    }

    public void reboot(String val) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle("Reboot")
                .setMessage(SystemInfoActivity.this.getString(R.string.reboot_confirm))
                .setNegativeButton(SystemInfoActivity.this.getString(R.string.cancel), null)
                .setPositiveButton(SystemInfoActivity.this.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            systemInf.reboot();
                            //showMessage("重启终端成功");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
        dialog.show();
    }
}
