package com.smart.st8050b_demo.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
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
import com.smart.st8050b_demo.util.BaseUtils;
import com.smart.st8050b_demo.util.ByteUtils;
import com.smart.st8050b_demo.util.HexUtil;
import com.smart.st8050b_demo.util.NfcAutoCheck;
import com.smart.st8050b_demo.util.PBOCLIB;
import com.topwise.cloudpos.aidl.AidlDeviceService;
import com.topwise.cloudpos.aidl.magcard.AidlMagCard;
import com.topwise.cloudpos.aidl.rfcard.AidlRFCard;
import com.topwise.cloudpos.data.RFCard;

import java.util.ArrayList;
import java.util.List;

public class RFCardActivity extends BaseActivity {
    private ListView listView;
    private ListView listView_cardtype;
    private List<OperatorInfo> operatorInfoList_cardtype = new ArrayList<>();
    private OperatorInfoAdapter adapter_cardtype = null;

    public AidlRFCard rfcard = null;

    private List<OperatorInfo> operatorInfoList = new ArrayList<>();
    private OperatorInfoAdapter adapter = null;
    private AidlMagCard magCardDev = null;
    private TextView txt_log = null;

    private final int SHOW_MESSAGE = 0;
    private String TAG = "SwipeCardActivity";
    private String strmess = "";
    private String activity_name = "";

    //pboc
    private PBOCLIB pboclib = new PBOCLIB();
    byte[] lastapdu = null;
    private boolean isReadCardId = false;

