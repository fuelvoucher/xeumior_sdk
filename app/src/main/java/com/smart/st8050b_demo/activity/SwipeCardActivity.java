package com.smart.st8050b_demo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.smart.st8050b_demo.MainActivity;
import com.smart.st8050b_demo.R;
import com.smart.st8050b_demo.adapter.OperatorInfoAdapter;
import com.smart.st8050b_demo.bean.OperatorInfo;
import com.smart.st8050b_demo.bean.OrderInfo;
import com.smart.st8050b_demo.util.AidlErrorCode;
import com.smart.st8050b_demo.util.BaseUtils;
import com.smart.st8050b_demo.util.ByteUtils;
import com.smart.st8050b_demo.util.DecodeUtils;
import com.smart.st8050b_demo.util.HexUtil;
import com.smart.st8050b_demo.util.PBOCLIB;
import com.smart.st8050b_demo.util.QRCodeUtil;
import com.topwise.cloudpos.aidl.AidlDeviceService;
import com.topwise.cloudpos.aidl.buzzer.AidlBuzzer;
import com.topwise.cloudpos.aidl.camera.AidlCameraScanCode;
import com.topwise.cloudpos.aidl.camera.AidlCameraScanCodeListener;
import com.topwise.cloudpos.aidl.decoder.AidlDecoderManager;
import com.topwise.cloudpos.aidl.emv.AidlCheckCardListener;
import com.topwise.cloudpos.aidl.emv.AidlPboc;
import com.topwise.cloudpos.aidl.emv.AidlPbocStartListener;
import com.topwise.cloudpos.aidl.emv.CardInfo;
import com.topwise.cloudpos.aidl.emv.EmvTransData;
import com.topwise.cloudpos.aidl.emv.PCardLoadLog;
import com.topwise.cloudpos.aidl.emv.PCardTransLog;
import com.topwise.cloudpos.aidl.iccard.AidlICCard;
import com.topwise.cloudpos.aidl.led.AidlLed;
import com.topwise.cloudpos.aidl.magcard.AidlMagCard;
import com.topwise.cloudpos.aidl.magcard.EncryptMagCardListener;
import com.topwise.cloudpos.aidl.magcard.MagCardListener;
import com.topwise.cloudpos.aidl.magcard.TrackData;
import com.topwise.cloudpos.aidl.printer.AidlPrinter;
import com.topwise.cloudpos.aidl.printer.AidlPrinterListener;
import com.topwise.cloudpos.aidl.printer.PrintItemObj;
import com.topwise.cloudpos.aidl.psam.AidlPsam;
import com.topwise.cloudpos.aidl.system.AidlSystem;
import com.topwise.cloudpos.data.AidlScanParam;
import com.topwise.cloudpos.data.EmvConstant;
import com.topwise.cloudpos.data.LedCode;
import com.topwise.cloudpos.data.PrinterConstant;
import com.topwise.cloudpos.data.PsamConstant;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Com.String2Bitmap.BitmapUtil;
import Com.String2Bitmap.StringBitmapParameter;

public class SwipeCardActivity extends BaseActivity {
    private ListView listView;
    private List<OperatorInfo> operatorInfoList = new ArrayList<>();
    private OperatorInfoAdapter adapter = null;
    private AidlMagCard magCardDev = null;
    private TextView txt_log = null;

    private final int SHOW_MESSAGE = 0;
    private String TAG = "SwipeCardActivity";
    private String strmess = "";
    private boolean isSwipeCard = false;
    private AidlICCard iccard = null;
    private String activity_name = "";


    //pboc
    private PBOCLIB pboclib = new PBOCLIB();
    byte[] lastapdu = null;
    private boolean isReadCardId = false;

    private AidlPboc pboc = null;
//    private PbocStartListener listener = null;
    private PbocStartListener pboclistener = null;

    private EmvTransData transData = null;

    //打印
    private AidlPrinter printerDev = null;

    //PSAM
    private AidlPsam psam = null;
    private AidlDeviceService deviceManager = null;


    private AidlCameraScanCode iScanner = null;


    private AidlDecoderManager iDecoder;
    private static final String QR_DECODE_DRAWABLE_NAME = "tp_decode_check";


    private AidlBuzzer iBeeper;

    private AidlLed iLed;
    Thread thread_led = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_card);
        initView();
        initData();

