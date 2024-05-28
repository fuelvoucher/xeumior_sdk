package com.smart.st8050b_demo.activity;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smart.st8050b_demo.R;
import com.smart.st8050b_demo.util.ByteUtils;
import com.smart.st8050b_demo.util.HexUtil;
import com.topwise.cloudpos.aidl.AidlDeviceService;
import com.topwise.cloudpos.aidl.emv.AidlCheckCardListener;
import com.topwise.cloudpos.aidl.emv.AidlPboc;
import com.topwise.cloudpos.aidl.emv.AidlPbocStartListener;
import com.topwise.cloudpos.aidl.emv.CardInfo;
import com.topwise.cloudpos.aidl.emv.EmvTransData;
import com.topwise.cloudpos.aidl.emv.PCardLoadLog;
import com.topwise.cloudpos.aidl.emv.PCardTransLog;
import com.topwise.cloudpos.aidl.magcard.TrackData;
import com.topwise.cloudpos.data.EmvConstant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadCardId_EMV extends BaseActivity implements View.OnClickListener {
    private final int SHOW_MESSAGE = 0;
    private Button btn_readid;
    private TextView txt_log;
    private AidlPboc pboc = null;
    private PbocStartListener pboclistener = null;
    private EmvTransData transData = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_card_id__emv);
        btn_readid = (Button)findViewById(R.id.btn_readid);
        btn_readid.setOnClickListener(this);
        txt_log = (TextView)findViewById(R.id.txt_log);
        transData = new EmvTransData((byte) 0x31,
                (byte) 0x02, true, false, false,
                (byte) 0x01, (byte) 0x00, new byte[]{0x00, 0x00, 0x00});
        pboclistener = new PbocStartListener();
    }

    @Override
    public void onDeviceConnected(AidlDeviceService serviceManager) {
        try {
            pboc = AidlPboc.Stub.asInterface(serviceManager.getEMVL2());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(btn_readid==v){
            if(isExistAidPublicKey()!=0){
                addCaParam();
                addAid();
            }
            checkCard();

        }
    }


    /*
   PBOC
    */
    private class PbocStartListener extends AidlPbocStartListener.Stub {
        @Override
        public void requestUserAuth(int certType, String certno) throws RemoteException {
//            sendmessage(getString(R.string.confirm_account_type));
//            sendmessage(getString(R.string.account_type) + certType);
//            sendmessage(getString(R.string.account_number) + certno);
            pboc.importUserAuthRes(true);
        }

        @Override
        public void requestTipsConfirm(String arg0) throws RemoteException {
//            sendmessage(getString(R.string.conform_information));
//            sendmessage(arg0);
//            pboc.importMsgConfirmRes(true);
        }

        @Override
        public void requestImportPin(int times, boolean lastFlag, String amount)
                throws RemoteException {
//            sendmessage(getString(R.string.input_offline_pin));
//            sendmessage(getString(R.string.input_frequency) + times);
//            sendmessage(getString(R.string.is_final_input_frequency) + lastFlag);
//            sendmessage(getString(R.string.amount_msg) + amount);
            pboc.importPin("26888888FFFFFFFF");
        }

        @Override
        public void finalAidSelect() throws RemoteException {
            pboc.importFinalAidSelectRes(true);
        }

        @Override
        public void requestImportAmount(int arg0) throws RemoteException {
//            sendmessage(getString(R.string.import_amount_type) + arg0);
//            sendmessage(getString(R.string.import_amount));
            pboc.importAmount("00.00");
        }

        @Override
        public void requestEcashTipsConfirm() throws RemoteException {
//            sendmessage(getString(R.string.confirm_electronic_cash));
            pboc.importECashTipConfirmRes(false);
        }

        @Override
        public void requestAidSelect(int times, String[] arg1)
                throws RemoteException {
//            sendmessage(getString(R.string.choice_app_list));
            String str = "";
            for (int i = 0; i < arg1.length; i++) {
                str += arg1[i];
            }
//            sendmessage(getString(R.string.application_list_content) + str);
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
//            sendmessage(getString(R.string.request_online));
            String Cardid = getCardNO();
            if(Cardid!=null&&Cardid.length()>0)
                sendmessage("RF CARD ID:" + Cardid + " expirydata = " + expirydata);
            pboc.endPBOC();
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
//            sendmessage(getString(R.string.first_e_cash_money_code) + arg0);
//            sendmessage(getString(R.string.first_e_cash_balance) + arg1);
//            sendmessage(getString(R.string.second_e_cash_money_code) + arg2);
//            sendmessage(getString(R.string.second_e_cash_balance) + arg3);
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
            //sendmessage(getString(R.string.card_info) + ":"+ arg0.getCardno());
            String Cardid = getCardNO();
            if(Cardid!=null&&Cardid.length()>0)
                sendmessage("RF CARD ID:" + Cardid + " expirydata = " + expirydata);
            pboc.endPBOC();
//            sendmessage(getString(R.string.please_confirm));
            //pboc.importConfirmCardInfoRes(true);
            //pboc.endPBOC();
        }
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


    public int isExistAidPublicKey() {
        int result = -1;
        try {
            result = this.pboc.isExistAidPublicKey();
            sendmessage(getString(R.string.check_aid_ca_key_result) + result);
        } catch (Exception e) {
            e.printStackTrace();
            sendmessage(getString(R.string.check_aid_ca_key_result_failed));
        }
        return result;
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

    public void consume() {
        try {
            //transData.setTranstype((byte) 0x31);
            pboc.processPBOC(transData, pboclistener);

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    String expirydata = "";
    private String  getCardNO() {
        expirydata = "";
        byte[] track2TlvList = getTlv(new String[]{"57"});

        if (track2TlvList == null) {
            return "";
        }
        byte[] temp = new byte[track2TlvList.length - 2];
        System.arraycopy(track2TlvList, 2, temp, 0, temp.length);
        String track2 = processTrack2(ByteUtils.byteArray2HexString(temp).toUpperCase());

        if (track2TlvList != null) {
            String[] datas = track2.split("D");
            String cardNo = datas[0];
            if(datas.length>0&&datas[1]!=null&&datas[1].length()>0){
                expirydata = datas[1].substring(0,4);
            }
            return cardNo;
        }
        return "";

    }

    private byte[] getTlv(String[] tags) {
        byte[] tempList = new byte[500];
        byte[] tlvList = null;
        try {
            int result = pboc.readKernelData(tags, tempList);

            if (result <= 0) {
                return null;
            } else {
                tlvList = new byte[result];
                System.arraycopy(tempList, 0, tlvList, 0, result);

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return tlvList;
    }

    private static String processTrack2(String track) {
        StringBuilder builder = new StringBuilder();
        String subStr = null;
        String resultStr = null;
        for (int i = 0; i < track.length(); i++) {
            subStr = track.substring(i, i + 1);
            if (!subStr.endsWith("F")) {
                builder.append(subStr);
            }
        }
        resultStr = builder.toString();
        return resultStr;
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
                        transData = new EmvTransData((byte) 0x00,
                                (byte) 0x01, true, false, false,
                                (byte) 0x02, (byte) 0x01, new byte[]{0x00, 0x00, 0x00});
                        consume();



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
                        consume();

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


}
