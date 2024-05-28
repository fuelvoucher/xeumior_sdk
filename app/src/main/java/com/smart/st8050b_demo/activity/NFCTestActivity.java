package com.smart.st8050b_demo.activity;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smart.st8050b_demo.R;
import com.smart.st8050b_demo.util.HexUtil;
import com.smart.st8050b_demo.util.NfcAutoCheck;
import com.topwise.cloudpos.aidl.AidlDeviceService;
import com.topwise.cloudpos.aidl.printer.AidlPrinter;
import com.topwise.cloudpos.aidl.printer.AidlPrinterListener;
import com.topwise.cloudpos.aidl.printer.PrintItemObj;
import com.topwise.cloudpos.aidl.rfcard.AidlRFCard;
import com.topwise.cloudpos.data.PrinterConstant;
import com.topwise.cloudpos.data.RFCard;

import java.util.ArrayList;

public class NFCTestActivity extends BaseActivity implements View.OnClickListener {
    public AidlRFCard rfcard = null;
    private NfcAutoCheck nfcAutoCheck = null;
    private final int SHOW_MESSAGE = 0;
    private final int CLEAN_MESSAGE = 1;
    private TextView txt_log = null;
    private Button btn_nfc,btn_print;
    private boolean nfcIsOpen = false;
    private String CurentUid = "";
    private  String TAG = "NFCTestActivity";

    //打印
    private AidlPrinter printerDev = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfctest);
        txt_log = (TextView)findViewById(R.id.txt_log);
        txt_log.setMovementMethod(ScrollingMovementMethod.getInstance());
        btn_nfc = (Button)findViewById(R.id.btn_nfcopen);
        btn_nfc.setOnClickListener(this);
        btn_print= (Button)findViewById(R.id.btn_print);
        btn_print.setOnClickListener(this);
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

        try {
            printerDev = AidlPrinter.Stub.asInterface(serviceManager.getPrinter());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void sendmessage(String mess)
    {
        Message msg = mHandler.obtainMessage();
        msg.what =SHOW_MESSAGE;
        msg.obj=mess;
        mHandler.sendMessage(msg);
    }
    private void cleanLog()
    {
        Message msg = mHandler.obtainMessage();
        msg.what =CLEAN_MESSAGE;
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
                case CLEAN_MESSAGE:
                    txt_log.setText("");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void setOnCheckListen()
    {
        if(rfcard!=null){
            nfcAutoCheck = new NfcAutoCheck(rfcard);
            nfcAutoCheck.setOnDataReceiveListener(new NfcAutoCheck.OnCheckCardListener() {
                @Override
                public void onCheckCardReceive(boolean isexist,int cardtype) {
//                    if(isexist)
//                    {
//                        sendmessage("card is exists" + " Card type is " + getCardType(cardtype));
//                    }else
//                    {
//                        sendmessage("card is not exists");
//                    }
//                    nfcAutoCheck.startAutoCheck(60);
                }

                @Override
                public void onCheckCardReceive(boolean isexist, int cardtype, String uid) {
                    Log.d(TAG,"is exist"+ isexist+ " card type " + cardtype + " Uid " + uid);
                    if(isexist&&!uid.isEmpty())
                    {
                        Log.d(TAG,"stopAutoCheck");
                        nfcAutoCheck.stopAutoCheck();
                        if(!CurentUid.equals(uid)){
                            sendmessage("card is exists " + " Card type is " + getCardType(cardtype) + " uid:" + uid);
                            // Searched for new cards
                            //do something
                            if(0x00 ==auth()) {
                                readBlockData();
                                CurentUid = uid;
                            }else{
                                //AUTH FAILE
                                CurentUid = "";
                            }

                        }else
                        {
                            //The original card was searched again and not processed.
                        }
                        Log.d(TAG,"startAutoCheck");
                        nfcAutoCheck.startAutoCheck(60);
                    }else
                    {
                        Log.d(TAG,"Check Flag is " + nfcAutoCheck.getCheckFlag());
                        if(!nfcAutoCheck.getCheckFlag())
                            nfcAutoCheck.startAutoCheck(60);
                        if(CurentUid!=null&&!CurentUid.isEmpty())
                        {
                            //The card was removed.
                            sendmessage("Card uid:" + CurentUid + " was removed!");
                        }else{
                            //cleanLog();
                            sendmessage("card is not exists");
                        }
                        CurentUid = "";

                    }

                }
            });
        }
    }

    public int auth() {
        int retCode = -1;
        try {
            int cardType = rfcard.getCardType();
            byte[] resetData = rfcard.reset(cardType);
            if (resetData==null){
                sendmessage(getStringByid(R.string.nfc_card_notexist));
                return -1;
            }
            retCode = rfcard.auth((byte) 0x00, (byte) 0x01, new byte[] {
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
        return retCode;
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
    @Override
    public void onDestroy(){
        close();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if(view==btn_nfc){
            if(!nfcIsOpen)
            {
                nfcIsOpen = true;
                btn_nfc.setText("NFC OFF");
                start_autoRead();
            }else
            {
                nfcIsOpen = false;
                btn_nfc.setText("NFC ON");
                stop_autoRead();
            }
        }else if(view==btn_print){
            stop_autoRead();
            printText_test1();
        }
    }


    public void printText_test1()
    {
        try {
            this.printerDev.setPrinterGray(4);
            printerDev.printText(new ArrayList<PrintItemObj>(){
                {


                    add(new PrintItemObj("Quittung       27.02.2018 12:01",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Quittung       27.02.2018 12:01",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));

                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {

                    start_autoRead();
                    //sendmessage(getStringByid(R.string.print_end) + endTime);
                }

                @Override
                public void onError(int arg0) throws RemoteException {
                    sendmessage(getStringByid(R.string.print_faile_errcode) + arg0);
                }
            });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
