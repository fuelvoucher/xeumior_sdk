package com.smart.st8050b_demo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.smart.st8050b_demo.R;
import com.smart.st8050b_demo.adapter.OperatorInfoAdapter;
import com.smart.st8050b_demo.bean.OperatorInfo;
import com.smart.st8050b_demo.util.BaseUtils;
import com.smart.st8050b_demo.util.HexUtil;
import com.topwise.cloudpos.aidl.AidlDeviceService;
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad;
import com.topwise.cloudpos.aidl.pinpad.GetPinListener;
import com.topwise.cloudpos.data.PinpadConstant;

import java.util.ArrayList;
import java.util.List;

public class PinpadActivity extends BaseActivity {

    private ListView listView;
    private List<OperatorInfo> operatorInfoList = new ArrayList<>();
    private OperatorInfoAdapter adapter = null;
    private TextView txt_log = null;
    private final int SHOW_MESSAGE = 0;
    private String TAG = "PinpadActivity";
    private String strmess = "";
    private String activity_name = "";

    private AidlPinpad pinpad = null; // 密码键盘接口
    private AidlDeviceService serviceManager = null;

    private boolean supportExtPinPad = false;
    private int pinPadType = PinpadConstant.PinpadId.BUILTIN;
    private int pinPadMode = 1;
    int mMainKey = 0x01;//主密钥
    int mWorkKey = 0x01;//工作密钥
    private RadioButton mPinPadMode0;
    private RadioButton mPinPadMode1;

