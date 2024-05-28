package com.smart.st8050b_demo.util;

import android.os.RemoteException;
import android.util.Log;

import com.topwise.cloudpos.aidl.rfcard.AidlRFCard;

/**
 * Created by Administrator on 2018/5/25.
 */

public class NfcAutoCheck {
    public AidlRFCard rfcard = null;
    private Thread checkThread = null;
    private boolean checkflag = false;
    private String TAG = "NfcAutoCheck";

    public boolean getCheckFlag(){
        return this.checkflag;
    }
    public NfcAutoCheck(AidlRFCard card)
    {
        rfcard = card;
    }

    /**
     *
     * @param timeout second
     */
    public void startAutoCheck(int timeout)
    {
        if(rfcard==null)
            return;
        try {
            boolean flag = rfcard.open();
            if(!flag)
                return;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //check card
        startCheckCardThread(timeout);
    }

    public void stopAutoCheck()
    {
        setCheckflag(false);
    }

    private void startCheckCardThread(int timesout)
    {
        if(checkThread==null)
            checkThread = new CheckCardThread("check card thread",timesout);
        else
        {
            if(isCheckflag()) {
                setCheckflag(false);
                checkThread.interrupt();
            }
        }
        setCheckflag(true);
        checkThread.start();

    }

    public boolean isCheckflag() {
        return checkflag;
    }

    public void setCheckflag(boolean checkflag) {
        this.checkflag = checkflag;
    }

    private class CheckCardThread extends Thread {
        private long beginTime = 0;
        private int timeOut = 0;
        private boolean isExist = false;
        CheckCardThread(String name,int _timeOut) {
            super(name);
            beginTime = 0;
            timeOut = _timeOut;
            isExist = false;
        }

        @Override
        public void run() {
            //执行耗时操作
            beginTime=System.currentTimeMillis();
            while(isCheckflag())            {
                long currentTimeMillis=System.currentTimeMillis();
                if((currentTimeMillis-beginTime)>timeOut*1000){
                    //TIME OUT
                    setCheckflag(false);
                    if(onReceiveListener!=null)
                    {
                        onReceiveListener.onCheckCardReceive(false,0);
                    }
                    break;
                }
                try {
                    isExist = rfcard.isExist();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if(isExist)
                {
                    int type = 0;
                    String uid = "";
                    try {
                        type = rfcard.getCardType();
                        uid = getCardId();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    setCheckflag(false);
                    if(onReceiveListener!=null)
                    {
                        onReceiveListener.onCheckCardReceive(true,type);
                        onReceiveListener.onCheckCardReceive(true,type,uid);
                    }
                    break;
                }else
                {
                    if(onReceiveListener!=null)
                    {
                        onReceiveListener.onCheckCardReceive(false,0,"");
                    }

                }
                try{
                    sleep(50);
                }catch (Exception e)
                {
                    Log.d(TAG,e.getMessage());
                }
            }
        }
    }

    public String getCardId()
    {
        String uid = "";
        try
        {
            byte[] id = rfcard.getCardCode();
            if(id!=null){
                if(id.length>0)
                {
                    byte[] newid = new byte[id.length-1];
                    System.arraycopy(id,0,newid,0,id.length-1);
                    uid = HexUtil.bcd2str(newid);
                }

            }else
            {
                uid = "";
            }

        }catch(RemoteException e){

        }
        return uid;
    }

    public OnCheckCardListener onReceiveListener = null;
    public static interface OnCheckCardListener {
        public void onCheckCardReceive(boolean isexist,int cardtype);
        public void onCheckCardReceive(boolean isexist,int cardtype,String uid);
    }
    public void setOnDataReceiveListener(OnCheckCardListener ReceiveListener) {
        onReceiveListener = ReceiveListener;
    }








}
