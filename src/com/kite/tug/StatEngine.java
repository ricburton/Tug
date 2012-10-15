package com.kite.tug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.GraphViewSeries;

import android.graphics.Color;
import android.util.Log;

public class StatEngine {

	// set the data to be collected
	public int total = 0;
	public Person cat = new Person();
	public Person rat = new Person();
	public int average_chars_sent = 0;
	public int average_chars_received = 0;

	public double averageSentSpeedRaw = 0;
	public double averageReceivedSpeedRaw = 0;
	public double medianSentSpeedRaw = 0;
	public double medianReceivedSpeedRaw = 0;

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

	// Double-texts engine stuff
	public double average_send_double_up_time = -1;
	public double average_receive_double_up_time = -1;

	//bunch engine stuff
	public int send_initate = 0;		//conversations the cat started
	public int receive_initiate = 0;	//conversations the rat started
	public int send_ender = 0;			//conversations the cat ended
	public int receive_ender = 0;		//conversations the rat ended
	private int total_send_to_receive_response_count = 0;
	private int total_receive_to_send_response_count = 0;
	private int total_send_to_receive_response_time = 0;
	private int total_receive_to_send_response_time = 0;
	public double average_send_to_receive_response_time = -1; //importantly, this is only calculated while in a conversation, so this removes outliers
	public double average_receive_to_send_response_time = -1;
	private int total_bunch_time = 0;		//for avg conversation duration/time
	private int total_bunch_gap_time = 0;	//for avg time between conversationss
	private int total_bunch_messages = 0;	//for avg conversation length
	public double average_bunch_time = -1;
	public int average_bunch_gap_time = -1;
	public int average_bunch_length = 0;
	private Message previous_message;
	private ArrayList<Bunch> bunches = new ArrayList<Bunch>();
	private Bunch potentialBunch = new Bunch(); //for adding, to allow the gap times to be calcd
	private Bunch previous_bunch;

	public StatEngine() { //constructor
	}

	// This method calculates the mean
	public static double findMean(ArrayList<Integer> anArray) {
		ArrayList<Integer> myArray = anArray;
		double arraySum = 0;
		double arrayAverage = 0;
		for (int x = 0; x < myArray.size() - 1; x++)
			arraySum += myArray.get(x);
		arrayAverage = arraySum / myArray.size();
		return arrayAverage;
	}

	// this method finds the median
	public static double findMedian(ArrayList<Integer> anArray) {
		ArrayList<Integer> myArray = anArray;
		Collections.sort(myArray);
		int arrayLength = 0;
		double arrayMedian = 0;
		int currentIndex = 0;
		arrayLength = myArray.size();
		if (arrayLength % 2 != 0) {
			currentIndex = ((arrayLength / 2) + 1);
			arrayMedian = myArray.get(currentIndex - 1);
		} else if (arrayLength == 0) {
			arrayMedian = 0; //temp fix
		} else {
			int indexOne = (arrayLength / 2);
			int indexTwo = arrayLength / 2 + 1;
			Log.d("MEDIAN", "indexone: " + indexOne + " indexTwo " + indexTwo);
			double arraysSum = myArray.get(indexOne - 1)
			+ myArray.get(indexTwo - 1);
			arrayMedian = arraysSum / 2;
		}
		return arrayMedian;
	}

	public int sendDayPlusCount() {
		return (sendWeekCount + sendWeekPlusCount);
	}
	public int receivedDayPlusCount() {
		return (receivedWeekCount + receivedWeekPlusCount);
	}