    private View mDialogContent;
    private EditText mDialogContentMainKeyNumEdt,mDialogContentMainKeyValEdt;
    private TextView mDialogContentMainKeyNum,mDialogContentMainKeyVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinpad);
        initView();
        initData();
    }

    @Override
    public void onDeviceConnected(AidlDeviceService serviceManager) {
        this.serviceManager = serviceManager;
        if (null != serviceManager) {
            try {
                if(!supportExtPinPad && pinPadType == PinpadConstant.PinpadId.EXTERNAL){
                    pinPadType = PinpadConstant.PinpadId.BUILTIN;
                }
                pinpad = AidlPinpad.Stub.asInterface(serviceManager
                        .getPinPad(pinPadType)); // 默认内置密码键盘
                pinpad.setPinKeyboardMode(pinPadMode);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void initView()
    {
        String title = "";
        Intent intent = this.getIntent();
        if(intent!=null)
        {
            title = intent.getExtras().getString("Name");
            activity_name = intent.getExtras().getString("Val");
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

        mPinPadMode0 = (RadioButton)findViewById(R.id.pinpad_mode0);
        mPinPadMode1 = (RadioButton)findViewById(R.id.pinpad_mode1);

        ((RadioGroup)findViewById(R.id.pinpad_mode)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch(checkedId)
                {
                    case R.id.pinpad_mode0:
                        setPinPadMode0();
                        break;
                    case R.id.pinpad_mode1:
                        setPinPadMode1();
                        break;
                }
            }
        });


    }

    private void initData()
    {
        operatorInfoList.clear();
        if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PINPAD)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_read_pin), "pinpad_read_pin".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_set_pin_second), "pinpad_set_pin_second".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_get_dukpt_pin), "pinpad_get_dukpt_pin".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_dukpt_inject), "pinpad_dukpt_inject".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_dukpt_get_mac), "pinpad_dukpt_get_mac".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_input_masterkey), "pinpad_input_masterkey".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_input_pik), "pinpad_input_pik".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_input_tdk), "pinpad_input_tdk".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_input_masterkey_mak), "pinpad_input_masterkey_mak".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_encrypt_tdk), "pinpad_encrypt_tdk".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_get_random), "pinpad_get_random".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_cancel_input_pin), "pinpad_cancel_input_pin".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_get_offline_pin), "pinpad_get_offline_pin".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_input_tek), "pinpad_input_tek".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_input_tek_encrypt_masterkey), "pinpad_input_tek_encrypt_masterkey".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_input_tek_encrypt_mak), "pinpad_input_tek_encrypt_mak".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_input_tek_pik), "pinpad_input_tek_pik".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_input_tek_tdk), "pinpad_input_tek_tdk".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_compute_mac), "pinpad_compute_mac".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_show), "pinpad_show".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.pinpad_confim_input), "pinpad_confim_input".toLowerCase()));
        }

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
            txt_log.setText("");
            if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PINPAD)) {
                if (operatorvalue.toLowerCase().equals("pinpad_read_pin".toLowerCase())) {
                    txt_log.setText("");
                    pinpad_read_pin();
                } else if (operatorvalue.toLowerCase().equals("pinpad_set_pin_second".toLowerCase())) {
                    txt_log.setText("");
                    pinpad_set_pin_second();
                } else if (operatorvalue.toLowerCase().equals("pinpad_input_masterkey".toLowerCase())) {
                    txt_log.setText("");
                    injectMain();
                } else if (operatorvalue.toLowerCase().equals("pinpad_input_pik".toLowerCase())) {
                    txt_log.setText("");
                    injectPIK();

                }else if (operatorvalue.toLowerCase().equals("pinpad_input_tdk".toLowerCase())) {
                    txt_log.setText("");
                    injectTDK();
                }else if (operatorvalue.toLowerCase().equals("pinpad_input_masterkey_mak".toLowerCase())) {
                    txt_log.setText("");
                    injectMAK();
                }else if (operatorvalue.toLowerCase().equals("pinpad_encrypt_tdk".toLowerCase())) {
                    txt_log.setText("");
                    encryptByTdk();
                }else if (operatorvalue.toLowerCase().equals("pinpad_get_random".toLowerCase())) {
                    txt_log.setText("");
                    getRandom();
                }else if (operatorvalue.toLowerCase().equals("pinpad_cancel_input_pin".toLowerCase())) {
                    txt_log.setText("");
                    cancelInputPin();
                }else if (operatorvalue.toLowerCase().equals("pinpad_get_offline_pin".toLowerCase())) {
                    txt_log.setText("");
                    inputPin2();
                }else if (operatorvalue.toLowerCase().equals("pinpad_input_tek".toLowerCase())) {
                    txt_log.setText("");
                    injectTEK();
                }else if (operatorvalue.toLowerCase().equals("pinpad_input_tek_encrypt_masterkey".toLowerCase())) {
                    txt_log.setText("");
                    injectEntryMain();
                }else if (operatorvalue.toLowerCase().equals("pinpad_input_tek_encrypt_mak".toLowerCase())) {
                    txt_log.setText("");
                    injectTWKMAK();
                }else if (operatorvalue.toLowerCase().equals("pinpad_input_tek_pik".toLowerCase())) {
                    txt_log.setText("");
                    injectTWKPIK();
                }else if (operatorvalue.toLowerCase().equals("pinpad_input_tek_tdk".toLowerCase())) {
                    txt_log.setText("");
                    injectTWKTDK();
                }else if (operatorvalue.toLowerCase().equals("pinpad_compute_mac".toLowerCase())) {
                    txt_log.setText("");
                    getMac();
                }else if (operatorvalue.toLowerCase().equals("pinpad_show".toLowerCase())) {
                    txt_log.setText("");
                    display();
                }else if (operatorvalue.toLowerCase().equals("pinpad_confim_input".toLowerCase())) {
                    txt_log.setText("");
                    confirmGetPin();
                }else if (operatorvalue.toLowerCase().equals("pinpad_get_dukpt_pin".toLowerCase())) {
                    txt_log.setText("");
                    dukptInputPin();
                }else if (operatorvalue.toLowerCase().equals("pinpad_dukpt_inject".toLowerCase())) {
                    txt_log.setText("");
                    loadDUKPTMainKey();
                }else if (operatorvalue.toLowerCase().equals("pinpad_dukpt_get_mac".toLowerCase())) {
                    txt_log.setText("");
                    dukptGetMac();
                }


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

    @SuppressLint("HandlerLeak")
    private Handler pinHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                sendmessage(msg.obj.toString());
            }
        }
    };

    /**
     * PIN输入监听器
     *
     * @author Tianxiaobo
     */
    private class MyGetPinListener extends GetPinListener.Stub {
        @Override
        public void onStopGetPin() throws RemoteException {
            pinHandler.obtainMessage(0x00,getStringByid(R.string.pinpad_cancel_pin)).sendToTarget();
        }

        @Override
        public void onInputKey(int arg0, String arg1) throws RemoteException {
            pinHandler.obtainMessage(0x00, getStar(arg0) + (arg1 == null ? "" : arg1)).sendToTarget();
        }

        @Override
        public void onError(int arg0) throws RemoteException {
            //showMessage("读取PIN输入错误，错误码" + arg0);
            pinHandler.obtainMessage(0x00,getStringByid(R.string.pinpad_error_pin) + arg0).sendToTarget();
        }

        @Override
        public void onConfirmInput(byte[] arg0) throws RemoteException {
            pinHandler.obtainMessage(0x00, getStringByid(R.string.pinpad_success_pin)
                    + (null == arg0 ?getString(R.string.pinpad_empty_pin) : getString(R.string.pinpad_is_pin)
                    + HexUtil.bcd2str(arg0))).sendToTarget();
        }

        @Override
        public void onCancelKeyPress() throws RemoteException {
            pinHandler.obtainMessage(0x00, getStringByid(R.string.pinpad_cancel_pin)).sendToTarget();
        }
    }



    // 密码键盘模式顺序
    public void setPinPadMode0() {
        try {
            pinPadMode = 0;
            mPinPadMode0.setChecked(true);
            mPinPadMode1.setChecked(false);
            boolean flag = pinpad.setPinKeyboardMode(pinPadMode);
            sendmessage("setPinPadMode0:" + flag);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 密码键盘模式乱序
    public void setPinPadMode1() {
        try {
            pinPadMode = 1;
            mPinPadMode0.setChecked(false);
            mPinPadMode1.setChecked(true);
            boolean flag = pinpad.setPinKeyboardMode(pinPadMode);
            sendmessage("setPinPadMode1:" + flag);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 输入联机PIN
     *
     */
    public void pinpad_read_pin() {
        if(!supportExtPinPad && pinPadType == PinpadConstant.PinpadId.EXTERNAL){
            sendmessage(getStringByid(R.string.pinpad_mess1));
            return;
        }
        final Bundle bundle = new Bundle();
        bundle.putInt("wkeyid", mWorkKey);
        bundle.putInt("keytype", 0x00);
        bundle.putByteArray("random", null);
        bundle.putInt("inputtimes", 1);
        bundle.putInt("minlength", 4);
        bundle.putInt("maxlength", 12);
        bundle.putString("pan", "0000000000000000");
        bundle.putString("tips", "RMB:2000.00");
        sendmessage(getStringByid(R.string.pinpad_input_pin));
        new Thread() {
            public void run() {
                try {
                    pinpad.getPin(bundle, new MyGetPinListener());
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            ;
        }.start();


    }

    /**
     * 输入PIN2次
     *
     */
    public void pinpad_set_pin_second() {
        if(!supportExtPinPad && pinPadType == PinpadConstant.PinpadId.EXTERNAL){
            sendmessage(getStringByid(R.string.pinpad_mess1));
            return;
        }
        final Bundle bundle = new Bundle();
        bundle.putInt("wkeyid", mWorkKey);
        bundle.putInt("keytype", 0x00);
        bundle.putByteArray("random", null);
        bundle.putInt("inputtimes", 2);
        bundle.putInt("minlength", 4);
        bundle.putInt("maxlength", 12);
        bundle.putString("pan", "0000000000000000");
        bundle.putString("tips", "RMB:2000.00");
        sendmessage(getStringByid(R.string.pinpad_input_pin));
        new Thread() {
            @Override
            public void run() {
                try {
                    pinpad.getPin(bundle, new MyGetPinListener());
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }



    /**
     * 注入主密钥 明文
     */
    public void injectMain() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(getStringByid(R.string.pinpad_injectMain))
                .setMessage(getStringByid(R.string.pinpad_injectMain_confirm))
                .setNegativeButton(getStringByid(R.string.cancel), null)
                .setPositiveButton(getStringByid(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
                            sendmessage(getStringByid(R.string.pinpad_master_key)+key);
                            boolean flag = pinpad.loadMainkey(mMainKey, HexUtil.hexStringToByte(key), null);

                            if (flag) {
                                sendmessage(getStringByid(R.string.pinpad_master_key_fill_success));
                            } else {
                                sendmessage(getStringByid(R.string.pinpad_master_key_fill_faile));
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
        dialog.show();
    }


    /**
     * 注入PIK
     */
    public void injectPIK() {
        try {
            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
            sendmessage(getStringByid(R.string.pinpad_pik_master)+key);
            boolean flag = pinpad
                    .loadWorkKey(
                            PinpadConstant.WKeyType.WKEY_TYPE_PIK,
                            mMainKey,
                            mWorkKey,
                            HexUtil.hexStringToByte(key), null);
//                            HexUtil.hexStringToByte("F40379AB9E0EC533F40379AB9E0EC533"),
//                            HexUtil.hexStringToByte("82E13665"));
            if (flag) {
                sendmessage(getStringByid(R.string.pinpad_pik_fill_success));
            } else {
                sendmessage(getStringByid(R.string.pinpad_pik_fill_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 注入TDK
     */
    public void injectTDK() {
        try {
            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
            sendmessage(getStringByid(R.string.pinpad_tdk_master)+key);
            boolean flag = pinpad
                    .loadWorkKey(
                            PinpadConstant.WKeyType.WKEY_TYPE_TDK,
                            mMainKey,
                            mWorkKey,
                            HexUtil.hexStringToByte(key), null);
//                            HexUtil.hexStringToByte("F40379AB9E0EC533F40379AB9E0EC533"),
//                            HexUtil.hexStringToByte("82E13665"));
            if (flag) {
                sendmessage(getStringByid(R.string.pinpad_tdk_fill_success));
            } else {
                sendmessage(getStringByid(R.string.pinpad_tdk_fill_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 注入MAK
     */
    public void injectMAK() {
        try {
            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
            sendmessage(getStringByid(R.string.pinpad_mak_master)+key);
            boolean flag = pinpad
                    .loadWorkKey(
                            PinpadConstant.WKeyType.WKEY_TYPE_MAK,
                            mMainKey,
                            mWorkKey,
                            HexUtil.hexStringToByte(key), null);
//                            HexUtil.hexStringToByte("F40379AB9E0EC533F40379AB9E0EC533"),
//                            HexUtil.hexStringToByte("82E13665"));
            if (flag) {
                sendmessage(getStringByid(R.string.pinpad_mak_fill_success));
            } else {
                sendmessage(getStringByid(R.string.pinpad_mak_fill_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * TDK数据加密
     */
    public void encryptByTdk() {
        try {
            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
            sendmessage(getStringByid(R.string.pinpad_encrypt_data)+key);
            byte[] entryData=new byte[key.length()/2];
            int flag=pinpad.encryptByTdk(mWorkKey, (byte) 0,null,HexUtil.hexStringToByte(key),entryData);
            if (flag==0) {
                sendmessage(getStringByid(R.string.pinpad_encrypt_success)+HexUtil.bcd2str(entryData));
            } else {
                sendmessage(getStringByid(R.string.pinpad_encrypt_faile)+flag);
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取随机数
     */
    public void getRandom() {
        try {
            byte[] data = pinpad.getRandom();
            if (null != data) {
                sendmessage(getStringByid(R.string.pinpad_getrandom_success) + HexUtil.bcd2str(data));
            } else {
                sendmessage(getStringByid(R.string.pinpad_getrandom_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 中断PIN输入
     */
    public void cancelInputPin() {
        try {
            pinpad.stopGetPin();
            sendmessage(getStringByid(R.string.pinpad_cancel_success));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sendmessage(getStringByid(R.string.pinpad_cancel_error));
        }
    }

    /**
     * 输入脱机PIN
     */
    public void inputPin2() {
        if(!supportExtPinPad && pinPadType == PinpadConstant.PinpadId.EXTERNAL){
            sendmessage(getStringByid(R.string.pinpad_mess1));
            return;
        }
        final Bundle bundle = new Bundle();
        bundle.putInt("wkeyid", mWorkKey);
        bundle.putInt("keytype", 0x01);
        bundle.putByteArray("random", null);
        bundle.putInt("inputtimes", 1);
        bundle.putInt("minlength", 4);
        bundle.putInt("maxlength", 12);
        bundle.putString("pan", "0000000000000000");
        bundle.putString("tips", "RMB:2000.00");
        sendmessage(getStringByid(R.string.pinpad_input_pin));
        new Thread() {
            @Override
            public void run() {
                try {
                    pinpad.getPin(bundle, new MyGetPinListener());
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 注入TEK
     */
    public void injectTEK() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(getStringByid(R.string.pinpad_injectTEK))
                .setMessage(getStringByid(R.string.pinpad_injectTEK_confirm))
                .setNegativeButton(getStringByid(R.string.cancel), null)
                .setPositiveButton(getStringByid(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
                            sendmessage(getStringByid(R.string.pinpad_TEK_key)+key);
                            boolean flag = pinpad.loadTEK(mMainKey, HexUtil.hexStringToByte(key), null);

                            if (flag) {
                                sendmessage(getStringByid(R.string.pinpad_TEK_fill_success));
                            } else {
                                sendmessage(getStringByid(R.string.pinpad_TEK_fill_faile));
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
        dialog.show();
    }

    /**
     * 注入TEK加密后的主密钥
     */
    public void injectEntryMain() {
        try {
            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
            sendmessage(getStringByid(R.string.pinpad_master)+key);
            boolean flag = pinpad.loadEncryptMainkey(mMainKey,mMainKey, HexUtil.hexStringToByte(key), null);

            if (flag) {
                sendmessage(getStringByid(R.string.pinpad_master_fill_success));
            } else {
                sendmessage(getStringByid(R.string.pinpad_master_fill_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 注入TEK加密后的MAK
     */
    public void injectTWKMAK() {
        try {
            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
            sendmessage(getStringByid(R.string.pinpad_mak_tek_master)+key);
            boolean flag = pinpad.loadTWK(PinpadConstant.WKeyType.WKEY_TYPE_MAK,mMainKey,mWorkKey, HexUtil.hexStringToByte(key), null);

            if (flag) {
                sendmessage(getStringByid(R.string.pinpad_mak_tek_fill_success));
            } else {
                sendmessage(getStringByid(R.string.pinpad_mak_tek_fill_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 注入TEK加密后的PIK
     */
    public void injectTWKPIK() {
        try {
            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
            sendmessage(getStringByid(R.string.pinpad_pik_tek_master)+key);
            boolean flag = pinpad.loadTWK(PinpadConstant.WKeyType.WKEY_TYPE_PIK,mMainKey,mWorkKey, HexUtil.hexStringToByte(key), null);

            if (flag) {
                sendmessage(getStringByid(R.string.pinpad_pik_tek_fill_success));
            } else {
                sendmessage(getStringByid(R.string.pinpad_pik_tek_fill_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 注入TEK加密后的TDK
     */
    public void injectTWKTDK() {
        try {
            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
            sendmessage(getStringByid(R.string.pinpad_tdk_tek_master)+key);
            boolean flag = pinpad.loadTWK(PinpadConstant.WKeyType.WKEY_TYPE_TDK,mMainKey,mWorkKey, HexUtil.hexStringToByte(key), null);

            if (flag) {
                sendmessage(getStringByid(R.string.pinpad_tdk_tek_fill_success));
            } else {
                sendmessage(getStringByid(R.string.pinpad_tdk_tek_fill_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 执行MAC计算
     */
    public void getMac() {
        int retCode = -1;
        Bundle bundle = new Bundle();
        bundle.putInt("wkeyid", mWorkKey);
        bundle.putByteArray(
                "data",
                HexUtil.hexStringToByte("F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533"));
        bundle.putByteArray("random", null);
        bundle.putInt("type", 0x00);
        byte[] mac = new byte[8];
        try {
            retCode = pinpad.getMac(bundle, mac);
            if (retCode != 0x00) {
                sendmessage(getStringByid(R.string.pinpad_mac_error) + retCode);
            } else {
                sendmessage(getStringByid(R.string.pinpad_mac_error1) + HexUtil.bcd2str(mac));
            }
            bundle.putInt("type", 0x01);
            retCode = pinpad.getMac(bundle, mac);
            if (retCode != 0x00) {
                sendmessage(getStringByid(R.string.pinpad_mac_error) + retCode);
            } else {
                sendmessage(getStringByid(R.string.pinpad_mac_error2) + HexUtil.bcd2str(mac));
            }

            bundle.putInt("type", 0x00);
            bundle.putByteArray("random", new byte[]{0x00, 0x01, 0x02, 0x03,
                    0x04, 0x05, 0x06, 0x07});
            retCode = pinpad.getMac(bundle, mac);
            if (retCode != 0x00) {
                sendmessage(getStringByid(R.string.pinpad_mac_error) + retCode);
            } else {
                sendmessage(getStringByid(R.string.pinpad_mac_error3) + HexUtil.bcd2str(mac));
            }
            bundle.putInt("type", 0x01);
            retCode = pinpad.getMac(bundle, mac);
            if (retCode != 0x00) {
                sendmessage(getStringByid(R.string.pinpad_mac_error) + retCode);
            } else {
                sendmessage(getStringByid(R.string.pinpad_mac_error4) + HexUtil.bcd2str(mac));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 外接密码键盘显示
     */
    public void display() {
        try {
            boolean flag = pinpad.display(getStringByid(R.string.pinpad_balance), "2000.00");
            if (flag) {
                sendmessage(getStringByid(R.string.pinpad_display_success));
            } else {
                sendmessage(getStringByid(R.string.pinpad_display_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 确认PIn输入
     */
    public void confirmGetPin() {
        try {
            pinpad.confirmGetPin();
            sendmessage(getStringByid(R.string.pinpad_confirm_pin_success));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void dukptInputPin() {
        if(!supportExtPinPad && pinPadType == PinpadConstant.PinpadId.EXTERNAL){
            sendmessage(getResources().getString(R.string.pinpad_mess1));
            return;
        }
        final Bundle bundle = new Bundle();
        bundle.putInt("wkeyid", mWorkKey);
        bundle.putInt("key_type", 13);
        bundle.putByteArray("random", null);
        bundle.putInt("inputtimes", 1);
        bundle.putInt("minlength", 4);
        bundle.putInt("maxlength", 12);
        bundle.putString("pan", "0000000000000000");
        bundle.putString("tips", "RMB:2000.00");
        sendmessage(getResources().getString(R.string.pinpad_input_pin));
        new Thread() {
            public void run() {
                try {
                    pinpad.getPin(bundle, new MyGetPinListener());
                    sendmessage( getString(R.string.pinpad_get_ksn)+HexUtil.bcd2str(pinpad.getDUKPTKsn(mWorkKey,false)));
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            ;
        }.start();


    }


    private void initDialogMainKey() {
        mDialogContent = View.inflate(this, R.layout.setting_put_mainkey_dialog, null);
        mDialogContentMainKeyNum = (TextView) mDialogContent.findViewById(R.id.mainkey_num_text);
        mDialogContentMainKeyNumEdt = (EditText) mDialogContent.findViewById(R.id.mainkey_num_edt);
        mDialogContentMainKeyVal= (TextView) mDialogContent.findViewById(R.id.mainkey_val_text);
        mDialogContentMainKeyValEdt= (EditText) mDialogContent.findViewById(R.id.mainkey_val_edt);
        mDialogContentMainKeyNum.setText(this.getString(R.string.input_ksn));
        mDialogContentMainKeyVal.setText(this.getString(R.string.input_dukpt_key));
    }

    public void loadDUKPTMainKey() {
        AlertDialog.Builder dialogBuilder3 = new AlertDialog.Builder(this);
        initDialogMainKey();
        dialogBuilder3.setView(mDialogContent)
                .setTitle(getResources().getString(R.string.pinpad_inject_dukpt_key))
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .setPositiveButton(getResources().getString(R.string.ok), null);
        final AlertDialog dialog3 = dialogBuilder3.show();
        dialog3.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
//                            String key=HexUtil.bcd2str(pinpad.getRandom())+HexUtil.bcd2str(pinpad.getRandom());
//                            String key = "1c323626c2197cb7";

                            String ksn = mDialogContentMainKeyNumEdt.getText().toString();
                            String dukpt = mDialogContentMainKeyValEdt.getText().toString();

                            String regex = "^[\\d|A-F]*$";
                            if(ksn != null && dukpt != null){

                                sendmessage("ksn = " + ksn.toUpperCase() + "  dukpt="+dukpt.toUpperCase());
                                if(ksn.length() != 20 || dukpt.length() != 32){
                                    sendmessage(getString(R.string.pinpad_input_error_lenght));
                                    dialog3.dismiss();
                                    return;
                                }

                                if(!ksn.toUpperCase().matches(regex) || !dukpt.toUpperCase().matches(regex)){
                                    sendmessage(getString(R.string.pinpad_please_input_code));
                                    dialog3.dismiss();
                                    return;
                                }
                            }


                            boolean flag = pinpad.loadDuKPTkey(0,mWorkKey,HexUtil.hexStringToByte(dukpt.toUpperCase()),
                                    HexUtil.hexStringToByte(ksn.toUpperCase()));

                            if (flag) {
                                sendmessage(getResources().getString(R.string.pin_dukpt_key_inject_success));
                                dialog3.dismiss();
                            } else {
                                sendmessage(getResources().getString(R.string.pin_dukpt_key_inject_fail));
                                dialog3.dismiss();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    public void dukptGetMac() {
        int retCode = -1;
        Bundle bundle = new Bundle();
        bundle.putInt("wkeyid", mWorkKey);
        bundle.putInt("key_type", 12);
        bundle.putByteArray(
                "data",
                HexUtil.hexStringToByte("F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533F40379AB9E0EC533"));
        bundle.putByteArray("random", null);
        bundle.putInt("type", 0x00);
        byte[] mac = new byte[8];
        try {
            sendmessage( getString(R.string.pinpad_get_ksn)+HexUtil.bcd2str(pinpad.getDUKPTKsn(mWorkKey, false)));
            retCode = pinpad.getMac(bundle, mac);
            if (retCode != 0x00) {
                sendmessage(getResources().getString(R.string.pin_mac_error_code) + retCode);
            } else {
                sendmessage(getResources().getString(R.string.pin_mac_919_mac_success1) + HexUtil.bcd2str(mac));
            }
            bundle.putInt("type", 0x01);
            sendmessage( getString(R.string.pinpad_get_ksn)+HexUtil.bcd2str(pinpad.getDUKPTKsn(mWorkKey, false)));
            retCode = pinpad.getMac(bundle, mac);
            if (retCode != 0x00) {
                sendmessage(getResources().getString(R.string.pin_mac_error_code) + retCode);
            } else {
                sendmessage(getResources().getString(R.string.pin_mac_ecb_mac_success1) + HexUtil.bcd2str(mac));
            }

            bundle.putInt("type", 0x00);
            bundle.putByteArray("random", new byte[]{0x00, 0x01, 0x02, 0x03,
                    0x04, 0x05, 0x06, 0x07});
            sendmessage( getString(R.string.pinpad_get_ksn)+HexUtil.bcd2str(pinpad.getDUKPTKsn(mWorkKey, false)));
            retCode = pinpad.getMac(bundle, mac);
            if (retCode != 0x00) {
                sendmessage(getResources().getString(R.string.pin_mac_error_code) + retCode);
            } else {
                sendmessage(getResources().getString(R.string.pin_mac_919_mac_success2) + HexUtil.bcd2str(mac));
            }
            bundle.putInt("type", 0x01);
            sendmessage( getString(R.string.pinpad_get_ksn)+HexUtil.bcd2str(pinpad.getDUKPTKsn(mWorkKey, false)));
            retCode = pinpad.getMac(bundle, mac);
            if (retCode != 0x00) {
                sendmessage(getResources().getString(R.string.pin_mac_error_code) + retCode);
            } else {
                sendmessage(getResources().getString(R.string.pin_mac_ecb_mac_success2) + HexUtil.bcd2str(mac));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    /**
     * 获取指定长度的*
     *
     * @param len
     * @return
     */
    public String getStar(int len) {
        String str = "";
        while (len > 0) {
            str += "*";
            len--;
        }
        return str;
    }



}
