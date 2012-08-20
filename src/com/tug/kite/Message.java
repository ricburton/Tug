package com.tug.kite;

public class Message {
	public String text;
	public int replyTime;
	public int messageStatus;
	
	public boolean isSent() {
		if (messageStatus == 2) {
			return true;
		} else {
			return false;
		}
	}
}
