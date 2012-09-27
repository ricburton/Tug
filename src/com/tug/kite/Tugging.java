package com.tug.kite;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.*;
import java.util.Random;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.GraphViewSeries;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.LineGraphView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Tugging extends Activity {
//a other comment
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	private static final int CONTACT_PICKER_RESULT = 1001;
	private static final String DEBUG_TAG = null;
	private static final String[] ladQuirk = {
		"Who's winning the game?",
		//TODO: Insert laddy things here rich!
		};

	public void doLaunchContactPicker(View view) {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
				Contacts.CONTENT_URI);
		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
	}

	// The method below is used for matching the last characters of the string
	public String getLastnCharacters(String inputString, int subStringLength) {
		int length = inputString.length();
		if (length <= subStringLength) {
			return inputString;
		}
		int startIndex = length - subStringLength;
		return inputString.substring(startIndex);
	}

	// The method below creates the counter and pushes it to the view as a
	// separate, runnable thread
	public void countUp(final TextView flipScore, final int topNum, final int speedNum) {
		new Thread(new Runnable() {
			int counter = 0;
			public void run() {
				flipScore.post(new Runnable() {
					public void run() {
						flipScore.setText("0");
						flipScore.setTextSize(55);
					    flipScore.setTextColor(Color.GRAY);
					} 
				});
				while (counter < topNum) {
					try {
						Thread.sleep(speedNum);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					flipScore.post(new Runnable() {

						public void run() {
							// change font-size for ridic numbers
							// TODO fix distortion
							if (counter > 99) { flipScore.setTextSize(45); }
							if (counter > 999) { flipScore.setTextSize(35);	}
							flipScore.setText("" + counter);
						}
					});
					counter++;
				}
				flipScore.post(new Runnable() { 
					public void run() {	flipScore.setTextColor(Color.BLACK); }
				});
			}
		}).start();
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

	// this method converts seconds into a nice string that can be printed
	public static String returnTime(Double seconds) {

		if (seconds < 0 ) { return "N/A"; }
		Double pure = seconds;
		Integer diff = (int) Math.round(pure);

		Integer diffSecs = diff;
		Integer diffMin = diff / (60); // minutes
		Integer diffHours = diff / (60 * 60); // hours
		Integer diffDays = diff / (24 * 60 * 60);

		String naturalTime = "";

		if (diffSecs < 60) {
			naturalTime = diffSecs.toString() + " secs";
		} else if (diffMin == 1 && diffHours < 1) {
			naturalTime = diffMin.toString() + " min";
		} else if (diffMin > 1 && diffHours < 1) {
			naturalTime = diffMin.toString() + " mins";
		} else if (diffHours == 1 && diffDays < 1) {
			naturalTime = diffHours.toString() + " hour";
		} else if (diffHours > 1 && diffDays < 1) {
			naturalTime = diffHours.toString() + " hrs";
		} else if (diffDays == 1) {
			naturalTime = diffDays.toString() + " day";
		} else if (diffDays > 1) {
			naturalTime = diffDays.toString() + " days";
		}
		return naturalTime;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				Cursor cursor = null;
				String phone = "";
				String name = "";
				try {
					Uri result = data.getData();
					Log.v(DEBUG_TAG,
							"Got a contact result: " + result.toString());

					// get the contact id from the Uri
					String id = result.getLastPathSegment();

					// query for everything phone
					cursor = getContentResolver().query(Phone.CONTENT_URI,
							null, Phone.CONTACT_ID + "=?", new String[] { id },
							null);

					int phoneIdx = cursor.getColumnIndex(Phone.DATA);
					int nameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME);

					// let's just get the first phone
					if (cursor.moveToFirst()) {
						
						name = cursor.getString(nameIdx);
						
					//	Toast.makeText(this, "Name: " + name,
								//Toast.LENGTH_LONG).show();
						
						phone = cursor.getString(phoneIdx);
						
						//Toast.makeText(this, "Phone: " + phone,
							//	Toast.LENGTH_LONG).show();
						
						
						Log.v(DEBUG_TAG, "Got phone: " + phone);
						Log.v(DEBUG_TAG, "Got name:" + name);
						//strip string down to first name
						int d = name.indexOf(" ");
						if (d > 0) {
						name = name.substring(0, d);
						} else if (name.length() == 0) {
							name = "NA";
						}
						// TODO change rat search into an array to allow for
						// multiple numbers

					} else {
						Log.w(DEBUG_TAG, "No results");

					}
				} catch (Exception e) {
					Log.e(DEBUG_TAG, "Failed to get phone data", e);
				} finally {
					if (cursor != null) {
						cursor.close();
					}

					// if the number cannot be found let the user know and stop
					// the stat engine running
					if (phone.length() == 0) {
						Toast.makeText(this, "No phone number found.",
								Toast.LENGTH_LONG).show();
					} else {

						// START THE STAT ENGINE!
						StatEngine engine = new StatEngine();
						Uri uriSMSURI = Uri.parse("content://sms"); // access
																	// the
																	// sms db
						Cursor cur = getContentResolver().query(uriSMSURI,
								null, null, null, "date ASC");
						cur.moveToFirst();
						// set the data to be collected
						Integer timesRun = 0;
						Integer draftCount = 0;
						String cleanPhone = getLastnCharacters(phone, 7);
						while (cur.moveToNext()) {
							// Number-matching is done from end to beginning
							// with 7 figures
							// Log.i("Number of message/draft", num.toString());
							if (cur.getString(cur.getColumnIndexOrThrow("address")) != null) { // catch drafts this way?
								String num = cur.getString(cur.getColumnIndexOrThrow("address"));
								// TODO fix draft-handling
								String cleanNum = getLastnCharacters(num, 7);

								Log.d(phone, "Debug: " + cleanPhone.toString()
										+ " == " + cleanNum.toString());

								if (cleanNum.equals(cleanPhone)) {
									//make a message object here
									Integer messageStatus = cur.getInt(cur.getColumnIndexOrThrow("type"));
									long replyTime = cur.getLong(cur.getColumnIndexOrThrow("date"))/1000;
									String message = cur.getString(cur.getColumnIndexOrThrow("body")); //cur.getString(11);
									Message msg = new Message(cur.getString(cur.getColumnIndexOrThrow("body")), replyTime, messageStatus);
									/*Log.d("Tugger",
											  " 0: " + cur.getColumnName(0) + " > " + cur.getString(0)
											+ " 1: " + cur.getColumnName(1) + " > " + cur.getString(1)
											+ " 2: " + cur.getColumnName(2) + " > " + cur.getString(2)
											+ " 3: " + cur.getColumnName(3) + " > " + cur.getString(3)
											+ " 4: " + cur.getColumnName(4) + " > " + cur.getString(4)
											+ " 5: " + cur.getColumnName(5) + " > " + cur.getString(5)
											+ " 6: " + cur.getColumnName(6) + " > " + cur.getString(6)
											+ " 7: " + cur.getColumnName(7) + " > " + cur.getString(7)
											+ " 8: " + cur.getColumnName(8) + " > " + cur.getString(8)
											+ " 9: " + cur.getColumnName(9) + " > " + cur.getString(9)
											+ " 10: " + cur.getColumnName(10) + " > " + cur.getString(10)
											+ " 11: " + cur.getColumnName(11) + " > " + cur.getString(11)
											+ " 12: " + cur.getColumnName(12) + " > " + cur.getString(12)
											+ " 13: " + cur.getColumnName(13) + " > " + cur.getString(13)
											+ " 14: " + cur.getColumnName(14) + " > " + cur.getString(14)
											+ " 15: " + cur.getColumnName(15) + " > " + cur.getString(15)
											+ " 16: " + cur.getColumnName(16) + " > " + cur.getString(16)
											+ " try: " + replyTime
									        + " try: " + cur.getString(cur.getColumnIndexOrThrow("date"))
											+ " try: " + cur.getString(cur.getColumnIndexOrThrow("body"))
											);*/
									//feed the engine with the message object
									engine.processMessage(msg);

								} else {
									// log no texts match to person here
									Log.d(phone, "Not a match to rat.");
								}

							} else {
								Log.i("DRAFT", "This is a draft: " + cur.getString(cur.getColumnIndexOrThrow("body")));

								draftCount++;
							}

						}
						//finish the engine here
						engine.finish();
						Log.i("Draft:", draftCount.toString());

						// if there's fewer than 2 messages, throw an error
						if (engine.total == 0) {
							Toast.makeText(this, "No messages found.",
									Toast.LENGTH_LONG).show();
						} else if (engine.total == 1) {
							Toast.makeText(this, "1 message is not enough",
									Toast.LENGTH_LONG).show();
						} else if (engine.total == 2) {
							Toast.makeText(this, "2 messages are not enough",
									Toast.LENGTH_LONG).show();

							// TODO handle small sent or received better in
							// graphing array
							
						} else if (engine.cat.count == 0) {
							Toast.makeText(this, "You have sent no messages",
									Toast.LENGTH_LONG).show();
						} else if (engine.rat.count == 0) {
							Toast.makeText(this, "You have received no messages",
									Toast.LENGTH_LONG).show();
						} else {
							timesRun++;
							if (timesRun > 1) {
								// graphBox.removeAllViews();
							}

							//Quirky message
							TextView qMessage = (TextView) findViewById(R.id.mainTitle);
							Random randNo = new Random();
							qMessage.setText(ladQuirk[randNo.nextInt(ladQuirk.length)]);
							
							// Name of adversary
							TextView nameOfRat = (TextView) findViewById(R.id.ratName);
							nameOfRat.setText(name);
							
							int larger;
							int speed;
							
							// Median Row
							TextView medianSent = (TextView) findViewById(R.id.medianSent);
							medianSent.setText(returnTime(engine.medianSentSpeedRaw));

							TextView medianReceived = (TextView) findViewById(R.id.medianReceived);
							// TODO - fix expanding cell-size on this
							medianReceived.setText(returnTime(engine.medianReceivedSpeedRaw));
							
							// Mean Row
							TextView meanSent = (TextView) findViewById(R.id.meanSent);
							meanSent.setText(returnTime(engine.average_receive_to_send_response_time));

							TextView meanReceived = (TextView) findViewById(R.id.meanReceived);
							// TODO - fix expanding cell-size on this
							meanReceived.setText(returnTime(engine.average_send_to_receive_response_time));

							// Score-cards!
							// TODO calculate counter end-times
							larger = (engine.cat.count < engine.rat.count) ? engine.rat.count : engine.cat.count;
							speed = (larger > 0) ? 2000/(larger) : 0;
							final TextView sentScore = (TextView) findViewById(R.id.sentScore);
							countUp(sentScore, engine.cat.count, speed);

							final TextView receivedScore = (TextView) findViewById(R.id.receivedScore);
							countUp(receivedScore, engine.rat.count, speed);

							// Questions Row
							larger = (engine.cat.questions < engine.rat.questions) ? engine.rat.questions : engine.cat.questions;
							speed = (larger > 0) ? 2500/(larger) : 0;
							TextView questionsSentCounter = (TextView) findViewById(R.id.questionsSent);
							countUp(questionsSentCounter, engine.cat.questions, speed);

							TextView questionsReceivedCounter = (TextView) findViewById(R.id.questionsReceived);
							countUp(questionsReceivedCounter, engine.rat.questions, speed);

							// Kisses Row
							larger = (engine.cat.kisses < engine.rat.kisses) ? engine.rat.kisses : engine.cat.kisses;
							speed = (larger > 0) ? 3000/(larger) : 0;
							TextView kissesSentCounter = (TextView) findViewById(R.id.kissesSent);
							countUp(kissesSentCounter, engine.cat.kisses, speed);

							TextView kissesReceivedCounter = (TextView) findViewById(R.id.kissesReceived);
							countUp(kissesReceivedCounter, engine.rat.kisses, speed);

							// Smileys Row
							larger = (engine.cat.smileys < engine.rat.smileys) ? engine.rat.smileys : engine.cat.smileys;
							speed = (larger > 0) ? 3500/(larger) : 0;
							TextView smileysSentCounter = (TextView) findViewById(R.id.smileysSent);
							countUp(smileysSentCounter, engine.cat.smileys, speed);

							TextView smileysReceivedCounter = (TextView) findViewById(R.id.smileysReceived);
							countUp(smileysReceivedCounter, engine.rat.smileys, speed);
							
							// Convo lengths times Row
							TextView convoLength = (TextView) findViewById(R.id.convoLength);
							convoLength.setText(engine.average_bunch_length + " " + getString(R.string.convoAvgFill) + " " + returnTime(engine.average_bunch_time));
							
							// Convo start Row
							larger = (engine.send_initate < engine.receive_initiate) ? engine.receive_initiate : engine.send_initate;
							speed = (larger > 0) ? 3500/(larger) : 0;
							TextView sendStart = (TextView) findViewById(R.id.sendStart);
							countUp(sendStart, engine.send_initate, speed);

							TextView receiveStart = (TextView) findViewById(R.id.receiveStart);
							countUp(receiveStart, engine.receive_initiate, speed);
							
							// Convo End Row
							larger = (engine.send_ender < engine.receive_ender) ? engine.receive_ender : engine.send_ender;
							speed = (larger > 0) ? 3500/(larger) : 0;
							TextView sendEnd = (TextView) findViewById(R.id.sendEnd);
							countUp(sendEnd, engine.send_ender, speed);

							TextView receiveEnd = (TextView) findViewById(R.id.receiveEnd);
							countUp(receiveEnd, engine.receive_ender, speed);

							// Doubles Row
							larger = (engine.cat.doubles < engine.rat.doubles) ? engine.rat.doubles : engine.cat.doubles;
							speed = (larger > 0) ? 4000/(larger) : 0;
							TextView doublesSentCounter = (TextView) findViewById(R.id.doublesSent);
							countUp(doublesSentCounter, engine.cat.doubles, speed);

							TextView doublesReceivedCounter = (TextView) findViewById(R.id.doublesReceived);
							countUp(doublesReceivedCounter, engine.rat.doubles,
									speed);
							
							// Avg double times Row
							TextView doubleSentTime = (TextView) findViewById(R.id.doubleSentTime);
							doubleSentTime.setText(returnTime(engine.average_send_double_up_time));

							TextView doublesReceivedTime = (TextView) findViewById(R.id.doubleReceivedTime);
							// TODO - fix expanding cell-size on this
							doublesReceivedTime.setText(returnTime(engine.average_receive_double_up_time));

							// Quarter Row
							larger = (engine.sendQuarterCount < engine.receivedQuarterCount) ? engine.receivedQuarterCount : engine.sendQuarterCount;
							speed = (larger > 0) ? 4500/(larger) : 0;
							TextView quarterSent = (TextView) findViewById(R.id.quartersSent);
							countUp(quarterSent, engine.sendQuarterCount, speed);

							TextView quarterReceived = (TextView) findViewById(R.id.quartersReceived);
							countUp(quarterReceived, engine.receivedQuarterCount, speed);

							// Hour Row
							larger = (engine.sendHourCount < engine.receivedHourCount) ? engine.receivedHourCount : engine.sendHourCount;
							speed = (larger > 0) ? 5000/(larger) : 0;
							TextView hourSent = (TextView) findViewById(R.id.hoursSent);
							countUp(hourSent, engine.sendHourCount, speed);

							TextView hourReceived = (TextView) findViewById(R.id.hoursReceived);
							countUp(hourReceived, engine.receivedHourCount, speed);

							// Day Row
							larger = (engine.sendDayCount < engine.receivedDayCount) ? engine.receivedDayCount : engine.sendDayCount;
							speed = (larger > 0) ? 5500/(larger) : 0;
							TextView daySent = (TextView) findViewById(R.id.daysSent);
							countUp(daySent, engine.sendDayCount, speed);

							TextView dayReceived = (TextView) findViewById(R.id.daysReceived);
							countUp(dayReceived, engine.receivedDayCount, speed);
							
							// Days plus Row
							larger = (engine.sendDayPlusCount() < engine.receivedDayPlusCount()) ? engine.receivedDayPlusCount() : engine.sendDayPlusCount();
							speed = (larger > 0) ? 5500/(larger) : 0;
							TextView dayplusSent = (TextView) findViewById(R.id.daysplusSent);
							countUp(dayplusSent, engine.sendDayPlusCount(), speed);

							TextView dayplusReceived = (TextView) findViewById(R.id.daysplusReceived);
							countUp(dayplusReceived, engine.receivedDayPlusCount(), speed);
						}
					}
				}

				break;
			}

		} else {
			Log.w(DEBUG_TAG, "Warning: activity result not ok");
		}
	}
}
