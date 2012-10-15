package com.kite.tug;

import java.util.ArrayList;

import android.util.Log;

public class Bunch {
	private ArrayList<Message> pMessages = new ArrayList<Message>(); //could probably get away without storing these...
	public int send_to_receive_response_count = 0;
	public int receive_to_send_response_count = 0;
	public int total_send_to_receive_response_time = 0;
	public int total_receive_to_send_response_time = 0;
	
	public static final int time_gap = 3600;

	public void addMessage(Message msg) {
		pMessages.add(msg);
		if (pMessages.size() > 1) {
			//potentially move this as one big loop when the bunch is finished off
			Message prev_msg = pMessages.get(pMessages.size() - 2);
			if (msg.isSent() != prev_msg.isSent()) { //detects response
				if (msg.isSent()) { //you sent the message, so its a receive-to-send response
					total_receive_to_send_response_time += msg.time - prev_msg.time;
					receive_to_send_response_count += 1;
				} else {
					total_send_to_receive_response_time += msg.time - prev_msg.time;
					send_to_receive_response_count += 1;
				}
			}
		}
	}

	public boolean isValid() {
		boolean hasReceived = false;
		boolean hasSent = false;
		for (Message msg : pMessages) {
			if (msg.isSent()) {
				hasSent = true;
			} else {
				hasReceived = true;
			}
			if (hasReceived && hasSent) {
				return true;
			}
		}
		return false;
	}
	
	public int duration() {
		return (int) (pMessages.get(pMessages.size()-1).time - pMessages.get(0).time);
	}
	
	public int length() {
		return pMessages.size();
	}

	public boolean send_initiate() {
		return pMessages.get(0).isSent();
	}
	
	public boolean send_ender() {
		return pMessages.get(pMessages.size() - 1).isSent();
	}
	
	public static int getTimeGap(Bunch newerBunch, Bunch olderBunch) {
		//get the time difference between the first message of the newer bunch
		// and the last message of the older bunch...
		return (int) (newerBunch.pMessages.get(0).time - olderBunch.pMessages.get(olderBunch.pMessages.size()-1).time);
	}
}
