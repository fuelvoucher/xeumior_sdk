package com.smart.st8050b_demo.bean;

public class IconInfo {
	private String name;
	private int ico;
	private int cmd;
	
	public IconInfo(String name,int ico,int cmd){
		this.name = name;
		this.ico  = ico;
		this.cmd  = cmd;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getIco() {
		return ico;
	}
	public void setIco(int ico) {
		this.ico = ico;
	}

	public int getCmd() {
		return cmd;
	}

	public void setCmd(int cmd) {
		this.cmd = cmd;
	}
	
	
}