//        try {
//            psam = AidlPsam.Stub.asInterface(deviceManager
//                    .getPSAMReader(PsamConstant.PSAM_DEV_ID_1));
//        } catch (RemoteException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
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
    }

    private void initData()
    {
        operatorInfoList.clear();
        if(activity_name.equals(BaseUtils.ACTIVITY_NAME_SWIPE)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.getTrackData), "getTrackData".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.getEncryptTrackData), "getEncryptTrackData".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.getEncryptFormatTrackData), "getEncryptFormatTrackData".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.cancelSwipe), "cancelSwipe".toLowerCase()));
        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_IC)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_open), "ic_open".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_reset), "ic_reset".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_exists), "ic_exists".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_apdu), "ic_apdu".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_halt), "ic_halt".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_pboc), "ic_pboc".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_close), "ic_close".toLowerCase()));

        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_NFC)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_open), "nfc_open".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_cardtype), "nfc_cardtype".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_reset), "nfc_reset".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.nfc_cardtype), "nfc_cardtype".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_halt), "ic_halt".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_pboc), "ic_pboc".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.ic_close), "ic_close".toLowerCase()));

        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PRINT)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.print_status), "print_status".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.print_text), "print_text".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.print_Arabic), "print_Arabic".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.print_sabra), "print_sabra".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.print_code), "print_code".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.print_pic), "print_pic".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.print_qrcode), "print_qrcode".toLowerCase()));
        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PSAM)) {
            findViewById(R.id.psam_slot).setVisibility(View.VISIBLE);
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.psam_open), "psam_open".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.psam_reset), "psam_reset".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.psam_apdu), "psam_apdu".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.psam_halt), "psam_halt".toLowerCase()));
            ((RadioGroup)findViewById(R.id.rgpsam_slot)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    switch(checkedId)
                    {
                        case R.id.psam_slot1:
                            getpsam(1);
                            break;
                        case R.id.psam_slot2:
                            getpsam(2);
                            break;
                    }
                }
            });

        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_BARCODE)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.qrcode), "qrcode".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.qrcode_stop), "qrcode_stop".toLowerCase()));
        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_DECODE)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.decode_init), "decode_init".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.decode1), "decode1".toLowerCase()));
        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_BEEP)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.beep_start), "beep_start".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.beep_stop), "beep_stop".toLowerCase()));
        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_LED)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.led_start), "led_start".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.led_stop), "led_stop".toLowerCase()));
        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PINPAD)) {
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.led_start), "led_start".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.led_stop), "led_stop".toLowerCase()));
        }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PBOCTEST)) {

            operatorInfoList.add(new OperatorInfo(this.getString(R.string.check_card), "check_card".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.cancel_check_card), "cancel_check_card".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.check_card_with_encrypted_TDK), "check_card_with_encrypted_TDK".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.cancel_check_encrypted_card), "cancel_check_encrypted_card".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.consume), "consume".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.read_e_cash_balance), "read_e_cash_balance".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.read_card_transaction_log), "read_card_transaction_log".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.read_the_looped_log), "read_the_looped_log".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.read_kernel_data), "read_kernel_data".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.readKernelEDData), "readKernelEDData".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.increase_public_key_parameters), "increase_public_key_parameters".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.update_public_key_parameters), "update_public_key_parameters".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.clear_public_key_parameters), "clear_public_key_parameters".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.clear_the_transaction_log), "clear_the_transaction_log".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.interrupt_the_PBOC_process), "interrupt_the_PBOC_process".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.end_the_PBOC_process), "end_the_PBOC_process".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.add_aid), "add_aid".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.update_aid), "update_aid".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.clear_aid), "clear_aid".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.set_tlv1), "set_tlv1".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.set_tlv2), "set_tlv2".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.set_tlv3), "set_tlv3".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.set_tlv4), "set_tlv4".toLowerCase()));
            operatorInfoList.add(new OperatorInfo(this.getString(R.string.isexist_aid_publicKey), "isexist_aid_publicKey".toLowerCase()));

            transData = new EmvTransData((byte) 0x00,
                    (byte) 0x01, true, false, false,
                    (byte) 0x01, (byte) 0x00, new byte[]{0x00, 0x00, 0x00});
            pboclistener = new PbocStartListener();
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
            if(activity_name.equals(BaseUtils.ACTIVITY_NAME_SWIPE)) {
                if (operatorvalue.toLowerCase().equals("getTrackData".toLowerCase())) {
                    txt_log.setText("");
                    getTrackData();
                } else if (operatorvalue.toLowerCase().equals("getEncryptTrackData".toLowerCase())) {
                    txt_log.setText("");
                    getEncryptTrackData();
                } else if (operatorvalue.toLowerCase().equals("getEncryptFormatTrackData".toLowerCase())) {
                    txt_log.setText("");
                    getEncryptFormatTrackData();
                } else if (operatorvalue.toLowerCase().equals("cancelSwipe".toLowerCase())) {
                    cancelSwipe();
                }
            }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_IC)) {
                if (operatorvalue.toLowerCase().equals("ic_open".toLowerCase())) {
                    txt_log.setText("");
                    ic_open();
                } else if (operatorvalue.toLowerCase().equals("ic_reset".toLowerCase())) {
                    txt_log.setText("");
                    ic_cardReset();
                }else if (operatorvalue.toLowerCase().equals("ic_exists".toLowerCase())) {
                    txt_log.setText("");
                    ic_isExists();
                }else if (operatorvalue.toLowerCase().equals("ic_apdu".toLowerCase())) {
                    txt_log.setText("");
                    ic_apduComm();
                }else if (operatorvalue.toLowerCase().equals("ic_halt".toLowerCase())) {
                    txt_log.setText("");
                    ic_halt();
                }else if (operatorvalue.toLowerCase().equals("ic_pboc".toLowerCase())) {
                    txt_log.setText("");
                    ic_pboc();
                }else if (operatorvalue.toLowerCase().equals("ic_close".toLowerCase())) {
                    txt_log.setText("");
                    ic_close();
                }
            }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PRINT)) {
                if (operatorvalue.toLowerCase().equals("print_status".toLowerCase())) {
                    txt_log.setText("");
                    getPrintState();
                } else if (operatorvalue.toLowerCase().equals("print_text".toLowerCase())) {
                    txt_log.setText("");
                    printText_test1();


                    //printText_Vietnamese();

                    //printQrImageTest();

                    // printText_xili();
                    //printText_test2();
                }else if (operatorvalue.toLowerCase().equals("print_Arabic".toLowerCase())) {
                    txt_log.setText("");
                    printText_Arabic();



                }else if (operatorvalue.toLowerCase().equals("print_sabra".toLowerCase())) {
                    txt_log.setText("");
                    printText_Sabra();



            }else if (operatorvalue.toLowerCase().equals("print_code".toLowerCase())) {
                    txt_log.setText("");
                    printBarCode();
                }else if (operatorvalue.toLowerCase().equals("print_pic".toLowerCase())) {
                    txt_log.setText("");
                    printBitmap();
                }else if (operatorvalue.toLowerCase().equals("print_qrcode".toLowerCase())) {
                    txt_log.setText("");
                    printQrCode();
                }else if (operatorvalue.toLowerCase().equals("ic_pboc".toLowerCase())) {
                    txt_log.setText("");
                    ic_pboc();
                }else if (operatorvalue.toLowerCase().equals("ic_close".toLowerCase())) {
                    txt_log.setText("");
                    ic_close();
                }
            }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PSAM)) {
                if (operatorvalue.toLowerCase().equals("psam_open".toLowerCase())) {
                    txt_log.setText("");
                    psamopen();
                } else if (operatorvalue.toLowerCase().equals("psam_reset".toLowerCase())) {
                    txt_log.setText("");
                    psamreset();
                }else if (operatorvalue.toLowerCase().equals("psam_apdu".toLowerCase())) {
                    txt_log.setText("");
                    psamapducmd();
                }else if (operatorvalue.toLowerCase().equals("psam_halt".toLowerCase())) {
                    txt_log.setText("");
                    psamclose();
                }
            }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_BARCODE)) {
                if (operatorvalue.toLowerCase().equals("qrcode".toLowerCase())) {
                    txt_log.setText("");
                    backScan();
                } else if (operatorvalue.toLowerCase().equals("qrcode_stop".toLowerCase())) {
                    txt_log.setText("");
                    stopScan();
                }
            }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_DECODE)) {
                if (operatorvalue.toLowerCase().equals("decode_init".toLowerCase())) {
                    txt_log.setText("");
                    decode_init();
                } else if (operatorvalue.toLowerCase().equals("decode1".toLowerCase())) {
                    txt_log.setText("");
                    decoder();
                }
            }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_BEEP)) {
                if (operatorvalue.toLowerCase().equals("beep_start".toLowerCase())) {
                    startbeep();
                } else if (operatorvalue.toLowerCase().equals("beep_stop".toLowerCase())) {
                    txt_log.setText("");
                    stopbeep();
                }
            }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_LED)) {
                if (operatorvalue.toLowerCase().equals("led_start".toLowerCase())) {
                    startled();
                } else if (operatorvalue.toLowerCase().equals("led_stop".toLowerCase())) {
                    txt_log.setText("");
                    stopled();
                }
            }else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PBOCTEST)) {
                if (operatorvalue.toLowerCase().equals("check_card".toLowerCase())) {
                    txt_log.setText("");
                    checkCard();
                } else if (operatorvalue.toLowerCase().equals("cancel_check_card".toLowerCase())) {
                    txt_log.setText("");
                    cancelCheckCard();
                }else if (operatorvalue.toLowerCase().equals("check_card_with_encrypted_TDK".toLowerCase())) {
                    txt_log.setText("");
                    checkCardWithEncryptedTDK();
                }else if (operatorvalue.toLowerCase().equals("cancelCheckEncryptedCard".toLowerCase())) {
                    txt_log.setText("");
                    cancelCheckEncryptedCard();
                }else if (operatorvalue.toLowerCase().equals("consume".toLowerCase())) {
                    txt_log.setText("");
                    consume();
                }else if (operatorvalue.toLowerCase().equals("read_e_cash_balance".toLowerCase())) {
                    txt_log.setText("");
                    readCardOfflineBalance();
                }else if (operatorvalue.toLowerCase().equals("read_card_transaction_log".toLowerCase())) {
                    txt_log.setText("");
                    readCardTransLog();
                }else if (operatorvalue.toLowerCase().equals("read_the_looped_log".toLowerCase())) {
                    txt_log.setText("");
                    readCardLoadLog();
                }else if (operatorvalue.toLowerCase().equals("read_kernel_data".toLowerCase())) {
                    txt_log.setText("");
                    readKernelData();
                }else if (operatorvalue.toLowerCase().equals("readKernelEDData".toLowerCase())) {
                    txt_log.setText("");
                    readKernelEDData();
                }else if (operatorvalue.toLowerCase().equals("increase_public_key_parameters".toLowerCase())) {
                    txt_log.setText("");
                    addCaParam();
                }else if (operatorvalue.toLowerCase().equals("update_public_key_parameters".toLowerCase())) {
                    txt_log.setText("");
                    updateCaParam();
                }else if (operatorvalue.toLowerCase().equals("clear_public_key_parameters".toLowerCase())) {
                    txt_log.setText("");
                    delCaParam();
                }else if (operatorvalue.toLowerCase().equals("clear_the_transaction_log".toLowerCase())) {
                    txt_log.setText("");
                    clearTransData();
                }else if (operatorvalue.toLowerCase().equals("interrupt_the_PBOC_process".toLowerCase())) {
                    txt_log.setText("");
                    abortPboc();
                }else if (operatorvalue.toLowerCase().equals("end_the_PBOC_process".toLowerCase())) {
                    txt_log.setText("");
                    endPboc();
                }else if (operatorvalue.toLowerCase().equals("add_aid".toLowerCase())) {
                    txt_log.setText("");
                    addAid();
                }else if (operatorvalue.toLowerCase().equals("update_aid".toLowerCase())) {
                    txt_log.setText("");
                    updateAid();
                }else if (operatorvalue.toLowerCase().equals("clear_aid".toLowerCase())) {
                    txt_log.setText("");
                    delAid();
                }else if (operatorvalue.toLowerCase().equals("set_tlv1".toLowerCase())) {
                    txt_log.setText("");
                    setTlv();
                }else if (operatorvalue.toLowerCase().equals("set_tlv2".toLowerCase())) {
                    txt_log.setText("");
                    setTlvWithEncoding(BaseUtils.UTF8);
                }else if (operatorvalue.toLowerCase().equals("set_tlv3".toLowerCase())) {
                    txt_log.setText("");
                    setTlvWithEncoding(BaseUtils.UTF16);
                }else if (operatorvalue.toLowerCase().equals("set_tlv4".toLowerCase())) {
                    txt_log.setText("");
                    setTlvWithEncoding(BaseUtils.GBK);
                }else if (operatorvalue.toLowerCase().equals("import_online_result".toLowerCase())) {
                    txt_log.setText("");
                    importOnlineResult();
                }else if (operatorvalue.toLowerCase().equals("isexist_aid_publicKey".toLowerCase())) {
                    txt_log.setText("");
                    isExistAidPublicKey();
                }
            }
        }
    };

    /**
     * 获取磁条卡数据
     *
     * @createtor：Administrator
     * @date:2015-8-4 上午7:29:30
     */
    public void getTrackData() {
        if (magCardDev != null) {
            try {
                isSwipeCard = true;
                sendmessage(getStringByid(R.string.swipecard));
                magCardDev.searchCard(6000, new MagCardListener.Stub() {
                    @Override
                    public void onTimeout() throws RemoteException {
                        sendmessage(getStringByid((R.string.swipe_timeout)));
                        isSwipeCard = false;
                    }
                    @Override
                    public void onSuccess(TrackData trackData)
                            throws RemoteException {
                        isSwipeCard = false;
                        strmess=getStringByid(R.string.swipe_success) + "\n";
                        strmess+="1" + getStringByid(R.string.track_data) + trackData.getFirstTrackData()+ "\n";
                        strmess+="2" + getStringByid(R.string.track_data) + trackData.getSecondTrackData()+ "\n";
                        strmess+="3" + getStringByid(R.string.track_data) + trackData.getThirdTrackData()+ "\n";
                        strmess+=getStringByid(R.string.card_no) + trackData.getCardno()+ "\n";
                        strmess+=getStringByid(R.string.expiryDate) + trackData.getExpiryDate()+ "\n";
                        strmess+=getStringByid(R.string.format_data) + trackData.getFormatTrackData()+ "\n";
                        strmess+=getStringByid(R.string.service_code) + trackData.getServiceCode();
                        sendmessage(strmess);
                    }

                    @Override
                    public void onGetTrackFail() throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid(R.string.swipe_faile));
                    }

                    @Override
                    public void onError(int arg0) throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid(R.string.swipe_error) + arg0);
                    }

                    @Override
                    public void onCanceled() throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid(R.string.swipe_canceled));
                    }
                });
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取密文磁道数据
     *
     * @createtor：Administrator
     * @date:2015-8-4 上午7:29:40
     */
    public void getEncryptTrackData(){
        sendmessage(getStringByid(R.string.swipecard));
        try {
            if(null != magCardDev){
                isSwipeCard = true;
                magCardDev.searchEncryptCard(60000, (byte)0x01, (byte)0x00, null, (byte)0x00, new EncryptMagCardListener.Stub() {
                    @Override
                    public void onTimeout() throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid((R.string.swipe_timeout)));
                    }

                    @Override
                    public void onSuccess(String[] trackData) throws RemoteException {
                        isSwipeCard = false;
                        strmess = getStringByid(R.string.swipe_success) + "\n";
                        strmess += getStringByid(R.string.track2_encryptdata) + trackData[0] + "\n";
                        strmess += getStringByid(R.string.track3_encryptdata) + trackData[1] +"\n";
                        strmess += getStringByid(R.string.card_no) + trackData[2];
                        sendmessage(strmess);
                    }

                    @Override
                    public void onGetTrackFail() throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid(R.string.swipe_faile));
                    }

                    @Override
                    public void onError(int arg0) throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid(R.string.swipe_error) + arg0);
                    }

                    @Override
                    public void onCanceled() throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid(R.string.swipe_canceled));
                    }
                });
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //按照格式化磁道数据获取加密数据
    public void getEncryptFormatTrackData(){
        sendmessage(getStringByid(R.string.swipecard));
        try {
            if(null != magCardDev){
                isSwipeCard = true;
                magCardDev.searchEncryptCard(60000, (byte)0x01, (byte)0x01, null, (byte)0x00, new EncryptMagCardListener.Stub() {
                    @Override
                    public void onTimeout() throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid((R.string.swipe_timeout)));
                    }

                    @Override
                    public void onSuccess(String[] trackData) throws RemoteException {
                        isSwipeCard = false;
                        strmess = getStringByid(R.string.swipe_success) + "\n";
                        strmess += getStringByid(R.string.track_encryptdata) + trackData[0] +"\n";
                        strmess += getStringByid(R.string.card_no) + trackData[2];
                        sendmessage(strmess);
                    }

                    @Override
                    public void onGetTrackFail() throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid(R.string.swipe_faile));
                    }

                    @Override
                    public void onError(int arg0) throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid(R.string.swipe_error) + arg0);
                    }

                    @Override
                    public void onCanceled() throws RemoteException {
                        isSwipeCard = false;
                        sendmessage(getStringByid(R.string.swipe_canceled));
                    }
                });
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 取消刷卡
       * @createtor：Administrator
     * @date:2015-8-4 上午7:29:49
     */
    public void cancelSwipe() {
        if(isSwipeCard == false){
            sendmessage(getStringByid(R.string.no_swipe));
            return;
        }
        isSwipeCard = false;
        if (null != magCardDev) {
            try {
                magCardDev.stopSearch();
                sendmessage(getStringByid(R.string.interrupted_success));
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                sendmessage(getStringByid(R.string.interrupted_error));
            } // 中断刷卡
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if(activity_name.equals(BaseUtils.ACTIVITY_NAME_SWIPE))
            cancelSwipe(); // 取消刷卡
        else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_IC))
            ic_close();
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


    private void sendmessage(String mess)
    {
        Message msg = mHandler.obtainMessage();
        msg.what =SHOW_MESSAGE;
        msg.obj=mess;
        mHandler.sendMessage(msg);
    }






    /*
    IC卡操作
     */

    public void ic_open() {
        if(isNormalVelocityClick(DELAY_TIME)) {
            try {
                boolean flag = iccard.open();
                if (flag) {
                    sendmessage(getStringByid(R.string.ic_open_success));
                } else {
                    sendmessage(getStringByid(R.string.ic_open_faile));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }


    public void ic_close() {
        if(isNormalVelocityClick(DELAY_TIME)) {
            try {
                boolean flag = iccard.close();
                if (flag) {
                    sendmessage(getStringByid(R.string.ic_close_success));
                } else {
                    sendmessage(getStringByid(R.string.ic_close_faile));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }

    public void ic_halt() {
        if(isNormalVelocityClick(DELAY_TIME)) {
            try {
                int ret = iccard.halt();
                if (ret == 0x00) {
                    sendmessage(getStringByid(R.string.ic_halt_success));
                } else {
                    sendmessage(getStringByid(R.string.ic_halt_faile));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }
    //	00A404000E315041592E5359532E4444463031
    public void ic_apduComm() {
        if(isNormalVelocityClick(DELAY_TIME)) {
            byte[] apdu = HexUtil
                    .hexStringToByte("0000000000");
            try {
                byte[] data = iccard.apduComm(apdu);
                if (null != data) {
                    sendmessage(getStringByid(R.string.ic_main_dir_result)+ HexUtil.bcd2str(data));
                } else {
                    sendmessage(getStringByid(R.string.ic_apdu_faile));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }

    public void ic_cardReset() {
        if(isNormalVelocityClick(DELAY_TIME)) {
            try {
                byte[] data = iccard.reset(0x00);
                if (null != data && data.length != 0) {
                    sendmessage(getStringByid(R.string.ic_reset_result) + HexUtil.bcd2str(data));
                } else {
                    sendmessage(getStringByid(R.string.ic_reset_faile));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }

    public void ic_isExists() {//检卡有事会失败，复位后再次检测
        if(isNormalVelocityClick(DELAY_TIME)) {
            try {
                boolean flag = iccard.isExist();
                if (flag) {
                    sendmessage(getStringByid(R.string.ic_card_exist));
                } else {
                    sendmessage(getStringByid(R.string.ic_card_notexist));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }


    public void ic_pboc()
    {
        boolean flag = false;
        try {
            flag = iccard.open();
            if (flag) {
                sendmessage(getStringByid(R.string.ic_open_success));
            } else {
                sendmessage(getStringByid(R.string.ic_open_faile));
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
                byte[] data = iccard.reset(0x00);
                if (null != data && data.length != 0) {
                    sendmessage(getStringByid(R.string.ic_reset_result) + HexUtil.bcd2str(data));
                    flag = true;
                } else {
                    sendmessage(getStringByid(R.string.ic_reset_faile));
                    flag = false;
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else return;
        if(flag)
        {
            flag = false;
            //reset success
            try {
                flag = iccard.isExist();
                if (flag) {
                    sendmessage(getStringByid(R.string.ic_card_exist));
                } else {
                    sendmessage(getStringByid(R.string.ic_card_notexist));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else return;
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
            result = iccard.apduComm(mCmd);
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


    /*
    打印  print
     */

    private String getCurTime(){
        Date date =new Date(System.currentTimeMillis());
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String time = format.format(date);
        return time;
    }


    public void printQrImageTest() {
        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.emv);
        Bitmap qrcodeBitmap = QRCodeUtil.createQRImage("123456789", 250, 250, null);
        Bitmap mergeBitmap = mergeBitmap_LR(logoBitmap, qrcodeBitmap, true);
        String startTime = getCurTime();
        sendmessage(getStringByid(R.string.print_begin) + startTime);
        try {
            this.printerDev.printBmp(0, mergeBitmap.getWidth(), mergeBitmap.getHeight(), mergeBitmap, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
                }

                @Override
                public void onError(int arg0) throws RemoteException {
                    sendmessage(getStringByid(R.string.print_faile_errcode) + arg0);
                }
            });

            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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

    /**
     * Combine two bitmap overlays into one bitmap, splicing left and right
     * @param leftBitmap
     * @param rightBitmap
     * @param isBaseMax Whether the bitmap with large width is used as the criterion, true is equal-ratio stretching of small graph and false is equal-ratio compression of large graph.
     * @return
     */
    public Bitmap mergeBitmap_LR(Bitmap leftBitmap, Bitmap rightBitmap, boolean isBaseMax) {

        if (leftBitmap == null || leftBitmap.isRecycled()
                || rightBitmap == null || rightBitmap.isRecycled()) {
            return null;
        }
        int height = 0; // The height after splicing should be large or small according to the parameters.
        if (isBaseMax) {
            height = leftBitmap.getHeight() > rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
        } else {
            height = leftBitmap.getHeight() < rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
        }

        // Bitmap after scaling
        Bitmap tempBitmapL = leftBitmap;
        Bitmap tempBitmapR = rightBitmap;

        if (leftBitmap.getHeight() != height) {
            tempBitmapL = Bitmap.createScaledBitmap(leftBitmap, (int)(leftBitmap.getWidth()*1f/leftBitmap.getHeight()*height), height, false);
        } else if (rightBitmap.getHeight() != height) {
            tempBitmapR = Bitmap.createScaledBitmap(rightBitmap, (int)(rightBitmap.getWidth()*1f/rightBitmap.getHeight()*height), height, false);
        }

        // Width after splicing
        int width = tempBitmapL.getWidth() + tempBitmapR.getWidth();

        // Output bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);


        Rect leftRect = new Rect(0, 0, tempBitmapL.getWidth(), tempBitmapL.getHeight());
        Rect rightRect  = new Rect(0, 0, tempBitmapR.getWidth(), tempBitmapR.getHeight());

        // The right side of the graph needs to be plotted in the same position, shifting to the right the width and height of the left side of the graph are the same.
        Rect rightRectT  = new Rect(tempBitmapL.getWidth(), 0, width, height);

        canvas.drawBitmap(tempBitmapL, leftRect, leftRect, null);
        canvas.drawBitmap(tempBitmapR, rightRect, rightRectT, null);
        return bitmap;
    }




    public void printQrCode(){
        Bitmap qrcodeBitmap = QRCodeUtil.createQRImage("123456789",300,300,null);
        try{
            this.printerDev.printBmp(0, qrcodeBitmap.getWidth(), qrcodeBitmap.getHeight(), qrcodeBitmap, new AidlPrinterListener.Stub() {
                @Override
                public void onPrintFinish() throws RemoteException {
                    sendmessage(getStringByid(R.string.print_success));
                }

                @Override
                public void onError(int arg0) throws RemoteException {
                    sendmessage(getStringByid(R.string.print_faile_errcode) + arg0);
                }
            });

            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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
    /**
     * 获取打印机状态
     * @createtor：Administrator
     * @date:2015-8-4 下午2:18:47
     */
    public void getPrintState(){
        try {
            int printState = printerDev.getPrinterState();
            sendmessage(getStringByid(R.string.get_print_status) + printState);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 打印文本
     * @createtor：Administrator
     * @date:2015-8-4 下午2:19:28
     */
    public void printText(){
        try {
            String startTime = getCurTime();
            sendmessage(getStringByid(R.string.print_begin) + startTime);
            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
//                    add(new PrintItemObj("                         "));
//                    add(new PrintItemObj("                         "));
//                    add(new PrintItemObj("مرحبا  سمارت  الصين  أيضا في العالم"));
//                    add(new PrintItemObj("                         "));
//                    add(new PrintItemObj("                         "));
//                    add(new PrintItemObj("مرحبا  سمارت  الصين  أيضا في العالم",24));
//
//                    add(new PrintItemObj("                         "));
//                    add(new PrintItemObj("                         "));
//                    add(new PrintItemObj("در حال حاضر من در دانشگاه مطالعات بین المللی شانگهای مطالعه ، می"));
//
//
//                    add(new PrintItemObj("                         "));
//                    add(new PrintItemObj("                         "));
//                    add(new PrintItemObj("در حال حاضر من در دانشگاه مطالعات بین المللی شانگهای مطالعه ، می",24));
//
//                    add(new PrintItemObj("                         "));
//                    add(new PrintItemObj("                         "));

                    add(new PrintItemObj(getStringByid(R.string.print_data1)));
                    add(new PrintItemObj(getStringByid(R.string.print_data1)));

                    add(new PrintItemObj(getStringByid(R.string.print_data2),24));
                    add(new PrintItemObj(getStringByid(R.string.print_data2),24));
                    add(new PrintItemObj(getStringByid(R.string.print_data3),8,true));
                    add(new PrintItemObj(getStringByid(R.string.print_data3),8,true));

                    add(new PrintItemObj(getStringByid(R.string.print_data4),8,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj(getStringByid(R.string.print_data4),8,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj(getStringByid(R.string.print_data5),8,false, PrintItemObj.ALIGN.CENTER));
                    add(new PrintItemObj(getStringByid(R.string.print_data5),8,false, PrintItemObj.ALIGN.CENTER));
                    add(new PrintItemObj(getStringByid(R.string.print_data6),8,false, PrintItemObj.ALIGN.RIGHT));
                    add(new PrintItemObj(getStringByid(R.string.print_data6),8,false, PrintItemObj.ALIGN.RIGHT));
                    add(new PrintItemObj(getStringByid(R.string.print_data7),8,false, PrintItemObj.ALIGN.LEFT,true));
                    add(new PrintItemObj(getStringByid(R.string.print_data7),8,false, PrintItemObj.ALIGN.LEFT,true));
                    add(new PrintItemObj(getStringByid(R.string.print_data8),8,false, PrintItemObj.ALIGN.LEFT,false,true));
                    add(new PrintItemObj(getStringByid(R.string.print_data8),8,false, PrintItemObj.ALIGN.LEFT,false,false));
                    add(new PrintItemObj(getStringByid(R.string.print_data9),8,false, PrintItemObj.ALIGN.LEFT,false,true,40));
                    add(new PrintItemObj(getStringByid(R.string.print_data9),8,false, PrintItemObj.ALIGN.LEFT,false,true,83));
                    add(new PrintItemObj(getStringByid(R.string.print_data10),8,false, PrintItemObj.ALIGN.LEFT,false,true,29,25));
                    add(new PrintItemObj(getStringByid(R.string.print_data10),8,false, PrintItemObj.ALIGN.LEFT,false,true,29,25));
                    add(new PrintItemObj(getStringByid(R.string.print_data11),8,false, PrintItemObj.ALIGN.LEFT,false,true,29,0,40));
                    add(new PrintItemObj(getStringByid(R.string.print_data11),8,false, PrintItemObj.ALIGN.LEFT,false,true,29,0,40));
                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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


    /**
     * 打印越南语
     */
    public void printText_Vietnamese()
    {
        try {
            this.printerDev.setPrinterGray(4);
            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
                    //add(new PrintItemObj("С открытием своего бизнеса все предельно ясно, знание китайского языка дает огромные конкурентные преимущества при старте и развитии собственного бизнеса: самостоятельный поиск партнеров, более выгодные условия сотрудничества (цены, сроки, качество), виденье новых возможностей. Тем не менее не всем по душе бизнес и многие предпочтут карьерный рост в успешных компаниях. Поэтому ниже мы больше поговорим именно о трудоустройстве в западных или Казахстанских компаниях. ",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                    //String str = "مخابز الأمين ذ.ذ.م";
                    String str = "Cùng một thế giới, cùng một giấc mơ.";
                    add(new PrintItemObj(str,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj(str,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));



                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj(new StringBuilder(str).reverse().toString(),PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj(new StringBuilder(str).reverse().toString(),PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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

    public void printText_xili()
    {
        try {
            this.printerDev.setPrinterGray(4);
            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
                    //add(new PrintItemObj("С открытием своего бизнеса все предельно ясно, знание китайского языка дает огромные конкурентные преимущества при старте и развитии собственного бизнеса: самостоятельный поиск партнеров, более выгодные условия сотрудничества (цены, сроки, качество), виденье новых возможностей. Тем не менее не всем по душе бизнес и многие предпочтут карьерный рост в успешных компаниях. Поэтому ниже мы больше поговорим именно о трудоустройстве в западных или Казахстанских компаниях. ",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                    //String str = "مخابز الأمين ذ.ذ.م";
                    String str = "éřťýúíìôpášďfģhjľžxčvbňm@#_€ &-+()/@№_&_± ()/¿¡;:'\"*©®£¥$¢√π×¶∆\\}==°^¢$¥£";
                    add(new PrintItemObj(str,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj(str,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));



                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj(new StringBuilder(str).reverse().toString(),PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj(new StringBuilder(str).reverse().toString(),PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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



    public void printText_test1()
    {
        try {
            this.printerDev.setPrinterGray(4);
            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
                    //add(new PrintItemObj("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

//                    add(new PrintItemObj("--------------------------------",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("Name               " + "Qty " + "Rat Amt",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("वेज चाऊमीन (नूडल्स) 7 50.0 350.0",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));


//                    add(new PrintItemObj("Dec 30,2018 19:52:53",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("Receipt Number:123456",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("Token:123-3456-7890",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                    add(new PrintItemObj("Quittung       27.02.2018 12:01",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Quittung       27.02.2018 12:01",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Fahrzeug: RA-SE 907",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Ordnungs-Nr:907",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Fanrer:   Peters Rolf",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("TAID:     18000000015",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Name:",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Name",PrinterConstant.FontSize.SMALL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Name",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("name",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Name:",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Name:",PrinterConstant.FontSize.XLARGE,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("------------------------------------------",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Von:",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("------------------------------------------",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("Nach:",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("------------------------------------------",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                    add(new PrintItemObj("Total:    9,00 EUR",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("MwST  7%:  0,59 EUR(enth.)",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("MwST.  7%:  0,59 EUR(enth.)",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("MwST.  7%:  0,59 EUR(enth.)",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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


    private Bitmap creatImage() {
        try {
//            InputStream ins = getAssets().open("res" + File.separator + "print.bmp");
//            Bitmap imageBitmap = BitmapFactory.decodeStream(ins);
            ArrayList<StringBitmapParameter> mParameters = new ArrayList<>();
            StringBitmapParameter mParameter = new StringBitmapParameter();
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter("Default font--------------------", Layout.Alignment.ALIGN_CENTER));
            mParameters.add(new StringBitmapParameter("Print Demo",Layout.Alignment.ALIGN_CENTER,50,10,true,false,false));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter("لتعبئة الرصيد اضغط رقم", Layout.Alignment.ALIGN_CENTER));
            mParameters.add(new StringBitmapParameter("*122* متبوعا بالرقم السريو من ثم#", Layout.Alignment.ALIGN_CENTER));
            mParameters.add(new StringBitmapParameter("To Top-up Your Balance Please Enter *112* Followed By Pin And #", Layout.Alignment.ALIGN_CENTER));
            mParameters.add(new StringBitmapParameter("--------------------------------", Layout.Alignment.ALIGN_CENTER));
            mParameter = new StringBitmapParameter("3043010573281063333333333333");
            mParameter.setBold(true);  //Set bold default false
            mParameter.setUnderline(true); //set Underline default false
            mParameter.setItalics(true); //set Italics default false
            mParameter.setLineSpacing(10);//Set the spacing between two lines default 0
            mParameter.setFontSize(20);//Set fontsize default 20
            mParameters.add(mParameter);
            mParameters.add(new StringBitmapParameter("To Top-up Your Balance Please Enter *112* Followed By Pin And #", Layout.Alignment.ALIGN_LEFT,20,5,true,false,false));
            mParameters.add(new StringBitmapParameter("--------------------------------", Layout.Alignment.ALIGN_CENTER));
            mParameters.add(new StringBitmapParameter("To Top-up Your Balance Please Enter *112* Followed By Pin And #", Layout.Alignment.ALIGN_RIGHT,20,5,true,false,false));
            mParameters.add(new StringBitmapParameter(""));

            String fontpath = "fonts/Newfolder10/ARIALN.TTF";
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter("ARIALN font---------------", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("لتعبئة الرصيد اضغط رقم", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("*122* متبوعا بالرقم السريو من ثم#", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));

            fontpath = "fonts/Newfolder10/arialbd.ttf";
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter("arialbd font---------------", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("لتعبئة الرصيد اضغط رقم", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("*122* متبوعا بالرقم السريو من ثم#", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));

            fontpath = "fonts/Newfolder10/arial.ttf";
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter("arial font---------------", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("لتعبئة الرصيد اضغط رقم", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("*122* متبوعا بالرقم السريو من ثم#", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));

            fontpath = "fonts/Newfolder10/ariali.ttf";
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter("ariali font---------------", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("لتعبئة الرصيد اضغط رقم", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("*122* متبوعا بالرقم السريو من ثم#", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            fontpath = "fonts/Newfolder10/calibrib.ttf";
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter("calibrib font---------------", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("لتعبئة الرصيد اضغط رقم", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("*122* متبوعا بالرقم السريو من ثم#", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));

            mParameters.add(new StringBitmapParameter(""));
            fontpath = "";
            mParameters.add(new StringBitmapParameter("default font---------------", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("To Top-up Your Balance Please Enter *112* Followed By Pin And #", Layout.Alignment.ALIGN_LEFT,30,5,true,false,false,fontpath));
            mParameters.add(new StringBitmapParameter(""));
            fontpath = "fonts/Nunito.ttf";
            mParameters.add(new StringBitmapParameter("Nunito font---------------", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("To Top-up Your Balance Please Enter *112* Followed By Pin And #", Layout.Alignment.ALIGN_LEFT,30,5,true,false,false,fontpath));
            mParameters.add(new StringBitmapParameter(""));
            fontpath = "fonts/BrushScriptStd.otf";
            mParameters.add(new StringBitmapParameter("BrushScriptStd font---------------", Layout.Alignment.ALIGN_CENTER,30,0,false,false,false,fontpath));
            mParameters.add(new StringBitmapParameter("To Top-up Your Balance Please Enter *112* Followed By Pin And #", Layout.Alignment.ALIGN_LEFT,30,5,true,false,false,fontpath));


            mParameter = new StringBitmapParameter("3043010573281063333333333333");
            mParameter.setBold(true);  //Set bold default false
            mParameter.setUnderline(true); //set Underline default false
            mParameter.setItalics(true); //set Italics default false
            mParameter.setLineSpacing(10);//Set the spacing between two lines default 0
            mParameter.setFontSize(20);//Set fontsize default 20
            mParameters.add(mParameter);
            mParameters.add(new StringBitmapParameter("To Top-up Your Balance Please Enter *112* Followed By Pin And #", Layout.Alignment.ALIGN_LEFT,20,5,true,false,false));
            mParameters.add(new StringBitmapParameter("--------------------------------", Layout.Alignment.ALIGN_CENTER));
            mParameters.add(new StringBitmapParameter("To Top-up Your Balance Please Enter *112* Followed By Pin And #", Layout.Alignment.ALIGN_RIGHT,20,5,true,false,false));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));





            BitmapUtil bitmapUtil = new BitmapUtil();
            Bitmap textBitmap = bitmapUtil.StringListtoBitmap(this, mParameters);

            Bitmap logo1 = BitmapFactory.decodeResource(getResources(),R.drawable.logo1);
            logo1 = bitmapUtil.fitBitmap(logo1,300);
            logo1 = bitmapUtil.addTextInBitmapFoot(logo1,"Alriyada",40);
            Bitmap logo2 = BitmapFactory.decodeResource(getResources(),R.drawable.logo2);
            logo2 = bitmapUtil.fitBitmap(logo2,300);
            logo2 = bitmapUtil.addTextInBitmapFoot(logo2,"Bahe wallet",40);

            Bitmap logo = bitmapUtil.addTwoLogo(logo1,logo2);

            Bitmap result = bitmapUtil.addBitmapInHead(logo,textBitmap);


            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
    打印阿拉伯语
     */
    public void printText_Arabic()
    {
        try {
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
            Bitmap bitmap = creatImage();
            String startTime = getCurTime();
            sendmessage(getStringByid(R.string.print_begin) + startTime);
            this.printerDev.printBmp(0, bitmap.getWidth(), bitmap.getHeight(), bitmap, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
                }

                @Override
                public void onError(int arg0) throws RemoteException {
                    sendmessage(getStringByid(R.string.print_faile_errcode) + arg0);
                }
            });
//            this.printerDev.printBarCode(-1, 162, 18, 65, "23418753401", new PrintStateChangeListener());
//
//            Bitmap qrcodeBitmap = QRCodeUtil.createQRImage("123456789",300,300,null);
//            try{
//                this.printerDev.printBmp(0, qrcodeBitmap.getWidth(), qrcodeBitmap.getHeight(), qrcodeBitmap, new AidlPrinterListener.Stub() {
//                    @Override
//                    public void onPrintFinish() throws RemoteException {
//                        sendmessage(getStringByid(R.string.print_success));
//                    }
//
//                    @Override
//                    public void onError(int arg0) throws RemoteException {
//                        sendmessage(getStringByid(R.string.print_faile_errcode) + arg0);
//                    }
//                });
//            } catch (RemoteException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        try {
//            this.printerDev.setPrinterGray(4);
//            printerDev.printText(new ArrayList<PrintItemObj>(){
//                {
//                    String astr = "لتعبئة الرصيد اضغط رقم";
//
////                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
////                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
////                    add(new PrintItemObj(astr,PrinterConstant.FontSize.XLARGE,false, PrintItemObj.ALIGN.RIGHT));
////                    add(new PrintItemObj("----------------",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
////                    add(new PrintItemObj(new StringBuilder(astr).reverse().toString(),PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
////                    add(new PrintItemObj(new StringBuilder(astr).reverse().toString(),PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
////                    add(new PrintItemObj(new StringBuilder(astr).reverse().toString(),PrinterConstant.FontSize.XLARGE,false, PrintItemObj.ALIGN.RIGHT));
//
////                    add(new PrintItemObj("----------------",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.CENTER));
////                    add(new PrintItemObj(new StringBuilder(astr).reverse().toString(),PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.CENTER));
////                    astr = "*122* متبوعا بالرقم السريو من ثم#";
////                    add(new PrintItemObj(new StringBuilder(astr).reverse().toString(),PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
////                    add(new PrintItemObj("ABCDEFGHIJKLMNABCDEFGHIJKLMNABCDEFGHIJKLMNABCDEFGHIJKLMN",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));
////
////                    add(new PrintItemObj("----------------",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
////                    add(new PrintItemObj("RIGHT",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
////                    add(new PrintItemObj("ABCDEFGHIJKLMNABCDEFGHIJKLMNABCDEFGHIJKLMNABCDEFGHIJKLMN",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
////                    add(new PrintItemObj("--------------",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.CENTER));
////                    add(new PrintItemObj("CENTER",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.CENTER));
////                    add(new PrintItemObj("ABCDEFGHIJKLMNABCDEFGHIJKLMNABCDEFGHIJKLMNABCDEFGHIJKLMN",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.CENTER));
////
////                    add(new PrintItemObj("ABCDEFGHIJKLMN",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.LEFT));
////                    add(new PrintItemObj("ABCDEFGHIJKLMN",PrinterConstant.FontSize.LARGE,true, PrintItemObj.ALIGN.LEFT));
////
////                    add(new PrintItemObj(",سعدت بلقائك.,سعدت بلقائك.سعدت بلقائك.,سعدت بلقائك.,سعدت بلقائك.سعدت بلقائك.",PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
//
//                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
//
//                }
//            }, new AidlPrinterListener.Stub() {
//
//                @Override
//                public void onPrintFinish() throws RemoteException {
//                    String endTime = getCurTime();
//                    sendmessage(getStringByid(R.string.print_end) + endTime);
//                }
//
//                @Override
//                public void onError(int arg0) throws RemoteException {
//                    sendmessage(getStringByid(R.string.print_faile_errcode) + arg0);
//                }
//            });
//        } catch (RemoteException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    private Bitmap creatImage_Sabra() {
        // Text to picture printing
        try {

            ArrayList<StringBitmapParameter> mParameters = new ArrayList<>();
            StringBitmapParameter mParameter = new StringBitmapParameter();
            String astr = "לקוח";
            mParameters.add(new StringBitmapParameter("Text to Picture Printing",Layout.Alignment.ALIGN_CENTER,30,10,true,false,false));
            mParameters.add(new StringBitmapParameter("fontsize 20",Layout.Alignment.ALIGN_CENTER,20,10,true,false,false));
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "לקוח";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "קוד לקוח";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "אסמכתא";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "-----------";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "זמן התחלה";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "זמן סיום";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "מספר משאית";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "שם נהג";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "גליל אפור 12 מלא";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "נפרקו";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "הועמסו";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "מחיר ליחידה";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));
            astr = "סה\"כ בש\"ח";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,20,5,false,false,false));

            mParameters.add(new StringBitmapParameter("fontsize 30",Layout.Alignment.ALIGN_CENTER,30,10,true,false,false));
            astr = "לקוח";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "קוד לקוח";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "אסמכתא";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "-----------";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "זמן התחלה";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "זמן סיום";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "מספר משאית";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "שם נהג";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "גליל אפור 12 מלא";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "נפרקו";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "הועמסו";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "מחיר ליחידה";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));
            astr = "סה\"כ בש\"ח";
            mParameters.add(new StringBitmapParameter(astr, Layout.Alignment.ALIGN_RIGHT,30,5,false,false,false));


            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));
            mParameters.add(new StringBitmapParameter(""));




            BitmapUtil bitmapUtil = new BitmapUtil();
            Bitmap textBitmap = bitmapUtil.StringListtoBitmap(this, mParameters);


            return textBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void print_sabraText(){
        try {
            this.printerDev.setPrinterGray(4);
            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
                    add(new PrintItemObj("Print text",PrinterConstant.FontSize.XLARGE,false, PrintItemObj.ALIGN.CENTER));
                    add(new PrintItemObj("Normal fontsize",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.CENTER));
                    String astr = "לקוח";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "קוד לקוח";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "אסמכתא";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "-----------";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "זמן התחלה";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "זמן סיום";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "מספר משאית";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "שם נהג";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "גליל אפור 12 מלא";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "נפרקו";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "הועמסו";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "מחיר ליחידה";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "סה\"כ בש\"ח";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.RIGHT));


                    add(new PrintItemObj("LARGE fontsize",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.CENTER));
                    astr = "לקוח";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "קוד לקוח";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "אסמכתא";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "-----------";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "זמן התחלה";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "זמן סיום";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "מספר משאית";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "שם נהג";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "גליל אפור 12 מלא";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "נפרקו";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "הועמסו";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "מחיר ליחידה";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "סה\"כ בש\"ח";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));
                    astr = "";
                    add(new PrintItemObj(astr,PrinterConstant.FontSize.LARGE,false, PrintItemObj.ALIGN.RIGHT));

                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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

    private void print_texttopicture(){
        try {
            Bitmap bitmap = creatImage_Sabra();
            String startTime = getCurTime();
            sendmessage(getStringByid(R.string.print_begin) + startTime);
            this.printerDev.printBmp(0, bitmap.getWidth(), bitmap.getHeight(), bitmap, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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
    public void printText_Sabra()
    {
        print_sabraText();  //text printing
        print_texttopicture();//text to picture printing


    }

    public void printText_test2()
    {
        final List<OrderInfo> orderInfoList = new ArrayList<>();
        orderInfoList.add(new OrderInfo("Chris Liu",5,100,"aaaaaaaaaaaaaa",8));
        orderInfoList.add(new OrderInfo("Madhav Bhatia",6,200,"bbbbbbbbbbbbb",19));
        orderInfoList.add(new OrderInfo("Irvim John Kenneth Loyd Martin Nero ",100000,34,"cccccccccccccccccccccccccc",19));
        orderInfoList.add(new OrderInfo("Adolph Blaine Charles David Earl Frederick Gerald Hubert Irvim John Kenneth Loyd Martin Nero Oliver Paul Quincy Randolph Sherman Thomas",10,3400.5,"ddddddddddddddddddddddddd",34));
        orderInfoList.add(new OrderInfo("Adolph Blaine",1001,340,"ee",19));


        try {
            this.printerDev.setPrinterGray(4);
            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
                    int NameLens = 16;
                    int QuantityLens = 9;
                    int PriceLens = 6;
                    int MaxLens = 32;
                    int ProfileLens = 12;
                    int AgeLens = 0;
                    //17 + 9 + 6
                    add(new PrintItemObj("Name             " + "Quantity " + "Price ",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    for(int i=0;i<orderInfoList.size();i++){
                        String name = orderInfoList.get(i).getName();
                        String Quantity = String.valueOf(orderInfoList.get(i).getQuantity());
                        String Price = String.valueOf(orderInfoList.get(i).getPrice());
                        String ptext = "";
                        if(name.length()<NameLens)
                        {
                            ptext += name ;
                        }else{
                            ptext += name.substring(0,NameLens) ;
                        }
                        ptext+=leftPadd(Quantity,(NameLens+QuantityLens/2-ptext.length())," ");
                        ptext+=leftPadd(Price,(MaxLens-ptext.length()-PriceLens + (PriceLens-Price.length())/2)," ");
                        Log.d(TAG,ptext);
                        add(new PrintItemObj(ptext,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                        if(name.length()>=NameLens){
                            ptext = "";
                            int index = NameLens;
                            while(index<name.length())
                            {
                                if(index+NameLens>name.length())
                                    ptext = name.substring(index,name.length()-1);
                                else
                                    ptext = name.substring(index,index+NameLens);
                                index+=NameLens;
                                Log.d(TAG,ptext);
                                add(new PrintItemObj(ptext,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                            }
                        }
                    }

                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));


                    Log.d(TAG,"Name                 " + "Profile");
                    add(new PrintItemObj("Name                 " + "Profile",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    NameLens = 20;
                    ProfileLens = MaxLens - NameLens-1;
                    for(int i=0;i<orderInfoList.size();i++){
                        String name = orderInfoList.get(i).getName();
                        String Quantity = String.valueOf(orderInfoList.get(i).getQuantity());
                        String Price = String.valueOf(orderInfoList.get(i).getPrice());
                        String profile = orderInfoList.get(i).getProfile();
                        String age = String.valueOf(orderInfoList.get(i).getAge());
                        String ptext = "";
                        while(true) {
                            ptext = "";
                            if(name.length()<1&&profile.length()<1)
                                break;
                            if (name.length() < NameLens) {
                                ptext += name;
                                name = "";
                            } else {
                                ptext += name.substring(0, NameLens);
                                name = name.substring(NameLens);
                            }
                            if (profile.length() < ProfileLens) {
                                ptext += leftPadd(profile, NameLens - ptext.length()+1, " ");
                                profile = "";
                            } else {
                                ptext += leftPadd(profile.substring(0,ProfileLens), NameLens-ptext.length()+1, " ");
                                profile = profile.substring(ProfileLens);
                            }
                            Log.d(TAG,ptext);
                            add(new PrintItemObj(ptext,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                        }
                    }

                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                    Log.d(TAG,"Name             " + "Profile    " + "Age");
                    add(new PrintItemObj("Name             " + "Profile    " + "Age",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    NameLens = 16;
                    ProfileLens = 10;
                    AgeLens = 3;
                    for(int i=0;i<orderInfoList.size();i++){
                        String name = orderInfoList.get(i).getName();
                        String Quantity = String.valueOf(orderInfoList.get(i).getQuantity());
                        String Price = String.valueOf(orderInfoList.get(i).getPrice());
                        String profile = orderInfoList.get(i).getProfile();
                        String age = String.valueOf(orderInfoList.get(i).getAge());
                        String ptext = "";
                        while(true) {
                            ptext = "";
                            if(name.length()<1&&profile.length()<1)
                                break;
                            if (name.length() < NameLens) {
                                ptext += name;
                                name = "";
                            } else {
                                ptext += name.substring(0, NameLens);
                                name = name.substring(NameLens);
                            }
                            if (profile.length() < ProfileLens) {
                                ptext += leftPadd(profile, NameLens - ptext.length()+1, " ");
                                profile = "";
                            } else {
                                ptext += leftPadd(profile.substring(0,ProfileLens), NameLens-ptext.length()+1, " ");
                                profile = profile.substring(ProfileLens);
                            }
                            if(age.length()>0) {
                                ptext += leftPadd(age, MaxLens - ptext.length() - AgeLens , " ");
                                age = "";
                            }
                            Log.d(TAG,ptext);
                            add(new PrintItemObj(ptext,PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                        }
                    }






                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));




                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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


    private String leftPadd(String str,int size,String ch)
    {
        String ret = "";
        for(int i=0;i<size;i++){
            ret+=ch;
        }
        return ret+str;
    }

    private Bitmap getbmp()
    {
        Bitmap bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.LEFT);// 若设置为center，则文本左半部分显示不全 paint.setColor(Color.RED);
        paint.setAntiAlias(true);// 消除锯齿
        paint.setTextSize(28);

        canvas.drawText("-----------------------------------------------", 5, 30, paint) ;

        canvas.drawText("Name                     Qty Rat Amt", 5, 60, paint) ;

        canvas.drawText("वेज चाऊमीन (नूडल्स) 7 50.0 350.0", 5, 90, paint) ;
        //canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.save();
        canvas.restore();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTargetDensity = 160;
        options.inDensity = 160;
        byte[] data = outputStream.toByteArray();
        Bitmap bitmapnew = BitmapFactory.decodeByteArray(data,0,data.length,options);
        return bitmapnew;
    }

    /**
     * 打印位图
     * @createtor：Administrator
     * @date:2015-8-4 下午2:39:33
     */
    public void printBitmap(){
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.sxlogo380);
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.logo3);
            //Bitmap bitmap = getbmp();
            String startTime = getCurTime();
            sendmessage(getStringByid(R.string.print_begin) + startTime);
            this.printerDev.printBmp(0, bitmap.getWidth(), bitmap.getHeight(), bitmap, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
                }

                @Override
                public void onError(int arg0) throws RemoteException {
                    sendmessage(getStringByid(R.string.print_faile_errcode) + arg0);
                }
            });


            printerDev.printText(new ArrayList<PrintItemObj>(){
            {
                add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

            }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
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

    private class PrintStateChangeListener extends AidlPrinterListener.Stub{

        @Override
        public void onError(int arg0) throws RemoteException {
            sendmessage(getStringByid(R.string.print_faile_errcode) + arg0);
        }

        @Override
        public void onPrintFinish() throws RemoteException {
            String endTime = getCurTime();
            sendmessage(getStringByid(R.string.print_end) + endTime);

        }

    }

    /**
     * 打印条码
     * @createtor：Administrator
     * @date:2015-8-4 下午3:02:21
     */
    private boolean printLf = false;
    public void printBarCode(){
        try {
            String startTime = getCurTime();
            sendmessage(getStringByid(R.string.print_begin) + startTime);
            this.printerDev.printBarCode(-1, 162, 18, 65, "23418753401", new PrintStateChangeListener());
            this.printerDev.printBarCode(-1, 162, 18, 66, "03400471", new PrintStateChangeListener());
            this.printerDev.printBarCode(-1, 162, 18, 67, "2341875340111", new PrintStateChangeListener());
            this.printerDev.printBarCode(-1, 162, 18, 68, "23411875", new PrintStateChangeListener());
            this.printerDev.printBarCode(-1, 162, 18, 69, "*23418*", new PrintStateChangeListener());
            this.printerDev.printBarCode(-1, 162, 18, 70, "234187534011", new PrintStateChangeListener());
            this.printerDev.printBarCode(-1, 162, 18, 71, "23418", new PrintStateChangeListener());
            this.printerDev.printBarCode(-1, 162, 18, 72, "23418", new PrintStateChangeListener());
            this.printerDev.printBarCode(-1, 162, 18, 73, "{A23418", new PrintStateChangeListener());
            printLf = true;
            Thread.sleep(3000);
            printerDev.printText(new ArrayList<PrintItemObj>(){
                {
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));
                    add(new PrintItemObj("",PrinterConstant.FontSize.NORMAL,false, PrintItemObj.ALIGN.LEFT));

                }
            }, new AidlPrinterListener.Stub() {

                @Override
                public void onPrintFinish() throws RemoteException {
                    String endTime = getCurTime();
                    sendmessage(getStringByid(R.string.print_end) + endTime);
                }

                @Override
                public void onError(int arg0) throws RemoteException {
                    sendmessage(getStringByid(R.string.print_faile_errcode) + arg0);
                }
            });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }




    /*
    PSAM
     */

    public void getpsam(int val) {

        try {
            psam = AidlPsam.Stub.asInterface(deviceManager
                    .getPSAMReader(val));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 打开PSAM卡
    public void psamopen() {
        if(isNormalVelocityClick(DELAY_TIME)) {
            try {
                boolean flag = psam.open();
                if (flag) {
                    sendmessage(getStringByid(R.string.psam_open_success));
                } else {
                    sendmessage(getStringByid(R.string.psam_open_faile));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }

    // apdu交互测试
    public void psamapducmd() {
        if(isNormalVelocityClick(DELAY_TIME)) {
            byte[] apdu = new byte[]{0x00, (byte) 0xB0, (byte) 0x96, 0x00, 0x06};
            try {
                byte[] data = psam.apduComm(apdu);
                if (null != data) {
                    sendmessage(getStringByid(R.string.result)+ HexUtil.bcd2str(data));
                } else {
                    sendmessage(getStringByid(R.string.nfc_apdu_faile));
                }

            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }

    // PSAM卡复位
    public void psamreset() {
        if(isNormalVelocityClick(DELAY_TIME)) {
            try {
                byte[] data = psam.reset(0x00);
                if (null != data) {
                    sendmessage(getStringByid(R.string.psam_reset_success)+ HexUtil.bcd2str(data));
                } else {
                    sendmessage(getStringByid(R.string.psam_reset_faile));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }

    // 关闭
    public void psamclose() {
        if(isNormalVelocityClick(DELAY_TIME)){
            try {
                boolean flag = psam.close();
                if (flag) {
                    sendmessage(getStringByid(R.string.psam_close_success));
                } else {
                    sendmessage(getStringByid(R.string.psam_close_faile));
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sendmessage(getStringByid(R.string.ic_dotnot_click_quickly));
        }
    }






    /*
    扫码
     */
    public void backScan() {
        Log.d(TAG,"backScan");
        sendmessage(getStringByid(R.string.qrcode_back_camera));
        if(iScanner==null) {
            try {
                iScanner = AidlCameraScanCode.Stub.asInterface(deviceManager.getCameraManager());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(iScanner==null)
        {

            sendmessage(getStringByid(R.string.qrcode_openfaile));
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(AidlScanParam.SCAN_CODE,new AidlScanParam(0,10));
        try {
            iScanner.scanCode(bundle, new AidlCameraScanCodeListener.Stub() {
                @Override
                public void onResult(String s) throws RemoteException {
                    sendmessage(getStringByid(R.string.qrcode_result) + s);
                }

                @Override
                public void onCancel() throws RemoteException {
                    sendmessage(getStringByid(R.string.qrcode_cancel));
                }

                @Override
                public void onError(int i) throws RemoteException {
                    sendmessage(getStringByid(R.string.error_code) + i);
                }

                @Override
                public void onTimeout() throws RemoteException {
                    sendmessage(getStringByid(R.string.qrcode_timeout));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopScan() {
        Log.d(TAG,"stopScan");
        try {
            iScanner.stopScan();
            sendmessage(getStringByid(R.string.qrcode_cancel));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    /*
    解码
     */
    public void decode_init() {
        try {
            if(iDecoder.init() == 0) {
                sendmessage(getStringByid(R.string.decode_init_success));
            } else {
//                sendmessage(getStringByid(R.string.decode_init_faile));
                iDecoder.exit();
                if(iDecoder.init() == 0) {
                    sendmessage(getStringByid(R.string.decode_init_success));
                } else {
                    sendmessage(getStringByid(R.string.decode_init_faile));
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void decoder() {
        try {
            DecodeUtils utils = new DecodeUtils(this);
//            Bitmap bitmap =BitmapFactory.decodeResource(getResources(), R.drawable.tp_decode_check);//  utils.getRes(QR_DECODE_DRAWABLE_NAME);
            InputStream in = getResources().getAssets().open("tp_decode_check.png");
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if(bitmap==null) return;
            String text = iDecoder.decode(utils.decodeBarcodeYUV(bitmap),bitmap.getWidth(),bitmap.getHeight());
            if(text == null) {
                sendmessage(getStringByid(R.string.decode_data) + "NULL");
            } else if(text.length() == 0){
                sendmessage(getStringByid(R.string.decode_data_non));
            } else {
                sendmessage(getStringByid(R.string.decode_data) + text);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decode_exit() {
        try {
            iDecoder.exit();
            sendmessage(getStringByid(R.string.decode_exit));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /*
    蜂鸣器
     */
    public void startbeep() {
        try {
            iBeeper.beep(0,10000);
            sendmessage(getStringByid(R.string.beep_start));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopbeep() {
        try {
            iBeeper.stopBeep();
            sendmessage(getStringByid(R.string.beep_stop));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*
    LED
     */

    Runnable ledrunnable = new Runnable() {
        @Override
        public void run() {
            int inv = 500;
            try {
                while(true) {
                    iLed.setLed(LedCode.OPER_LED_ALL, false);
                    for (int i = 1; i < 5; i++) {
                        iLed.setLed(i, true);
                        Thread.sleep(inv);
                        iLed.setLed(i, false);
                    }
                    iLed.setLed(LedCode.OPER_LED_ALL, true);
                    Thread.sleep(inv);
                    iLed.setLed(LedCode.OPER_LED_ALL, false);
                    Thread.sleep(inv);
                    iLed.setLed(LedCode.OPER_LED_ALL, true);
                    Thread.sleep(inv);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    public void startled()
    {
        if(thread_led==null)
        {
           // ledrunnable.run();
            thread_led = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int inv = 500;
                        while(true) {
                            iLed.setLed(LedCode.OPER_LED_ALL, false);
                            for (int i = 1; i < 5; i++) {
                                iLed.setLed(i, true);
                                Thread.sleep(inv);
                                iLed.setLed(i, false);
                            }
                            iLed.setLed(LedCode.OPER_LED_ALL, true);
                            Thread.sleep(inv);
                            iLed.setLed(LedCode.OPER_LED_ALL, false);
                            Thread.sleep(inv);
                            iLed.setLed(LedCode.OPER_LED_ALL, true);
                            Thread.sleep(inv);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread_led.start();
        }
    }


    public void stopled()
    {
        if(thread_led!=null) {
            thread_led.interrupt();
            thread_led = null;
        }

        try {
            iLed.setLed(LedCode.OPER_LED_ALL, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /*
    PBOC
     */
    private class PbocStartListener extends AidlPbocStartListener.Stub {
        @Override
        public void requestUserAuth(int certType, String certno) throws RemoteException {
            sendmessage(getString(R.string.confirm_account_type));
            sendmessage(getString(R.string.account_type) + certType);
            sendmessage(getString(R.string.account_number) + certno);
            pboc.importUserAuthRes(true);
        }

        @Override
        public void requestTipsConfirm(String arg0) throws RemoteException {
            sendmessage(getString(R.string.conform_information));
            sendmessage(arg0);
            pboc.importMsgConfirmRes(true);
        }

        @Override
        public void requestImportPin(int times, boolean lastFlag, String amount)
                throws RemoteException {
            sendmessage(getString(R.string.input_offline_pin));
            sendmessage(getString(R.string.input_frequency) + times);
            sendmessage(getString(R.string.is_final_input_frequency) + lastFlag);
            sendmessage(getString(R.string.amount_msg) + amount);
            pboc.importPin("26888888FFFFFFFF");
        }

        @Override
        public void finalAidSelect() throws RemoteException {
            pboc.importFinalAidSelectRes(true);
        }

        @Override
        public void requestImportAmount(int arg0) throws RemoteException {
            sendmessage(getString(R.string.import_amount_type) + arg0);
            sendmessage(getString(R.string.import_amount));
            pboc.importAmount("666.00");
        }

        @Override
        public void requestEcashTipsConfirm() throws RemoteException {
            sendmessage(getString(R.string.confirm_electronic_cash));
            pboc.importECashTipConfirmRes(false);
        }

        @Override
        public void requestAidSelect(int times, String[] arg1)
                throws RemoteException {
            sendmessage(getString(R.string.choice_app_list));
            String str = "";
            for (int i = 0; i < arg1.length; i++) {
                str += arg1[i];
            }
            sendmessage(getString(R.string.application_list_content) + str);
            pboc.importAidSelectRes(0x01);
        }

        @Override
        public void onTransResult(int arg0) throws RemoteException {
            sendmessage(getString(R.string.PBOC_result));
            switch (arg0) {
                case 0x01:
                    sendmessage(getString(R.string.allow_trading));
                    break;
                case 0x02:
                    sendmessage(getString(R.string.Refuse_to_deal));
                    break;
                case 0x03:
                    sendmessage(getString(R.string.stop_trading));
                    break;
                case 0x04:
                    sendmessage(getString(R.string.downgrade));
                    break;
                case 0x05:
                    sendmessage(getString(R.string.user_other_view));
                    break;
                case 0x06:
                    sendmessage(getString(R.string.unknown_exception));
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onRequestOnline() throws RemoteException {
            sendmessage(getString(R.string.request_online));
        }

        @Override
        public void onReadCardTransLog(PCardTransLog[] arg0) throws RemoteException {
            if (null != arg0) {
                for (int i = 0; i < arg0.length; i++) {
                    sendmessage("第" + i + getString(R.string.transaction_information));
                    sendmessage(getString(R.string.trans_type) + arg0[i].getTranstype());
                    sendmessage(getString(R.string.trans_time) + arg0[i].getTransTime());
                    sendmessage(getString(R.string.trans_date) + arg0[i].getTransDate());
                    sendmessage(getString(R.string.other_amount) + arg0[i].getOtheramt());
                    sendmessage(getString(R.string.currency_code) + arg0[i].getMoneyCode());
                    sendmessage(getString(R.string.merchants_name) + arg0[i].getMerchantName());
                    sendmessage(getString(R.string.country_code) + arg0[i].getCountryCode());
                    sendmessage(getString(R.string.amount_of_the_transaction) + arg0[i].getAmt());
                    sendmessage(getString(R.string.transaction_statistics) + HexUtil.bcd2str(arg0[i].getAppTransCount()));
                }
            }
        }

        @Override
        public void onReadCardOffLineBalance(String arg0, String arg1, String arg2,
                                             String arg3) throws RemoteException {
            sendmessage(getString(R.string.first_e_cash_money_code) + arg0);
            sendmessage(getString(R.string.first_e_cash_balance) + arg1);
            sendmessage(getString(R.string.second_e_cash_money_code) + arg2);
            sendmessage(getString(R.string.second_e_cash_balance) + arg3);
        }

        @Override
        public void onReadCardLoadLog(String arg0, String arg1, PCardLoadLog[] arg2)
                throws RemoteException {
            sendmessage(getString(R.string.transaction_statistics) + arg0);
            sendmessage(getString(R.string.log_check_code) + arg1);
            for (int i = 0; i < arg2.length; i++) {
                sendmessage(getString(R.string.log_info_index, i));
                sendmessage(arg2[i].getPutdata_p1());
                sendmessage(arg2[i].getPutdata_p2());
                sendmessage(arg2[i].getTransDate());
                sendmessage(arg2[i].getTransTime());
                sendmessage(arg2[i].getBefore_putdata());
                sendmessage(HexUtil.bcd2str(arg2[i].getAppTransCount()));
                sendmessage(arg2[i].getAfter_putdata());
            }
        }

        @Override
        public void onError(int arg0) throws RemoteException {
            sendmessage(getString(R.string.pboc_error) + arg0);
        }

        @Override
        public void onConfirmCardInfo(CardInfo arg0) throws RemoteException {
            sendmessage(getString(R.string.card_info) + arg0.getCardno());
            sendmessage(getString(R.string.please_confirm));
            pboc.importConfirmCardInfoRes(true);
        }
    }


    public void checkCard() {
        if (null != pboc) {
            sendmessage(getString(R.string.insert_or_swipe_card));
            try {
                pboc.checkCard(true, true, true, 60000, new AidlCheckCardListener.Stub() {

                    @Override
                    public void onTimeout() throws RemoteException {
                        sendmessage(getString(R.string.search_card_timeout));
                    }

                    @Override
                    public void onSwipeCardFail() throws RemoteException {
                        sendmessage(getString(R.string.swipe_failed));
                        pboc.cancelCheckCard();
                    }

                    @Override
                    public void onFindRFCard() throws RemoteException {
                        sendmessage(getString(R.string.swiped_rf_card));
                        transData.setSlotType((byte) EmvConstant.SlotType.SLOT_TYPE_RF);
                        transData.setEmvFlow((byte) 0x02);
//						pboc.cancelCheckCard();
                    }

                    @Override
                    public void onFindMagCard(TrackData trackData) throws RemoteException {
                        sendmessage(getString(R.string.swiped_ic_card));
                        sendmessage(getString(R.string.card_number) + trackData.getCardno());
                        sendmessage(getString(R.string.first_track_data) + trackData.getFirstTrackData());
                        sendmessage(getString(R.string.secound_track_data) + trackData.getSecondTrackData());
                        sendmessage(getString(R.string.third_rack_data) + trackData.getThirdTrackData());
                        sendmessage(getString(R.string.card_service_code) + trackData.getServiceCode());
                        sendmessage(getString(R.string.validity) + trackData.getExpiryDate());
                        sendmessage(getString(R.string.format_track_data) + trackData.getFormatTrackData());
//						pboc.cancelCheckCard();
                    }

                    @Override
                    public void onFindICCard() throws RemoteException {
                        sendmessage(getString(R.string.swiped_mgic_card));
                        transData.setSlotType((byte) EmvConstant.SlotType.SLOT_TYPE_IC);
                        transData.setEmvFlow((byte) 0x01);
//						pboc.cancelCheckCard();
                    }

                    @Override
                    public void onError(int arg0) throws RemoteException {
                        sendmessage(getString(R.string.search_card_failed) + arg0);
                    }

                    @Override
                    public void onCanceled() throws RemoteException {
                        sendmessage(getString(R.string.cancel_search_card));
                    }
                });
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void cancelCheckCard() {
        try {
            pboc.cancelCheckCard();
            sendmessage(getString(R.string.cancel_search_card_succuess));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void checkCardWithEncryptedTDK() {
        Log.v("asewang", "111111111111");
        if (null != pboc) {
            sendmessage(getString(R.string.insert_or_swipe_card));
            try {
                pboc.checkCardWithEncryptedTDK(EmvConstant.AppType.APPTYPE_LAKALA_PAYMENT, null, true, true, true, 60000, new AidlCheckCardListener.Stub() {

                    @Override
                    public void onTimeout() throws RemoteException {
                        sendmessage(getString(R.string.search_card_timeout));
                    }

                    @Override
                    public void onSwipeCardFail() throws RemoteException {
                        sendmessage(getString(R.string.swipe_failed));
                        pboc.cancelCheckCardWithEncryptedTDK();
                    }

                    @Override
                    public void onFindRFCard() throws RemoteException {
                        sendmessage(getString(R.string.swiped_rf_card));
                        transData.setSlotType((byte) EmvConstant.SlotType.SLOT_TYPE_RF);
                    }

                    @Override
                    public void onFindMagCard(TrackData trackData) throws RemoteException {
                        sendmessage(getString(R.string.swiped_ic_card));
                        sendmessage(getString(R.string.card_number) + trackData.getCardno());
                        sendmessage(getString(R.string.first_track_data) + trackData.getFirstTrackData());
                        sendmessage(getString(R.string.secound_track_data) + trackData.getSecondTrackData());
                        sendmessage(getString(R.string.third_rack_data) + trackData.getThirdTrackData());
                        sendmessage(getString(R.string.card_service_code) + trackData.getServiceCode());
                        sendmessage(getString(R.string.validity) + trackData.getExpiryDate());
                        sendmessage(getString(R.string.format_track_data) + trackData.getFormatTrackData());
                    }

                    @Override
                    public void onFindICCard() throws RemoteException {
                        sendmessage(getString(R.string.swiped_mgic_card));
                        transData.setSlotType((byte) EmvConstant.SlotType.SLOT_TYPE_IC);
                    }

                    @Override
                    public void onError(int arg0) throws RemoteException {
                        sendmessage(getString(R.string.search_card_failed) + arg0);
                    }

                    @Override
                    public void onCanceled() throws RemoteException {
                        sendmessage(getString(R.string.cancel_search_card));
                    }
                });
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void cancelCheckEncryptedCard() {
        try {
            pboc.cancelCheckCardWithEncryptedTDK();
            sendmessage(getString(R.string.cancel_encrypted_search_card_succuess));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void consume() {
        try {
            transData.setTranstype((byte) 0x00);
            pboc.processPBOC(transData, pboclistener);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void readCardOfflineBalance() {
        transData.setTranstype((byte) 0xF2);
        try {
            pboc.processPBOC(transData, pboclistener);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void readCardTransLog() {
        transData.setTranstype((byte) 0xF3);
        try {
            pboc.processPBOC(transData, pboclistener);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void readCardLoadLog() {
        transData.setTranstype((byte) 0xF4);
        try {
            pboc.processPBOC(transData, pboclistener);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void readKernelData() {
        byte[] data = new byte[2048];
        String[] tags = {"9F26", "9F37", "9F36", "95", "9A", "9C", "9F02", "5F2A", "82", "9F1A", "9F03", "9F33", "9F34", "9F35", "9F1E", "84", "9F09", "9F41", "9F63"};
        try {
            int retCode = pboc.readKernelData(tags, data);
            if (retCode > 0) {
                sendmessage(getString(R.string.kernel_data_read_success) + HexUtil.bcd2str(data));
            } else {
                sendmessage(getString(R.string.kernel_data_read_failed));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readKernelEDData() {
        String[] tags = {"57", "5A", "5F34", "5F24"};
        try {
            byte[] retCode = pboc.readKernelEDData(0, null, tags);
            if (retCode != null) {
                sendmessage(getString(R.string.kernel_data_read_success) + HexUtil.bcd2str(retCode));
            } else {
                sendmessage(getString(R.string.kernel_data_read_failed));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCaParam() {
        try {
            boolean flag = this.pboc.updateCAPK(0x01, "9F0605A0000000659F220112DF05083230313431323331DF060101DF070101DF0281B0ADF05CD4C5B490B087C3467B0F3043750438848461288BFEFD6198DD576DC3AD7A7CFA07DBA128C247A8EAB30DC3A30B02FCD7F1C8167965463626FEFF8AB1AA61A4B9AEF09EE12B009842A1ABA01ADB4A2B170668781EC92B60F605FD12B2B2A6F1FE734BE510F60DC5D189E401451B62B4E06851EC20EBFF4522AACC2E9CDC89BC5D8CDE5D633CFD77220FF6BBD4A9B441473CC3C6FEFC8D13E57C3DE97E1269FA19F655215B23563ED1D1860D8681DF040103DF0314874B379B7F607DC1CAF87A19E400B6A9E25163E8");
            if (flag) {
                sendmessage(getString(R.string.add_pub_key_params_success));
            } else {
                sendmessage(getString(R.string.add_pub_key_params_failed));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateCaParam() {
        try {
            boolean flag = this.pboc.updateCAPK(0x02, "9F0605A0000000659F220112DF05083230313431323331DF060101DF070101DF0281B0ADF05CD4C5B490B087C3467B0F3043750438848461288BFEFD6198DD576DC3AD7A7CFA07DBA128C247A8EAB30DC3A30B02FCD7F1C8167965463626FEFF8AB1AA61A4B9AEF09EE12B009842A1ABA01ADB4A2B170668781EC92B60F605FD12B2B2A6F1FE734BE510F60DC5D189E401451B62B4E06851EC20EBFF4522AACC2E9CDC89BC5D8CDE5D633CFD77220FF6BBD4A9B441473CC3C6FEFC8D13E57C3DE97E1269FA19F655215B23563ED1D1860D8681DF040103DF0314874B379B7F607DC1CAF87A19E400B6A9E25163E8");
            if (flag) {
                sendmessage(getString(R.string.update_pub_key_success));
            } else {
                sendmessage(getString(R.string.update_pub_key_failed));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void delCaParam() {
        try {
            boolean flag = this.pboc.updateCAPK(0x03, null);
            if (flag) {
                sendmessage(getString(R.string.clear_pub_key_success));
            } else {
                sendmessage(getString(R.string.clear_pub_key_failed));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void clearTransData() {
        try {
            boolean flag = this.pboc.clearKernelICTransLog();
            if (flag) {
                sendmessage(getString(R.string.clear_pay_log_success));
            } else {
                sendmessage(getString(R.string.clear_ic_lay_log_failed));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void abortPboc() {
        try {
            this.pboc.abortPBOC();
            sendmessage(getString(R.string.pause_pboc_success));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void endPboc() {
        try {
            this.pboc.endPBOC();
            sendmessage(getString(R.string.stop_pboc_success));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void addAid() {
        try {
            //读取assert下的IC卡参数配置文件，将相关参数加载到EMV内核
            try {
                boolean updateResult = false;
                boolean flag = true;
                int i = 0;
                String success = "";
                String fail = "";
                // 获取IC卡参数信息
                InputStream ins = this.getAssets().open("icparam/ic_param.txt");
                if (ins != null && ins.available() != 0x00) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(ins));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        // 未到达文件末尾
                        if (null != line) {
                            if (line.startsWith("AID")) {
                                // 更新AID
                                updateResult = this.pboc
                                        .updateAID(0x01,
                                                line.split("=")[1]);
                                i++;
                                if (updateResult) {
                                    success = success + i + ",";
                                } else {
                                    fail = fail + i + ",";
                                }
//							} else { // 更新RID
//								updateResult = this.pboc.updateCAPK(0x01, line.split("=")[1]);
//								Debug.d("加入公钥参数结果" + updateResult);
//								sendmessage("增加公钥参数参数" + updateResult);
                            }
                        }
                    }

                    if (TextUtils.isEmpty(fail)) {
                        sendmessage(getString(R.string.add_aid_success, i));
                    } else if (TextUtils.isEmpty(success)) {
                        sendmessage(getString(R.string.add_aid_all_failed, i));
                    } else {
                        success = success.substring(0, success.length() - 1);
                        fail = fail.substring(0, fail.length() - 1);
                        //sendmessage("增加"+i+"条AID参数\n第"+success+"条增加成功\n第"+fail+"条增加失败");
                        sendmessage(getString(R.string.add_aid_failed));
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateAid() {
        try {
            boolean flag = this.pboc.updateAID(0x02, "9F0607A0000000041010DF0101009F08020002DF1105FC5080A000DF1205F85080F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF180100");
            if (flag) {
                sendmessage(getString(R.string.update_aid_success));
            } else {
                sendmessage(getString(R.string.update_aid_failed));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void delAid() {
        try {
            boolean flag = this.pboc.updateAID(0x03, null);
            if (flag) {
                sendmessage(getString(R.string.del_aid_success));
            } else {
                sendmessage(getString(R.string.del_aid_failed));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setTlv() {
        try {
            this.pboc.setTlv("5F2A", HexUtil.hexStringToByte("0156"));
            sendmessage(getString(R.string.set_params_success));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setTlvWithEncoding(int encode) {
//        int encode = 0;
//        switch (v.getId()) {
//            case R.id.code_utf_8:
//                encode = 1;
//                break;
//            case R.id.code_utf_16:
//                encode = 2;
//                break;
//            case R.id.code_gbk:
//                encode = 3;
//                break;
//            default:
//                break;
//        }
        String str = "提示";
        try {
            this.pboc.setTlvWithEncoding("9f4e", str.getBytes(), encode);
            sendmessage(getString(R.string.set_params_success));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //

    /**
     * 导入联机应答结果
     */
    public void importOnlineResult() {
        try {
            this.pboc.importOnlineResp(true, "00", "72129F18041122334486098418000004AABBCCDD");
            sendmessage(getString(R.string.import_online_request_success));
        } catch (Exception e) {
            e.printStackTrace();
            sendmessage(getString(R.string.import_online_request_failed));
        }
    }

    public void isExistAidPublicKey() {
        try {
            int result = this.pboc.isExistAidPublicKey();
            sendmessage(getString(R.string.check_aid_ca_key_result) + result);
        } catch (Exception e) {
            e.printStackTrace();
            sendmessage(getString(R.string.check_aid_ca_key_result_failed));
        }
    }





    @Override
    public void onDeviceConnected(AidlDeviceService serviceManager) {
        deviceManager = serviceManager;
        try {
            magCardDev = AidlMagCard.Stub.asInterface(serviceManager
                    .getMagCardReader());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            iccard = AidlICCard.Stub.asInterface(serviceManager
                    .getInsertCardReader());
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

        try {
            psam = AidlPsam.Stub.asInterface(serviceManager
                    .getPSAMReader(PsamConstant.PSAM_DEV_ID_1));
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            iScanner = AidlCameraScanCode.Stub.asInterface(serviceManager.getCameraManager());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            sendmessage(e.getMessage().toString());
            e.printStackTrace();

        }
        try {
            iDecoder = AidlDecoderManager.Stub.asInterface(serviceManager.getDecoder());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            iBeeper = AidlBuzzer.Stub.asInterface(serviceManager.getBuzzer());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            Bundle bundle = new Bundle();
            bundle.putInt("LED_ID",1);
            iLed = AidlLed.Stub.asInterface(serviceManager.getLed());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            pboc = AidlPboc.Stub.asInterface(serviceManager.getEMVL2());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy(){
        if(activity_name.equals(BaseUtils.ACTIVITY_NAME_PSAM))
            psamclose();
        else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_DECODE))
            decode_exit();
        else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_BEEP))
            stopbeep();
        else if(activity_name.equals(BaseUtils.ACTIVITY_NAME_LED))
            stopled();
        super.onDestroy();
    }
}