    private NfcAutoCheck nfcAutoCheck = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfcard);
        initView();
        initData();
    }

    @Override
    public void onDeviceConnected(AidlDeviceService serviceManager) {
        try {
            rfcard = AidlRFCard.Stub
                    .asInterface(serviceManager.getRFIDReader());
            setOnCheckListen();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy(){
        close();
        super.onDestroy();
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

        listView_cardtype= (ListView)findViewById(R.id.list_cardtype);
        listView_cardtype.setOnItemClickListener(onItemClickListener_cardtype);

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

    private void setOnCheckListen()
    {
        if(rfcard!=null){
            nfcAutoCheck = new NfcAutoCheck(rfcard);
            nfcAutoCheck.setOnDataReceiveListener(new NfcAutoCheck.OnCheckCardListener() {
                @Override
                public void onCheckCardReceive(boolean isexist,int cardtype) {
                    if(isexist)
                    {
                        sendmessage("card is exists" + " Card type is " + getCardType(cardtype));
                    }else
                    {
                        sendmessage("card is not exists");
                    }
                    nfcAutoCheck.startAutoCheck(60);
                }

                @Override
                public void onCheckCardReceive(boolean isexist, int cardtype, String uid) {

                }
            });
        }
    }

    private void initData()
    {
        operatorInfoList_cardtype.clear();
        if(activity_name.equals(BaseUtils.ACTIVITY_NAME_NFC)) {
            operatorInfoList_cardtype.add(new OperatorInfo(this.getString(R.string.card_type_m1), "card_type_m1".toLowerCase()));
            operatorInfoList_cardtype.add(new OperatorInfo(this.getString(R.string.card_type_nfc_cpu), "card_type_nfc_cpu".toLowerCase()));
            operatorInfoList_cardtype.add(new OperatorInfo(this.getString(R.string.card_type_auto), "card_type_auto".toLowerCase()));
            operatorInfoList_cardtype.add(new OperatorInfo(this.getString(R.string.ntag), "card_type_ntag".toLowerCase()));
            operatorInfoList_cardtype.add(new OperatorInfo(this.getString(R.string.Desfire), "card_type_desfire".toLowerCase()));
        }
        adapter_cardtype = new OperatorInfoAdapter(this,operatorInfoList_cardtype);
        adapter_cardtype.setData(operatorInfoList_cardtype);
        listView_cardtype.setAdapter(adapter_cardtype);
    }

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position > operatorInfoList.size()) return;
            String operatorname = operatorInfoList.get(position).get_OperatorName();
            String operatorvalue = operatorInfoList.get(position).get_OperatorValue();
            if(activity_name.equals(BaseUtils.ACTIVITY_NAME_NFC)) {
                if (operatorvalue.toLowerCase().equals("nfc_open".toLowerCase())) {
                    open();
                }else if (operatorvalue.toLowerCase().equals("nfc_reset".toLowerCase())) {
                    reset();
                }else if (operatorvalue.toLowerCase().equals("nfc_exists".toLowerCase())) {
                    isExists();
                }else if (operatorvalue.toLowerCase().equals("nfc_cardtype".toLowerCase())) {
                    readCardType();
                }else if (operatorvalue.toLowerCase().equals("nfc_auth".toLowerCase())) {
                    auth();
                }else if (operatorvalue.toLowerCase().equals("nfc_readblock".toLowerCase())) {
                    readBlockData();
                }else if (operatorvalue.toLowerCase().equals("nfc_writeblock".toLowerCase())) {
                    writeBlock();
                }else if (operatorvalue.toLowerCase().equals("nfc_halt".toLowerCase())) {
                    halt();
                }else if (operatorvalue.toLowerCase().equals("nfc_apdu".toLowerCase())) {
                    apduComm();
                }else if (operatorvalue.toLowerCase().equals("nfc_pboc".toLowerCase())) {
                    nfc_pboc();
                }else if (operatorvalue.toLowerCase().equals("nfc_autoread".toLowerCase())) {
                    start_autoRead();
                }else if (operatorvalue.toLowerCase().equals("ntag_open".toLowerCase())) {
                    open();
                }else if (operatorvalue.toLowerCase().equals("ntag_reset".toLowerCase())) {
                    reset();
                }else if (operatorvalue.toLowerCase().equals("ntag_exists".toLowerCase())) {
                    isExists();
                }else if (operatorvalue.toLowerCase().equals("ntag_cardtype".toLowerCase())) {
                    readCardType();
                }else if (operatorvalue.toLowerCase().equals("ntag_auth".toLowerCase())) {
                    auth(4);
                }else if (operatorvalue.toLowerCase().equals("ntag_readblock".toLowerCase())) {
                    readBlockData(4);
                }else if (operatorvalue.toLowerCase().equals("ntag_writeblock".toLowerCase())) {
                    writeBlock(4);
                }else if (operatorvalue.toLowerCase().equals("ntag_halt".toLowerCase())) {
                    halt();
                }else if (operatorvalue.toLowerCase().equals("desfire_open".toLowerCase())) {
                    open();
                }else if (operatorvalue.toLowerCase().equals("desfire_reset".toLowerCase())) {
                    reset();
                }else if (operatorvalue.toLowerCase().equals("desfire_cardtype".toLowerCase())) {
                    readCardType();
                }else if (operatorvalue.toLowerCase().equals("desfire_cardtype".toLowerCase())) {
                    readCardType();
                }else if (operatorvalue.toLowerCase().equals("desfire_cardtype".toLowerCase())) {
                    readCardType();
                }else if (operatorvalue.toLowerCase().equals("desfire_cardid".toLowerCase())) {
                    getCardIdDesfire();
                }else if (operatorvalue.toLowerCase().equals("desfire_version".toLowerCase())) {

                }
            }


        }
    };

    private final AdapterView.OnItemClickListener onItemClickListener_cardtype = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(position>operatorInfoList_cardtype.size()) return;
            String operatorname = operatorInfoList_cardtype.get(position).get_OperatorName();
            String operatorvalue = operatorInfoList_cardtype.get(position).get_OperatorValue();

            if (activity_name.equals(BaseUtils.ACTIVITY_NAME_NFC)) {
                if (operatorvalue.toLowerCase().equals("card_type_m1".toLowerCase())) {
                    init_data_listView(operatorvalue.toLowerCase());
                } else if (operatorvalue.toLowerCase().equals("card_type_nfc_cpu".toLowerCase())) {
                    init_data_listView(operatorvalue.toLowerCase());
                }else if (operatorvalue.toLowerCase().equals("card_type_auto".toLowerCase())) {
                    init_data_listView(operatorvalue.toLowerCase());
                }else if (operatorvalue.toLowerCase().equals("card_type_ntag".toLowerCase())) {
                    init_data_listView(operatorvalue.toLowerCase());
                }else if(operatorvalue.toLowerCase().equals("card_type_desfire".toLowerCase())){
                    init_data_listView(operatorvalue.toLowerCase());
                }
            }




        }

    };

    private void init_data_listView(String val)
    {
        if (val.toLowerCase().equals("card_type_m1".toLowerCase())) {
            operatorInfoList.clear();
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_open), "nfc_open".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_exists), "nfc_exists".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_cardtype), "nfc_cardtype".toLowerCase()));
            //operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_reset), "nfc_reset".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_auth), "nfc_auth".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_readblock), "nfc_readblock".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_writeblock), "nfc_writeblock".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_halt), "nfc_halt".toLowerCase()));
        }else if (val.toLowerCase().equals("card_type_nfc_cpu".toLowerCase())) {
            operatorInfoList.clear();
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_open), "nfc_open".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_cardtype), "nfc_cardtype".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_reset), "nfc_reset".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_apdu), "nfc_apdu".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_pboc), "nfc_pboc".toLowerCase()));
        }else if (val.toLowerCase().equals("card_type_auto".toLowerCase())) {
            operatorInfoList.clear();
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_autoread), "nfc_autoread".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_autoread_stop), "nfc_autoread_stop".toLowerCase()));
        }else if (val.toLowerCase().equals("card_type_ntag".toLowerCase())) {
            operatorInfoList.clear();
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ntag_open), "ntag_open".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ntag_reset), "ntag_reset".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ntag_exists), "ntag_exists".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ntag_cardtype), "ntag_cardtype".toLowerCase()));
            //operatorInfoList.add(new OperatorInfo(this.getString(R.string.ntag_auth), "ntag_auth".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ntag_readblock), "ntag_readblock".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ntag_writeblock), "ntag_writeblock".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ntag_halt), "ntag_halt".toLowerCase()));
        }else if (val.toLowerCase().equals("card_type_desfire".toLowerCase())) {
            operatorInfoList.clear();
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.Desfire_open), "desfire_open".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.Desfire_reset), "desfire_reset".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.Desfire_cardtype), "desfire_cardtype".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.Desfire_cardid), "desfire_cardid".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.Desfire_version), "desfire_version".toLowerCase()));
        }
        adapter = new OperatorInfoAdapter(this,operatorInfoList);
        adapter.setData(operatorInfoList);
        listView.setAdapter(adapter);
    }

    private void sendmessage(String mess)
    {
        Message msg = mHandler.obtainMessage();
        msg.what =SHOW_MESSAGE;
        msg.obj=mess;
        mHandler.sendMessage(msg);
    }

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

    // 打开设备
    public void open() {
        try {
            boolean flag = rfcard.open();
            if (flag) {
                sendmessage(getStringByid(R.string.nfc_open_success));
            } else {
                sendmessage(getStringByid(R.string.nfc_open_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 关闭设备
    public void close() {
        try {
            boolean flag = rfcard.close();
            if (flag) {
                sendmessage(getStringByid(R.string.nfc_close_success));
            } else {
                sendmessage(getStringByid(R.string.nfc_close_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 检测是否为在位
    public void isExists() {
        try {
            boolean flag = rfcard.isExist();
            if (flag) {
                sendmessage(getStringByid(R.string.nfc_card_exist));
            } else {
                sendmessage(getStringByid(R.string.nfc_card_notexist));
            }
            getCardId();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getCardId()
    {
        try
        {
            byte[] id = rfcard.getCardCode();
            if(id!=null){
                sendmessage("Uid:" + HexUtil.bcd2str(id));
            }else
            {
                sendmessage("Uid is null");
            }

        }catch(RemoteException e){

        }
    }


    public void getCardIdDesfire()
    {
        try
        {
            byte[] tmpid = new byte[7];
            byte[] id = rfcard.getCardCode();
            if(id!=null&&id.length>7){
                System.arraycopy(id,0,tmpid,0,7);
            }
            if(id!=null){
                sendmessage("Uid:" + HexUtil.bcd2str(tmpid));
            }else
            {
                sendmessage("Uid is null");
            }

        }catch(RemoteException e){

        }
    }

    // 上电复位
    public void reset() {
        try {
            int type = rfcard.getCardType();
            byte[] data = rfcard.reset(type);
            if (null != data) {
                sendmessage(getStringByid(R.string.ic_reset_result)+ HexUtil.bcd2str(data));
            } else {
                sendmessage(getStringByid(R.string.ic_reset_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 断开
    public void halt() {
        try {
            int ret = rfcard.halt();
            if (ret == 0x00) {
                sendmessage(getStringByid(R.string.nfc_halt_success));
            } else {
                sendmessage(getStringByid(R.string.nfc_halt_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 读取卡类型
    public void readCardType() {
        try {
            int type = rfcard.getCardType();
            if(type == 0) {
                sendmessage(getStringByid(R.string.nfc_read_cardtype_faile));
            } else {
                sendmessage(getStringByid(R.string.nfc_read_cardtype_success) + type);
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sendmessage(getStringByid(R.string.nfc_read_cardtype_faile));
        }
    }

    // apdu通讯
    public void apduComm() {
        try {
            byte[] apdu = HexUtil
                    .hexStringToByte("00A404000E315041592E5359532E4444463031");
            byte[] data = rfcard.apduComm(apdu);
            if (data != null) {
                sendmessage(getStringByid(R.string.nfc_apdu_success) + HexUtil.bcd2str(data));
            } else {
                sendmessage(getStringByid(R.string.nfc_apdu_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // 认证
    public void auth() {
        try {
            int cardType = rfcard.getCardType();
            byte[] resetData = rfcard.reset(cardType);
            if (resetData==null){
                sendmessage(getStringByid(R.string.nfc_card_notexist));
                return;
            }
            int retCode = rfcard.auth((byte) 0x00, (byte) 0x01, new byte[] {
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, resetData);
            sendmessage(getStringByid(R.string.nfc_auth_result)+ retCode);
            if (0x00 == retCode) {
                sendmessage(getStringByid(R.string.nfc_auth_success));
            } else {
                sendmessage(getStringByid(R.string.nfc_auth_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void auth(int block) {
        try {
            int cardType = rfcard.getCardType();
            byte[] resetData = rfcard.reset(cardType);
            if (resetData==null){
                sendmessage(getStringByid(R.string.nfc_card_notexist));
                return;
            }
            int retCode = rfcard.auth((byte) 0x00, (byte) block, new byte[] {
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, resetData);
            sendmessage(getStringByid(R.string.nfc_auth_result)+ retCode);
            if (0x00 == retCode) {
                sendmessage(getStringByid(R.string.nfc_auth_success));
            } else {
                sendmessage(getStringByid(R.string.nfc_auth_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // 读取块数据
    public void readBlockData() {
        try {
            byte[] data = new byte[16];
            int length = rfcard.readBlock((byte) 0x01, data);
            if (length == 0x00) {
                //showMessage("读取块数据成功" + HexUtil.bcd2str(data));
                sendmessage(getStringByid(R.string.nfc_read_success) + HexUtil.bcd2str(data));
            } else {
                sendmessage(getStringByid(R.string.nfc_read_faile));
            }

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 读取块数据
    public void readBlockData(int block) {
        try {
            byte[] data = new byte[16];
            int length = rfcard.readBlock((byte) block, data);
            if (length == 0x00) {
                //showMessage("读取块数据成功" + HexUtil.bcd2str(data));
                sendmessage(getStringByid(R.string.nfc_read_success) + HexUtil.bcd2str(data));
            } else {
                sendmessage(getStringByid(R.string.nfc_read_faile));
            }

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // 写块数据
    public void writeBlock() {
        try {
            int retCode = rfcard.writeBlock((byte) 0x01, new byte[] {
                    (byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,
                    (byte)0xFF,(byte)0xFE,(byte)0xFF,(byte)0xFF,
                    (byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,
                    (byte)0x01,(byte)0xFE,(byte)0x01,(byte)0xFE
            });
            sendmessage(getStringByid(R.string.nfc_write_result)+retCode);
            if (retCode == 0x00) {
                sendmessage(getStringByid(R.string.nfc_write_success));
            } else {
                sendmessage(getStringByid(R.string.nfc_write_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void writeBlock(int block) {
        try {
            int retCode = rfcard.writeBlock((byte) block, new byte[] {
                    (byte)0x00,(byte)0x01,(byte)0x02,(byte)0x03,
                    (byte)0xFF,(byte)0xFE,(byte)0xFF,(byte)0xFF,
                    (byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,
                    (byte)0x01,(byte)0xFE,(byte)0x01,(byte)0xFE
            });
            sendmessage(getStringByid(R.string.nfc_write_result)+retCode);
            if (retCode == 0x00) {
                sendmessage(getStringByid(R.string.nfc_write_success));
            } else {
                sendmessage(getStringByid(R.string.nfc_write_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void nfc_pboc()
    {
        boolean flag = false;
        try {
            flag = rfcard.open();
            if (flag) {
                sendmessage(getStringByid(R.string.nfc_open_success));
            } else {
                sendmessage(getStringByid(R.string.nfc_open_faile));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(flag)
        {
            flag = false;
            //OPEN success
            try {
                int type = rfcard.getCardType();
                byte[] data = rfcard.reset(type);
                if (null != data) {
                    sendmessage(getStringByid(R.string.ic_reset_result)+ HexUtil.bcd2str(data));
                    flag = true;
                } else {
                    sendmessage(getStringByid(R.string.ic_reset_faile));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else return;
//        if(flag)
//        {
//            flag = false;
//            //reset success
//            try {
//                flag = rfcard.isExist();
//                if (flag) {
//                    sendmessage(getStringByid(R.string.ic_card_exist));
//                } else {
//                    sendmessage(getStringByid(R.string.ic_card_notexist));
//                }
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }else return;
        if(flag)
        {
            //CARD ISEXIST
            isReadCardId = true;
            pboclib.init();
            lastapdu = null;
            IC_SEND(pboclib.getsenddata());

        }
    }

    private void IC_SEND(String str)
    {
        byte[] result = null;
        byte[] mCmd = ByteUtils.hexString2ByteArray(str);
        int length = mCmd.length;
        Log.e(TAG, "send = " + ByteUtils.byteArray2HexString(mCmd));
        try {
            result = rfcard.apduComm(mCmd);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.e(TAG, "rec = " + ByteUtils.byteArray2HexString(result));

        if (!TextUtils.isEmpty(ByteUtils.byteArray2HexString(result))) {
            Log.e(TAG, "ATR = " + ByteUtils.byteArray2HexString(result));
            if(isReadCardId)
            {
                String data = pboclib.parsedata(result);
                if(data.length()>0&&pboclib.CARDID.length()<1)
                {
                    IC_SEND(data);
                    return;
                }
                if(pboclib.CARDID.length()>0)
                {
                    Log.d(TAG, "cardNo = " + pboclib.CARDID);
                    sendmessage("CardNo：" + pboclib.CARDID);
                    return;
                }
            }

        }


    }


    private void start_autoRead()
    {
        nfcAutoCheck.startAutoCheck(60);
        sendmessage("Start auto check card");
    }

    private void stop_autoRead()
    {
        if(nfcAutoCheck!=null)
        {
            nfcAutoCheck.stopAutoCheck();
            if(rfcard!=null)
                close();
        }
        sendmessage("Stoped auto check card");

    }


    private String getCardType(int cardtype)
    {
        String ret = "";
        switch (cardtype)
        {
            case RFCard.RFCardType.UNSUPPORTED:
                ret = "UNSUPPORTED";
                break;
            case RFCard.RFCardType.TYPEA:
                ret = "TYPEA";
                break;
            case RFCard.RFCardType.TYPEB:
                ret = "TYPEB";
                break;
            case RFCard.RFCardType.MIFARE_ONE:
                ret = "MIFARE_ONE";
                break;
            case RFCard.RFCardType.MIFARE_S50:
                ret = "MIFARE_S50";
                break;
            case RFCard.RFCardType.MIFARE_ONE_S70:
                ret = "MIFARE_ONE_S70";
                break;
            case RFCard.RFCardType.MIFARE_ULTRALIGHT_C:
                ret = "MIFARE_ULTRALIGHT_C";
                break;
            case RFCard.RFCardType.MIFARE_PLUS:
                ret = "MIFARE_PLUS";
                break;
            case RFCard.RFCardType.MIFARE_DESFIRE:
                ret = "MIFARE_DESFIRE";
                break;
            case RFCard.RFCardType.MIFARE_CPU:
                ret = "MIFARE_CPU";
                break;
            case RFCard.RFCardType.MIFARE_PRO:
                ret = "MIFARE_PRO";
                break;
            case RFCard.RFCardType.MIFARE_S50_PRO:
                ret = "MIFARE_S50_PRO";
                break;
            case RFCard.RFCardType.MIFARE_S70_PRO:
                ret = "MIFARE_S70_PRO";
                break;

        }
        return ret;
    }

    public void DesfireSend(byte[] mCmd){
        byte[] result=new byte[0];
        int length = mCmd.length;
        Log.e(TAG, "send = " + ByteUtils.byteArray2HexString(mCmd));
        try {
            result = rfcard.apduComm(mCmd);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.e(TAG, "rec = " + ByteUtils.byteArray2HexString(result));
    }





}