	private void addBunch(Bunch bnch) {
		bunches.add(bnch); //save this bunch
		//here do things like increment appropriate counts.
		total_bunch_time += bnch.duration();
		total_bunch_messages += bnch.length();
		total_send_to_receive_response_time  += bnch.total_send_to_receive_response_time;
		total_send_to_receive_response_count += bnch.send_to_receive_response_count;
		total_receive_to_send_response_time  += bnch.total_receive_to_send_response_time;
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
		//calc the reply speed breakdowns TODO: remove this repetition
		for (int i = 0; i < rat.replyTimes.size(); i++) {
			// count delay categories
			Integer diff = (int) Math.round(rat.replyTimes.get(i));
			Integer diffMin = diff / (60); // minutes
			Integer diffHours = diff / (60 * 60); // hours
			Integer diffDays = diff / (24 * 60 * 60);
			if      (diffMin < 15)                    { receivedQuarterCount++; }
			else if (diffMin > 15 && diffMin < 60)    { receivedHourCount++; }
			else if (diffHours > 1 && diffHours < 24) { receivedDayCount++; }
			else if (diffDays > 1 && diffDays < 7)    { receivedWeekCount++; }
			else if (diffDays > 7)                    { receivedWeekPlusCount++; }
		}

		// calc the send speed breakdowns TODO: remove this repetition
		for (int i = 0; i < cat.replyTimes.size(); i++) {
			// count delay categories
			Integer diff = (int) Math.round(cat.replyTimes.get(i));
			Integer diffMin = diff / (60); // minutes
			Integer diffHours = diff / (60 * 60); // hours
			Integer diffDays = diff / (24 * 60 * 60);
			if      (diffMin < 15)                    { sendQuarterCount++; }
			else if (diffMin > 15 && diffMin < 60)    { sendHourCount++; }
			else if (diffHours > 1 && diffHours < 24) { sendDayCount++; }
			else if (diffDays > 1 && diffDays < 7)    { sendWeekCount++; }
			else if (diffDays > 7)                    { sendWeekPlusCount++; }
		}
		// Average Calculating
		averageSentSpeedRaw = findMean(cat.replyTimes);
		averageReceivedSpeedRaw = findMean(rat.replyTimes);

		// Median Calculating
		// TODO fix calculations on small number of results
		medianSentSpeedRaw = findMedian(cat.replyTimes);
		medianReceivedSpeedRaw = findMedian(rat.replyTimes);

		//calc average bunch time and gap and avg bunch length
		if (bunches.size() > 0) {
			average_bunch_time = total_bunch_time / bunches.size();
			average_bunch_gap_time = total_bunch_gap_time / bunches.size();
			average_bunch_length = total_bunch_messages / bunches.size();
		}
		//calc average send and receive times within bunches
		Log.d("Engine", "total receive to send response count: " + total_receive_to_send_response_count);
		if (total_receive_to_send_response_count > 0) {
			average_receive_to_send_response_time = total_receive_to_send_response_time / total_receive_to_send_response_count;
		}
		if (total_receive_to_send_response_count > 0) {
			average_send_to_receive_response_time = total_send_to_receive_response_time / total_send_to_receive_response_count;
		}
		//calc average double up times
		if (rat.doubles > 0) {
			average_receive_double_up_time = rat.total_double_up_times / rat.doubles;
		}
		if (cat.doubles > 0) {
			average_send_double_up_time = cat.total_double_up_times  / cat.doubles;
		}
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

	public static int countSmileys(String searchText) {
		String[] smileys = { ":)", ";)", ":P", ":p",
				":D", ";D", ":-)", ";-)", ":-P", ":-p", ":-D", ";-D" };
		int smileyCount = 0;
		for (int i = 0; i < smileys.length; i++) {
			smileyCount += countOccurrences(searchText, smileys[i]);
		}
		return smileyCount;
	}

	public void processMessage(Message message) {
		total++;
		Person person;
		if (message.isSent()) {
			person = cat;
		} else { // message received
			person = rat;	
		}
		person.count++;
		Log.d("StatEngine", "The person chars is: " + person.chars);
		Log.d("StatEngine", "The msg is: " + message);
		Log.d("StatEngine", "The msg txt is: " + message.text);

		person.chars += message.text.length();						//length of message

		person.kisses += countKisses(message.text);				    //kisses sent
		person.questions += countOccurrences(message.text, "?");	//questons sent
		person.smileys += countSmileys(message.text);				//smiley's sent

		if (previous_message != null) {
			int time_gap = (int) (message.time - previous_message.time);
			if (time_gap < Bunch.time_gap) { //if the messages are close enough together (need a variable in Bunch class)
				potentialBunch.addMessage(message); //then start collecting into a potential bunch
			} else { //if the messages are not close enough together then end the bunch 
				Log.d("Engine", "about to check if potential is valid");
				if (potentialBunch.isValid()) { //if the bunch contains both sent and received messages then it is saved and added
					addBunch(potentialBunch);
				}
				potentialBunch = new Bunch(); //make a new one
			}
			// see if this is a reply or a double
			if (previous_message.isSent() == message.isSent()) { //double up
				person.doubles++;
				person.total_double_up_times += time_gap;
			} else {
				person.replyTimes.add(time_gap);
			}
		}
		previous_message = message;
	}
}
