package com.kite.tug;

public class Message {
	public String text;
	public long time;
	public int messageStatus;
	
	Message(String txt, long t, int stat) {
		text = txt;
		time = t;
		messageStatus = stat;
	}
	
	public boolean isSent() {
		if (messageStatus == 2) {
			return true; //2 means sent from local point of view
		} else {
			return false; //1 mean received from local point of view
		}
	}
}
