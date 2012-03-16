package com.tug.kite;

import java.util.ArrayList;
import java.util.Collections;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.GraphViewSeries;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.LineGraphView;

import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Tugging extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	private static final int CONTACT_PICKER_RESULT = 1001;
	private static final String DEBUG_TAG = null;

	public void doLaunchContactPicker(View view) {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
				Contacts.CONTENT_URI);
		// TODO remove status bar and application name
		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
	}

	// The method below is used for matching the last characters of the string
	public class SubStringEx {
		public String getLastnCharacters(String inputString, int subStringLength) {
			int length = inputString.length();
			if (length <= subStringLength) {
				return inputString;
			}
			int startIndex = length - subStringLength;
			return inputString.substring(startIndex);
		}
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
		} else {
			int indexOne = (arrayLength / 2);
			int indexTwo = arrayLength / 2 + 1;
			double arraysSum = myArray.get(indexOne - 1)
					+ myArray.get(indexTwo - 1);
			arrayMedian = arraysSum / 2;
		}
		return arrayMedian;
	}

	public static String returnTime(Double milliSeconds) {
		// int i = (int)myDouble;

		Double pure = milliSeconds;
		Integer diff = (int) Math.round(pure);

		Integer diffSecs = diff / 1000;
		Integer diffMin = diff / (60 * 1000); // minutes
		Integer diffHours = diff / (60 * 60 * 1000); // hours
		Integer diffDays = diff / (24 * 60 * 60 * 1000);

		String naturalTime = "";

		if (diffSecs < 60) {
			naturalTime = diffSecs.toString() + " secs";
		} else if (diffMin > 1 && diffHours < 1) {
			naturalTime = diffMin.toString() + " mins";
		} else if (diffHours > 1 && diffDays < 1) {
			naturalTime = diffHours.toString() + " hrs";
		} else if (diffDays > 1) {
			naturalTime = diffDays.toString() + " days";
		}
		return naturalTime;

	}

	// TODO error handling for email input doesn't work
	// TODO small arrays cause problems e.g. Merel

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				Cursor cursor = null;
				String phone = "";
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

					// let's just get the first phone
					if (cursor.moveToFirst()) {
						phone = cursor.getString(phoneIdx);
						Log.v(DEBUG_TAG, "Got phone: " + phone);

					} else {
						Log.w(DEBUG_TAG, "No results");
					}
				} catch (Exception e) {
					Log.e(DEBUG_TAG, "Failed to get phone data", e);
				} finally {
					if (cursor != null) {
						cursor.close();
					}

					// START THE STAT ENGINE!
					Uri uriSMSURI = Uri.parse("content://sms"); // access the
																// sms db
					Cursor cur = getContentResolver().query(uriSMSURI, null,
							null, null, null);

					// set the data to be collected

					Integer total = 0;
					Integer sent = 0;
					Integer received = 0;

					Integer kissesSent = 0;
					Integer kissesReceived = 0;
					Integer questionsSent = 0;
					Integer questionsReceived = 0;

					Integer receivedQuarterCount = 0;
					Integer receivedHourCount = 0;
					Integer receivedDayCount = 0;
					Integer receivedWeekCount = 0;
					Integer receivedWeekPlusCount = 0;

					Integer sendQuarterCount = 0;
					Integer sendHourCount = 0;
					Integer sendDayCount = 0;
					Integer sendWeekCount = 0;
					Integer sendWeekPlusCount = 0;

					// declare the ArrayList of reply-time integers
					Integer lastMessageStatus = 0; // sent = 2, received = 1
					Integer lastMessageTime = 0;
					ArrayList<Integer> replySpeeds = new ArrayList<Integer>();
					ArrayList<Integer> sendSpeeds = new ArrayList<Integer>();

					// Double-texts
					Integer sentDoubles = 0;
					Integer receivedDoubles = 0;

					Integer timesRun = 0;

					while (cur.moveToNext()) {

						// TODO look at the number outside the first zero.
						// Matching the number +27 (84) 5543262 to 0845543262 to
						// +27845543262
						// That should be done from end to beginning

						/**
						 * String ssn ="123456789"; SubStringEx subEx = new
						 * SubStringEx(); String last4Digits =
						 * subEx.getLastnCharacters(ssn,4);
						 * System.out.println("Last 4 digits are " +
						 * last4Digits); //will print 6789
						 * 
						 */

						// set the rat to be analysed
						String rat = phone;
						// strip the last 9 digits
						SubStringEx subRatter = new SubStringEx(); // new
																	// SubStringEx
																	// object
																	// from the
																	// class
						String cleanRat = subRatter.getLastnCharacters(rat, 9);

						// get the raw number of the message
						String num = cur.getString(2);
						// string the last 9 digits
						SubStringEx subNumber = new SubStringEx(); // TODO this
																	// could be
																	// code-bloat?
						// can you just use the method and not need a new
						// SubStringEx object?

						String cleanNum = subNumber.getLastnCharacters(num, 9);

						if (cleanRat.equals(cleanNum)) {
							Integer messageStatus = cur.getInt(8);
							Integer replyTime = cur.getInt(4);
							total++;
							// messages sent
							if (messageStatus == 2) {
								sent++;
								Log.d(rat, "Message Sent: " + cur.getString(11));

								// see if this is a reply or a follow-up text
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
								if (cur.getString(11).indexOf(" x ") > 0
										|| cur.getString(11).indexOf(" x") > 0) {
									kissesSent++;
								}
								// question-marks sent
								if (cur.getString(11).indexOf("?") > 0) {
									questionsSent++;
								}
							} else if (messageStatus == 1) { // messages
																// received
								received++;

								// see if this a reply or a follow-up text
								if (lastMessageStatus == 1) {
									receivedDoubles++;
								} else if (lastMessageStatus == 2) {
									Integer replyDiff = lastMessageTime
											- replyTime;
									replySpeeds.add(replyDiff);
									Log.d("Difference in time",
											replyDiff.toString());
								}

								Log.d(rat,
										"Message Received: "
												+ cur.getString(11));
								// kisses received
								if (cur.getString(11).indexOf(" x ") > 0
										|| cur.getString(11).indexOf(" x") > 0) {
									kissesReceived++;
								}
								// question-marks received
								if (cur.getString(11).indexOf("?") > 0) {
									questionsReceived++;
									Log.d(rat, "Question Received");
								}

							} else {

								Log.d(rat, "Not sent or received. Odd");
							}

							// set the LastMessageStatus for next loop
							lastMessageStatus = messageStatus;
							lastMessageTime = replyTime;
						} else {
							// log no texts match to person here
							Log.d(rat, "Not a match to rat.");
						}

					}

					Log.i("Sent Doubles", sentDoubles.toString());
					Log.i("Received Doubles", receivedDoubles.toString());
					Log.i("Send Speeds", sendSpeeds.toString());
					Log.i("Reply Speeds", replySpeeds.toString());

					timesRun++;

					// GRAPHING TIME

					// replySpeeds

					Integer replyNum = replySpeeds.size();
					GraphViewData[] replyData = new GraphViewData[replyNum];

					Log.i("About to spin data in", "DATA GOING IN");
					for (int i = 0; i < replyNum; i++) {

						// push data into graph's line array
						replyData[i] = new GraphViewData(i, replySpeeds.get(i));
						Log.i("pushing reply data", replySpeeds.get(i)
								.toString());

						// count delay categories
						Integer diff = (int) Math.round(replySpeeds.get(i));
						Integer diffMin = diff / (60 * 1000); // minutes
						Integer diffHours = diff / (60 * 60 * 1000); // hours
						Integer diffDays = diff / (24 * 60 * 60 * 1000);

						if (diffMin < 15) {
							receivedQuarterCount++;
						} else if (diffMin > 15 && diffMin < 60) {
							receivedHourCount++;
						} else if (diffHours > 1 && diffHours < 24) {
							receivedDayCount++;
						} else if (diffDays > 1 && diffDays < 7) {
							receivedWeekCount++;
						} else if (diffDays > 7) {
							receivedWeekPlusCount++;
						}

					}

					GraphViewSeries seriesReplies = new GraphViewSeries(
							"Replying speeds", Color.rgb(200, 50, 00),
							replyData);

					// sendSpeeds
					Integer sendNum = sendSpeeds.size();
					GraphViewData[] sendData = new GraphViewData[sendNum];

					for (int i = 0; i < sendNum; i++) {
						// push data into graph's array
						sendData[i] = new GraphViewData(i, sendSpeeds.get(i));
						Log.i("pushing send data", sendSpeeds.get(i).toString());

						// count delay categories
						Integer diff = (int) Math.round(sendSpeeds.get(i));
						Integer diffMin = diff / (60 * 1000); // minutes
						Integer diffHours = diff / (60 * 60 * 1000); // hours
						Integer diffDays = diff / (24 * 60 * 60 * 1000);

						if (diffMin < 15) {
							sendQuarterCount++;
						} else if (diffMin > 15 && diffMin < 60) {
							sendHourCount++;
						} else if (diffHours > 1 && diffHours < 24) {
							sendDayCount++;
						} else if (diffDays > 1 && diffDays < 7) {
							sendWeekCount++;
						} else if (diffDays > 7) {
							sendWeekPlusCount++;
						}

					}

					GraphViewSeries seriesSent = new GraphViewSeries(
							"Sending speeds", Color.rgb(0, 184, 0), sendData);

					// LinearLayout graphBox = (LinearLayout)
					// findViewById(R.id.graph1);

					GraphView graphView = new BarGraphView(this, "Reply Speeds");

					if (timesRun > 1) {
						// graphBox.removeAllViews();
					}

					graphView.addSeries(seriesReplies);
					graphView.addSeries(seriesSent);
					// set legend
					graphView.setShowLegend(true);
					graphView.setLegendAlign(LegendAlign.BOTTOM);
					graphView.setLegendWidth(200);

					Log.d("Update Graph", "About to update...");

					// graphBox.addView(graphView); // TODO fix graph updating

					// Average Calculating
					Double averageSentSpeedRaw = findMean(sendSpeeds);
					Double averageReceivedSpeedRaw = findMean(replySpeeds);

					String averageSentSpeed = returnTime(averageSentSpeedRaw);
					String averageReceivedSpeed = returnTime(averageReceivedSpeedRaw);

					// Median Calculating
					Double medianSentSpeedRaw = findMedian(sendSpeeds);
					Double medianReceivedSpeedRaw = findMedian(replySpeeds);

					String medianSentSpeed = returnTime(medianSentSpeedRaw);
					String medianReceivedSpeed = returnTime(medianReceivedSpeedRaw);

					// Push the data to the view

					// Score-cards!

					

					
					final int topNum = sent; // the total number
					final TextView sentScore = (TextView) findViewById(R.id.sentScore);
					// ...
					// when you want to start the counting start the thread
					// bellow.
					new Thread(new Runnable() {
						int counter = 0;
						public void run() {
							while (counter < topNum) {
								try {
									Thread.sleep(20);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								sentScore.post(new Runnable() {

									public void run() {
										sentScore.setText("" + counter);

									}
									
								});
								counter++;
							}
							
						}

					}).start();

					long freezeTime = SystemClock.uptimeMillis();
					/**
					 * for (int i = 0; i < sent; i++) { if
					 * ((SystemClock.uptimeMillis() - freezeTime) > 500) {
					 * sentScore.setText(sent.toString()); } }
					 * 
					 * int count = 0; while (count != sent) { if
					 * ((SystemClock.uptimeMillis() - freezeTime) > 500) {
					 * count++; sentScore.setText("" + count); } }
					 */

					/**
					 * final long start = mStartTime; long millis =
					 * SystemClock.uptimeMillis() - start; int seconds = (int)
					 * (millis / 1000); int minutes = seconds / 60; seconds =
					 * seconds % 60;
					 * 
					 * if (seconds < 10) { mTimeLabel.setText("" + minutes +
					 * ":0" + seconds); } else { mTimeLabel.setText("" + minutes
					 * + ":" + seconds); }
					 * 
					 * mHandler.postAtTime(this, start + (((minutes * 60) +
					 * seconds + 1) * 1000));
					 * 
					 * for (int i = 0; i < sent; i++) { // try {
					 * Thread.sleep(500);
					 * 
					 * } catch (InterruptedException ie) { sentScore.setText(i);
					 * } }
					 * 
					 */

					// Messages Row
					TextView messagesSent = (TextView) findViewById(R.id.MessagesSent);
					messagesSent.setText(sent.toString());

					TextView messagesReceived = (TextView) findViewById(R.id.MessagesReceived);
					messagesReceived.setText(received.toString());

					// Average Row
					TextView averageSent = (TextView) findViewById(R.id.AverageSent);
					averageSent.setText(averageSentSpeed);

					TextView averageReceived = (TextView) findViewById(R.id.AverageReceived);
					averageReceived.setText(averageReceivedSpeed);

					// Median Row
					TextView medianSent = (TextView) findViewById(R.id.MedianSent);
					medianSent.setText(medianSentSpeed);

					TextView medianReceived = (TextView) findViewById(R.id.MedianReceived);
					medianReceived.setText(medianReceivedSpeed);

					// Questions Row
					TextView questionsSentCounter = (TextView) findViewById(R.id.QuestionsSent);
					questionsSentCounter.setText(questionsSent.toString());

					TextView questionsReceivedCounter = (TextView) findViewById(R.id.QuestionsReceived);
					questionsReceivedCounter.setText(questionsReceived
							.toString());

					// Kisses Row
					TextView kissesSentCounter = (TextView) findViewById(R.id.KissesSent);
					kissesSentCounter.setText(kissesSent.toString());

					TextView kissesReceivedCounter = (TextView) findViewById(R.id.KissesReceived);
					kissesReceivedCounter.setText(kissesReceived.toString());

					// Doubles Row
					TextView doublesSentCounter = (TextView) findViewById(R.id.DoublesSent);
					doublesSentCounter.setText(sentDoubles.toString());

					TextView doublesReceivedCounter = (TextView) findViewById(R.id.DoublesReceived);
					doublesReceivedCounter.setText(receivedDoubles.toString());

					if (phone.length() == 0) {
						Toast.makeText(this, "No phone found for contact.",
								Toast.LENGTH_LONG).show();
					}

				}

				break;
			}

		} else {
			Log.w(DEBUG_TAG, "Warning: activity result not ok");
		}
	}
}