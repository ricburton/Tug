package com.tug.kite;

import java.util.ArrayList;

public class Bunch {
	private ArrayList<Message> pMessages;
	public void addMessage(Message msg) { pMessages.add(msg); }

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
}
