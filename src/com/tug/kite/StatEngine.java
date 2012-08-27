package com.tug.kite;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class StatEngine {

	// set the data to be collected

	public int total = 0;
	public int sent = 0;
	public int received = 0;

	public int kissesSent = 0;
	public int kissesReceived = 0;
	public int questionsSent = 0;
	public int questionsReceived = 0;
	public int smileysSent = 0;
	public int smileyReceived = 0;
	private int charsSent = 0;		//for average text length
	private int charsReceived = 0;
	public int average_chars_sent = 0;
	public int average_chars_received = 0;

	public int sendQuarterCount = 0;
	public int sendHourCount = 0;
	public int sendDayCount = 0;
	public int sendWeekCount = 0;
	public int sendWeekPlusCount = 0;

	public int receivedQuarterCount = 0;
	public int receivedHourCount = 0;
	public int receivedDayCount = 0;
	public int receivedWeekCount = 0;
	public int receivedWeekPlusCount = 0;

	// declare the ArrayList of reply-time integers
	private ArrayList<Integer> replySpeeds = new ArrayList<Integer>(); // Noone needs these arrays, we shall calculate means here
	private ArrayList<Integer> sendSpeeds = new ArrayList<Integer>();

	// Double-texts engine stuff
	public int sentDoubles = 0; //send_double_up_count
	public int receivedDoubles = 0; //receive_double_up_count
	private int total_send_double_up_times = 0;
    private int total_receive_double_up_times = 0;
    public int average
    
                               
	//bunch engine stuff
	public int send_initate = 0;		//conversations the cat started
	public int receive_initiate = 0;	//conversations the rat started
	public int send_ender = 0;			//conversations the cat ended
	public int receive_ender = 0;		//conversations the rat ended
	private int total_send_to_receive_response_count = 0;
	private int total_receive_to_send_response_count = 0;
	private int total_send_to_receive_response_time = 0;
	private int total_receive_to_send_response_time = 0;
	public int average_send_to_receive_response_time = 0; //importantly, this is only calculated while in a conversation, so this removes outliers
	public int average_receive_to_send_response_time = 0;
	private int total_bunch_time = 0;		//for avg conversation duration/time
	private int total_bunch_gap_time = 0;	//for avg time between conversationss
	private int total_bunch_messages = 0;	//for avg conversation length
	public int average_bunch_time = 0;
	public int average_bunch_gap_time = 0;
	public int average_bunch_length = 0;
	private Message previous_message;
	private ArrayList<Bunch> bunches;
	private Bunch potentialBunch; //for adding, to allow the gap times to be calcd
	private Bunch previous_bunch;
	
	public StatEngine() { //constructor
	}
	
	private void addBunch(Bunch bnch) {
        //here do things like increment appropriate counts.
        total_bunch_time += bnch.duration();
        total_bunch_messages += bnch.length();
        total_send_to_receive_response_time += bnch.total_send_to_receive_response_time;
        total_send_to_receive_response_count += bnch.send_to_receive_response_count;
        total_receive_to_send_response_time += bnch.total_receive_to_send_response_time;
        total_receive_to_send_response_count += bnch.receive_to_send_response_count;
		if (previous_bunch != null) {
			total_bunch_gap_time += Bunch.getTimeGap(bnch, previous_bunch);
		}
		if (bnch.send_initiate()) { send_initate++; } else { receive_initiate++; }
		if (bnch.send_ender()) { send_ender++; } else { receive_ender++; }
		
		previous_bunch = bnch;
	}
	
	public void finish() {
		//check the potential bunch and add it if necessary
		if (potentialBunch.isValid()) {
			addBunch(potentialBunch);
		}
		//calc average bunch time and gap and avg bunch length
		//calc average double up times
	}
	
	// The method below is for counting things like kisses and question-marks
	public static int countOccurrences(String base, String searchFor) {

		int len = searchFor.length();
		int result = 0;
		if (len > 0) {
			int start = base.indexOf(searchFor);
			while (start != -1) {
				result++;
				start = base.indexOf(searchFor, start + len);
			}
		}
		
		return result;

	}

	public static int countKisses(String searchText) {
		int kisses = 0;
		Pattern patternOneKiss = Pattern.compile("(\\p{Punct}|\\s)(?i)x(\\s|$)"); // (punctuation or whitespace) followed by x followed by (whitespace or end of line)
		Pattern patternManyKiss = Pattern.compile("(?i)x{2,}"); // 2 or more 'x's after each other
		Matcher matcherOneKiss = patternOneKiss.matcher(searchText);
		Matcher matcherManyKiss = patternManyKiss.matcher(searchText);
		
		while (matcherOneKiss.find()) {
		  kisses++;
		}
		while (matcherManyKiss.find()) {
		  kisses = kisses + matcherManyKiss.end() - matcherManyKiss.start();
		}
		return kisses;
	}
	
	public void processMessage(String message, int replyTime, int messageStatus) {
		// messages sent
		if (messageStatus == 2) {
			total++;
			sent++;
			// length of message
			sentLengths.add(message.length());

			Log.d("ENGINE", "Message Sent: " + message);

			// see if this is a reply or a double

			if (lastMessageStatus == 2) {
				sentDoubles++;
			} else if (lastMessageStatus == 1) {
				Integer sendDiff = lastMessageTime
						- replyTime;
				sendSpeeds.add(sendDiff);
				Log.d("Difference in time",
						sendDiff.toString());
			}

			// kisses sent
			kissesSent = kissesSent + countKisses(message);

			questionsSent = questionsSent + countOccurrences(
					message, "?");
			// smiley's sent
			// TODO: Regex this!
			String[] smileys = { ":)", ";)", ":P",
					":D", ";D" };
			for (int i = 0; i < smileys.length; i++) {

				if (message.indexOf(smileys[i]) > 0) {
					smileysSent++;

				}
			}
			// set the LastMessageStatus for next loop
			lastMessageStatus = messageStatus;
			lastMessageTime = replyTime;
		} else if (messageStatus == 1) { // messages
											// received
			total++;
			received++;
			receivedLengths.add(message.length());
			// see if this a reply or a follow-up
			// text
			if (lastMessageStatus == 1) {
				receivedDoubles++;
			} else if (lastMessageStatus == 2) {
				Integer replyDiff = lastMessageTime
						- replyTime;
				replySpeeds.add(replyDiff);
				Log.d("Difference in time",
						replyDiff.toString());
			}

			Log.d("ENGINE", "Message Received: "
					+ message);
			// kisses received
			kissesReceived = kissesReceived + countKisses(message);
			
			
			questionsReceived = questionsReceived + countOccurrences(
					message, "?");
			// smiley's received
			//TODO: Regex this shit!
			String[] smileys = { ":)", ";)", ":P",
					":D", ";D" };
			for (int i = 0; i < smileys.length; i++) {

				if (message.indexOf(smileys[i]) > 0) {
					smileyReceived++;

				}
			}
			// set the LastMessageStatus for next loop
			lastMessageStatus = messageStatus;
			lastMessageTime = replyTime;
		} else {

			Log.d("ENGINE",	"Not sent or received. Odd");
		}

	}
	
}
