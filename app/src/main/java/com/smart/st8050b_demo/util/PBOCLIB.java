package com.smart.st8050b_demo.util;

import java.util.Arrays;



/*
 * PB0C解析
 */
public class PBOCLIB {
	public final int STEP1 = 1;
	public final int STEP2 = 2;
	public final int STEP3 = 3;
	public final int STEP4 = 4;
	public final int STEP5 = 5;
	public final int STEP6 = 6;
	
	
	public int curent_step = STEP1;
	public String AID = "";
	public String CARDID = "";
	public void init()
	{
		curent_step = STEP1;
		AID = "";
		CARDID = "";
	}
	
	public String parsedata(byte[] data)
	{
		String str = "";
		String retstr = "";
		int sw1 = data[0]&0xff;
		str =ByteUtils.byteArray2HexString(data).toUpperCase();
		
		if(sw1==0x6c)
		{
			//GET RESPONSE
			retstr = readrecord(data[1]);
			return retstr;
		}
		
		switch(curent_step)
		{
			case STEP1:				
				if(sw1>0x60&&sw1<0x63)
				{
					//GET RESPONSE
					retstr = getResponse(data[1]);
				}else
				{
					curent_step=STEP2;
					//READ RECORD
					//APDU:00B2010C00
					retstr = readrecord();
				}
			break;
			case STEP2:
				if(sw1>0x60&&sw1<0x63)
				{
					//GET RESPONSE
					retstr = readrecord(data[1]);
				}else
				{
					
					curent_step=STEP3;
					//解析AID
					byte[] baid = Arrays.copyOfRange(data, 6, 14);
					AID = ByteUtils.byteArray2HexString(baid);
					retstr = getGPO();
					
				}
				break;
			case STEP3:
				if(sw1>0x60&&sw1<0x63)
				{
					//GET RESPONSE
					retstr = getResponse(data[1]);
				}else
				{
					retstr = setAID();
					curent_step=STEP4;
				}
				break;
			case STEP4:
				if(sw1>0x60&&sw1<0x63)
				{
					//GET RESPONSE
					retstr = getResponse(data[1]);
				}else
				{
					retstr = getGPO();
					curent_step=STEP5;
				}
				break;
			case STEP5:
				if(sw1>0x60&&sw1<0x63)
				{
					//GET RESPONSE
					retstr = getResponse(data[1]);
				}else
				{
					retstr = readrecord();
					curent_step=STEP6;
				}
				break;
			case STEP6:
				if(sw1>0x60&&sw1<0x63)
				{
					//GET RESPONSE
					retstr = readrecord(data[1]);
				}else
				{
					//取取卡号
					int pos = str.indexOf("57")+4;
					int pos1 = str.indexOf("D");
//					if(pos>0)
//					{
//						CARDID = str.substring(pos,pos+19);
//					}
					if(pos>0&&pos1>pos)
					{
						CARDID = str.substring(pos,pos1);
					}
				}
				break;
		}
		
		return retstr;
	}
	public String getResponse(byte d)
	{
		String str = "00C00000"+oneBytes2HexString(d);		
		return str;
	}
	
	public String readrecord()
	{
		String str = "00B2010C00";		
		return str;
	}
	public String readrecord(byte d)
	{
		String str = "00B2010C"+oneBytes2HexString(d);		
		return str;
	}
	
	public String setAID()
	{
		String str = "00A4040008"+AID;		
		return str;
	}
	
	public String getGPO()
	{
		String str = "80A800000B83099F7A019F02065F2A02";
		return str;
	}
	
	public String getsenddata()
	{
		String str = "";
		switch(curent_step)
		{
			case STEP1:
				str = "00a404000E315041592E5359532E4444463031";
				break;
		}
		
		return str;
	}
	
	private String oneBytes2HexString(byte b)
	{
		String hex = Integer.toHexString(b & 0xFF);   
		if (hex.length() == 1) {    
		       hex = '0' + hex;    
		     }  
		return hex;
	}

}
