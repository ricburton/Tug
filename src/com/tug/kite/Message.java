package com.tug.kite;

public class Message {
	public String text;
	public int time;
	public int messageStatus;
	
	public boolean isSent() {
		if (messageStatus == 2) {
			return true; //2 means sent from local point of view
		} else {
			return false; //1 mean received from local point of view
		}
	}
}
